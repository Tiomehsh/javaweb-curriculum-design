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
    
    // 检查用户权限（只有系统管理员可以访问）
    boolean isSystemAdmin = "SYSTEM_ADMIN".equals(currentAdmin.getRole());
    if (!isSystemAdmin) {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>部门管理 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        
        /* 页面操作区域 */
        .page-actions {
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .breadcrumb {
            color: #7f8c8d;
            font-size: 14px;
        }
        
        .breadcrumb span {
            margin: 0 5px;
        }
        
        /* add-btn 样式已移至 style.css */

        /* 部门管理特定样式 */
        .departments-container {
            padding: 20px 0;
        }

        .department-group {
            margin-bottom: 40px;
        }

        .group-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
            padding: 15px 20px;
            background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
            border-radius: 10px;
            color: white;
        }

        .group-title {
            display: flex;
            align-items: center;
            gap: 10px;
            margin: 0;
            font-size: 1.2rem;
            font-weight: 600;
        }

        .group-title i {
            font-size: 1.4rem;
        }

        .group-count {
            background: rgba(255, 255, 255, 0.2);
            padding: 5px 12px;
            border-radius: 20px;
            font-weight: bold;
            min-width: 30px;
            text-align: center;
        }

        .department-cards {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 20px;
            padding: 0 10px;
        }

        .department-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            border: 1px solid #e1e8ed;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .department-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
        }

        .department-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #34495e 0%, #2c3e50 100%);
        }

        /* 部门卡片特有样式 - 基础 card-header 和 card-title 已移至 style.css */
        .card-header {
            align-items: flex-start;
            margin-bottom: 16px;
        }

        .card-title {
            font-size: 1.1rem;
            line-height: 1.4;
        }

        .card-type {
            background: #34495e;
            color: white;
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .card-info {
            margin: 16px 0;
        }

        .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            color: #5a6c7d;
            font-size: 0.9rem;
        }

        .info-item i {
            width: 16px;
            margin-right: 8px;
            color: #34495e;
        }

        .info-item:last-child {
            margin-bottom: 0;
        }

        .card-actions {
            display: flex;
            gap: 8px;
            padding-top: 16px;
            border-top: 1px solid #e9ecef;
        }

        .card-actions .btn {
            flex: 1;
            padding: 8px 12px;
            font-size: 0.85rem;
            border-radius: 6px;
            transition: all 0.2s ease;
        }

        .btn-edit {
            background: #3498db;
            color: white;
            border: none;
        }

        .btn-edit:hover {
            background: #2980b9;
            transform: translateY(-1px);
        }

        .btn-delete {
            background: #e74c3c;
            color: white;
            border: none;
        }

        .btn-delete:hover {
            background: #c0392b;
            transform: translateY(-1px);
        }

        .empty-state {
            text-align: center;
            padding: 40px 20px;
            color: #7f8c8d;
        }

        .empty-state i {
            font-size: 3rem;
            margin-bottom: 16px;
            opacity: 0.5;
        }

        .empty-state p {
            font-size: 1rem;
            margin: 0;
        }

        /* 加载状态 */
        .loading {
            text-align: center;
            padding: 40px;
            color: #7f8c8d;
        }

        .loading i {
            font-size: 2rem;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #2c3e50;
            font-weight: 500;
        }

        .required {
            color: #e74c3c;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .btn-primary {
            background-color: #34495e;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #2c3e50;
        }

        .btn-secondary {
            background-color: #95a5a6;
            color: white;
        }

        .btn-secondary:hover {
            background-color: #7f8c8d;
        }

        /* 响应式设计 */
        @media (max-width: 768px) {
            .department-cards {
                grid-template-columns: 1fr;
            }
            
            .group-header {
                padding: 12px 16px;
            }
            
            .department-card {
                padding: 20px;
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
                    <a href="departments.jsp" class="nav-item active">
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
                    <h1 class="page-title">部门管理</h1>
                </div>
                <div class="user-menu">
                    <div class="user-info">
                        <div class="user-avatar"><%= currentAdmin.getRealName().substring(0, 1) %></div>
                        <div>
                            <div style="font-weight: 500;"><%= currentAdmin.getRealName() %></div>
                            <div style="font-size: 12px; color: #7f8c8d;">系统管理员</div>
                        </div>
                    </div>
                    <button class="logout-btn" onclick="logout()">退出登录</button>
                </div>
            </nav>
            
            <!-- 内容区域 -->
            <div class="content">
                <!-- 页面操作区域 -->
                <div class="page-actions">
                    <div></div>
                    
                    <button class="add-btn" onclick="openAddDepartmentModal()">
                        + 添加部门
                    </button>
                </div>

                <!-- 部门列表 -->
                <div class="departments-container">
                    <!-- 行政部门 -->
                    <div class="department-group">
                        <div class="group-header">
                            <h2 class="group-title">
                                ⚙️ 行政部门
                            </h2>
                            <div class="group-count" id="admin-count">0</div>
                        </div>
                        <div class="department-cards" id="admin-departments">
                            <!-- 行政部门卡片将在这里动态加载 -->
                        </div>
                    </div>

                    <!-- 直属部门 -->
                    <div class="department-group">
                        <div class="group-header">
                            <h2 class="group-title">
                                🏛️ 直属部门
                            </h2>
                            <div class="group-count" id="direct-count">0</div>
                        </div>
                        <div class="department-cards" id="direct-departments">
                            <!-- 直属部门卡片将在这里动态加载 -->
                        </div>
                    </div>

                    <!-- 学院 -->
                    <div class="department-group">
                        <div class="group-header">
                            <h2 class="group-title">
                                🎓 学院
                            </h2>
                            <div class="group-count" id="college-count">0</div>
                        </div>
                        <div class="department-cards" id="college-departments">
                            <!-- 学院卡片将在这里动态加载 -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 添加部门模态框 -->
    <div id="add-department-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>+ 添加部门</h3>
                <button class="modal-close" onclick="closeModal('add-department-modal')">
                    ✕
                </button>
            </div>
            <div class="modal-body">
                <form id="add-department-form">
                    <div class="form-group">
                        <label for="add-dept-type">部门类型 <span class="required">*</span></label>
                        <select id="add-dept-type" required>
                            <option value="">请选择部门类型</option>
                            <option value="行政部门">行政部门</option>
                            <option value="直属部门">直属部门</option>
                            <option value="学院">学院</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="add-dept-name">部门名称 <span class="required">*</span></label>
                        <input type="text" id="add-dept-name" required placeholder="请输入部门名称">
                    </div>
                    
                    <div class="form-group">
                        <label for="add-contact-person">联系人</label>
                        <input type="text" id="add-contact-person" placeholder="请输入联系人姓名">
                    </div>
                    
                    <div class="form-group">
                        <label for="add-contact-phone">联系电话</label>
                        <input type="text" id="add-contact-phone" pattern="[0-9-]{7,15}" title="请输入有效的电话号码" placeholder="请输入联系电话">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('add-department-modal')">取消</button>
                <button class="btn btn-primary" onclick="addDepartment()">
                    💾 保存
                </button>
            </div>
        </div>
    </div>

    <!-- 编辑部门模态框 -->
    <div id="edit-department-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>✏️ 编辑部门</h3>
                <button class="modal-close" onclick="closeModal('edit-department-modal')">
                    ✕
                </button>
            </div>
            <div class="modal-body">
                <form id="edit-department-form">
                    <input type="hidden" id="edit-dept-id">
                    
                    <div class="form-group">
                        <label for="edit-dept-type">部门类型 <span class="required">*</span></label>
                        <select id="edit-dept-type" required>
                            <option value="">请选择部门类型</option>
                            <option value="行政部门">行政部门</option>
                            <option value="直属部门">直属部门</option>
                            <option value="学院">学院</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-dept-name">部门名称 <span class="required">*</span></label>
                        <input type="text" id="edit-dept-name" required placeholder="请输入部门名称">
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-contact-person">联系人</label>
                        <input type="text" id="edit-contact-person" placeholder="请输入联系人姓名">
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-contact-phone">联系电话</label>
                        <input type="text" id="edit-contact-phone" pattern="[0-9-]{7,15}" title="请输入有效的电话号码" placeholder="请输入联系电话">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('edit-department-modal')">取消</button>
                <button class="btn btn-primary" onclick="updateDepartment()">
                    💾 保存
                </button>
            </div>
        </div>
    </div>

<script src="${pageContext.request.contextPath}/js/modalUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/departments.js"></script>
    <script>
        // 退出登录函数
        function logout() {
            if (confirm('确定要退出登录吗？')) {
                window.location.href = window.contextPath + '/admin/logout';
            }
        }
    </script>
</body>
</html>
