package com.servlet;

import com.entity.Student;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link StudentFormValidator} 的单元测试（新增修改模块，负责人：朱景澈）。
 *
 * <p>只测纯逻辑，不依赖数据库和 Servlet 容器，因此 {@code mvnw test} 在任何机器上都能跑通，
 * 是本模块"提交前自测"的一部分。</p>
 *
 * @author 朱景澈
 */
class StudentFormValidatorTest {

    /**
     * 构造一个合法学生，便于各用例改单个字段。
     *
     * @return 合法学生对象
     */
    private static Student validStudent() {
        return new Student("2024010101", "张三", "男", "计科2401",
                "计算机科学与技术", "13800138000", "zhangsan@example.com");
    }

    @Test
    @DisplayName("合法字段：新增与修改都应通过校验")
    void validStudentShouldPass() {
        assertNull(StudentFormValidator.validate(validStudent(), true));
        assertNull(StudentFormValidator.validate(validStudent(), false));
    }

    @Test
    @DisplayName("可选字段为空：班级、专业、电话、邮箱留空也应通过")
    void optionalFieldsBlankShouldPass() {
        Student student = new Student("2024010101", "李四", "女", "", "", "", "");
        assertNull(StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("学号为空：新增时提示必填")
    void blankStuNoShouldFailOnAdd() {
        Student student = validStudent();
        student.setStuNo("   ");
        assertEquals("学号不能为空", StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("学号为空：修改时提示无法定位")
    void blankStuNoShouldFailOnUpdate() {
        Student student = validStudent();
        student.setStuNo(null);
        assertEquals("学号缺失，无法定位要修改的学生",
                StudentFormValidator.validate(student, false));
    }

    @Test
    @DisplayName("学号格式非法：包含汉字或过短都应被拒绝")
    void invalidStuNoShouldFail() {
        Student student = validStudent();
        student.setStuNo("2024张三");
        assertEquals("学号只能是 4~20 位字母或数字", StudentFormValidator.validate(student, true));

        student.setStuNo("12");
        assertEquals("学号只能是 4~20 位字母或数字", StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("姓名超长：超过 20 个字符应被拒绝")
    void tooLongNameShouldFail() {
        Student student = validStudent();
        student.setName("张".repeat(21));
        assertEquals("姓名长度不能超过 20 个字符", StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("性别非法：只能是男或女")
    void invalidGenderShouldFail() {
        Student student = validStudent();
        student.setGender("未知");
        assertEquals("性别只能是“男”或“女”", StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("电话非法：含字母或超长应被拒绝")
    void invalidPhoneShouldFail() {
        Student student = validStudent();
        student.setPhone("1380013800a");
        assertNotNull(StudentFormValidator.validate(student, true));

        student.setPhone("1380013800012345");
        assertNotNull(StudentFormValidator.validate(student, true));

        student.setPhone("020-88886666");
        assertNull(StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("邮箱非法：缺少 @ 或域名应被拒绝")
    void invalidEmailShouldFail() {
        Student student = validStudent();
        student.setEmail("zhangsan.example.com");
        assertEquals("邮箱格式不正确，示例：student@example.com",
                StudentFormValidator.validate(student, true));

        student.setEmail("zhangsan@example");
        assertNotNull(StudentFormValidator.validate(student, true));
    }

    @Test
    @DisplayName("表单对象为空：给出兜底提示而不是抛异常")
    void nullStudentShouldFailGracefully() {
        assertEquals("表单数据为空，请重新填写", StudentFormValidator.validate(null, true));
    }

    @Test
    @DisplayName("trim / isBlank：首尾空白与全空白字符串的处理")
    void trimAndIsBlankShouldWork() {
        assertEquals("张三", StudentFormValidator.trim("  张三  "));
        assertNull(StudentFormValidator.trim(null));
        assertEquals(true, StudentFormValidator.isBlank("   "));
        assertEquals(true, StudentFormValidator.isBlank(null));
        assertEquals(false, StudentFormValidator.isBlank("张三"));
    }
}
