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
 * 登录认证 Servlet（登录模块，负责人：易思韵）。
 *
 * <p>处理 {@code POST /login} 表单提交：校验参数 → 调用 {@link UserDao#findByUsername(String)}
 * 查询账号 → 比对密码 → 登录成功后把 {@link User} 放入 Session（key 固定为 {@code loginUser}）→
 * 按角色重定向到管理员首页或学生首页。{@code GET /login} 直接转发到登录页。</p>
 *
 * <p><b>安全要点</b>：</p>
 * <ol>
 *     <li>账号不存在与密码错误返回<b>同一句提示</b>，避免被用来枚举系统账号。</li>
 *     <li>登录成功后先 {@link HttpSession#invalidate()} 旧会话、再新建会话，防止 Session Fixation。</li>
 *     <li>Session 中保存的 {@link User} 密码字段置为 {@code null}，会话里不保留明文密码；
 *         修改密码时由 {@code PasswordUpdateServlet} 重新查库校验旧密码。</li>
 * </ol>
 *
 * @author 易思韵
 */
@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 登录页路径（相对上下文根）。 */
    private static final String LOGIN_PAGE = "/login.jsp";

    /** 管理员首页路径（相对上下文根）。 */
    private static final String ADMIN_HOME = "/admin/index.jsp";

    /** 学生首页路径（相对上下文根）。 */
    private static final String STUDENT_HOME = "/student/index.jsp";

    /** 账号或密码错误的统一提示，不区分“账号不存在”和“密码错误”。 */
    private static final String LOGIN_FAILED_MESSAGE = "账号或密码错误，请重新输入";

    /**
     * 展示登录页（{@code GET /login}）。
     *
     * <p>直接访问本地址时转发到 {@code login.jsp}；已登录用户的跳转由登录页内的
     * JSTL 判断完成，这里不做业务处理。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }

    /**
     * 处理登录表单提交（{@code POST /login}）。
     *
     * <p>参数：{@code username}、{@code password}。校验失败或认证失败时转发回登录页，
     * 并通过 request 属性 {@code error}（错误提示）、{@code username}（回显账号）反馈；
     * 认证成功时重定向到对应角色的首页。</p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trimToEmpty(request.getParameter("username"));
        String password = request.getParameter("password");

        // 1. 基础非空校验：不查库，直接回显错误
        if (username.isEmpty() || password == null || password.isEmpty()) {
            forwardToLogin(request, response, "账号和密码不能为空", username);
            return;
        }

        // 2. 查库认证：SQLException 统一转成用户可读提示，不把堆栈吐到页面
        UserDao userDao = new UserDaoImpl();
        try {
            User user = userDao.findByUsername(username);
            if (user == null || !Objects.equals(password, user.getPassword())) {
                forwardToLogin(request, response, LOGIN_FAILED_MESSAGE, username);
                return;
            }

            // 3. 登录成功：先销毁旧会话再新建，防止 Session Fixation
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            // 不在会话中保存明文密码，修改密码时重新查库校验
            user.setPassword(null);
            session.setAttribute(SESSION_LOGIN_USER, user);

            // 4. 按角色进入不同首页（学生不能进管理员页，Filter 再做一层兜底）
            String target = user.isAdmin() ? ADMIN_HOME : STUDENT_HOME;
            response.sendRedirect(request.getContextPath() + target);
        } catch (SQLException e) {
            // 只记录账号，不记录密码
            getServletContext().log("登录认证失败：数据库访问异常，username=" + username, e);
            forwardToLogin(request, response,
                    "数据库连接失败，请稍后重试（请检查 MySQL 是否启动、db.properties 配置是否正确）",
                    username);
        }
    }

    /**
     * 转发回登录页并携带错误提示与账号回显。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param error    错误提示
     * @param username 需要回显的账号（密码不回显）
     * @throws ServletException 转发失败时抛出
     * @throws IOException      IO 异常
     */
    private void forwardToLogin(HttpServletRequest request, HttpServletResponse response,
                                String error, String username) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("username", username);
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }

    /**
     * 去掉首尾空白，{@code null} 转空串。
     *
     * @param value 原始字符串，可为 null
     * @return 非 null 的字符串
     */
    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
