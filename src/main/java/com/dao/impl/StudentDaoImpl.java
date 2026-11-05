package com.dao.impl;

import com.dao.StudentDao;
import com.entity.Student;
import com.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link StudentDao} 的 JDBC 实现（组长公共代码）。
 *
 * <p>实现要点：</p>
 * <ol>
 *   <li>全部 SQL 使用 {@link PreparedStatement} 占位符传参，防止 SQL 注入；</li>
 *   <li>连接、语句、结果集通过 try-with-resources 自动按逆序关闭；</li>
 *   <li>模糊查询对 {@code %}、{@code _}、{@code !} 做转义，配合 {@code ESCAPE '!'} 使用；</li>
 *   <li>分页参数做边界处理：页码 &lt; 1 按 1、每页条数按 [1,100] 截断，超大偏移量直接返回空列表。</li>
 * </ol>
 *
 * @author 杨亮（组长）
 */
public class StudentDaoImpl implements StudentDao {

    /** 查询列，顺序与 {@link #mapRow(ResultSet)} 一致。 */
    private static final String COLUMNS = "stu_no, name, gender, class_name, major, phone, email";

    /** 默认每页条数。 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限，防止一次拉取过多数据。 */
    private static final int MAX_PAGE_SIZE = 100;

    private static final String SQL_FIND_BY_STU_NO =
            "SELECT " + COLUMNS + " FROM t_student WHERE stu_no = ?";

    private static final String SQL_FIND_ALL =
            "SELECT " + COLUMNS + " FROM t_student ORDER BY stu_no";

    private static final String SQL_COUNT_PREFIX = "SELECT COUNT(*) FROM t_student";

    private static final String SQL_PAGE_PREFIX = "SELECT " + COLUMNS + " FROM t_student";

    private static final String SQL_EXISTS_BY_STU_NO =
            "SELECT 1 FROM t_student WHERE stu_no = ?";

    private static final String SQL_INSERT =
            "INSERT INTO t_student (stu_no, name, gender, class_name, major, phone, email) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE t_student SET name = ?, gender = ?, class_name = ?, "
                    + "major = ?, phone = ?, email = ? WHERE stu_no = ?";

    private static final String SQL_DELETE =
            "DELETE FROM t_student WHERE stu_no = ?";

    private static final String SQL_FIND_ALL_CLASSES =
            "SELECT DISTINCT class_name FROM t_student "
                    + "WHERE class_name IS NOT NULL AND class_name <> '' ORDER BY class_name";

