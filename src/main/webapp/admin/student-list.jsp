<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:if test="${not empty sessionScope.loginUser and not sessionScope.loginUser.admin}">
    <c:redirect url="/student/index.jsp"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学生信息列表 - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <style>
        .list-card { max-width: 1180px; }
        .search-form { display: flex; flex-wrap: wrap; align-items: center; gap: 10px 12px; margin: 4px 0 18px; }
        .search-form label { color: var(--color-muted); font-size: 14px; }
        .search-input { width: 200px; }
        .search-select { width: 160px; }
        .search-add { margin-left: auto; }
        .table-wrap { overflow-x: auto; }
        .data-table { width: 100%; border-collapse: collapse; margin-bottom: 18px; font-size: 14px; }
        .data-table th, .data-table td { padding: 10px 12px; border: 1px solid var(--color-border); text-align: center; white-space: nowrap; }
        .data-table th { background: #f8fafc; color: var(--color-muted); font-weight: 600; }
        .data-table tbody tr:hover { background: #f8fafc; }
        .btn-sm { padding: 6px 10px; font-size: 13px; }
        .btn-secondary { background: #909399; border-color: #909399; }
        .btn-secondary:hover { background: #73767a; border-color: #73767a; }
        .btn-success { background: #67c23a; border-color: #67c23a; }
        .btn-success:hover { background: #529b2e; border-color: #529b2e; }
        .btn-danger { background: #f56c6c; border-color: #f56c6c; }
        .btn-danger:hover { background: #c45656; border-color: #c45656; }
        .pagination { display: flex; flex-wrap: wrap; justify-content: center; align-items: center; gap: 10px; margin-top: 18px; }
        .pagination a { padding: 6px 10px; color: var(--color-primary); text-decoration: none; border: 1px solid var(--color-border); border-radius: 4px; font-size: 14px; }
        .pagination a:hover { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }
        .page-info { color: var(--color-muted); font-size: 14px; }
        @media (max-width: 640px) {
            .search-input, .search-select { width: 100%; }
            .search-add { margin-left: 0; }
        }
    </style>
</head>
<body class="app-page">
<header class="topbar">
    <div class="topbar-inner">
        <a class="topbar-brand" href="${pageContext.request.contextPath}/admin/index.jsp">学生信息管理系统</a>
        <span class="topbar-user">管理员 · <c:out value="${sessionScope.loginUser.username}"/></span>
        <nav class="topbar-nav" aria-label="主导航">
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/index.jsp">首页</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/student/add">新增学生</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/password/update">修改密码</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page-main">
    <section class="card list-card">
        <div class="card-header">
            <h1 class="card-title">学生信息管理列表</h1>
            <p class="card-desc">支持按姓名、班级筛选，点击“详情”查看完整档案。</p>
        </div>

        <c:if test="${param.msg eq 'deleteSuccess'}">
            <div class="alert alert-success">删除成功！该学生对应的登录账号已同步删除。</div>
        </c:if>
        <c:if test="${param.msg eq 'deleteFail'}">
            <div class="alert alert-error">删除失败！该学生可能已被删除，或数据库暂时不可用，请刷新后重试。</div>
        </c:if>
        <c:if test="${param.msg eq 'paramError'}">
            <div class="alert alert-error">参数非法，请从列表页重新操作。</div>
        </c:if>

        <form class="search-form" action="${pageContext.request.contextPath}/student/list" method="get">
            <label for="name">姓名</label>
            <input class="form-input search-input" type="text" id="name" name="name"
                   value="<c:out value='${name}'/>" placeholder="输入姓名关键字">

            <label for="className">班级</label>
            <select class="form-input search-select" id="className" name="className">
                <option value="">全部班级</option>
                <c:forEach items="${classes}" var="cls">
                    <option value="<c:out value='${cls}'/>" ${cls eq className ? 'selected' : ''}><c:out value="${cls}"/></option>
                </c:forEach>
            </select>

            <input type="hidden" name="page" value="1">
            <input type="hidden" name="pageSize" value="${pageSize}">

            <button type="submit" class="btn btn-primary">查询</button>
            <a href="${pageContext.request.contextPath}/student/list" class="btn btn-secondary">重置</a>
            <a href="${pageContext.request.contextPath}/student/add" class="btn btn-success search-add">+ 新增学生</a>
        </form>

        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>学号</th>
                    <th>姓名</th>
                    <th>性别</th>
                    <th>班级</th>
                    <th>专业</th>
                    <th>联系电话</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty list}">
                        <tr><td colspan="7" style="padding: 30px;">暂无数据</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${list}" var="stu">
                            <c:url var="detailUrl" value="/student/detail">
                                <c:param name="stuNo" value="${stu.stuNo}"/>
                            </c:url>
                            <c:url var="updateUrl" value="/student/update">
                                <c:param name="stuNo" value="${stu.stuNo}"/>
                            </c:url>
                            <tr>
                                <td><c:out value="${stu.stuNo}"/></td>
                                <td><c:out value="${stu.name}"/></td>
                                <td><c:out value="${stu.gender}"/></td>
                                <td><c:out value="${stu.className}"/></td>
                                <td><c:out value="${stu.major}"/></td>
                                <td><c:out value="${stu.phone}"/></td>
                                <td>
                                    <a href="${detailUrl}" class="btn btn-primary btn-sm">详情</a>
                                    <a href="${updateUrl}" class="btn btn-primary btn-sm">修改</a>
                                    <form action="${pageContext.request.contextPath}/admin/deleteStudent" method="post"
                                          style="display:inline"
                                          onsubmit="return confirm('确认删除这条学生信息吗？删除会同步删除登录账号！')">
                                        <input type="hidden" name="stuNo" value="<c:out value='${stu.stuNo}'/>">
                                        <input type="hidden" name="name" value="<c:out value='${name}'/>">
                                        <input type="hidden" name="className" value="<c:out value='${className}'/>">
                                        <input type="hidden" name="page" value="${page}">
                                        <input type="hidden" name="pageSize" value="${pageSize}">
                                        <button type="submit" class="btn btn-danger btn-sm">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <c:choose>
                <c:when test="${totalPages lt 1}">
                    <span class="page-info">共 0 条数据</span>
                </c:when>
                <c:otherwise>
                    <span class="page-info">共 ${total} 条数据，第 ${page} / ${totalPages} 页</span>
                    <c:if test="${page gt 1}">
                        <a href="${pageContext.request.contextPath}/student/list?name=${encodedName}&className=${encodedClassName}&page=1&pageSize=${pageSize}">首页</a>
                        <a href="${pageContext.request.contextPath}/student/list?name=${encodedName}&className=${encodedClassName}&page=${page - 1}&pageSize=${pageSize}">上一页</a>
                    </c:if>
                    <c:if test="${page lt totalPages}">
                        <a href="${pageContext.request.contextPath}/student/list?name=${encodedName}&className=${encodedClassName}&page=${page + 1}&pageSize=${pageSize}">下一页</a>
                        <a href="${pageContext.request.contextPath}/student/list?name=${encodedName}&className=${encodedClassName}&page=${totalPages}&pageSize=${pageSize}">末页</a>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <p class="page-footer">学生信息管理系统 · 列表页</p>
</main>
</body>
</html>


