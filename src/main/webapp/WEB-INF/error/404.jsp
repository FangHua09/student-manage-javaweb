<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面不存在 - 学生信息管理系统</title>
    <style>
        body { margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center;
               font-family: "Microsoft YaHei", Arial, sans-serif; background: #f5f7fa; color: #333; }
        .card { padding: 48px 56px; background: #fff; border-radius: 12px; text-align: center;
                box-shadow: 0 8px 24px rgba(0, 0, 0, .08); }
        .code { font-size: 72px; font-weight: 700; color: #4a90d9; line-height: 1; margin: 0; }
        .tip { margin: 16px 0 28px; color: #666; }
        a { display: inline-block; padding: 10px 24px; background: #4a90d9; color: #fff;
            border-radius: 6px; text-decoration: none; }
        a:hover { background: #357abd; }
    </style>
</head>
<body>
<div class="card">
    <p class="code">404</p>
    <p class="tip">你访问的页面不存在，可能地址输入有误或页面已被移除。</p>
    <a href="<%= request.getContextPath() %>/login.jsp">返回登录页</a>
</div>
</body>
</html>
