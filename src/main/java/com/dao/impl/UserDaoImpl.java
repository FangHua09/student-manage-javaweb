package com.dao.impl;

import com.dao.UserDao;
import com.entity.User;
import com.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link UserDao} 的 JDBC 实现（组长公共代码）。
 *
 * <p>全部 SQL 使用 {@link PreparedStatement} 占位符传参，禁止字符串拼接，避免 SQL 注入；
 * 连接、语句、结果集通过 try-with-resources 自动按逆序关闭。</p>
 *
 * @author 杨亮（组长）
 */
public class UserDaoImpl implements UserDao {

    private static final String COLUMNS = "user_id, username, password, role, stu_no";

    private static final String SQL_FIND_BY_USERNAME =
            "SELECT " + COLUMNS + " FROM t_user WHERE username = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT " + COLUMNS + " FROM t_user WHERE user_id = ?";

    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE t_user SET password = ? WHERE user_id = ?";

    @Override
    public User findByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public User findById(int userId) throws SQLException {
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PASSWORD)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * 把结果集当前行映射为 {@link User}。
     *
     * @param rs 已定位到目标行的结果集
     * @return 用户对象
     * @throws SQLException 读取字段失败时抛出
     */
    private static User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getInt("role"));
        user.setStuNo(rs.getString("stu_no"));
        return user;
    }
}
