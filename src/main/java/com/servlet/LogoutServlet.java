package com.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 退出登录 Servlet（登录模块，负责人：易思韵）。
 *
 * <p>处理 {@code GET /logout} 与 {@code POST /logout}：销毁当前会话（{@link HttpSession#invalidate()}），
 * 然后重定向到登录页并携带 {@code logout=1}，由登录页展示“已安全退出登录”的提示。</p>
 *
 * <p>销毁会话而不是只移除 {@code loginUser} 属性，可以确保会话中所有登录态数据一并清除。</p>
 *
 * @author 易思韵
 */
@WebServlet(name = "logoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * 处理 GET 方式的退出（页面导航链接使用）。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 重定向失败时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logout(request, response);
    }

    /**
     * 处理 POST 方式的退出（表单提交时使用）。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 重定向失败时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logout(request, response);
    }

    /**
     * 销毁会话并重定向到登录页。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 重定向失败时抛出
     */
    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=1");
    }
}
