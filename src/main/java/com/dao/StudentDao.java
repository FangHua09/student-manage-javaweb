package com.dao;

import com.entity.Student;

import java.sql.SQLException;
import java.util.List;

/**
 * 学生信息表 DAO 接口（组长公共代码）。
 *
 * <p>覆盖需求分析报告中的全部数据库操作：列表分页、按姓名/班级模糊查询、详情查看、
 * 新增、修改、删除、学号重复校验。各模块统一调用本接口，禁止在 Servlet / JSP 中直接写 SQL。</p>
 *
 * <p><b>分页约定（全组统一）</b>：页码 {@code page} 从 1 开始；{@code pageSize} 为每页条数。
 * 页码小于 1 时按第 1 页处理；每页条数小于 1 时按默认 10 条、超过 100 时按 100 条处理，
 * 保证越界参数不会拼出非法 SQL。列表查询与总数统计使用同一组查询条件，
 * 调用方应成对调用 {@link #count(String, String)} 与 {@link #findByPage(String, String, int, int)}。</p>
 *
 * <p><b>模糊查询约定</b>：姓名、班级参数传入原始关键字即可，DAO 内部负责拼接 {@code %...%}
 * 并转义 {@code %}、{@code _}、{@code !} 等 LIKE 通配符，全部通过 PreparedStatement 占位符传参。</p>
 *
 * <p>实现类：{@code com.dao.impl.StudentDaoImpl}。</p>
 *
 * @author 杨亮（组长）
 */
public interface StudentDao {

    /**
     * 按学号查询学生（主键查询，用于详情页与修改回显）。
     *
     * @param stuNo 学号，为空时返回 null
     * @return 学生对象；不存在时返回 null
     * @throws SQLException 数据库访问异常
     */
    Student findByStuNo(String stuNo) throws SQLException;

    /**
     * 查询全部学生，按学号升序。
     *
     * <p>数据量较大时应使用分页方法；本方法供导出、调试或数据量很小的场景使用。</p>
     *
     * @return 学生列表，不会返回 null
     * @throws SQLException 数据库访问异常
     */
    List<Student> findAll() throws SQLException;

    /**
     * 分页 + 条件查询学生列表，按学号升序。
     *
     * @param name      姓名模糊查询关键字，null 或空串表示不限定
     * @param className 班级模糊查询关键字，null 或空串表示不限定
     * @param page      页码，从 1 开始；小于 1 时按 1 处理
     * @param pageSize  每页条数；小于 1 时按 10 处理，超过 100 时按 100 处理
     * @return 当前页学生列表，不会返回 null
     * @throws SQLException 数据库访问异常
     */
    List<Student> findByPage(String name, String className, int page, int pageSize) throws SQLException;

    /**
     * 按与分页查询相同的条件统计总记录数。
     *
     * @param name      姓名模糊查询关键字，null 或空串表示不限定
     * @param className 班级模糊查询关键字，null 或空串表示不限定
     * @return 满足条件的记录数
     * @throws SQLException 数据库访问异常
     */
    int count(String name, String className) throws SQLException;

    /**
     * 判断学号是否已存在（新增前校验，避免主键冲突直接抛 SQL 异常）。
     *
     * @param stuNo 学号
     * @return 已存在返回 true
     * @throws SQLException 数据库访问异常
     */
    boolean existsByStuNo(String stuNo) throws SQLException;

    /**
     * 新增学生记录。
     *
     * @param student 学生对象（学号、姓名必填）
     * @return 新增成功（影响 1 行）返回 true
     * @throws SQLException 数据库访问异常（如主键冲突、外键约束）
     */
    boolean insert(Student student) throws SQLException;

    /**
     * 按学号修改学生记录（学号本身不可修改）。
     *
     * @param student 学生对象，学号用于定位，其余字段为更新值
     * @return 修改成功（影响 1 行）返回 true
     * @throws SQLException 数据库访问异常
     */
    boolean update(Student student) throws SQLException;

    /**
     * 按学号删除学生记录。
     *
     * <p>注意：{@code t_user.stu_no} 外键为 ON DELETE CASCADE，删除学生会同时删除其登录账号，
     * 调用方（删除模块）必须在页面上给出二次确认提示。</p>
     *
     * @param stuNo 学号
     * @return 删除成功（影响 1 行）返回 true
     * @throws SQLException 数据库访问异常
     */
    boolean delete(String stuNo) throws SQLException;

    /**
     * 查询全部不重复班级名称，按班级名升序，供搜索页下拉框使用。
     *
     * @return 班级名称列表，不会返回 null
     * @throws SQLException 数据库访问异常
     */
    List<String> findAllClasses() throws SQLException;
}
