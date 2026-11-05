# 学生信息管理系统（JavaWeb课程大作业）
> 技术栈：Servlet + JSP + JDBC + MySQL + Tomcat
> 课程：Web技术与应用（2026秋）
> 小组：242班 5人小组

## ✅ 项目简介
本系统是简易B/S架构学生信息管理平台，为课程综合实训项目。
实现**管理员、普通学生双角色权限**，基于原生JDBC操作数据库，完成学生信息增删改查、条件搜索、登录权限控制，不使用SSM、SpringBoot等高级框架，贴合本学期课程考点。

### 功能清单
1. 用户登录：账号密码校验，Session保存登录状态
2. 权限控制：Filter过滤器拦截未登录访问
3. 管理员：学生信息新增、修改、删除、模糊查询
4. 普通学生：仅查看本人信息、修改个人密码
5. 前端：HTML+CSS+原生JS表单校验

## 👥 小组成员与分工
| 角色 | 姓名 | 负责模块 | 分支名称 |
| ---- | ---- | -------- | -------- |
| 组长 | 杨亮 | 项目框架搭建、JDBC工具类、DAO层、项目整合调试、文档汇总 | feature/framework-jdbc |
| 组员 | 易思韵 | 登录模块、Session会话、Filter权限拦截 | feature/login-filter |
| 组员 | 朱景澈 | 学生信息新增、修改模块（Servlet+JSP页面） | feature/student-add-update |
| 组员 | 王竣熙 | 学生列表展示、模糊搜索、删除功能 | feature/student-list-delete-search |
| 组员 | 贺尧 | 页面美化、JS表单校验、系统测试、需求分析/报告文档 | feature/ui-validate-doc |

## 🛠️ 开发环境
- JDK：1.8
- 服务器：Tomcat 8.5 / 9.0
- 数据库：MySQL 8.0
- IDE：IDEA / Eclipse
- 版本控制：Git + GitHub

## 📂 项目结构
student-manage-javaweb
├── src
│   ├── com.entity        // JavaBean 实体类（User、Student）
│   ├── com.dao           // DAO 数据库访问层
│   ├── com.util          // JDBC 工具类
│   ├── com.servlet       // Servlet 控制器
│   └── com.filter        // 登录过滤器 Filter
├── webapp
│   ├── login.jsp         // 登录页面
│   ├── index.jsp         // 主页
│   ├── student           // 学生相关页面
│   ├── admin             // 管理员页面
│   └── css/js            // 样式、前端校验脚本
├── sql
│   └── student_db.sql    // 数据库建表脚本
├── doc
│   └── 需求分析报告.docx
├── .gitignore
└── README.md

## 📌 数据库说明
两张核心数据表：
1. `t_user`：用户表（存储管理员、学生账号、密码、角色）
2. `t_student`：学生信息表（学号、姓名、性别、班级、联系电话等）

> ⚠️ 注意：仓库内不存放明文数据库密码配置文件
> 请复制 `db.properties.template` 并重命名为 `db.properties`，自行填写本地MySQL账号密码

## 🧩 Git协作规范（小组必须遵守）
1. `main` 为主稳定分支，**禁止直接提交代码到main**
2. 每个人在自己的feature分支开发，写完提交后发起PR（Pull Request）
3. 组长负责审核PR，确认功能正常后合并入main
4. 合并main之后，所有人第一时间 `git pull origin main` 更新本地代码
5. 提交注释格式：`【模块名】简要描述本次改动`
> 示例：`【JDBC工具类】封装连接与资源关闭，捕获SQL异常`

## 🚀 部署运行步骤
1. 克隆项目到本地
```bash
git clone https://github.com/FangHua09/student-manage-javaweb.git
'''
1. 打开 MySQL，执行 `sql/student_db.sql` 导入数据表
2. 复制 `db.properties.template` → `db.properties`，修改为你本地 MySQL 账号密码
3. IDEA/Eclipse 导入项目，配置 Tomcat 服务器
4. 部署项目，启动 Tomcat，访问登录页面

