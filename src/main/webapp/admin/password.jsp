<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    修改密码页（管理员）（易思韵负责）
    契约：POST /password/update；参数 oldPassword / newPassword / confirmPassword；
          request 属性 error、success；失败转发回本页，成功 PRG 到 /password/update?success=1。
    与 student/password.jsp 结构一致，仅“返回首页”链接不同。
    未登录防御：会话中没有 loginUser 时重定向回登录页。
--%>
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>修改密码 - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="app-page">
<header class="topbar">
    <div class="topbar-inner">
        <a class="topbar-brand" href="${pageContext.request.contextPath}/admin/index.jsp">学生信息管理系统</a>
        <span class="topbar-user">管理员 · <c:out value="${loginUser.username}"/></span>
        <nav class="topbar-nav">
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/index.jsp">返回首页</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page-main">
    <section class="card form-card">
        <div class="card-header">
            <h1 class="card-title">修改密码</h1>
            <p class="card-desc">为保障账号安全，请定期更换密码。</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <c:choose>
            <c:when test="${not empty success && success ne '1'}">
                <div class="alert alert-success" role="status"><c:out value="${success}"/></div>
            </c:when>
            <c:when test="${not empty success || param.success == '1'}">
                <div class="alert alert-success" role="status">密码修改成功，请牢记新密码。</div>
            </c:when>
        </c:choose>

        <form method="post" action="${pageContext.request.contextPath}/password/update">
            <div class="form-group">
                <label for="oldPassword">当前密码</label>
                <input class="form-input" type="password" id="oldPassword" name="oldPassword"
                       required maxlength="64" autocomplete="current-password"
                       placeholder="请输入当前密码">
            </div>
            <div class="form-group">
                <label for="newPassword">新密码</label>
                <input class="form-input" type="password" id="newPassword" name="newPassword"
                       required minlength="6" maxlength="20" autocomplete="new-password"
                       placeholder="请输入新密码">
            </div>
            <div class="form-group">
                <label for="confirmPassword">确认新密码</label>
                <input class="form-input" type="password" id="confirmPassword" name="confirmPassword"
                       required minlength="6" maxlength="20" autocomplete="new-password"
                       placeholder="请再次输入新密码">
            </div>
            <p class="hint">新密码 6~20 位，不能与旧密码相同。</p>

            <div class="form-actions">
                <button class="btn btn-primary" type="submit">保存修改</button>
            </div>
        </form>
    </section>

    <p class="page-footer">学生信息管理系统 · JavaWeb 课程大作业</p>
</main>
</body>
</html>
