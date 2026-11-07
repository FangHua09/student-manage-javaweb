<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生详细信息</title>
    <style>
        body { font-family: "Microsoft YaHei", sans-serif; padding: 20px; background-color: #f4f7f6; }
        .container { max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h2 { text-align: center; color: #333; margin-bottom: 30px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
        .info-row { display: flex; margin-bottom: 15px; border-bottom: 1px dashed #eee; padding-bottom: 10px; }
        .label { width: 120px; font-weight: bold; color: #606266; }
        .value { flex: 1; color: #333; }
        .btn-back { display: block; width: 100px; margin: 30px auto 0; text-align: center; padding: 10px; background: #409eff; color: white; text-decoration: none; border-radius: 4px; }
        .btn-back:hover { background: #66b1ff; }
    </style>
</head>
<body>
<div class="container">
    <h2>学生详细信息</h2>

    <div class="info-row">
        <div class="label">学号：</div>
        <div class="value"><c:out value="${student.stuNo}"/></div>
    </div>
    <div class="info-row">
        <div class="label">姓名：</div>
        <div class="value"><c:out value="${student.name}"/></div>
    </div>
    <div class="info-row">
        <div class="label">性别：</div>
        <div class="value"><c:out value="${student.gender}"/></div>
    </div>
    <div class="info-row">
        <div class="label">班级：</div>
        <div class="value"><c:out value="${student.className}"/></div>
    </div>
    <div class="info-row">
        <div class="label">专业：</div>
        <div class="value"><c:out value="${student.major}"/></div>
    </div>
    <div class="info-row">
        <div class="label">联系电话：</div>
        <div class="value"><c:out value="${student.phone}"/></div>
    </div>
    <div class="info-row">
        <div class="label">电子邮箱：</div>
        <div class="value"><c:out value="${student.email}"/></div>
    </div>

    返回列表
</div>
</body>
</html>