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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/student/list")
public class StudentListServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SESSION_LOGIN_USER = "loginUser";

    private final StudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 只有管理员可以访问列表页；未登录回登录页，学生回学生首页
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if (!loginUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/student/index.jsp");
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

            // 4. 修正页码：空结果也固定为第 1 页，避免显示“第 999 / 0 页”
            if (page < 1) page = 1;
            if (totalPages == 0) {
                page = 1;
            } else if (page > totalPages) {
                page = totalPages;
            }

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
            getServletContext().log("学生列表查询失败：name=" + name + ", className=" + className
                    + ", page=" + page + ", pageSize=" + pageSize, e);
            request.setAttribute("errorMsg", "数据库连接失败或查询异常，请稍后再试！");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/WEB-INF/error/500.jsp").forward(request, response);
        }
    }

    private static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_LOGIN_USER);
        return value instanceof User ? (User) value : null;
    }
}