    @Override
    public Student findByStuNo(String stuNo) throws SQLException {
        if (!hasText(stuNo)) {
            return null;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STU_NO)) {
            ps.setString(1, stuNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    @Override
    public List<Student> findByPage(String name, String className, int page, int pageSize) throws SQLException {
        int safePage = Math.max(page, 1);
        int safePageSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        long offset = (long) (safePage - 1) * safePageSize;

        // 页码极大时偏移量可能溢出 int，直接返回空列表，避免 LIMIT 参数异常
        if (offset > Integer.MAX_VALUE) {
            return Collections.emptyList();
        }

        List<String> params = new ArrayList<>();
        String where = buildWhere(name, className, params);
        String sql = SQL_PAGE_PREFIX + where + " ORDER BY stu_no LIMIT ?, ?";

        List<Student> students = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = fillLikeParams(ps, params);
            ps.setInt(index++, (int) offset);
            ps.setInt(index, safePageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
        }
        return students;
    }

    @Override
    public int count(String name, String className) throws SQLException {
        List<String> params = new ArrayList<>();
        String where = buildWhere(name, className, params);
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_PREFIX + where)) {
            fillLikeParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean existsByStuNo(String stuNo) throws SQLException {
        if (!hasText(stuNo)) {
            return false;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_STU_NO)) {
            ps.setString(1, stuNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean insert(Student student) throws SQLException {
        if (student == null || !hasText(student.getStuNo()) || !hasText(student.getName())) {
            return false;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, student.getStuNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, hasText(student.getGender()) ? student.getGender().trim() : "男");
            ps.setString(4, trimOrNull(student.getClassName()));
            ps.setString(5, trimOrNull(student.getMajor()));
            ps.setString(6, trimOrNull(student.getPhone()));
            ps.setString(7, trimOrNull(student.getEmail()));
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean update(Student student) throws SQLException {
        if (student == null || !hasText(student.getStuNo()) || !hasText(student.getName())) {
            return false;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, student.getName().trim());
            ps.setString(2, hasText(student.getGender()) ? student.getGender().trim() : "男");
            ps.setString(3, trimOrNull(student.getClassName()));
            ps.setString(4, trimOrNull(student.getMajor()));
            ps.setString(5, trimOrNull(student.getPhone()));
            ps.setString(6, trimOrNull(student.getEmail()));
            ps.setString(7, student.getStuNo().trim());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(String stuNo) throws SQLException {
        if (!hasText(stuNo)) {
            return false;
        }
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, stuNo.trim());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<String> findAllClasses() throws SQLException {
        List<String> classes = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_CLASSES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                classes.add(rs.getString(1));
            }
        }
        return classes;
    }

    /**
     * 根据姓名、班级关键字拼装 WHERE 子句，并把 LIKE 参数按顺序放入 params。
     *
     * @param name      姓名关键字
     * @param className 班级关键字
     * @param params    输出参数列表（LIKE 模式串）
     * @return WHERE 子句（无条件时返回 " WHERE 1 = 1"）
     */
    private static String buildWhere(String name, String className, List<String> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (hasText(name)) {
            where.append(" AND name LIKE ? ESCAPE '!'");
            params.add("%" + escapeLike(name.trim()) + "%");
        }
        if (hasText(className)) {
            where.append(" AND class_name LIKE ? ESCAPE '!'");
            params.add("%" + escapeLike(className.trim()) + "%");
        }
        return where.toString();
    }

    /**
     * 按顺序填充 LIKE 参数。
     *
     * @param ps     预编译语句
     * @param params LIKE 模式串列表
     * @return 下一个待设置参数的下标（从 1 开始）
     * @throws SQLException 设置参数失败时抛出
     */
    private static int fillLikeParams(PreparedStatement ps, List<String> params) throws SQLException {
        int index = 1;
        for (String param : params) {
            ps.setString(index++, param);
        }
        return index;
    }

    /**
     * 转义 LIKE 通配符，配合 {@code ESCAPE '!'} 使用。
     *
     * <p>把 {@code !}、{@code %}、{@code _} 前分别加上 {@code !}，
     * 例如用户搜索 {@code %} 时按普通字符匹配，而不是“匹配全部”。</p>
     *
     * @param keyword 原始关键字
     * @return 转义后的关键字
     */
    private static String escapeLike(String keyword) {
        StringBuilder escaped = new StringBuilder(keyword.length() + 8);
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            if (c == '!' || c == '%' || c == '_') {
                escaped.append('!');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    /**
     * 把结果集当前行映射为 {@link Student}。
     *
     * @param rs 已定位到目标行的结果集
     * @return 学生对象
     * @throws SQLException 读取字段失败时抛出
     */
    private static Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStuNo(rs.getString("stu_no"));
        student.setName(rs.getString("name"));
        student.setGender(rs.getString("gender"));
        student.setClassName(rs.getString("class_name"));
        student.setMajor(rs.getString("major"));
        student.setPhone(rs.getString("phone"));
        student.setEmail(rs.getString("email"));
        return student;
    }

    /**
     * 判断字符串是否非空（含去除首尾空白后）。
     *
     * @param value 待判断字符串
     * @return 非 null 且包含非空白字符时返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 去除首尾空白，空串转为 null，便于写入可空列。
     *
     * @param value 原始字符串
     * @return 处理后的字符串或 null
     */
    private static String trimOrNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
