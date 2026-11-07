<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生信息列表</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; padding: 20px; background-color: #f4f7f6; }
        .container { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .search-form { margin-bottom: 20px; display: flex; gap: 10px; align-items: center; }
        .search-form input, .search-form select { padding: 6px; border: 1px solid #ccc; border-radius: 4px; }
        .btn { padding: 7px 15px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 14px;}
        .btn:hover { background: #66b1ff; }
        .btn-danger { background: #f56c6c; }
        .btn-danger:hover { background: #f78989; }
        .btn-success { background: #67c23a; }
        
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; text-align: center; }
        th, td { border: 1px solid #ebeef5; padding: 12px; }
        th { background-color: #f5f7fa; color: #606266; font-weight: bold; }
        tr:hover { background-color: #f5f7fa; }
        
        .pagination { display: flex; justify-content: center; align-items: center; gap: 15px; margin-top: 20px; }
        .pagination a { color: #409eff; text-decoration: none; padding: 5px 10px; border: 1px solid #dcdfe6; border-radius: 4px;}
        .pagination a:hover { background: #409eff; color: white; }
        .pagination span { color: #606266; }
    </style>
</head>
<body>
<div class="container">
    <h2>学生信息管理列表</h2>

    <!-- 搜索表单 (GET 请求) -->
    <form class="search-form" action="${pageContext.request.contextPath}/student/list" method="get">
        <label>姓名：</label>
        <input type="text" name="name" value="<c:out value='${name}'/>" placeholder="输入姓名关键字">
        
        <label>班级：</label>
        <select name="className">
            <option value="">全部班级</option>
            <c:forEach items="${classes}" var="cls">
                <option value="${cls}" ${cls == className ? 'selected' : ''}>${cls}</option>
            </c:forEach>
        </select>
        
        <!-- 隐藏域：搜索时重置到第一页，每页条数保留 -->
        <input type="hidden" name="page" value="1">
        <input type="hidden" name="pageSize" value="${pageSize}">
        
        <button type="submit" class="btn">查询</button>
        重置
        + 新增学生
    </form>

    <!-- 数据表格 -->
    <table>
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
                        <tr>
                            <!-- 规范要求：输出用户数据需转义防 XSS -->
                            <td><c:out value="${stu.stuNo}"/></td>
                            <td><c:out value="${stu.name}"/></td>
                            <td><c:out value="${stu.gender}"/></td>
                            <td><c:out value="${stu.className}"/></td>
                            <td><c:out value="${stu.major}"/></td>
                            <td><c:out value="${stu.phone}"/></td>
                            <td>
    <!-- 直接连 Servlet -->
    <a href="${pageContext.request.contextPath}/student/detail?stuNo=${stu.stuNo}" class="btn">详情</a>
    
    <!-- 修改-->
    <a href="${pageContext.request.contextPath}/student/update?stuNo=${stu.stuNo}" class="btn">修改</a>
    
    <!-- 删除暂时用 # 占位 -->
<a href="#" class="btn btn-danger">删除</a>
</td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>

   <!-- 分页栏 -->
<!-- 分页栏 -->
    <div class="pagination">
        <span>共 ${total} 条数据，第 ${page} / ${totalPages} 页</span>
        
        <c:if test="${page > 1}">
            <!-- 翻页必须带上搜索条件和每页条数 -->
            <a href="${pageContext.request.contextPath}/student/list?name=${name}&className=${className}&page=1&pageSize=${pageSize}">首页</a>
            <a href="${pageContext.request.contextPath}/student/list?name=${name}&className=${className}&page=${page - 1}&pageSize=${pageSize}">上一页</a>
        </c:if>
        
        <c:if test="${page < totalPages}">
            <a href="${pageContext.request.contextPath}/student/list?name=${name}&className=${className}&page=${page + 1}&pageSize=${pageSize}">下一页</a>
            <a href="${pageContext.request.contextPath}/student/list?name=${name}&className=${className}&page=${totalPages}&pageSize=${pageSize}">末页</a>
        </c:if>
    </div>
</div>
</body>
</html>