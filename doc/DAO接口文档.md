# 公共层（实体 / JDBC 工具类 / DAO）接口文档

> 维护人：组长 杨亮　｜　适用版本：v1.0（框架搭建阶段）
> 本文是**全组模块联调的接口契约**。组员开发登录、增删改查、列表、Filter 时直接调用本文中的类和方法，
> 不要在各自模块里重复写实体、连接工具类或 SQL。接口如有调整，先改本文档并在群里同步。

## 1. 公共层总览

```text
src/main/java/com/
├── entity/
│   ├── User.java              用户实体（t_user）
│   └── Student.java           学生实体（t_student）
├── util/
│   └── DbUtil.java            JDBC 工具类：读配置、取连接、关资源、回滚
└── dao/
    ├── UserDao.java           用户 DAO 接口
    ├── StudentDao.java        学生 DAO 接口
    └── impl/
        ├── UserDaoImpl.java
        └── StudentDaoImpl.java
```

所有 DAO 方法都声明 `throws java.sql.SQLException`，Servlet 中捕获后转成友好提示或 `ServletException`。
DAO 实现类无状态，每次请求 `new UserDaoImpl()` / `new StudentDaoImpl()` 即可，无需单例。

## 2. DbUtil（`com.util.DbUtil`）

| 方法 | 说明 |
| ---- | ---- |
| `static Connection getConnection() throws SQLException` | 从 classpath 读取 `db.properties` 并返回连接 |
| `static boolean isConfigured()` | 配置是否就绪（文件存在、必填项完整、驱动可加载） |
| `static boolean canConnect()` | 真实建立一次连接探测，供测试/自检使用 |
| `static void close(ResultSet rs, Statement stmt, Connection conn)` | 按 ResultSet → Statement → Connection 逆序关闭，允许 null |
| `static void close(Connection conn)` | 只关闭连接 |
| `static void rollback(Connection conn)` | 事务回滚并恢复自动提交 |

典型写法（DAO / Servlet 通用）：

```java
try (Connection conn = DbUtil.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, value);
    try (ResultSet rs = ps.executeQuery()) {
        // 处理结果
    }
}
```

传统写法（课程要求掌握 finally 释放时）：

```java
Connection conn = null;
PreparedStatement ps = null;
ResultSet rs = null;
try {
    conn = DbUtil.getConnection();
    // ...
} catch (SQLException e) {
    // 记录日志或转成业务异常
} finally {
    DbUtil.close(rs, ps, conn);
}
```

> 配置文件约定：`src/main/resources/db.properties`（由 `db.properties.template` 复制），
> 已被 `.gitignore` 忽略，**禁止提交**。读取必须走 ClassLoader，不能用相对文件路径。

## 3. UserDao（`com.dao.UserDao`）— 登录 / 修改密码模块使用

| 方法 | 参数 | 返回 | 说明 |
| ---- | ---- | ---- | ---- |
| `findByUsername(String username)` | 登录账号 | `User` 或 `null` | 按账号查询；不存在返回 null |
| `findById(int userId)` | 用户主键 | `User` 或 `null` | 按主键查询 |
| `updatePassword(int userId, String newPassword)` | 用户主键、新密码 | `boolean` | 新密码为空直接返回 false |

登录流程建议（易思韵模块）：

```java
UserDao userDao = new UserDaoImpl();
User user = userDao.findByUsername(username);
if (user != null && user.getPassword().equals(password)) {
    session.setAttribute("loginUser", user);   // 统一 Session key：loginUser
    if (user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/admin/index.jsp");
    } else {
        response.sendRedirect(request.getContextPath() + "/student/index.jsp");
    }
}
```

`User` 自带便捷方法：`isAdmin()`、`isStudent()`、`getRoleName()`（EL 中可用 `${loginUser.roleName}`）。

## 4. StudentDao（`com.dao.StudentDao`）— 增删改查 / 列表 / 查询模块使用

| 方法 | 参数 | 返回 | 使用模块 |
| ---- | ---- | ---- | ---- |
| `findByStuNo(String stuNo)` | 学号 | `Student` 或 `null` | 详情 / 修改回显 / 学生查看本人信息 |
| `findAll()` | — | `List<Student>` | 调试、导出（正式列表请用分页） |
| `findByPage(String name, String className, int page, int pageSize)` | 姓名关键字、班级关键字、页码、每页条数 | `List<Student>` | 列表 / 查询 |
| `count(String name, String className)` | 姓名关键字、班级关键字 | `int` | 分页总数（与 `findByPage` 同条件） |
| `existsByStuNo(String stuNo)` | 学号 | `boolean` | 新增前学号重复校验 |
| `insert(Student student)` | 学生对象 | `boolean` | 新增 |
| `update(Student student)` | 学生对象（学号定位） | `boolean` | 修改 |
| `delete(String stuNo)` | 学号 | `boolean` | 删除 |
| `findAllClasses()` | — | `List<String>` | 搜索页班级下拉框 |

### 4.1 分页约定（全组统一）

