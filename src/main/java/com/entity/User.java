package com.entity;

import java.io.Serializable;

/**
 * 用户实体类（对应数据表 {@code t_user}）。
 *
 * <p>字段与表结构一一对应：user_id、username、password、role、stu_no。</p>
 *
 * <p>角色约定：{@link #ROLE_ADMIN}（0）为管理员，{@link #ROLE_STUDENT}（1）为学生；
 * 学生记录通过 {@code stuNo} 关联 {@code t_student.stu_no}，管理员记录的 {@code stuNo} 为 {@code null}。</p>
 *
 * <p>本类由组长统一维护，登录模块、修改密码、Filter 权限判断均直接复用，请勿各自新增同名实体。</p>
 *
 * @author 杨亮（组长）
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色：管理员。 */
    public static final int ROLE_ADMIN = 0;

    /** 角色：学生。 */
    public static final int ROLE_STUDENT = 1;

    /** 用户主键，自增（对应 t_user.user_id）。 */
    private Integer userId;

    /** 登录账号（对应 t_user.username，唯一）。 */
    private String username;

    /** 登录密码（对应 t_user.password，课程项目按明文存储）。 */
    private String password;

    /** 角色：0 管理员，1 学生（对应 t_user.role）。 */
    private int role;

    /** 关联学号，管理员为 null（对应 t_user.stu_no）。 */
    private String stuNo;

    public User() {
    }

    public User(Integer userId, String username, String password, int role, String stuNo) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.stuNo = stuNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getStuNo() {
        return stuNo;
    }

    public void setStuNo(String stuNo) {
        this.stuNo = stuNo;
    }

    /**
     * 是否管理员。
     *
     * @return role 为 0 时返回 true
     */
    public boolean isAdmin() {
        return role == ROLE_ADMIN;
    }

    /**
     * 是否学生。
     *
     * @return role 为 1 时返回 true
     */
    public boolean isStudent() {
        return role == ROLE_STUDENT;
    }

    /**
     * 角色中文名，便于 JSP 页面直接展示（EL：{@code ${sessionScope.loginUser.roleName}}）。
     *
     * @return “管理员”或“学生”
     */
    public String getRoleName() {
        return role == ROLE_ADMIN ? "管理员" : "学生";
    }

    /**
     * 调试输出（不包含密码，避免日志泄漏）。
     *
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return "User{userId=" + userId
                + ", username='" + username + '\''
                + ", role=" + role
                + ", stuNo='" + stuNo + '\''
                + '}';
    }
}
