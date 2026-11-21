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
import java.io.Serial;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 学生详情 Servlet。
 *
 * <p>URL：{@code /student/detail?stuNo=...}。管理员可以查看任意学生，
 * 学生只能查看本人信息；学生请求其他学号时会被送回自己的详情页并提示无权查看。</p>
 */
@WebServlet("/student/detail")
public class StudentDetailServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SESSION_LOGIN_USER = "loginUser";
    private static final String DETAIL_PAGE = "/admin/student-detail.jsp";
    private static final String LOGIN_PAGE = "/login.jsp";
    private static final String STUDENT_HOME = "/student/index.jsp";
    private static final String DB_ERROR_MESSAGE =
            "数据库连接失败，请稍后重试（请检查 MySQL 是否启动、db.properties 配置是否正确）";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + LOGIN_PAGE);
            return;
        }

        String stuNo = trimToEmpty(request.getParameter("stuNo"));

        // 非管理员只能查看本人信息，越权请求直接送回本人详情页
        if (!loginUser.isAdmin()) {
            String ownStuNo = trimToEmpty(loginUser.getStuNo());
            if (ownStuNo.isEmpty()) {
                response.sendRedirect(request.getContextPath() + STUDENT_HOME);
                return;
            }
            if (!Objects.equals(ownStuNo, stuNo)) {
                response.sendRedirect(request.getContextPath()
                        + "/student/detail?stuNo=" + encode(ownStuNo) + "&denied=1");
                return;
            }
        }

        if (stuNo.isEmpty()) {
            request.setAttribute("errorMsg", "缺少学号参数，无法查看学生详情。");
            request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);
            return;
        }

        StudentDao studentDao = new StudentDaoImpl();
        Student student = null;
        String errorMsg = null;
        try {
            student = studentDao.findByStuNo(stuNo);
            if (student == null) {
                errorMsg = "未找到学号为 " + stuNo + " 的学生，可能已被删除。";
            }
        } catch (SQLException e) {
            getServletContext().log("学生详情查询失败：stuNo=" + stuNo, e);
            errorMsg = DB_ERROR_MESSAGE;
        }

        request.setAttribute("student", student);
        request.setAttribute("errorMsg", errorMsg);
        request.getRequestDispatcher(DETAIL_PAGE).forward(request, response);
    }

    private static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_LOGIN_USER);
        return value instanceof User ? (User) value : null;
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
