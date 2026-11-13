package com.servlet;

import com.dao.StudentDao;
import com.dao.impl.StudentDaoImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/deleteStudent")
public class DeleteStudentServlet extends HttpServlet {
    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 登录权限校验：未登录跳回登录页
        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 2. 获取学号 stuNo（字符串）
        String stuNo = request.getParameter("stuNo");
        if (stuNo == null || stuNo.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/student/list?msg=paramError");
            return;
        }

        // 3. 调用 DAO 删除（外键级联删除登录账号）
        boolean delOk;
        try {
            delOk = studentDao.delete(stuNo);
        } catch (SQLException e) {
            e.printStackTrace();
            delOk = false;
        }

        // 4. 删除完成后重定向回学生列表，携带提示消息
        if (delOk) {
            response.sendRedirect(request.getContextPath() + "/student/list?msg=deleteSuccess");
        } else {
            response.sendRedirect(request.getContextPath() + "/student/list?msg=deleteFail");
        }
    }

    // 禁止 GET 方式删除，地址栏直接访问返回 405
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write("不允许GET方式删除学生！");
    }
}
