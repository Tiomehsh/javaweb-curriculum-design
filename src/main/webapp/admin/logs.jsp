<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>系统日志 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        
        /* 日志类型标签样式 */
        .log-type {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        
        .log-type-login {
            background-color: #e3f2fd;
            color: #0d47a1;
        }
        
        .log-type-logout {
            background-color: #f3e5f5;
            color: #4a148c;
        }
        
        .log-type-add {
            background-color: #e8f5e9;
            color: #1b5e20;
        }
        
        .log-type-update {
            background-color: #fff3e0;
            color: #e65100;
        }
        
        .log-type-delete {
            background-color: #ffebee;
            color: #b71c1c;
        }
        
        .log-type-approve {
            background-color: #e0f7fa;
            color: #006064;
        }
        
        .log-type-reject {
            background-color: #fce4ec;
            color: #880e4f;
        }
        
        .log-type-complete {
            background-color: #f1f8e9;
            color: #33691e;
        }
        
        /* 过滤和搜索区域 */
        .filter-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            margin-bottom: 20px;
        }
        
        .search-group, .filter-group {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        /* 详情表格样式 */
        .detail-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 15px;
        }
        
        .detail-table th, .detail-table td {
            padding: 8px;
            text-align: left;
            border-bottom: 1px solid #e9ecef;
        }
        
        .detail-section {
            margin-bottom: 20px;
        }
        
        .detail-section h4 {
            margin-top: 0;
            border-bottom: 1px solid #e9ecef;
            padding-bottom: 5px;
        }
        
        /* 分页控件样式 */
        .pagination {
            display: flex;
            justify-content: center;
            gap: 8px;
            margin-top: 30px;
        }
        
        .pagination button {
            padding: 6px 12px;
            background: white;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        
        .pagination button:hover:not(.active):not(:disabled) {
            background-color: #f8f9fa;
            border-color: #dee2e6;
        }
        
        .pagination button.active {
            background-color: #34495e;
            color: white;
            border-color: #34495e;
        }
        
        .pagination button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
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
                    <a href="dashboard.jsp" class="nav-item">
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
                    <a href="logs.jsp" class="nav-item active">
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
                    <h1 class="page-title">系统日志</h1>
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
                <div class="content-card">
                    <div class="card-header">
                        <h2 class="card-title">系统日志</h2>
                        
                        <div class="filter-actions">
                            <div class="search-group">
                                <input type="text" id="search-input" class="form-control" placeholder="搜索操作人/内容">
                                <button class="search-btn" onclick="searchLogs()">
                                    🔍 搜索
                                </button>
                            </div>
                            <div class="filter-group">
                                <select id="type-filter" class="form-control">
                                    <option value="">全部类型</option>
                                    <option value="LOGIN">登录</option>
                                    <option value="LOGOUT">登出</option>
                                    <option value="ADD">添加</option>
                                    <option value="UPDATE">更新</option>
                                    <option value="DELETE">删除</option>
                                    <option value="APPROVE">审批</option>
                                    <option value="REJECT">拒绝</option>
                                    <option value="COMPLETE">完成</option>
                                </select>
                                <input type="date" id="date-filter" class="form-control" placeholder="按日期筛选">
                                <button class="filter-btn" onclick="filterLogs()">
                                    📊 筛选
                                </button>
                            </div>
                        </div>
                    </div>
                
                    <div class="card-body">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>操作类型</th>
                                    <th>操作人</th>
                                    <th>操作内容</th>
                                    <th>IP地址</th>
                                    <th>操作时间</th>
                                    <th>详情</th>
                                </tr>
                            </thead>
                            <tbody id="logs-table-body">
                                <!-- 数据将通过JavaScript动态加载 -->
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 40px;">
                                        加载中...
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    
                        <div class="pagination" id="pagination">
                            <!-- 分页控件将通过JavaScript动态生成 -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 日志详情弹窗 -->
    <div id="log-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>日志详情</h3>
                <span class="close" onclick="closeModal('log-modal')">&times;</span>
            </div>
            <div class="modal-body" id="log-details">
                <!-- 日志详情将通过JavaScript动态加载 -->
            </div>
            <div class="modal-footer">
                <button class="btn" onclick="closeModal('log-modal')">关闭</button>
            </div>
        </div>
    </div>
    
    <footer>
        <div class="container">
            &copy; 2023 高校预约接待系统 - 版权所有
        </div>
    </footer>
    
    <script src="${pageContext.request.contextPath}/js/modalUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/logs.js"></script>
    
    <!-- 页面特有样式已移至页面顶部style标签内 -->
</body>
</html>
