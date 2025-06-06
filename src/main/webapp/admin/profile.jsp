<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.javawebcurriculumdesign.model.Admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    // 检查用户是否已登录
    Admin currentAdmin = (Admin) session.getAttribute("admin");
    if (currentAdmin == null) {
        response.sendRedirect(request.getContextPath() + "/admin/login.jsp");
        return;
    }
    
    // 获取管理员角色
    boolean isSystemAdmin = "SYSTEM_ADMIN".equals(currentAdmin.getRole());
    boolean isDepartmentAdmin = "DEPARTMENT_ADMIN".equals(currentAdmin.getRole());
    boolean isReceptionAdmin = "RECEPTION_ADMIN".equals(currentAdmin.getRole());
    boolean isAuditAdmin = "AUDIT_ADMIN".equals(currentAdmin.getRole());
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人设置 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        
        /* 标签页样式 */
        .tab-container {
            width: 100%;
        }
        
        .tabs {
            display: flex;
            border-bottom: 1px solid #e9ecef;
            margin-bottom: 30px;
        }
        
        .tab-btn {
            padding: 12px 24px;
            background: none;
            border: none;
            border-bottom: 3px solid transparent;
            cursor: pointer;
            font-size: 16px;
            color: #6c757d;
            outline: none;
            transition: all 0.3s ease;
        }
        
        .tab-btn:hover {
            color: #34495e;
        }
        
        .tab-btn.active {
            border-bottom: 3px solid #34495e;
            color: #34495e;
            font-weight: 500;
        }
        
        .tab-content {
            display: none;
        }
        
        .tab-content.active {
            display: block;
            animation: fadeIn 0.3s ease;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
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
                    <a href="logs.jsp" class="nav-item">
                        📋 系统日志
                    </a>
                    </c:if>
                    <a href="profile.jsp" class="nav-item active">
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
                    <h1 class="page-title">个人设置</h1>
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
                        <h2 class="card-title">个人设置</h2>
                    </div>
                    
                    <div class="card-body">
                        <div class="tab-container">
                            <div class="tabs">
                                <button id="info-tab" class="tab-btn active" onclick="switchTab('info')">
                                    👤 基本信息
                                </button>
                                <button id="password-tab" class="tab-btn" onclick="switchTab('password')">
                                    🔒 修改密码
                                </button>
                            </div>
                            
                            <!-- 基本信息表单 -->
                            <div id="info-tab-content" class="tab-content active">
                                <form id="info-form">
                                    <div class="form-group">
                                        <label for="username">用户名</label>
                                        <input type="text" id="username" class="form-control" readonly value="${admin.loginName}">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="real-name">姓名 <span class="required">*</span></label>
                                        <input type="text" id="real-name" class="form-control" required value="${admin.realName}">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="role">角色</label>
                                        <input type="text" id="role" class="form-control" readonly value="<%
                                            String roleName = "";
                                            String role = currentAdmin.getRole();
                                            switch (role) {
                                                case "SYSTEM_ADMIN": roleName = "系统管理员"; break;
                                                case "DEPARTMENT_ADMIN": roleName = "部门管理员"; break;
                                                case "RECEPTION_ADMIN": roleName = "接待管理员"; break;
                                                case "AUDIT_ADMIN": roleName = "审计管理员"; break;
                                                default: roleName = role;
                                            }
                                            out.print(roleName);
                                        %>">
                                    </div>
                                    
                                    <div class="form-group" id="department-group">
                                        <label for="department">所属部门</label>
                                        <input type="text" id="department" class="form-control" readonly value="${admin.deptId != null ? admin.deptId : ''}">
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="phone">联系电话</label>
                                        <input type="text" id="phone" class="form-control" value="${admin.phone != null ? admin.phone : ''}" pattern="[0-9]{11}" title="请输入11位手机号">
                                    </div>
                                    
                                    <div class="form-group">
                                        <button type="button" class="btn btn-primary" onclick="updateProfile()">
                                            💾 保存修改
                                        </button>
                                    </div>
                                </form>
                            </div>
                            
                            <!-- 修改密码表单 -->
                            <div id="password-tab-content" class="tab-content">
                                <form id="password-form">
                                    <div class="form-group">
                                        <label for="old-password">当前密码 <span class="required">*</span></label>
                                        <input type="password" id="old-password" class="form-control" required>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="new-password">新密码 <span class="required">*</span></label>
                                        <input type="password" id="new-password" class="form-control" required>
                                        <div id="password-strength"></div>
                                        <div id="password-errors"></div>
                                    </div>
                                    
                                    <div class="form-group">
                                        <label for="confirm-password">确认新密码 <span class="required">*</span></label>
                                        <input type="password" id="confirm-password" class="form-control" required>
                                    </div>
                                    
                                    <div class="form-group">
                                        <button type="button" class="btn btn-primary" onclick="updatePassword()">
                                            🔄 修改密码
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/js/session-manager.js"></script>
    <script src="${pageContext.request.contextPath}/js/password-validator.js"></script>
    <script src="${pageContext.request.contextPath}/js/modalUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/profile.js"></script>

    <script>
        // 初始化密码强度检查
        document.addEventListener('DOMContentLoaded', function() {
            if (window.passwordValidator) {
                passwordValidator.initPasswordInput('new-password', 'password-strength', 'password-errors');
            }
        });
    </script>
    
    <!-- 页面特有样式已移至页面顶部style标签内 -->
</body>
</html>