- 页码 `page` **从 1 开始**，不是从 0。
- `page < 1` 按第 1 页处理；`pageSize < 1` 按默认 10 条；`pageSize > 100` 截断为 100。
- 页码极大（偏移量溢出）时返回空列表，不抛异常。
- 统计总数与查询列表必须使用**同一组条件**：先 `count(name, className)`，再
  `findByPage(name, className, page, pageSize)`。

列表模块（王竣熙）建议写法：

```java
StudentDao studentDao = new StudentDaoImpl();
String name = normalize(request.getParameter("name"));
String className = normalize(request.getParameter("className"));
int page = parseInt(request.getParameter("page"), 1);
int pageSize = 10;

int total = studentDao.count(name, className);
int totalPages = (total + pageSize - 1) / pageSize;   // 总记录数为 0 时 totalPages = 0
if (totalPages > 0 && page > totalPages) { page = totalPages; }
List<Student> list = studentDao.findByPage(name, className, page, pageSize);
request.setAttribute("page", page);
request.setAttribute("totalPages", totalPages);
request.setAttribute("total", total);
request.setAttribute("list", list);
request.getRequestDispatcher("/admin/student-list.jsp").forward(request, response);
```

### 4.2 模糊查询约定

- 传入**原始关键字**即可，DAO 自动拼 `%关键字%`。
- `%`、`_`、`!` 由 DAO 转义（`LIKE ? ESCAPE '!'`），所有报文都走 `PreparedStatement`，无 SQL 注入。
- 关键字为 null 或空串表示不限定该条件；姓名和班级可同时使用。

### 4.3 增删改约定

- `insert` 前先 `existsByStuNo` 判重，给用户友好提示；不要依赖主键冲突异常。
- `update` 以学号定位，学号本身不允许修改。
- 删除学生会因外键级联删除其登录账号，删除前必须 `onclick="return confirm(...)"` 二次确认。
- 新增 / 修改 / 删除返回 `true` 表示影响 1 行，`false` 表示参数非法或数据不存在。

## 5. 全组统一的命名约定（联调关键）

### 5.1 Session 属性

| Key | 类型 | 说明 |
| ---- | ---- | ---- |
| `loginUser` | `com.entity.User` | 登录用户对象（Filter、JSP 统一从该 key 读取） |

### 5.2 请求参数名

| 页面 / 功能 | 参数名 |
| ---- | ---- |
| 登录 | `username`、`password` |
| 修改密码 | `oldPassword`、`newPassword`、`confirmPassword` |
| 学生表单（新增 / 修改） | `stuNo`、`name`、`gender`、`className`、`major`、`phone`、`email` |
| 列表 / 搜索 | `name`、`className`、`page`、`pageSize` |
| 详情 / 删除 / 修改定位 | `stuNo` |

### 5.3 建议 URL 映射（各模块按此实现，便于联调）

| URL（相对上下文） | 负责模块 | 说明 |
| ---- | ---- | ---- |
| `/login`（POST）、`/logout` | 登录模块 | 登录、退出 |
| `/password/update` | 登录模块 | 修改密码 |
| `/student/list` | 列表查询模块 | 分页 + 条件查询 |
| `/student/detail` | 列表查询模块 | 详情 |
| `/student/add`、`/student/update` | 新增修改模块 | 共用表单页 |
| `/student/delete` | 删除模块 | 删除 |

### 5.4 JSP 约定

- 统一编码 UTF-8；`web.xml` 已配置全站 JSP `page-encoding=UTF-8`，无需每页再写 `pageEncoding`。
- JSTL 3.0 的 taglib URI 统一使用 **`jakarta.tags.core`**（旧的 `http://java.sun.com/jsp/jstl/core` 已废弃；
  3.0.1 实现 jar 里虽然保留了兼容 TLD，但全组必须统一新 URI，不要混用）：

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```

- 页面取值用 EL：`${sessionScope.loginUser.username}`、`${student.name}`、`${list}`
- JSP 中不写数据库代码；业务数据由 Servlet 放入 request / session 后转发。

## 6. 异常、事务与资源

1. DAO 抛出的 `SQLException` 在 Servlet 中捕获：记录 `ServletContext.log` 或 `e.printStackTrace()`，
   给用户返回统一错误提示，不要把堆栈输出到页面。
2. 单条 SQL 无需显式事务，连接默认自动提交。
3. 多步写操作（如“删除学生 + 删除账号”这类组合）才需要
   `conn.setAutoCommit(false)` + `commit`，异常时调用 `DbUtil.rollback(conn)`。
4. 连接必须在 finally 或 try-with-resources 中关闭，禁止把 `Connection` 存到 Servlet 成员变量 / Session。
5. 数据库不可用时，页面应提示“数据库连接失败”，不能直接 500 白屏。

## 7. 接口变更流程

1. 先修改本文件（方法签名、参数名、Session key 等契约）。
2. 同步修改 `src/main/java/com/{entity,dao,util}` 与 `src/test/java` 下对应测试。
3. 在小组群说明变更影响范围，组员 `git pull origin main` 后再继续开发。
4. 不允许个人在 feature 分支私自修改公共层后直接合并——公共层由组长维护。
