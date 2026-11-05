# 学生信息管理系统（JavaWeb课程大作业）

> 技术栈：Servlet + JSP + JDBC + MySQL + Tomcat（原生技术，不使用 SSM / Spring Boot）
> 课程：JavaWeb程序设计（2026秋）
> 小组：242班 5人小组

> 📌 本 README 已按本机实际开发环境（JDK 17 + Tomcat 11 + MySQL 8.0）重写，
> 覆盖 GitHub 仓库中的旧版环境说明（旧版写的 JDK 1.8 / Tomcat 8.5 已不适用）。

## ✅ 项目简介

本系统是简易 B/S 架构学生信息管理平台，为课程综合实训项目。
实现**管理员、普通学生双角色权限**，基于原生 JDBC 操作数据库，完成学生信息增删改查、条件搜索、分页、登录权限控制。

### 功能清单

1. 用户登录：账号密码校验，Session 保存登录状态
2. 权限控制：Filter 过滤器拦截未登录访问，学生只能查看本人信息
3. 管理员：学生信息新增、修改、删除、分页列表、按姓名/班级模糊查询、详情查看
4. 普通学生：仅查看本人信息、修改个人密码
5. 前端：HTML + CSS + 原生 JS 表单校验、删除二次确认

## 👥 小组成员与分工

| 角色 | 姓名 | 负责模块 | 分支名称 |
| ---- | ---- | -------- | -------- |
| 组长 | 杨亮 | 项目整体框架搭建、数据库设计、JDBC 工具类与 DAO 层开发、代码整合联调、文档汇总 | `feature/framework-jdbc` |
| 组员 | 易思韵 | 登录认证模块、Session 会话管理、Filter 权限拦截、密码修改功能 | `feature/login-filter` |
| 组员 | 朱景澈 | 学生信息新增与修改模块，Servlet 控制器与 JSP 表单页面开发 | `feature/student-add-update` |
| 组员 | 王竣熙 | 学生信息列表展示、分页与条件查询、详情查看功能 | `feature/student-list-search` |
| 组员 | 贺尧 | 删除功能、页面样式美化与 JS 表单校验、系统测试、报告排版 | `feature/delete-ui-test` |

## 🛠️ 开发环境

- JDK：**17**（Tomcat 11 最低要求 Java 17，不支持 JDK 1.8）
- 服务器：**Tomcat 11.0**（Jakarta EE 11 / Servlet 6.1）
- 数据库：MySQL 8.0（库名 `student_db`，字符集 utf8mb4）
- 构建工具：Maven（项目自带 `mvnw` / `mvnw.cmd`，war 打包）
- IDE：IDEA / Eclipse
- 版本控制：Git + GitHub

> ⚠️ Tomcat 10 及以上使用 `jakarta.servlet.*` 包，Tomcat 8.5/9 使用 `javax.servlet.*` 包，两者代码**不能混用**。
> 本项目统一使用 Jakarta（`jakarta.servlet-api 6.1.0`），请勿使用 Tomcat 9 及以下版本运行。

## 🚦 当前进度（v1.0 框架搭建阶段）

| 内容 | 负责人 | 状态 |
| ---- | ---- | ---- |
| 项目框架（Maven / Jakarta / JSTL / 统一编码 / 错误页） | 组长 | ✅ 已完成 |
| 数据库设计（建库建表 + 测试数据） | 组长 | ✅ 已完成 |
| JDBC 工具类 `DbUtil` | 组长 | ✅ 已完成 |
| 实体 + DAO 接口 + JDBC 实现 | 组长 | ✅ 已完成 |
| 公共层 JUnit 5 集成测试、WAR 打包与 Tomcat 11 部署冒烟 | 组长 | ✅ 已完成 |
| 登录 / Session / Filter、增删改查、列表分页、页面样式等业务模块 | 组员 | ⏳ 待开发（接口契约已就绪） |

**组员开发前请先读**：[`doc/DAO接口文档.md`](doc/DAO接口文档.md)（实体字段、DAO 方法签名、请求参数名、Session key、URL 约定），
避免各写各的参数名导致联调对不上。

