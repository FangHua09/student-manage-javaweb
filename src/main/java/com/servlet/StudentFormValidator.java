package com.servlet;

import com.entity.Student;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Pattern;

/**
 * 学生表单参数的读取与校验（新增 / 修改模块内部使用，负责人：朱景澈）。
 *
 * <p>新增与修改共用同一张表单，字段校验规则完全一致，因此把"取参数 + 校验"抽到本类，
 * 避免两个 Servlet 各写一份导致提示语和规则不一致。本类不访问数据库，
 * 学号重复、记录是否存在等需要查库的校验仍由 Servlet 调用 DAO 完成。</p>
 *
 * <p>校验规则与 {@code t_student} 表字段长度保持一致：
 * stu_no VARCHAR(20)、name VARCHAR(20)、gender CHAR(2)、class_name VARCHAR(30)、
 * major VARCHAR(30)、phone VARCHAR(15)、email VARCHAR(50)。</p>
 *
 * @author 朱景澈
 */
public final class StudentFormValidator {

    /** 学号：4~20 位字母或数字。 */
    private static final Pattern STU_NO_PATTERN = Pattern.compile("^[0-9A-Za-z]{4,20}$");

    /** 联系电话：6~15 位数字，允许用减号分隔区号（如 020-88886666）。 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9][0-9-]{5,14}$");

    /** 邮箱：简单的 xxx@xxx.xx 形式（不追求 RFC 完整性，够用即可）。 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[0-9A-Za-z._%+-]+@[0-9A-Za-z.-]+\\.[A-Za-z]{2,}$");

    /** 性别合法取值。 */
    private static final String GENDER_MALE = "男";
    private static final String GENDER_FEMALE = "女";

    /** 各字段最大长度（与数据库列长度一致）。 */
    private static final int MAX_STU_NO = 20;
    private static final int MAX_NAME = 20;
    private static final int MAX_CLASS_NAME = 30;
    private static final int MAX_MAJOR = 30;
    private static final int MAX_PHONE = 15;
    private static final int MAX_EMAIL = 50;

    private StudentFormValidator() {
        // 工具类，禁止实例化
    }

    /**
     * 从请求参数构造学生对象（只做 trim，不做校验）。
     *
     * <p>参数名遵循《DAO接口文档》§5.2：{@code stuNo}、{@code name}、{@code gender}、
     * {@code className}、{@code major}、{@code phone}、{@code email}。</p>
     *
     * @param request HTTP 请求
     * @return 学生对象；缺失的参数为 null
     */
    public static Student fromRequest(HttpServletRequest request) {
        Student student = new Student();
        student.setStuNo(trim(request.getParameter("stuNo")));
        student.setName(trim(request.getParameter("name")));
        student.setGender(trim(request.getParameter("gender")));
        student.setClassName(trim(request.getParameter("className")));
        student.setMajor(trim(request.getParameter("major")));
        student.setPhone(trim(request.getParameter("phone")));
        student.setEmail(trim(request.getParameter("email")));
        return student;
    }

    /**
     * 校验学生表单字段。
     *
     * @param student 学生对象（由 {@link #fromRequest} 生成或从数据库回显）
     * @param isAdd   是否为新增：为 {@code true} 时学号必填；修改时学号由隐藏域传入、不允许改动
     * @return 校验通过返回 {@code null}，否则返回第一条错误提示
     */
    public static String validate(Student student, boolean isAdd) {
        if (student == null) {
            return "表单数据为空，请重新填写";
        }

        // ---------- 学号 ----------
        String stuNo = trim(student.getStuNo());
        if (isBlank(stuNo)) {
            return isAdd ? "学号不能为空" : "学号缺失，无法定位要修改的学生";
        }
        if (!STU_NO_PATTERN.matcher(stuNo).matches()) {
            return "学号只能是 4~20 位字母或数字";
        }
        if (stuNo.length() > MAX_STU_NO) {
            return "学号长度不能超过 " + MAX_STU_NO + " 位";
        }

        // ---------- 姓名 ----------
        String name = trim(student.getName());
        if (isBlank(name)) {
            return "姓名不能为空";
        }
        if (name.length() > MAX_NAME) {
            return "姓名长度不能超过 " + MAX_NAME + " 个字符";
        }

        // ---------- 性别 ----------
        String gender = trim(student.getGender());
        if (isBlank(gender)) {
            return "请选择性别";
        }
        if (!GENDER_MALE.equals(gender) && !GENDER_FEMALE.equals(gender)) {
            return "性别只能是“男”或“女”";
        }

        // ---------- 班级、专业（可空，只校验长度） ----------
        String className = trim(student.getClassName());
        if (className != null && className.length() > MAX_CLASS_NAME) {
            return "班级长度不能超过 " + MAX_CLASS_NAME + " 个字符";
        }
        String major = trim(student.getMajor());
        if (major != null && major.length() > MAX_MAJOR) {
            return "专业长度不能超过 " + MAX_MAJOR + " 个字符";
        }

        // ---------- 电话（可空，填了就要合法） ----------
        String phone = trim(student.getPhone());
        if (!isBlank(phone)) {
            if (phone.length() > MAX_PHONE) {
                return "联系电话长度不能超过 " + MAX_PHONE + " 位";
            }
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return "联系电话只能是 6~15 位数字，可用减号分隔区号";
            }
        }

        // ---------- 邮箱（可空，填了就要合法） ----------
        String email = trim(student.getEmail());
        if (!isBlank(email)) {
            if (email.length() > MAX_EMAIL) {
                return "邮箱长度不能超过 " + MAX_EMAIL + " 个字符";
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return "邮箱格式不正确，示例：student@example.com";
            }
        }

        return null;
    }

    /**
     * 去除首尾空白；null 原样返回。
     *
     * @param value 原始字符串
     * @return 去空白后的字符串
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 判断字符串是否为 null 或全空白。
     *
     * @param value 待判断字符串
     * @return 为空返回 true
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
