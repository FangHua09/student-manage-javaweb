package com.dao;

import com.dao.impl.StudentDaoImpl;
import com.entity.Student;
import com.util.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link StudentDao} 集成测试（依赖 sql/student_db.sql 写入的 100 条测试数据）。
 *
 * <p>新增/修改/删除测试使用固定测试学号 {@value #TEST_STU_NO}，每个用例前后都会清理，不污染正式数据。</p>
 *
 * @author 杨亮（组长）
 */
class StudentDaoTest {

    /** 测试专用学号，避免影响正式测试数据。 */
    private static final String TEST_STU_NO = "TEST9001";

    private final StudentDao studentDao = new StudentDaoImpl();

    @BeforeAll
    static void requireDatabase() {
        assumeTrue(DbUtil.canConnect(), "本地 MySQL 不可用，跳过 StudentDao 集成测试");
    }

    @BeforeEach
    void cleanBefore() throws SQLException {
        studentDao.delete(TEST_STU_NO);
    }

    @AfterEach
    void cleanAfter() throws SQLException {
        studentDao.delete(TEST_STU_NO);
    }

    @Test
    void findByStuNoReturnsExistingStudent() throws SQLException {
        Student student = studentDao.findByStuNo("2024010101");
        assertNotNull(student, "应能查到内置学生 2024010101");
        assertEquals("2024010101", student.getStuNo());
        assertEquals("何菊", student.getName());
        assertEquals("计科2401", student.getClassName());
        assertNotNull(student.getMajor());
        assertNotNull(student.getPhone());
    }

    @Test
    void findByStuNoReturnsNullWhenMissing() throws SQLException {
        assertNull(studentDao.findByStuNo("NOT_EXIST_0001"));
        assertNull(studentDao.findByStuNo(null));
        assertNull(studentDao.findByStuNo("  "));
    }

    @Test
    void findAllAndCountAreConsistent() throws SQLException {
        int total = studentDao.count(null, null);
        assumeTrue(total > 0, "t_student 表为空，跳过测试");
        List<Student> students = studentDao.findAll();
        assertEquals(total, students.size());
        assertEquals("2024010101", students.get(0).getStuNo(), "列表应按学号升序");
    }

    @Test
    void findByPageReturnsRequestedPageInOrder() throws SQLException {
        int total = studentDao.count(null, null);
        assumeTrue(total >= 6, "数据不足 6 条，跳过分页测试");

        List<Student> first = studentDao.findByPage(null, null, 1, 5);
        List<Student> second = studentDao.findByPage(null, null, 2, 5);

        assertEquals(5, first.size());
        assertEquals(5, second.size());
        List<String> firstNos = first.stream().map(Student::getStuNo).toList();
        assertEquals(firstNos.stream().sorted().toList(), firstNos, "同一页内应按学号升序");
        assertTrue(Collections.disjoint(firstNos, second.stream().map(Student::getStuNo).toList()),
                "相邻两页数据不应重复");
    }

    @Test
    void findByPageNormalizesInvalidParameters() throws SQLException {
        int total = studentDao.count(null, null);
        assumeTrue(total >= 10, "数据不足 10 条，跳过分页边界测试");

        assertEquals(10, studentDao.findByPage(null, null, 0, 0).size(),
                "页码 < 1 应按第 1 页、每页条数 < 1 应按默认 10 条处理");
        assertEquals(5, studentDao.findByPage(null, null, -3, 5).size());
        assertTrue(studentDao.findByPage(null, null, Integer.MAX_VALUE, 100).isEmpty(),
                "超大页码应返回空列表而不是抛异常");
    }

    @Test
    void fuzzySearchByPartialName() throws SQLException {
        int matched = studentDao.count("何", null);
        assumeTrue(matched > 0, "测试数据中不存在姓名含“何”的学生，跳过");
        List<Student> students = studentDao.findByPage("何", null, 1, 100);
        assertEquals(matched, students.size());
        students.forEach(student -> assertTrue(student.getName().contains("何")));
    }

    @Test
    void fuzzySearchByPartialClassName() throws SQLException {
        int matched = studentDao.count(null, "计科2401");
        assumeTrue(matched > 0, "测试数据中不存在班级含“计科2401”的学生，跳过");
        List<Student> students = studentDao.findByPage(null, "计科2401", 1, 100);
        assertEquals(matched, students.size());
        students.forEach(student -> assertTrue(student.getClassName().contains("计科2401")));
    }

    @Test
    void fuzzySearchCombinesNameAndClassConditions() throws SQLException {
        int both = studentDao.count("何", "计科2401");
        int onlyName = studentDao.count("何", null);
        assertTrue(both >= 0);
        assertTrue(both <= onlyName, "同时限定姓名和班级时结果不应多于只限定姓名");
    }

    @Test
    void likeWildcardsAreEscaped() throws SQLException {
        int total = studentDao.count(null, null);
        assumeTrue(total > 0, "t_student 表为空，跳过测试");
        assertTrue(studentDao.count("%", null) < total,
                "% 应按普通字符匹配，不应返回全部记录（LIKE 通配符未转义）");
        assertTrue(studentDao.count("_", null) < total,
                "_ 应按普通字符匹配，不应匹配任意单个字符");
    }

    @Test
    void findAllClassesReturnsDistinctClassNames() throws SQLException {
        assumeTrue(studentDao.count(null, null) > 0, "t_student 表为空，跳过测试");
        List<String> classes = studentDao.findAllClasses();
        assertFalse(classes.isEmpty());
        assertTrue(classes.contains("计科2401"));
        assertEquals(classes.stream().distinct().toList(), classes, "班级列表不应重复");
    }

    @Test
    void crudRoundTrip() throws SQLException {
        Student student = new Student(TEST_STU_NO, "测试学生", "女", "测试2401",
                "测试专业", "13900000000", "test@stu.edu.cn");

        assertFalse(studentDao.existsByStuNo(TEST_STU_NO));
        assertTrue(studentDao.insert(student));
        assertTrue(studentDao.existsByStuNo(TEST_STU_NO));

        Student loaded = studentDao.findByStuNo(TEST_STU_NO);
        assertNotNull(loaded);
        assertEquals("测试学生", loaded.getName());
        assertEquals("女", loaded.getGender());
        assertEquals("测试2401", loaded.getClassName());
        assertEquals("测试专业", loaded.getMajor());
        assertEquals("13900000000", loaded.getPhone());
        assertEquals("test@stu.edu.cn", loaded.getEmail());

        loaded.setName("测试学生修改");
        loaded.setClassName("测试2402");
        loaded.setPhone("13911112222");
        assertTrue(studentDao.update(loaded));

        Student updated = studentDao.findByStuNo(TEST_STU_NO);
        assertNotNull(updated);
        assertEquals("测试学生修改", updated.getName());
        assertEquals("测试2402", updated.getClassName());
        assertEquals("13911112222", updated.getPhone());

        assertTrue(studentDao.delete(TEST_STU_NO));
        assertFalse(studentDao.existsByStuNo(TEST_STU_NO));
        assertNull(studentDao.findByStuNo(TEST_STU_NO));
    }

    @Test
    void insertRejectsMissingRequiredFields() throws SQLException {
        assertFalse(studentDao.insert(null));
        assertFalse(studentDao.insert(new Student("  ", "姓名", "男", null, null, null, null)));
        assertFalse(studentDao.insert(new Student(TEST_STU_NO, " ", "男", null, null, null, null)));
    }
}
