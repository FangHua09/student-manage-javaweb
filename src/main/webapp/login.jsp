<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    登录页（易思韵负责）
    契约：POST /login，参数 username / password；request 属性 error、username；
          退出登录后由 /logout 重定向到本页 ?logout=1。
    已登录用户直接跳转到对应角色首页，避免重复看到登录页。
--%>
<c:if test="${not empty sessionScope.loginUser}">
    <c:redirect url="${sessionScope.loginUser.admin ? '/admin/index.jsp' : '/student/index.jsp'}"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="login-page">
<main class="login-card">
    <header class="login-header">
        <h1 class="brand">学生信息管理系统</h1>
        <p class="subtitle">账号登录</p>
    </header>

    <c:if test="${param.logout == '1'}">
        <div class="alert alert-success" role="status">已安全退出登录。</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
    </c:if>

    <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
        <div class="form-group">
            <label for="username">账号</label>
            <input class="form-input" type="text" id="username" name="username"
                   value="<c:out value='${username}'/>" required maxlength="30"
                   autocomplete="username" placeholder="请输入登录账号">
        </div>
        <div class="form-group">
            <label for="password">密码</label>
            <input class="form-input" type="password" id="password" name="password"
                   required maxlength="64" autocomplete="current-password"
                   placeholder="请输入登录密码">
        </div>
        <button class="btn btn-primary btn-block" type="submit">登 录</button>
    </form>

    <footer class="login-footer">
        <p class="demo-tip">演示账号：管理员 admin / 123456；学生 2024010101 / 123456</p>
    </footer>
</main>
</body>
</html>
