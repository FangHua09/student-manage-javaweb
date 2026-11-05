package com.entity;

import java.io.Serializable;

/**
 * 学生实体类（对应数据表 {@code t_student}）。
 *
 * <p>字段与表结构一一对应：stu_no、name、gender、class_name、major、phone、email。
 * 主键为学号 {@code stuNo}；本类由组长统一维护，新增、修改、列表、查询、详情各模块共用。</p>
 *
 * <p>JSP 中可通过 EL 表达式直接取值，例如 {@code ${student.stuNo}}、{@code ${student.className}}。</p>
 *
 * @author 杨亮（组长）
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 学号，主键（对应 t_student.stu_no）。 */
    private String stuNo;

    /** 姓名（对应 t_student.name）。 */
    private String name;

    /** 性别：男 / 女（对应 t_student.gender）。 */
    private String gender;

    /** 班级（对应 t_student.class_name）。 */
    private String className;

    /** 专业（对应 t_student.major）。 */
    private String major;

    /** 联系电话（对应 t_student.phone）。 */
    private String phone;

    /** 电子邮箱（对应 t_student.email）。 */
    private String email;

    public Student() {
    }

    public Student(String stuNo, String name, String gender, String className,
                   String major, String phone, String email) {
        this.stuNo = stuNo;
        this.name = name;
        this.gender = gender;
        this.className = className;
        this.major = major;
        this.phone = phone;
        this.email = email;
    }

    public String getStuNo() {
        return stuNo;
    }

    public void setStuNo(String stuNo) {
        this.stuNo = stuNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 调试输出。
     *
     * @return 学生信息字符串
     */
    @Override
    public String toString() {
        return "Student{stuNo='" + stuNo + '\''
                + ", name='" + name + '\''
                + ", gender='" + gender + '\''
                + ", className='" + className + '\''
                + ", major='" + major + '\''
                + ", phone='" + phone + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
