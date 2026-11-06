package com.servlet;

import com.dao.UserDao;
import com.dao.impl.UserDaoImpl;
import com.entity.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 修改密码 Servlet（登录模块，负责人：易思韵）。
 *
 * <p>URL：{@code /password/update}。</p>
 * <ul>
 *     <li>{@code GET}：按当前登录角色转发到 {@code /admin/password.jsp} 或 {@code /student/password.jsp}；
 *         带 {@code success=1} 时展示修改成功提示（配合 PRG 模式，避免刷新重复提交）。</li>
 *     <li>{@code POST}：参数 {@code oldPassword}、{@code newPassword}、{@code confirmPassword}，
 *         校验旧密码、新密码长度、两次输入一致性后调用 {@link UserDao#updatePassword(int, String)}。</li>
 * </ul>
 *
 * <p><b>安全要点</b>：旧密码必须与数据库中的当前密码比对，不能只依赖页面；由于登录时 Session
 * 中的 {@link User} 已清空密码字段，这里通过 {@link UserDao#findById(int)} 重新读取当前密码校验。
 * 学生与管理员都可以修改自己的密码，用户身份只从 Session 获取，不信任任何请求参数。</p>
 *
 * @author 易思韵
 */
@WebServlet(name = "passwordUpdateServlet", urlPatterns = "/password/update")
public class PasswordUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 新密码最小长度。 */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** 新密码最大长度。 */
    private static final int MAX_PASSWORD_LENGTH = 20;

    /** 管理员改密页路径（相对上下文根）。 */
    private static final String ADMIN_PASSWORD_PAGE = "/admin/password.jsp";

    /** 学生改密页路径（相对上下文根）。 */
    private static final String STUDENT_PASSWORD_PAGE = "/student/password.jsp";

    /** 数据库异常时的统一提示。 */
    private static final String DB_ERROR_MESSAGE =
            "数据库连接失败，请稍后重试（请检查 MySQL 是否启动、db.properties 配置是否正确）";

    /**
     * 展示修改密码页面（{@code GET /password/update}）。
     *
     * <p>正常访问时由登录过滤器保证已登录；这里再做一次兜底判断，
     * 未登录会话直接重定向到登录页，避免空指针。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        if ("1".equals(request.getParameter("success"))) {
            request.setAttribute("success", "密码修改成功，请牢记新密码");
        }
        forwardToPasswordPage(request, response, loginUser);
    }

    /**
     * 处理修改密码表单提交（{@code POST /password/update}）。
     *
     * <p>任一校验不通过时转发回改密页并给出具体错误提示；修改成功后重定向到
     * {@code /password/update?success=1}（PRG 模式）。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 1. 表单层校验（不查库）
        String error = validateForm(oldPassword, newPassword, confirmPassword);
        if (error != null) {
            forwardWithError(request, response, loginUser, error);
            return;
        }

        // 2. 校验旧密码 + 更新密码（查库）
        UserDao userDao = new UserDaoImpl();
        try {
            User current = userDao.findById(loginUser.getUserId());
            if (current == null || !Objects.equals(oldPassword, current.getPassword())) {
                forwardWithError(request, response, loginUser, "旧密码不正确，请重新输入");
                return;
            }
            if (Objects.equals(oldPassword, newPassword)) {
                forwardWithError(request, response, loginUser, "新密码不能与旧密码相同");
                return;
            }
            boolean updated = userDao.updatePassword(loginUser.getUserId(), newPassword);
            if (!updated) {
                forwardWithError(request, response, loginUser, "密码修改失败，请稍后重试");
                return;
            }
            // 3. PRG：重定向后再展示成功提示，刷新页面不会重复提交
            response.sendRedirect(request.getContextPath() + "/password/update?success=1");
        } catch (SQLException e) {
            getServletContext().log("修改密码失败：数据库访问异常，userId=" + loginUser.getUserId(), e);
            forwardWithError(request, response, loginUser, DB_ERROR_MESSAGE);
        }
    }

    /**
     * 校验修改密码表单的非数据库规则。
     *
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     * @param confirmPassword 确认新密码
     * @return 校验通过返回 {@code null}，否则返回错误提示
     */
    private String validateForm(String oldPassword, String newPassword, String confirmPassword) {
        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return "请完整填写旧密码、新密码和确认密码";
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH || newPassword.length() > MAX_PASSWORD_LENGTH) {
            return "新密码长度需为 " + MIN_PASSWORD_LENGTH + "~" + MAX_PASSWORD_LENGTH + " 位";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "两次输入的新密码不一致";
        }
        if (newPassword.equals(oldPassword)) {
            return "新密码不能与旧密码相同";
        }
        return null;
    }

    /**
     * 转发回当前角色对应的改密页并携带错误提示。
     *
     * @param request   HTTP 请求
     * @param response  HTTP 响应
     * @param loginUser 当前登录用户
     * @param error     错误提示
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response,
                                  User loginUser, String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        forwardToPasswordPage(request, response, loginUser);
    }

    /**
     * 按角色转发到管理员或学生的修改密码页。
     *
     * @param request   HTTP 请求
     * @param response  HTTP 响应
     * @param loginUser 当前登录用户
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardToPasswordPage(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        String page = loginUser.isAdmin() ? ADMIN_PASSWORD_PAGE : STUDENT_PASSWORD_PAGE;
        request.getRequestDispatcher(page).forward(request, response);
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

    /**
     * 判断字符串是否为 null 或全空白。
     *
     * @param value 待判断字符串
     * @return 为空返回 true
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
