package com.servlet;

import com.dao.StudentDao;
import com.dao.impl.StudentDaoImpl;
import com.entity.Student;
import com.entity.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 新增学生 Servlet（新增修改模块，负责人：朱景澈）。
 *
 * <p>URL：{@code /student/add}（见《DAO接口文档》§5.3）。</p>
 * <ul>
 *     <li>{@code GET}：转发到共用表单页 {@code /admin/student-form.jsp}（{@code mode=add}）；
 *         带 {@code success=1} 时展示新增成功提示（配合 PRG 模式，刷新不会重复提交）。</li>
 *     <li>{@code POST}：参数 {@code stuNo}、{@code name}、{@code gender}、{@code className}、
 *         {@code major}、{@code phone}、{@code email}；先做表单校验，再调用
 *         {@link StudentDao#existsByStuNo(String)} 判重，最后 {@link StudentDao#insert(Student)}。</li>
 * </ul>
 *
 * <p><b>权限</b>：只有管理员可以新增学生。虽然 {@code LoginFilter} 已拦截未登录访问，
 * 但 {@code /student/**} 学生的登录态是允许通过的，因此本 Servlet 必须自己做一次
 * "是否管理员"的服务端判断，学生访问直接重定向回学生首页。</p>
 *
 * @author 朱景澈
 */
@WebServlet(name = "studentAddServlet", urlPatterns = "/student/add")
public class StudentAddServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 新增与修改共用的表单页（放在 {@code /admin} 下，受过滤器角色保护）。 */
    private static final String FORM_PAGE = "/admin/student-form.jsp";

    /** 学生首页路径（学生角色无权新增，重定向回这里）。 */
    private static final String STUDENT_HOME = "/student/index.jsp";

    /** 表单模式：新增。 */
    private static final String MODE_ADD = "add";

    /** 数据库异常时的统一提示（与登录模块保持一致的口径）。 */
    private static final String DB_ERROR_MESSAGE =
            "数据库连接失败，请稍后重试（请检查 MySQL 是否启动、db.properties 配置是否正确）";

    /**
     * 展示新增表单（{@code GET /student/add}）。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureAdmin(request, response)) {
            return;
        }
        if ("1".equals(request.getParameter("success"))) {
            request.setAttribute("success", "学生新增成功，可以继续录入下一条。");
        }
        forwardToForm(request, response, new Student());
    }

    /**
     * 处理新增表单提交（{@code POST /student/add}）。
     *
     * <p>校验失败或学号重复时转发回表单页并保留用户已填内容；
     * 新增成功后重定向到 {@code /student/add?success=1}（PRG 模式）。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ensureAdmin(request, response)) {
            return;
        }

        // 1. 取参数 + 表单校验（不查库）
        Student form = StudentFormValidator.fromRequest(request);
        String error = StudentFormValidator.validate(form, true);
        if (error != null) {
            forwardWithError(request, response, form, error);
            return;
        }

        // 2. 学号重复校验 + 入库（查库）
        StudentDao studentDao = new StudentDaoImpl();
        try {
            if (studentDao.existsByStuNo(form.getStuNo())) {
                forwardWithError(request, response, form,
                        "学号 " + form.getStuNo() + " 已存在，请核对后重新输入");
                return;
            }
            if (!studentDao.insert(form)) {
                forwardWithError(request, response, form, "新增失败，请稍后重试");
                return;
            }
            // 3. PRG：重定向后再展示提示，刷新页面不会重复插入
            response.sendRedirect(request.getContextPath() + "/student/add?success=1");
        } catch (SQLException e) {
            getServletContext().log("新增学生失败：数据库访问异常，stuNo=" + form.getStuNo(), e);
            forwardWithError(request, response, form, DB_ERROR_MESSAGE);
        }
    }

    /**
     * 校当前用户是否为管理员；不是则做相应跳转。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 是管理员返回 true；未登录或非管理员返回 false（已自行跳转）
     * @throws IOException IO 异常
     */
    private boolean ensureAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }
        if (!loginUser.isAdmin()) {
            // 学生只能查看本人信息，不允许进入新增页面
            response.sendRedirect(request.getContextPath() + STUDENT_HOME);
            return false;
        }
        return true;
    }

    /**
     * 转发到共用表单页并携带表单数据（新增模式）。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param form     回显用的学生对象
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response, Student form)
            throws ServletException, IOException {
        request.setAttribute("mode", MODE_ADD);
        request.setAttribute("form", form);
        request.setAttribute("classList", loadClassesSafely());
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }

    /**
     * 转发回表单页并携带错误提示与用户已填内容。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param form     回显用的学生对象
     * @param error    错误提示
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  Student form, String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        forwardToForm(request, response, form);
    }

    /**
     * 读取班级列表供表单输入框提示（班级下拉自动补全）。
     *
     * <p>查库失败不应影响新增表单的展示：数据库不可用时返回空列表，
     * 用户仍然可以手工输入班级，真正提交时再由 DAO 抛出异常并提示。</p>
     *
     * @return 班级名称列表，异常时返回空列表
     */
    private List<String> loadClassesSafely() {
        try {
            return new StudentDaoImpl().findAllClasses();
        } catch (SQLException e) {
            getServletContext().log("读取班级列表失败，班级输入框将不提供自动提示", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从 Session 读取当前登录用户。
     *
     * @param request HTTP 请求
     * @return 登录用户；未登录返回 {@code null}
     */
    private static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_LOGIN_USER);
        return value instanceof User ? (User) value : null;
    }
}
