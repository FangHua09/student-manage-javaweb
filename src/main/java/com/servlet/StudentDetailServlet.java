package com.servlet;

import com.dao.StudentDao;
import com.dao.impl.StudentDaoImpl;
import com.entity.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/student/detail")
public class StudentDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String stuNo = request.getParameter("stuNo");
        StudentDao studentDao = new StudentDaoImpl();
        Student student = null;
        try {
            student = studentDao.findByStuNo(stuNo);
        } catch (SQLException e) {
            // 替换 e.printStackTrace()
            getServletContext().log("学生详情查询失败：stuNo=" + stuNo, e);
            request.setAttribute("errorMsg", "数据库查询异常！");
        }
        request.setAttribute("student", student);
        request.getRequestDispatcher("/admin/student-detail.jsp").forward(request, response);
    }
}