## 📁 项目结构（Maven 标准目录）

```text
student_manage_javaweb
├── pom.xml                              // Maven 依赖与打包配置
├── mvnw / mvnw.cmd / .mvn               // Maven Wrapper（无需本机安装 Maven）
├── db.properties.template               // 数据库配置模板（复制为 src/main/resources/db.properties）
├── .gitignore / .gitattributes          // Git 忽略规则 / 换行规范
├── sql
│   └── student_db.sql                   // 建库建表脚本 + 100 条测试数据
├── doc                                  // 开发文档（需求报告含个人信息，单独归档，不提交公开仓库）
│   ├── 数据库设计说明.md                  // 表结构、索引、外键、测试数据
│   ├── DAO接口文档.md                    // 公共层接口契约（组员必读）
│   ├── 开发规范.md                       // 命名、分层、编码、Git 规范
│   └── 整合联调说明.md                    // 构建、部署、模块进度、FAQ
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       ├── entity              // JavaBean 实体类（User、Student）
    │   │       ├── dao                 // DAO 接口
    │   │       │   └── impl            // DAO 的 JDBC 实现
    │   │       ├── util                // JDBC 工具类（DbUtil）
    │   │       ├── servlet             // Servlet 控制器（组员开发）
    │   │       └── filter              // 登录过滤器（组员开发）
    │   ├── resources
    │   │   └── db.properties           // 本地数据库配置（不提交，用 DbUtil 从 classpath 读取）
    │   └── webapp
    │       ├── WEB-INF
    │       │   ├── web.xml             // 部署描述符（编码 / session / JSP / 错误页）
    │       │   └── error               // 统一 404 / 500 错误页
    │       ├── login.jsp               // 登录页面
    │       ├── index.jsp               // 系统入口（未登录跳登录页）
    │       ├── student                 // 学生相关页面
    │       ├── admin                   // 管理员页面
    │       └── css / js                // 样式、前端校验脚本
    └── test
        └── java                        // 公共层 JUnit 5 集成测试
```

## 🧱 公共层速览（组长维护）

| 类 | 作用 | 供谁使用 |
| ---- | ---- | ---- |
| `com.entity.User` / `Student` | 与 `t_user` / `t_student` 一一对应的 JavaBean | 全组 |
| `com.util.DbUtil` | classpath 读配置、取连接、资源逆序关闭、事务回滚 | DAO / 需要手写 JDBC 的场景 |
| `com.dao.UserDao` + `impl` | 按账号/主键查用户、修改密码 | 登录 / 修改密码模块 |
| `com.dao.StudentDao` + `impl` | 分页 + 模糊查询、详情、增删改查、班级列表 | 列表 / 增删改 / 查询模块 |

完整方法签名、参数约定、示例代码见 [`doc/DAO接口文档.md`](doc/DAO接口文档.md)。
**公共层禁止组员私自修改**；确需调整时先改接口文档并在群里同步。

## 📌 数据库说明

数据库名：`student_db`（字符集 utf8mb4）

两张核心数据表：

1. `t_student`：学生信息表（学号、姓名、性别、班级、专业、联系电话、邮箱）
2. `t_user`：用户表（账号、密码、角色、关联学号），`t_user.stu_no` 外键关联 `t_student.stu_no`（级联删除）

`t_user.role`：`0` = 管理员，`1` = 学生；管理员记录的 `stu_no` 为 `NULL`。

> 🔒 密码说明：课程项目为便于演示，测试账号密码按**明文**存储（均为 `123456`）。
> 真实项目必须改为 BCrypt / SHA-256 加盐散列，答辩时需说明该局限，详见 `doc/数据库设计说明.md` §6。

已内置测试数据（执行 `sql/student_db.sql` 即可写入）：

