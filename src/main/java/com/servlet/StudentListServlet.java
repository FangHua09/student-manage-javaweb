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

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/student/list")
public class StudentListServlet extends HttpServlet {

    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 【修复 PR 问题 1：越权拦截】
        // 只有管理员可以访问列表页，学生如果在地址栏乱输，直接拦截
        User loginUser = (User) request.getSession().getAttribute("loginUser");
        if (loginUser == null || !loginUser.isAdmin()) {
            request.setAttribute("errorMsg", "您无权访问学生列表页面！");
            request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
            return;
        }

        // 1. 获取并清理参数（防 null 和空串）
        String name = request.getParameter("name");
        String className = request.getParameter("className");
        if (name == null) name = "";
        if (className == null) className = "";

        // 2. 解析页码和每页条数
        int page = 1;
        int pageSize = 10;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
            String pageSizeStr = request.getParameter("pageSize");
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                pageSize = Integer.parseInt(pageSizeStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
            pageSize = 10;
        }

        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        try {
            // 3. 查总数并计算总页数
            int total = studentDao.count(name, className);
            int totalPages = (total + pageSize - 1) / pageSize;

            // 4. 修正页码
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;

            // 5. 查询当页数据
            List<Student> list = studentDao.findByPage(name, className, page, pageSize);
            List<String> classes = studentDao.findAllClasses();

            // 【修复 PR 问题 5：URL 编码防止中文翻页乱码】
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String encodedClassName = URLEncoder.encode(className, "UTF-8");

            // 6. 存入 request
            request.setAttribute("list", list);
            request.setAttribute("classes", classes);
            request.setAttribute("total", total);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            
            // 原始参数（供表单回显）
            request.setAttribute("name", name);
            request.setAttribute("className", className);
            // 编码后的参数（供分页链接使用）
            request.setAttribute("encodedName", encodedName);
            request.setAttribute("encodedClassName", encodedClassName);

            // 7. 转发到 JSP
            request.getRequestDispatcher("/admin/student-list.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "数据库连接失败或查询异常，请稍后再试！");
            request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
        }
    }
}