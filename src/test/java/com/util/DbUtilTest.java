package com.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link DbUtil} 集成测试。
 *
 * <p>测试会真实连接本地 MySQL。若当前机器没有 db.properties 或数据库未启动，
 * 通过 {@link org.junit.jupiter.api.Assumptions} 跳过，而不是让整个构建失败
 * （方便组员克隆项目后先跑通编译）。</p>
 *
 * @author 杨亮（组长）
 */
class DbUtilTest {

    /** 事务测试专用学号，测试前后都会清理。 */
    private static final String TEST_STU_NO = "TEST_TX_0001";

    @BeforeAll
    static void requireDatabase() {
        assumeTrue(DbUtil.isConfigured(), "缺少 src/main/resources/db.properties，跳过 JDBC 集成测试");
        assumeTrue(DbUtil.canConnect(), "本地 MySQL 不可用，跳过 JDBC 集成测试");
    }

    @Test
    void getConnectionReturnsUsableConnection() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    void closeToleratesNullResources() {
        DbUtil.close(null, null, null);
        DbUtil.close((Connection) null);
    }

    @Test
    void closeReleasesResources() throws SQLException {
        Connection conn = DbUtil.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT 1");

        DbUtil.close(rs, stmt, conn);

        assertTrue(rs.isClosed(), "ResultSet 应被关闭");
        assertTrue(stmt.isClosed(), "Statement 应被关闭");
        assertTrue(conn.isClosed(), "Connection 应被关闭");
    }

    @Test
    void transactionRollbackDiscardsInsertedRow() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            // 清理可能残留的测试数据（自动提交模式下立即生效）
            try (PreparedStatement clean = conn.prepareStatement(
                    "DELETE FROM t_student WHERE stu_no = ?")) {
                clean.setString(1, TEST_STU_NO);
                clean.executeUpdate();
            }

            conn.setAutoCommit(false);
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO t_student (stu_no, name, gender, class_name, major, phone, email) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insert.setString(1, TEST_STU_NO);
                insert.setString(2, "事务测试");
                insert.setString(3, "男");
                insert.setString(4, "测试班级");
                insert.setString(5, "测试专业");
                insert.setString(6, "13800000000");
                insert.setString(7, "tx@test.cn");
                assertEquals(1, insert.executeUpdate());
            }

            DbUtil.rollback(conn);
            assertTrue(conn.getAutoCommit(), "回滚后应恢复自动提交");

            try (PreparedStatement query = conn.prepareStatement(
                    "SELECT COUNT(*) FROM t_student WHERE stu_no = ?")) {
                query.setString(1, TEST_STU_NO);
                try (ResultSet rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1), "回滚后不应查到测试数据");
                }
            }
        }
    }
}
