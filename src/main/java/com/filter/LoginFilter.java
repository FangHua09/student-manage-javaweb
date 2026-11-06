package com.filter;

import com.entity.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 登录与权限拦截过滤器（登录模块，负责人：易思韵）。
 *
 * <p>映射 {@code /*}，对所有请求做两道服务端校验：</p>
 * <ol>
 *     <li><b>未登录拦截</b>：除登录页、登录接口与静态资源白名单外，未登录访问一律重定向到
 *         {@code /login.jsp}，防止用户直接敲 URL 绕过登录。</li>
 *     <li><b>角色越权拦截</b>：已登录的学生访问 {@code /admin/**} 时重定向到学生首页；
 *         页面隐藏菜单只是体验优化，真实权限必须由服务端保证。</li>
 * </ol>
 *
 * <p>过滤器只做“是否放行”的判断，具体业务校验（如学生只能看本人数据、修改密码校验旧密码）
 * 仍由对应 Servlet 在服务端完成，不能只依赖本过滤器。</p>
 *
 * <p>说明：{@code @WebFilter} 默认只拦截 REQUEST 类型分派，容器内部转发（forward）与错误页
 * 分派不会再次经过本过滤器，因此不会出现错误页被重定向的死循环。</p>
 *
 * @author 易思韵
 */
@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

    /** Session 中登录用户对象的 key（全组统一约定）。 */
    private static final String SESSION_LOGIN_USER = "loginUser";

    /** 登录页路径（相对上下文根）。 */
    private static final String LOGIN_PAGE = "/login.jsp";

    /** 学生首页路径（相对上下文根）。 */
    private static final String STUDENT_HOME = "/student/index.jsp";

    /** 管理员目录（相对上下文根），学生角色禁止访问。 */
    private static final String ADMIN_AREA = "/admin";

    /**
     * 完全公开的请求路径白名单（精确匹配，相对上下文根）。
     *
     * <p>{@code "/"} 是上下文根，由欢迎文件 {@code login.jsp} 响应；
     * {@code /login} 是登录提交接口，必须允许未登录访问。</p>
     */
    private static final String[] PUBLIC_PATHS = {
            "/",
            "/login.jsp",
            "/login",
            "/favicon.ico"
    };

    /**
     * 公开的静态资源前缀白名单（相对上下文根）。
     *
     * <p>登录页在登录前就要加载 CSS / JS / 图片，这些资源不包含业务数据，放行不影响安全。</p>
     */
    private static final String[] PUBLIC_PREFIXES = {
            "/css/",
            "/js/",
            "/images/",
            "/static/"
    };

    /**
     * 过滤器初始化（无额外资源需要初始化）。
     *
     * @param filterConfig 过滤器配置
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // 无状态过滤器，无需初始化资源
    }

    /**
     * 执行登录态与角色校验。
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = getPathWithinApplication(httpRequest);
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User loginUser = session == null ? null : asLoginUser(session.getAttribute(SESSION_LOGIN_USER));

        // 未登录：统一回登录页（不暴露目标地址，避免被用来探测受保护路径）
        if (loginUser == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + LOGIN_PAGE);
            return;
        }

        // 已登录学生访问管理员目录：服务端拦截并送回学生首页
        if (loginUser.isStudent() && isAdminArea(path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + STUDENT_HOME);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 过滤器销毁（无资源需要释放）。
     */
    @Override
    public void destroy() {
        // 无状态过滤器，无需释放资源
    }

    /**
     * 取得相对上下文根的请求路径，如 {@code /student/index.jsp}。
     *
     * <p>不使用 {@code getServletPath()}，因为其返回值受 Servlet 映射规则影响；
     * 用请求 URI 去掉上下文路径更稳定。</p>
     *
     * @param request HTTP 请求
     * @return 相对上下文根的路径，至少为 {@code "/"}
     */
    private static String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }

    /**
     * 判断路径是否属于公开白名单（无需登录即可访问）。
     *
     * @param path 相对上下文根的路径
     * @return 公开返回 true
     */
    private static boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.equals(path)) {
                return true;
            }
        }
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断路径是否位于管理员目录下。
     *
     * @param path 相对上下文根的路径
     * @return 属于 {@code /admin} 或 {@code /admin/**} 返回 true
     */
    private static boolean isAdminArea(String path) {
        return ADMIN_AREA.equals(path) || path.startsWith(ADMIN_AREA + "/");
    }

    /**
     * 把 Session 属性安全转换为登录用户对象。
     *
     * @param value Session 属性值
     * @return 登录用户；类型不符或为空返回 {@code null}
     */
    private static User asLoginUser(Object value) {
        return value instanceof User ? (User) value : null;
    }
}
