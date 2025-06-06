<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>控制面板 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        /* 详情表格样式已移至全局CSS文件 */

        /* 统计卡片样式 */
        .stats-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 25px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.12);
        }
        
        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
            background: linear-gradient(180deg, #34495e, #2c3e50);
        }
        
        .stat-card.green::before {
            background: linear-gradient(180deg, #34495e, #2c3e50);
        }
        
        .stat-card.orange::before {
            background: linear-gradient(180deg, #34495e, #2c3e50);
        }
        
        .stat-card.purple::before {
            background: linear-gradient(180deg, #34495e, #2c3e50);
        }
        
        .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin-bottom: 15px;
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .stat-card.green .stat-icon {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .stat-card.orange .stat-icon {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .stat-card.purple .stat-icon {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .stat-label {
            color: #7f8c8d;
            font-size: 14px;
            margin-bottom: 8px;
        }
        
        .stat-value {
            font-size: 36px;
            font-weight: 700;
            color: #2c3e50;
            line-height: 1;
        }
        
        .stat-trend {
            margin-top: 10px;
            font-size: 12px;
            color: #95a5a6;
        }
        
        /* 快捷操作按钮 - 卡片样式已移至 style.css */
        .quick-actions {
            margin-bottom: 30px;
        }
        
        .quick-actions h3 {
            margin: 0 0 15px 0;
            color: #2c3e50;
            font-size: 18px;
        }
        
        .action-buttons {
            gap: 15px;
        }
        
        .action-btn {
            padding: 10px 20px;
            border: 2px solid #34495e;
            background: white;
            color: #34495e;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .action-btn:hover {
            background: #34495e;
            color: white;
            transform: translateY(-2px);
        }
        
        /* 最近预约表格样式 - 基础表格样式已移至 style.css */
        .recent-appointments h3 {
            margin: 0 0 20px 0;
            color: #2c3e50;
            font-size: 18px;
        }
        
        .type-public {
            background-color: #e3f2fd;
            color: #1976d2;
        }
        
        .type-official {
            background-color: #e8f5e9;
            color: #388e3c;
        }
        
        .view-btn {
            padding: 6px 16px;
            background-color: #34495e;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: background-color 0.3s ease;
        }
        
        .view-btn:hover {
            background-color: #2c3e50;
        }
        
        /* 响应式设计 */
        @media (max-width: 768px) {
            .stats-container {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- 侧边栏 -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <div class="sidebar-logo">
                    <img src="${pageContext.request.contextPath}/images/浙江工业大学-logo.svg" alt="Logo">
                    <h2>校园通行码系统</h2>
                </div>
            </div>
            
            <nav class="sidebar-nav">
                <div class="nav-section">
                    <div class="nav-section-title">主菜单</div>
                    <a href="dashboard.jsp" class="nav-item active">
                        📊 控制面板
                    </a>
                </div>
                
                <div class="nav-section">
                    <div class="nav-section-title">预约管理</div>
                    <c:if test="${admin.role == 'SYSTEM_ADMIN' || admin.role == 'RECEPTION_ADMIN'}">
                    <a href="public_appointments.jsp" class="nav-item">
                        👥 公众预约管理
                    </a>
                    </c:if>
                    <c:if test="${admin.role == 'SYSTEM_ADMIN' || admin.role == 'RECEPTION_ADMIN' || admin.role == 'DEPARTMENT_ADMIN'}">
                    <a href="official_appointments.jsp" class="nav-item">
                        🏢 公务预约管理
                    </a>
                    </c:if>
                </div>
                
                <c:if test="${admin.role == 'SYSTEM_ADMIN'}">
                <div class="nav-section">
                    <div class="nav-section-title">系统管理</div>
                    <a href="departments.jsp" class="nav-item">
                        🏛️ 部门管理
                    </a>
                    <a href="admins.jsp" class="nav-item">
                        👤 管理员管理
                    </a>
                </div>
                </c:if>
                
                <div class="nav-section">
                    <div class="nav-section-title">其他</div>
                    <c:if test="${admin.role == 'SYSTEM_ADMIN' || admin.role == 'AUDIT_ADMIN'}">
                    <a href="logs.jsp" class="nav-item">
                        📋 系统日志
                    </a>
                    </c:if>
                    <a href="profile.jsp" class="nav-item">
                        ⚙️ 个人设置
                    </a>
                </div>
            </nav>
        </aside>
        
        <!-- 主内容区域 -->
        <div class="main-content">
            <!-- 顶部导航栏 -->
            <nav class="top-navbar">
                <div class="header-left">
                    <button class="sidebar-toggle" onclick="toggleSidebar()" title="收起/展开侧边栏">
                        ☰
                    </button>
                    <h1 class="page-title">控制面板</h1>
                </div>
                <div class="user-menu">
                    <div class="user-info">
                        <div class="user-avatar">${admin.realName.substring(0,1)}</div>
                        <div>
                            <div style="font-weight: 500;">${admin.realName}</div>
                            <div style="font-size: 12px; color: #7f8c8d;">
                                <c:choose>
                                    <c:when test="${admin.role == 'SYSTEM_ADMIN'}">系统管理员</c:when>
                                    <c:when test="${admin.role == 'RECEPTION_ADMIN'}">接待管理员</c:when>
                                    <c:when test="${admin.role == 'DEPARTMENT_ADMIN'}">部门管理员</c:when>
                                    <c:when test="${admin.role == 'AUDIT_ADMIN'}">审计管理员</c:when>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                    <button class="logout-btn" onclick="logout()">退出登录</button>
                </div>
            </nav>
            
            <!-- 内容区域 -->
            <div class="content">
                <!-- 统计卡片 -->
                <div class="stats-container">
                    <div class="stat-card">
                        <div class="stat-icon">📅</div>
                        <div class="stat-label">今日公众预约</div>
                        <div class="stat-value" id="public-count">0</div>
                        <div class="stat-trend">较昨日 +12%</div>
                    </div>
                    
                    <div class="stat-card green">
                        <div class="stat-icon">🏢</div>
                        <div class="stat-label">今日公务预约</div>
                        <div class="stat-value" id="official-count">0</div>
                        <div class="stat-trend">较昨日 +5%</div>
                    </div>
                    
                    <div class="stat-card orange">
                        <div class="stat-icon">⏳</div>
                        <div class="stat-label">待处理预约</div>
                        <div class="stat-value" id="pending-count">0</div>
                        <div class="stat-trend">需要及时处理</div>
                    </div>
                    
                    <div class="stat-card purple">
                        <div class="stat-icon">📊</div>
                        <div class="stat-label">本月总预约</div>
                        <div class="stat-value" id="monthly-count">0</div>
                        <div class="stat-trend">月度统计</div>
                    </div>
                </div>
                
                <!-- 快捷操作 -->
                <div class="quick-actions">
                    <h3>快捷操作</h3>
                    <div class="action-buttons">
                        <c:if test="${admin.role == 'SYSTEM_ADMIN' || admin.role == 'RECEPTION_ADMIN'}">
                        <button class="action-btn" onclick="window.location.href='public_appointments.jsp'">
                            <span>👥</span> 处理公众预约
                        </button>
                        </c:if>
                        <c:if test="${admin.role == 'SYSTEM_ADMIN' || admin.role == 'RECEPTION_ADMIN' || admin.role == 'DEPARTMENT_ADMIN'}">
                        <button class="action-btn" onclick="window.location.href='official_appointments.jsp'">
                            <span>🏢</span> 处理公务预约
                        </button>
                        </c:if>
                        <c:if test="${admin.role == 'SYSTEM_ADMIN'}">
                        <button class="action-btn" onclick="window.location.href='admins.jsp'">
                            <span>+</span> 添加管理员
                        </button>
                        </c:if>
                        <button class="action-btn" onclick="window.location.href='profile.jsp'">
                            <span>👤</span> 修改密码
                        </button>
                    </div>
                </div>
                
                <!-- 最近预约列表 -->
                <div class="recent-appointments">
                    <h3>最近预约记录</h3>
                    <table class="appointments-table">
                        <thead>
                            <tr>
                                <th>预约编号</th>
                                <th>预约类型</th>
                                <th>预约人</th>
                                <th>预约时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="recent-appointments-data">
                            <tr>
                                <td colspan="6" style="text-align: center; padding: 40px;">
                                    加载中...
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 预约详情弹窗 -->
    <div id="appointment-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>预约详情</h3>
                <span class="close" onclick="closeModal('appointment-modal')">&times;</span>
            </div>
            <div class="modal-body" id="appointment-details">
                <!-- 预约详情将通过JavaScript动态加载 -->
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="closeModal('appointment-modal')">关闭</button>
            </div>
        </div>
    </div>
    
    <footer>
        <div class="container">
            &copy; 2023 高校预约接待系统 - 版权所有
        </div>
    </footer>

    <script src="${pageContext.request.contextPath}/js/session-manager.js"></script>
    <script src="${pageContext.request.contextPath}/js/modalUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>
