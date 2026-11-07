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

@WebServlet("/student/detail")
public class StudentDetailServlet extends HttpServlet {

    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String stuNo = request.getParameter("stuNo");
        
        // 参数为空时，重定向回列表页
        if (stuNo == null || stuNo.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/student/list");
            return;
        }

        // 权限校验：如果当前登录的是学生，只能查看自己的信息
        HttpSession session = request.getSession(false);
        if (session != null) {
            User loginUser = (User) session.getAttribute("loginUser");
            if (loginUser != null && loginUser.isStudent()) {
                // 如果传过来的学号不是当前登录学生的学号，拒绝访问
                if (!stuNo.equals(loginUser.getStuNo())) {
                    request.setAttribute("errorMsg", "您无权查看其他学生的信息！");
                    request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
                    return;
                }
            }
        }

        try {
            Student student = studentDao.findByStuNo(stuNo);
            if (student == null) {
                request.setAttribute("errorMsg", "未找到该学号对应的学生信息！");
                request.getRequestDispatcher("/WEB-INF/error/404.jsp").forward(request, response);
                return;
            }

            request.setAttribute("student", student);
            request.getRequestDispatcher("/admin/student-detail.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "数据库查询异常！");
            request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
        }
    }
}
