package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC 工具类（组长公共代码）。
 *
 * <p>职责：统一从 classpath 读取数据库配置、获取连接、按“结果集 → 语句 → 连接”的逆序释放资源、
 * 提供事务回滚辅助方法。Servlet / DAO 中不允许再各自散落驱动加载与连接代码。</p>
 *
 * <p>配置读取约定：配置文件为 {@code src/main/resources/db.properties}，
 * 通过 {@code DbUtil.class.getClassLoader().getResourceAsStream("db.properties")} 读取，
 * <b>不要使用相对文件路径</b>，否则部署到 Tomcat 后工作目录变化会导致找不到文件。</p>
 *
 * <p>典型用法：</p>
 * <pre>{@code
 * try (Connection conn = DbUtil.getConnection();
 *      PreparedStatement ps = conn.prepareStatement(sql)) {
 *     // 设置参数、执行 SQL
 * }
 * }</pre>
 *
 * <p>若需要手动管理资源（课程要求的传统写法），使用
 * {@link #close(ResultSet, Statement, Connection)} 在 finally 中按逆序关闭。</p>
 *
 * @author 杨亮（组长）
 */
public final class DbUtil {

    /** classpath 下的数据库配置文件名称（相对路径由 ClassLoader 解析）。 */
    private static final String CONFIG_FILE = "db.properties";

    /** 默认 JDBC 驱动类，兼容 MySQL Connector/J 8。 */
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final Logger LOGGER = Logger.getLogger(DbUtil.class.getName());

    /** 懒加载的配置缓存，volatile + 双重检查保证线程安全。 */
    private static volatile Properties config;

    /** 工具类禁止实例化。 */
    private DbUtil() {
    }

    /**
     * 加载并校验 classpath 下的 db.properties（只加载一次）。
     *
     * @return 数据库配置
     * @throws IllegalStateException 配置文件缺失、读取失败、驱动加载失败或缺少必填项时抛出
     */
    private static Properties loadConfig() {
        Properties cached = config;
        if (cached != null) {
            return cached;
        }
        synchronized (DbUtil.class) {
            if (config != null) {
                return config;
            }
            Properties props = new Properties();
            try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (in == null) {
                    throw new IllegalStateException("classpath 下未找到 " + CONFIG_FILE
                            + "：请先复制 db.properties.template 为 src/main/resources/db.properties，"
                            + "并填写本地 MySQL 账号密码");
                }
                // 显式使用 UTF-8 读取，避免配置文件包含中文（如注释、用户名）时乱码
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IllegalStateException("读取数据库配置文件 " + CONFIG_FILE + " 失败", e);
            }
            require(props, "url");
            require(props, "username");
            String driver = props.getProperty("driver", DEFAULT_DRIVER).trim();
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("未找到 JDBC 驱动类 " + driver
                        + "，请确认 pom.xml 已引入 mysql-connector-j 依赖", e);
            }
            config = props;
            return props;
        }
    }

    /**
     * 校验必填配置项。
     *
     * @param props 配置
     * @param key   配置项名称
     * @throws IllegalStateException 值为空时抛出
     */
    private static void require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(CONFIG_FILE + " 缺少必填配置项：" + key);
        }
    }

    /**
     * 获取数据库连接。调用方必须负责关闭（推荐 try-with-resources）。
     *
     * @return 数据库连接
     * @throws SQLException 配置缺失或连接失败时抛出
     */
    public static Connection getConnection() throws SQLException {
        Properties props;
        try {
            props = loadConfig();
        } catch (IllegalStateException e) {
            // 统一转换为 SQLException，DAO / Servlet 只需处理一种检查型异常
            throw new SQLException(e.getMessage(), e);
        }
        return DriverManager.getConnection(
                props.getProperty("url").trim(),
                props.getProperty("username").trim(),
                props.getProperty("password", ""));
    }

    /**
     * 数据库配置是否已就绪（文件存在、必填项完整、驱动可加载）。
     *
     * <p>供测试类在缺少本地配置时优雅跳过集成测试，避免新人克隆项目后 mvn test 直接失败。</p>
     *
     * @return 配置可用返回 true
     */
    public static boolean isConfigured() {
        try {
            loadConfig();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 尝试建立一次真实连接，用于启动自检或测试环境探测。
     *
     * @return 能连上数据库返回 true
     */
    public static boolean canConnect() {
        if (!isConfigured()) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "数据库连接检测失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 按“ResultSet → Statement → Connection”的逆序关闭资源。
     *
     * <p>任一资源关闭失败都不会影响后续资源关闭，也不会向调用方抛出异常。
     * 允许传入 null（例如只查数时没有 ResultSet）。</p>
     *
     * @param rs   结果集，可为 null
     * @param stmt 语句对象，可为 null
     * @param conn 连接对象，可为 null
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, "关闭 ResultSet 失败", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, "关闭 Statement 失败", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, "关闭 Connection 失败", e);
            }
        }
    }

    /**
     * 只关闭连接（无语句 / 结果集的场景）。
     *
     * @param conn 连接对象，可为 null
     */
    public static void close(Connection conn) {
        close(null, null, conn);
    }

    /**
     * 回滚事务并恢复自动提交。用于新增、修改、删除等多步操作的异常处理。
     *
     * @param conn 连接对象，可为 null
     */
    public static void rollback(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "事务回滚失败", e);
        }
    }
}
