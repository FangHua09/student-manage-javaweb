<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生详细信息 - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="app-page">
<header class="topbar">
    <div class="topbar-inner">
        <a class="topbar-brand"
           href="${pageContext.request.contextPath}${sessionScope.loginUser.admin ? '/admin/index.jsp' : '/student/index.jsp'}">学生信息管理系统</a>
        <span class="topbar-user">
            <c:out value="${sessionScope.loginUser.roleName}"/> · <c:out value="${sessionScope.loginUser.username}"/>
        </span>
        <nav class="topbar-nav" aria-label="主导航">
            <c:if test="${sessionScope.loginUser.admin}">
                <a class="nav-link" href="${pageContext.request.contextPath}/student/list">学生列表</a>
            </c:if>
            <a class="nav-link" href="${pageContext.request.contextPath}/password/update">修改密码</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page-main">
    <section class="card form-card">
        <div class="card-header">
            <h1 class="card-title">学生详细信息</h1>
            <p class="card-desc">
                <c:choose>
                    <c:when test="${sessionScope.loginUser.admin}">管理员可查看所有学生的完整信息。</c:when>
                    <c:otherwise>这里显示您本人的档案信息。</c:otherwise>
                </c:choose>
            </p>
        </div>

        <c:if test="${param.denied eq '1'}">
            <div class="alert alert-error">您只能查看本人的信息，已为您跳转到本人详情页。</div>
        </c:if>
        <c:if test="${not empty errorMsg}">
            <div class="alert alert-error"><c:out value="${errorMsg}"/></div>
        </c:if>

        <c:choose>
            <c:when test="${empty student}">
                <p class="card-desc">没有可显示的学生信息，请返回后重新选择。</p>
            </c:when>
            <c:otherwise>
                <table class="info-table">
                    <tr>
                        <th>学号</th>
                        <td><c:out value="${student.stuNo}"/></td>
                    </tr>
                    <tr>
                        <th>姓名</th>
                        <td><c:out value="${student.name}"/></td>
                    </tr>
                    <tr>
                        <th>性别</th>
                        <td><c:out value="${student.gender}"/></td>
                    </tr>
                    <tr>
                        <th>班级</th>
                        <td><c:out value="${student.className}"/></td>
                    </tr>
                    <tr>
                        <th>专业</th>
                        <td><c:out value="${student.major}"/></td>
                    </tr>
                    <tr>
                        <th>联系电话</th>
                        <td><c:out value="${student.phone}"/></td>
                    </tr>
                    <tr>
                        <th>电子邮箱</th>
                        <td><c:out value="${student.email}"/></td>
                    </tr>
                </table>
            </c:otherwise>
        </c:choose>

        <div class="form-actions">
            <c:choose>
                <c:when test="${sessionScope.loginUser.admin}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/student/list">返回学生列表</a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/student/index.jsp">返回学生首页</a>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <p class="page-footer">学生信息管理系统 · 详情页</p>
</main>
</body>
</html>