| 角色 | 账号 | 密码 | 说明 |
| ---- | ---- | ---- | ---- |
| 管理员 | `admin` | `123456` | 可增删改查全部学生 |
| 学生 | `2024010101` ~ `2024010125` | `123456` | 计科2401 |
| 学生 | `2024020101` ~ `2024020125` | `123456` | 软件2402 |
| 学生 | `2024030101` ~ `2024030125` | `123456` | 网络2403 |
| 学生 | `2024040101` ~ `2024040125` | `123456` | 数据2404 |

> ⚠️ 仓库内不存放明文数据库密码。
> 请复制 `db.properties.template` 到 `src/main/resources/db.properties`，自行填写本地 MySQL 账号密码；
> `db.properties` 已在 `.gitignore` 中，不会被提交。
> 读取方式：`DbUtil` 通过 `getResourceAsStream("db.properties")` 从 classpath 读取，禁止使用相对文件路径。

## 🧩 开发规范（摘要）

完整规范见 [`doc/开发规范.md`](doc/开发规范.md)，组员必须遵守的硬性约定：

1. Java 包结构统一：`com.entity / com.dao / com.util / com.servlet / com.filter`，禁止改根包。
2. 编码统一 UTF-8；所有 Servlet 使用 `jakarta.servlet.*`，禁止混用 `javax`。
3. SQL 只能写在 DAO 层，全部使用 `PreparedStatement`；Servlet、JSP 中不得出现 SQL。
4. 统一命名：Session key `loginUser`；请求参数 `stuNo`、`className`、`page`、`pageSize` 等（详见接口文档）。
5. JSP 使用 JSTL 3.0：`<%@ taglib prefix="c" uri="jakarta.tags.core" %>`（旧 URI `http://java.sun.com/jsp/jstl/core` 已废弃，全组统一新 URI）。
6. 提交前必须 `.\mvnw.cmd clean test` 编译通过；不提交 `db.properties`、`target/`、`.idea/`。

## 🧩 Git 协作规范（小组必须遵守）

1. `main` 为主稳定分支，**禁止直接提交代码到 main**
2. 每个人在自己的 feature 分支开发，写完提交后发起 PR（Pull Request）
3. 组长负责审核 PR，确认功能正常后合并入 main
4. 合并 main 之后，所有人第一时间 `git pull origin main` 更新本地代码
5. 提交注释格式：`【模块名】简要描述本次改动`
   > 示例：`【DAO层】新增分页查询与模糊查询实现，补充 JUnit 集成测试`

## 🚀 部署运行步骤

1. 克隆项目到本地

```bash
git clone https://github.com/FangHua09/student-manage-javaweb.git
```

2. 打开 MySQL，执行建库建表脚本（会创建 `student_db` 库、两张表并写入 100 条测试数据）

```powershell
# PowerShell 不支持 < 输入重定向，用 cmd /c 包一层
cmd /c "mysql -uroot -p --default-character-set=utf8mb4 < sql\student_db.sql"
```

```bat
:: CMD 中可直接执行
mysql -uroot -p --default-character-set=utf8mb4 < sql\student_db.sql
```

3. 复制 `db.properties.template` → `src/main/resources/db.properties`，修改为自己本地 MySQL 账号密码

4. 构建并运行测试（项目自带 Maven Wrapper，无需安装 Maven）

```powershell
.\mvnw.cmd clean test       # 编译 + JUnit 5 集成测试（数据库不可用时自动跳过）
.\mvnw.cmd clean package    # 打包
```

> CMD 窗口中直接写 `mvnw.cmd ...`（不需要 `.\` 前缀）；PowerShell 必须写 `.\mvnw.cmd`。

5. 把 `target/student_manage_javaweb.war` 复制到 Tomcat 11 的 `webapps/` 目录，启动 Tomcat；
   或在 IDEA 中配置 Tomcat 11 部署 `student_manage_javaweb:war exploded`（context 填 `/student_manage_javaweb`）

6. 浏览器访问 <http://localhost:8080/student_manage_javaweb/login.jsp>，
   使用 `admin / 123456` 或任意学号 / `123456` 登录

> 详细构建、部署、联调与常见问题见 [`doc/整合联调说明.md`](doc/整合联调说明.md)。
