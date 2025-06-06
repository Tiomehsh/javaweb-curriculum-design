<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* 首页特定样式 */
        /* 使用全局样式的 main-container，无需额外设置 */
        
        /* 系统标题区域 */
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
        
        
        /* 预约按钮区域 */
        .appointment-buttons {
            display: flex;
            flex-direction: column;
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .appointment-btn {
            display: block;
            padding: 20px;
            border-radius: 6px;
            text-decoration: none;
            text-align: left;
            border: none;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        
        .appointment-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        
        .btn-content {
            display: flex;
            align-items: center;
        }
        
        .btn-icon {
            font-size: 1.8em;
            margin-right: 15px;
            flex-shrink: 0;
        }
        
        .btn-text {
            flex: 1;
        }
        
        .btn-title {
            font-size: 1.2em;
            font-weight: 600;
            margin-bottom: 4px;
            display: block;
        }
        
        .btn-description {
            font-size: 0.9em;
            opacity: 0.9;
            font-weight: normal;
            line-height: 1.3;
        }
        
        .public-btn {
            background: #1e88e5;
            color: white;
            border: 1px solid #1e88e5;
        }
        
        .public-btn:hover {
            background: #1565c0;
            color: white;
            text-decoration: none;
        }
        
        .official-btn {
            background: #43a047;
            color: white;
            border: 1px solid #43a047;
        }
        
        .official-btn:hover {
            background: #2e7d32;
            color: white;
            text-decoration: none;
        }
        
        
        /* 查询链接 */
        .query-link {
            text-align: center;
            margin-top: 25px;
            padding-top: 25px;
            border-top: 1px solid #f0f0f0;
        }
        
        .query-link a {
            color: #1e88e5;
            text-decoration: none;
            font-size: 1em;
            padding: 10px 20px;
            border: 1px solid #1e88e5;
            border-radius: 6px;
            display: inline-block;
            background: white;
            font-weight: 500;
        }
        
        .query-link a:hover {
            background: #1e88e5;
            color: white;
            text-decoration: none;
        }
        
        /* 页脚信息 */
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
        
        /* 响应式设计 */
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
            
            .appointment-btn {
                padding: 18px;
                font-size: 1em;
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
            <a href="${pageContext.request.contextPath}/admin/login.jsp" class="card-header-link">🔧 管理员入口</a>
        </div>
        
        <!-- 系统标题区域 -->
        <div class="system-header">
            <div class="university-logo">
                <img src="${pageContext.request.contextPath}/images/浙江工业大学-logo.svg" alt="浙江工业大学" onerror="this.style.display='none'; this.parentElement.innerHTML='<div style=&quot;color: white; font-size: 1.5em; font-weight: bold;&quot;>ZJUT</div>';">
            </div>
            <h1 class="system-title">校园通行码预约管理系统</h1>
            <p class="system-subtitle">浙江工业大学</p>
        </div>
        
        <!-- 预约按钮区域 -->
        <div class="appointment-buttons">
            <a href="public_appointment.jsp" class="appointment-btn public-btn">
                <div class="btn-content">
                    <span class="btn-icon">👥</span>
                    <div class="btn-text">
                        <span class="btn-title">社会公众预约</span>
                        <div class="btn-description">面向社会公众的校园参观预约服务</div>
                    </div>
                </div>
            </a>
            
            <a href="official_appointment.jsp" class="appointment-btn official-btn">
                <div class="btn-content">
                    <span class="btn-icon">🏢</span>
                    <div class="btn-text">
                        <span class="btn-title">公务预约</span>
                        <div class="btn-description">政府机关、企事业单位公务来访预约</div>
                    </div>
                </div>
            </a>
        </div>
        
        <!-- 查询链接 -->
        <div class="query-link">
            <a href="appointment_query.jsp">
                🔍 我的预约查询
            </a>
        </div>
        
        <!-- 页脚信息 -->
        <div class="footer-info">
            <div class="service-time">服务时间：周一至周五 8:00-17:30</div>
            <div class="contact">咨询热线：400-123-4567</div>
            <div class="copyright">&copy; 2023 浙江工业大学 版权所有</div>
        </div>
    </div>
</body>
</html>