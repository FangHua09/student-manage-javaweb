<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    学生信息表单页（新增 / 修改共用）（朱景澈负责）
    契约：
      · 新增：GET /student/add          → mode=add，form 为空对象
      · 修改：GET /student/update?stuNo= → mode=edit，form 为数据库回显的学生
      · 提交：POST 到 /student/add 或 /student/update，参数 stuNo、name、gender、
              className、major、phone、email（见《DAO接口文档》§5.2）
      · request 属性：mode、form、classList、error、success
    说明：学号是主键且被 t_user 外键引用，修改时只读，只作为定位条件。
    未登录防御：会话中没有 loginUser 时重定向回登录页（Filter 之外的双重保护）。
--%>
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${mode eq 'edit' ? '修改学生信息' : '新增学生'} - 学生信息管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body class="app-page">
<header class="topbar">
    <div class="topbar-inner">
        <a class="topbar-brand" href="${pageContext.request.contextPath}/admin/index.jsp">学生信息管理系统</a>
        <span class="topbar-user">管理员 · <c:out value="${loginUser.username}"/></span>
        <nav class="topbar-nav">
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/index.jsp">返回首页</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/student/list">学生列表</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
        </nav>
    </div>
</header>

<main class="page-main">
    <section class="card form-card">
        <div class="card-header">
            <h1 class="card-title">${mode eq 'edit' ? '修改学生信息' : '新增学生'}</h1>
            <p class="card-desc">
                ${mode eq 'edit'
                        ? '学号为主键且被登录账号引用，不允许修改；修改完成后点击“保存修改”。'
                        : '带 * 的为必填项；学号将作为该学生的唯一标识，保存后不可修改。'}
            </p>
        </div>

        <%-- 服务端校验失败 / 数据库异常时的提示 --%>
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <%-- 新增或修改成功后的提示（PRG 模式，刷新不会重复提交） --%>
        <c:if test="${not empty success}">
            <div class="alert alert-success" role="status"><c:out value="${success}"/></div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/${mode eq 'edit' ? 'student/update' : 'student/add'}">
            <div class="form-group">
                <label for="stuNo">学号 <span aria-hidden="true" style="color:#dc2626;">*</span></label>
                <input class="form-input" type="text" id="stuNo" name="stuNo"
                       required maxlength="20" pattern="[0-9A-Za-z]{4,20}"
                       placeholder="例如 2024010101（4~20 位字母或数字）"
                       value="<c:out value='${form.stuNo}'/>"
                       ${mode eq 'edit' ? 'readonly' : ''}>
                <c:choose>
                    <c:when test="${mode eq 'edit'}">
                        <p class="hint">学号不可修改（主键、且被 t_user 外键引用）。</p>
                    </c:when>
                    <c:otherwise>
                        <p class="hint">学号保存后不可修改，请仔细核对。</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="form-group">
                <label for="name">姓名 <span aria-hidden="true" style="color:#dc2626;">*</span></label>
                <input class="form-input" type="text" id="name" name="name"
                       required maxlength="20" placeholder="例如 张三"
                       value="<c:out value='${form.name}'/>">
            </div>

            <div class="form-group">
                <label for="gender">性别 <span aria-hidden="true" style="color:#dc2626;">*</span></label>
                <select class="form-input" id="gender" name="gender" required>
                    <option value="男" ${empty form.gender or form.gender eq '男' ? 'selected' : ''}>男</option>
                    <option value="女" ${form.gender eq '女' ? 'selected' : ''}>女</option>
                </select>
            </div>

            <div class="form-group">
                <label for="className">班级</label>
                <input class="form-input" type="text" id="className" name="className"
                       maxlength="30" list="classOptions" placeholder="例如 计科2401（可从已有班级中选择）"
                       value="<c:out value='${form.className}'/>">
                <datalist id="classOptions">
                    <c:forEach items="${classList}" var="cls">
                        <option value="<c:out value='${cls}'/>"></option>
                    </c:forEach>
                </datalist>
                <p class="hint">班级可为空；输入时会提示数据库中已有的班级名称。</p>
            </div>

            <div class="form-group">
                <label for="major">专业</label>
                <input class="form-input" type="text" id="major" name="major"
                       maxlength="30" placeholder="例如 计算机科学与技术"
                       value="<c:out value='${form.major}'/>">
            </div>

            <div class="form-group">
                <label for="phone">联系电话</label>
                <input class="form-input" type="tel" id="phone" name="phone"
                       maxlength="15" pattern="[0-9][0-9\-]{5,14}"
                       placeholder="例如 13800138000 或 020-88886666"
                       value="<c:out value='${form.phone}'/>">
                <p class="hint">可选；填写时只能是 6~15 位数字，可用减号分隔区号。</p>
            </div>

            <div class="form-group">
                <label for="email">电子邮箱</label>
                <input class="form-input" type="email" id="email" name="email"
                       maxlength="50" placeholder="例如 student@example.com"
                       value="<c:out value='${form.email}'/>">
                <p class="hint">可选；填写时须符合邮箱格式。</p>
            </div>

            <div class="form-actions">
                <button class="btn btn-primary" type="submit">
                    ${mode eq 'edit' ? '保存修改' : '保存新增'}
                </button>
                <a class="link" href="${pageContext.request.contextPath}/admin/index.jsp">返回首页</a>
                <a class="link" href="${pageContext.request.contextPath}/student/list">返回学生列表</a>
            </div>
        </form>
    </section>

    <p class="page-footer">学生信息管理系统 · JavaWeb 课程大作业</p>
</main>
</body>
</html>
