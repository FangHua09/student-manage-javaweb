package com.servlet;

import com.dao.StudentDao;
import com.dao.impl.StudentDaoImpl;
import com.entity.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * 删除学生 Servlet。
 *
 * <p>URL：{@code /admin/deleteStudent}，仅支持 POST。</p>
 *
 * <p><b>权限</b>：虽然 {@code LoginFilter} 已拦截未登录访问，
 * 但本 Servlet 仍独立做一次管理员判断，与 {@link StudentAddServlet}、
 * {@link StudentListServlet} 的权限规范保持一致；学生身份直接重定向回学生首页。</p>
 */
@WebServlet("/admin/deleteStudent")
public class DeleteStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 学生首页路径（学生角色无权删除，重定向回这里）。 */
    private static final String STUDENT_HOME = "/student/index.jsp";

    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 权限校验：未登录跳回登录页；非管理员跳回学生首页
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!loginUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + STUDENT_HOME);
            return;
        }

        // 2. 获取学号 stuNo（字符串）
        String stuNo = request.getParameter("stuNo");
        if (stuNo == null || stuNo.isBlank()) {
            response.sendRedirect(buildListUrl(request, "paramError"));
            return;
        }

        // 3. 调用 DAO 删除（外键级联删除登录账号）
        boolean delOk;
        try {
            delOk = studentDao.delete(stuNo);
        } catch (SQLException e) {
            // 异常写入容器日志，不向标准输出堆栈，避免泄漏数据库细节
            getServletContext().log("删除学生失败：数据库访问异常，stuNo=" + stuNo, e);
            delOk = false;
        }

        // 4. 删除完成后重定向回学生列表，保留姓名/班级筛选与分页位置，携带提示消息
        String msg = delOk ? "deleteSuccess" : "deleteFail";
        response.sendRedirect(buildListUrl(request, msg));
    }

    /**
     * 禁止 GET 方式删除，地址栏直接访问返回 405。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write("不允许GET方式删除学生！");
    }

    /**
     * 拼接返回列表页的 URL，保留当前姓名、班级筛选条件与分页位置，中文编码防乱码
     */
    private String buildListUrl(HttpServletRequest request, String msg) {
        String name = trimToEmpty(request.getParameter("name"));
        String className = trimToEmpty(request.getParameter("className"));
        String page = trimToEmpty(request.getParameter("page"));
        String pageSize = trimToEmpty(request.getParameter("pageSize"));

        StringBuilder url = new StringBuilder(request.getContextPath())
                .append("/student/list?msg=").append(msg);
        if (!name.isEmpty()) {
            url.append("&name=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        }
        if (!className.isEmpty()) {
            url.append("&className=")
                    .append(URLEncoder.encode(className, StandardCharsets.UTF_8));
        }
        if (!page.isEmpty()) {
            url.append("&page=").append(page);
        }
        if (!pageSize.isEmpty()) {
            url.append("&pageSize=").append(pageSize);
        }
        return url.toString();
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 从 Session 读取当前登录用户。
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

