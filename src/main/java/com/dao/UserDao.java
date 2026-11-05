package com.dao;

import com.entity.User;

import java.sql.SQLException;

/**
 * 用户表 DAO 接口（组长公共代码）。
 *
 * <p>登录模块使用 {@link #findByUsername(String)} 查询账号后在 Servlet 中比对密码；
 * 修改密码功能使用 {@link #updatePassword(int, String)}；
 * Filter 权限判断直接从 Session 中已登录的 {@link User} 对象读取角色，不需要重复查库。</p>
 *
 * <p>实现类：{@code com.dao.impl.UserDaoImpl}。所有方法统一声明 {@link SQLException}，
 * 调用方（Servlet）应捕获后包装为 {@code ServletException} 或给出友好提示。</p>
 *
 * @author 杨亮（组长）
 */
public interface UserDao {

    /**
     * 按登录账号查询用户（唯一索引，最多一条）。
     *
     * @param username 登录账号，为空时返回 null
     * @return 用户对象；账号不存在时返回 null
     * @throws SQLException 数据库访问异常
     */
    User findByUsername(String username) throws SQLException;

    /**
     * 按用户主键查询用户。
     *
     * @param userId 用户主键
     * @return 用户对象；不存在时返回 null
     * @throws SQLException 数据库访问异常
     */
    User findById(int userId) throws SQLException;

    /**
     * 修改指定用户的登录密码。
     *
     * @param userId      用户主键
     * @param newPassword 新密码，为空时直接返回 false
     * @return 更新成功（影响 1 行）返回 true
     * @throws SQLException 数据库访问异常
     */
    boolean updatePassword(int userId, String newPassword) throws SQLException;
}
