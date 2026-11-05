package com.dao;

import com.dao.impl.UserDaoImpl;
import com.entity.User;
import com.util.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link UserDao} 集成测试。
 *
 * <p>查询类用例使用 sql/student_db.sql 写入的内置账号；
 * 修改密码类用例使用本测试临时创建的账号（{@code test_user_9002} / {@code TEST9002}），
 * 每个用例前后自动删除，不会修改 admin 或种子学生的密码，也不会污染演示数据。</p>
 *
 * @author 杨亮（组长）
 */
class UserDaoTest {

    /** 临时测试学生学号。 */
    private static final String TEMP_STU_NO = "TEST9002";

    /** 临时测试登录账号。 */
    private static final String TEMP_USERNAME = "test_user_9002";

    /** 临时测试初始密码。 */
    private static final String TEMP_PASSWORD = "test123456";

    private final UserDao userDao = new UserDaoImpl();

    @BeforeAll
    static void requireDatabase() {
        assumeTrue(DbUtil.canConnect(), "本地 MySQL 不可用，跳过 UserDao 集成测试");
    }

    @BeforeEach
    void createTempUser() throws SQLException {
        deleteTempUser();
        try (Connection conn = DbUtil.getConnection()) {
            try (PreparedStatement student = conn.prepareStatement(
                    "INSERT INTO t_student (stu_no, name, gender, class_name, major, phone, email) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                student.setString(1, TEMP_STU_NO);
                student.setString(2, "临时测试用户");
                student.setString(3, "男");
                student.setString(4, "测试2401");
                student.setString(5, "测试专业");
                student.setString(6, "13800000001");
                student.setString(7, "temp@test.cn");
                student.executeUpdate();
            }
            try (PreparedStatement user = conn.prepareStatement(
                    "INSERT INTO t_user (username, password, role, stu_no) VALUES (?, ?, ?, ?)")) {
                user.setString(1, TEMP_USERNAME);
                user.setString(2, TEMP_PASSWORD);
                user.setInt(3, User.ROLE_STUDENT);
                user.setString(4, TEMP_STU_NO);
                user.executeUpdate();
            }
        }
    }

    @AfterEach
    void removeTempUser() throws SQLException {
        deleteTempUser();
    }

    /**
     * 删除临时账号与临时学生（先删子表 t_user，再删父表 t_student）。
     *
     * @throws SQLException 清理失败时抛出
     */
    private void deleteTempUser() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            try (PreparedStatement user = conn.prepareStatement("DELETE FROM t_user WHERE username = ?")) {
                user.setString(1, TEMP_USERNAME);
                user.executeUpdate();
            }
            try (PreparedStatement student = conn.prepareStatement("DELETE FROM t_student WHERE stu_no = ?")) {
                student.setString(1, TEMP_STU_NO);
                student.executeUpdate();
            }
        }
    }

    @Test
    void findAdminByUsername() throws SQLException {
        User admin = userDao.findByUsername("admin");
        assertNotNull(admin, "应能查到内置管理员账号 admin");
        assertEquals("admin", admin.getUsername());
        assertEquals(User.ROLE_ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());
        assertFalse(admin.isStudent());
        assertNull(admin.getStuNo(), "管理员不关联学号");
        assertEquals("管理员", admin.getRoleName());
    }

    @Test
    void findStudentByUsername() throws SQLException {
        User student = userDao.findByUsername("2024010101");
        assertNotNull(student, "应能查到内置学生账号 2024010101");
        assertEquals(User.ROLE_STUDENT, student.getRole());
        assertEquals("2024010101", student.getStuNo());
        assertTrue(student.isStudent());
        assertEquals("学生", student.getRoleName());
    }

    @Test
    void findUnknownUsernameReturnsNull() throws SQLException {
        assertNull(userDao.findByUsername("no_such_user_9999"));
        assertNull(userDao.findByUsername(null));
        assertNull(userDao.findByUsername("   "));
    }

    @Test
    void findByIdMatchesUsernameLookup() throws SQLException {
        User admin = userDao.findByUsername("admin");
        assertNotNull(admin);
        User byId = userDao.findById(admin.getUserId());
        assertNotNull(byId);
        assertEquals(admin.getUsername(), byId.getUsername());
    }

    @Test
    void updatePasswordChangesValue() throws SQLException {
        User user = userDao.findByUsername(TEMP_USERNAME);
        assertNotNull(user, "临时测试账号应已创建");
        String newPassword = "tmp_" + System.currentTimeMillis();

        assertTrue(userDao.updatePassword(user.getUserId(), newPassword));
        User reloaded = userDao.findByUsername(TEMP_USERNAME);
        assertNotNull(reloaded);
        assertEquals(newPassword, reloaded.getPassword(), "修改后应能查到新密码");
    }

    @Test
    void updatePasswordRejectsEmptyPassword() throws SQLException {
        User user = userDao.findByUsername(TEMP_USERNAME);
        assertNotNull(user);

        assertFalse(userDao.updatePassword(user.getUserId(), ""));
        assertFalse(userDao.updatePassword(user.getUserId(), null));
        assertEquals(TEMP_PASSWORD, userDao.findByUsername(TEMP_USERNAME).getPassword(),
                "非法新密码不应修改原密码");
    }
}
