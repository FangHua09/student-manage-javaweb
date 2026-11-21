<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    学生首页
    契约：顶部栏为“学生 · ${loginUser.username}”，导航修改密码 /password/update、退出登录 /logout；
          账号信息表展示登录账号、角色、关联学号；“我的信息”链接指向本人详情页。
    防御：未登录重定向登录页；管理员误入时重定向管理员首页（Filter 之外的双重保护）。
--%>
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:if test="${sessionScope.loginUser.admin}">
    <c:redirect url="/admin/index.jsp"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生首页 - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="app-page">
<header class="topbar">
    <div class="topbar-inner">
        <a class="topbar-brand" href="${pageContext.request.contextPath}/student/index.jsp">学生信息管理系统</a>
        <span class="topbar-user">学生 · <c:out value="${loginUser.username}"/></span>
        <nav class="topbar-nav">
            <a class="nav-link" href="${pageContext.request.contextPath}/password/update">修改密码</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page-main">
    <section class="card">
        <div class="card-header">
            <h1 class="card-title">学生首页</h1>
            <p class="welcome">欢迎回来，<c:out value="${loginUser.username}"/>！</p>
        </div>

        <table class="info-table">
            <caption class="visually-hidden">当前登录账号信息</caption>
            <tr>
                <th scope="row">登录账号</th>
                <td><c:out value="${loginUser.username}"/></td>
            </tr>
            <tr>
                <th scope="row">角色</th>
                <td><c:out value="${loginUser.roleName}"/></td>
            </tr>
            <tr>
                <th scope="row">关联学号</th>
                <td><c:out value="${loginUser.stuNo}"/></td>
            </tr>
        </table>

        <h2 class="section-title">功能入口</h2>
        <p class="module-note">您可以查看本人的档案信息，也可以随时修改登录密码。</p>
        <div class="module-list">
            <c:url var="myDetailUrl" value="/student/detail">
                <c:param name="stuNo" value="${loginUser.stuNo}"/>
            </c:url>
            <a class="link" href="${myDetailUrl}">查看我的信息</a>
        </div>
    </section>

    <p class="page-footer">学生信息管理系统 · JavaWeb 课程大作业</p>
</main>
</body>
</html>
