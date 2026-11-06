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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 修改学生 Servlet（新增修改模块，负责人：朱景澈）。
 *
 * <p>URL：{@code /student/update}（见《DAO接口文档》§5.3）。</p>
 * <ul>
 *     <li>{@code GET}：按 {@code stuNo} 调用 {@link StudentDao#findByStuNo(String)} 回显数据，
 *         转发到共用表单页 {@code /admin/student-form.jsp}（{@code mode=edit}）。</li>
 *     <li>{@code POST}：学号只作为定位条件（隐藏域传递，页面只读），其余字段按表单校验后
 *         调用 {@link StudentDao#update(Student)} 更新。</li>
 * </ul>
 *
 * <p><b>为什么学号不能被修改</b>：{@code stu_no} 是 {@code t_student} 的主键，
 * 同时被 {@code t_user.stu_no} 外键引用，直接改主键会牵连用户表，
 * 因此本模块把学号做成只读，并只以它作为 {@code WHERE} 条件。</p>
 *
 * @author 朱景澈
 */
@WebServlet(name = "studentUpdateServlet", urlPatterns = "/student/update")
public class StudentUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 新增与修改共用的表单页。 */
    private static final String FORM_PAGE = "/admin/student-form.jsp";

    /** 学生首页路径（学生角色无权修改学生信息）。 */
    private static final String STUDENT_HOME = "/student/index.jsp";

    /** 表单模式：修改。 */
    private static final String MODE_EDIT = "edit";

    /** 数据库异常时的统一提示。 */
    private static final String DB_ERROR_MESSAGE =
            "数据库连接失败，请稍后重试（请检查 MySQL 是否启动、db.properties 配置是否正确）";

    /**
     * 展示修改表单并回显学生数据（{@code GET /student/update?stuNo=...}）。
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

        String stuNo = StudentFormValidator.trim(request.getParameter("stuNo"));
        if (StudentFormValidator.isBlank(stuNo)) {
            forwardWithError(request, response, new Student(), "缺少学号参数，无法定位要修改的学生");
            return;
        }

        try {
            Student student = new StudentDaoImpl().findByStuNo(stuNo);
            if (student == null) {
                forwardWithError(request, response, new Student(),
                        "未找到学号为 " + stuNo + " 的学生，请确认该学生是否已被删除");
                return;
            }
            if ("1".equals(request.getParameter("success"))) {
                request.setAttribute("success", "学生信息修改成功。");
            }
            forwardToForm(request, response, student);
        } catch (SQLException e) {
            getServletContext().log("查询学生失败，stuNo=" + stuNo, e);
            forwardWithError(request, response, new Student(), DB_ERROR_MESSAGE);
        }
    }

    /**
     * 处理修改表单提交（{@code POST /student/update}）。
     *
     * <p>校验失败、学生不存在或更新失败时转发回表单页并保留用户输入；
     * 更新成功后重定向到 {@code /student/update?stuNo=...&success=1}（PRG 模式）。</p>
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

        // 1. 取参数 + 表单校验（学号来自隐藏域，修改时不再要求"必填"以外的额外规则）
        Student form = StudentFormValidator.fromRequest(request);
        String error = StudentFormValidator.validate(form, false);
        if (error != null) {
            forwardWithError(request, response, form, error);
            return;
        }

        // 2. 确认记录仍然存在，再执行更新
        StudentDao studentDao = new StudentDaoImpl();
        try {
            if (studentDao.findByStuNo(form.getStuNo()) == null) {
                forwardWithError(request, response, form,
                        "该学生已不存在（可能已被删除），请返回列表确认");
                return;
            }
            if (!studentDao.update(form)) {
                forwardWithError(request, response, form, "修改失败，请稍后重试");
                return;
            }
            // 3. PRG：重定向回修改页并展示成功提示
            String stuNoParam = URLEncoder.encode(form.getStuNo(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath()
                    + "/student/update?stuNo=" + stuNoParam + "&success=1");
        } catch (SQLException e) {
            getServletContext().log("修改学生失败：数据库访问异常，stuNo=" + form.getStuNo(), e);
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
            // 学生只能查看本人信息，不允许修改学生档案
            response.sendRedirect(request.getContextPath() + STUDENT_HOME);
            return false;
        }
        return true;
    }

    /**
     * 转发到共用表单页并携带表单数据（修改模式）。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param form     回显用的学生对象
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response, Student form)
            throws ServletException, IOException {
        request.setAttribute("mode", MODE_EDIT);
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
     * 读取班级列表供表单输入框提示；查库失败返回空列表，不影响表单展示。
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
