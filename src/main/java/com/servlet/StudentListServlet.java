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
import java.util.List;

@WebServlet("/student/list")
public class StudentListServlet extends HttpServlet {

    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. 获取并清理参数（防 null 和空串）
        String name = request.getParameter("name");
        String className = request.getParameter("className");
        if (name == null) name = "";
        if (className == null) className = "";

        // 2. 解析页码和每页条数（做好容错处理）
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
            // 解析失败就用默认值
            page = 1;
            pageSize = 10;
        }
        
        // 规范要求：pageSize < 1 按默认 10 条；> 100 截断为 100
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        try {
            // 3. 查总数并计算总页数
            int total = studentDao.count(name, className);
            int totalPages = (total + pageSize - 1) / pageSize;

            // 4. 修正页码（防止越界）
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;

            // 5. 查询当页数据
            List<Student> list = studentDao.findByPage(name, className, page, pageSize);
            List<String> classes = studentDao.findAllClasses(); // 班级下拉框数据

            // 6. 把数据存入 request，供 JSP 使用
            request.setAttribute("list", list);
            request.setAttribute("classes", classes);
            request.setAttribute("total", total);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("name", name);           // 回显搜索条件
            request.setAttribute("className", className); // 回显搜索条件

            // 7. 转发到 JSP 页面
            request.getRequestDispatcher("/admin/student-list.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "数据库连接失败或查询异常，请稍后再试！");
            request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
        }
    }
}
