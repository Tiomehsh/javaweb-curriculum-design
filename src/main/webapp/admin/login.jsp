<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员登录 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* 登录页特定样式 - 继承首页样式 */
        /* 使用全局样式的 main-container，无需额外设置 */
        
        /* 系统标题区域 - 与首页保持一致 */
        .system-header {
            text-align: center;
            margin-bottom: 40px;
            padding: 20px 0;
            border-bottom: 2px solid #f0f0f0;
        }
        
        .university-logo {
            width: 100px;
            height: 100px;
            margin: 0 auto 20px;
            background: white;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 2px solid #f0f0f0;
        }
        
        .university-logo img {
            width: 80px;
            height: 80px;
            object-fit: contain;
        }
        
        .system-title {
            font-size: 1.6em;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 8px;
            line-height: 1.3;
        }
        
        .system-subtitle {
            color: #1e88e5;
            font-size: 1em;
            font-weight: 500;
            margin-bottom: 0;
        }
        
        /* 错误提示 */
        .error-message {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
            padding: 12px 15px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 0.9em;
            text-align: center;
        }
        
        /* 表单样式 */
        .login-form .form-group {
            margin-bottom: 20px;
        }
        
        .login-form label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #495057;
            font-size: 0.95em;
        }
        
        .login-form .form-control {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 6px;
            font-size: 1em;
            transition: all 0.2s ease;
            background: white;
        }
        
        .login-form .form-control:focus {
            outline: none;
            border-color: #1e88e5;
            box-shadow: 0 0 0 3px rgba(30, 136, 229, 0.1);
        }
        
        /* 记住我复选框 */
        .remember-me {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
        }
        
        .remember-me input[type="checkbox"] {
            width: 18px;
            height: 18px;
            margin-right: 8px;
            cursor: pointer;
        }
        
        .remember-me label {
            margin: 0;
            font-weight: normal;
            color: #666;
            cursor: pointer;
            user-select: none;
        }
        
        /* 登录按钮 */
        .login-btn {
            width: 100%;
            padding: 14px 20px;
            background: #1e88e5;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 1.05em;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        
        .login-btn:hover {
            background: #1976d2;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);
        }
        
        .login-btn:active {
            transform: translateY(0);
        }
        
        .login-btn:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }
        
        /* 页脚信息 - 使用首页相同的样式 */
        .footer-info {
            text-align: center;
            color: #666;
            font-size: 0.85em;
            line-height: 1.5;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #f0f0f0;
        }
        
        .footer-info .service-time {
            margin-bottom: 8px;
            color: #2c3e50;
            font-weight: 500;
        }
        
        .footer-info .contact {
            margin-bottom: 10px;
            color: #1e88e5;
        }
        
        .footer-info .copyright {
            font-size: 0.8em;
            color: #999;
        }
        
        /* 响应式 */
        @media (max-width: 768px) {
            .card-top-links {
                position: static;
                text-align: right;
                margin-bottom: 15px;
            }
        }
        
        @media (max-width: 480px) {
            .main-container {
                margin: 20px;
                padding: 20px;
            }
            
            .system-header {
                padding: 15px 0;
                margin-bottom: 25px;
            }
            
            .system-title {
                font-size: 1.4em;
            }
            
            .university-logo {
                width: 80px;
                height: 80px;
            }
            
            .university-logo img {
                width: 60px;
                height: 60px;
            }
            
            .card-header-link {
                font-size: 0.85em;
                padding: 5px 10px;
            }
        }
    </style>
</head>
<body>
    <div class="main-container">
        <!-- 卡片顶部链接 -->
        <div class="card-top-links">
            <a href="${pageContext.request.contextPath}/public/index.jsp" class="card-header-link">← 返回首页</a>
        </div>
        
        <!-- 系统标题区域 -->
        <div class="system-header">
            <div class="university-logo">
                <img src="../images/浙江工业大学-logo.svg" alt="浙江工业大学" onerror="this.style.display='none'; this.parentElement.innerHTML='<div style=&quot;color: white; font-size: 1.5em; font-weight: bold;&quot;>ZJUT</div>';">
            </div>
            <h1 class="system-title">校园通行码预约管理系统</h1>
            <p class="system-subtitle">管理员登录</p>
        </div>
        
        <% if (request.getParameter("error") != null) { %>
            <div class="error-message">
                用户名或密码错误，请重新登录
            </div>
        <% } %>
        
        <form id="loginForm" action="../api/admin/login" method="post" class="login-form">
            <div class="form-group">
                <label for="loginName">用户名</label>
                <input type="text"
                       id="loginName"
                       name="loginName"
                       class="form-control"
                       placeholder="请输入用户名"
                       autocomplete="username"
                       required>
            </div>
            
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password"
                       id="password"
                       name="password"
                       class="form-control"
                       placeholder="请输入密码"
                       autocomplete="current-password"
                       required>
            </div>
            
            <div class="remember-me">
                <input type="checkbox" id="rememberMe" name="rememberMe">
                <label for="rememberMe">记住用户名</label>
            </div>
            
            <button type="button" class="login-btn" onclick="submitLogin()">
                登 录
            </button>
        </form>
        
        <!-- 页脚信息 -->
        <div class="footer-info">
            <div class="service-time">服务时间：周一至周五 8:00-17:30</div>
            <div class="contact">技术支持：信息化办公室</div>
            <div class="copyright">&copy; 2024 浙江工业大学 版权所有</div>
        </div>
    </div>

    <script src="../js/admin-login.js"></script>
</body>
</html>