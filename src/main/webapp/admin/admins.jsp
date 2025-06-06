<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员管理 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        
        /* 页面特有样式 - 表格和卡片样式已移至 style.css */
        
        /* 角色标签样式 */
        .role-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        
        .role-system {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .role-department {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .role-reception {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        .role-audit {
            background-color: rgba(52, 73, 94, 0.1);
            color: #34495e;
        }
        
        /* 状态标签样式 */
        .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
        }
        
        .status-active {
            background-color: #d4edda;
            color: #155724;
        }
        
        .status-inactive {
            background-color: #f8d7da;
            color: #721c24;
        }
        
        /* 页面特有按钮样式 */
        .btn-password {
            background-color: #7f8c8d;
            color: white;
        }
        
        .btn-password:hover {
            background-color: #6c7b7d;
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
                    <a href="admins.jsp" class="nav-item active">
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
                    <h1 class="page-title">管理员管理</h1>
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
                        <h2 class="card-title">管理员列表</h2>
                        <button class="btn-add" onclick="openAddAdminModal()">
                            <span>+</span> 添加管理员
                        </button>
                    </div>
                    
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>用户名</th>
                                <th>姓名</th>
                                <th>角色</th>
                                <th>所属部门</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="admins-table-body">
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 40px;">
                                    加载中...
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 添加管理员弹窗 -->
    <div id="add-admin-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>添加管理员</h3>
                <span class="close" onclick="closeModal('add-admin-modal')">&times;</span>
            </div>
            <div class="modal-body">
                <form id="add-admin-form">
                    <div class="form-group">
                        <label for="add-username">用户名 <span class="required">*</span></label>
                        <input type="text" id="add-username" required autocomplete="username">
                    </div>
                    
                    <div class="form-group">
                        <label for="add-password">密码 <span class="required">*</span></label>
                        <input type="password" id="add-password" required autocomplete="new-password">
                        <div id="add-password-strength"></div>
                        <div id="add-password-errors"></div>
                    </div>

                    <div class="form-group">
                        <label for="add-confirm-password">确认密码 <span class="required">*</span></label>
                        <input type="password" id="add-confirm-password" required autocomplete="new-password">
                    </div>
                    
                    <div class="form-group">
                        <label for="add-real-name">姓名 <span class="required">*</span></label>
                        <input type="text" id="add-real-name" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="add-role">角色 <span class="required">*</span></label>
                        <select id="add-role" required onchange="toggleDepartmentField('add')">
                            <option value="">请选择角色</option>
                            <option value="SYSTEM_ADMIN">系统管理员</option>
                            <option value="DEPARTMENT_ADMIN">部门管理员</option>
                            <option value="RECEPTION_ADMIN">接待管理员</option>
                            <option value="AUDIT_ADMIN">审计管理员</option>
                        </select>
                    </div>
                    
                    <div class="form-group" id="add-dept-group" style="display: none;">
                        <label for="add-dept-id">所属部门 <span class="required">*</span></label>
                        <select id="add-dept-id">
                            <option value="">请选择部门</option>
                            <!-- 部门选项将通过JavaScript动态加载 -->
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="add-phone">联系电话</label>
                        <input type="text" id="add-phone" pattern="[0-9]{11}" title="请输入11位手机号">
                    </div>
                    
                    <div class="form-group">
                        <label for="add-email">电子邮箱</label>
                        <input type="email" id="add-email">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="addAdmin()">保存</button>
                <button class="btn btn-cancel" onclick="closeModal('add-admin-modal')">取消</button>
            </div>
        </div>
    </div>
    
    <!-- 编辑管理员弹窗 -->
    <div id="edit-admin-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>编辑管理员</h3>
                <span class="close" onclick="closeModal('edit-admin-modal')">&times;</span>
            </div>
            <div class="modal-body">
                <form id="edit-admin-form">
                    <input type="hidden" id="edit-id">
                    
                    <div class="form-group">
                        <label for="edit-username">用户名</label>
                        <input type="text" id="edit-username" readonly>
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-real-name">姓名 <span class="required">*</span></label>
                        <input type="text" id="edit-real-name" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-role">角色 <span class="required">*</span></label>
                        <select id="edit-role" required onchange="toggleDepartmentField('edit')">
                            <option value="">请选择角色</option>
                            <option value="SYSTEM_ADMIN">系统管理员</option>
                            <option value="DEPARTMENT_ADMIN">部门管理员</option>
                            <option value="RECEPTION_ADMIN">接待管理员</option>
                            <option value="AUDIT_ADMIN">审计管理员</option>
                        </select>
                    </div>
                    
                    <div class="form-group" id="edit-dept-group" style="display: none;">
                        <label for="edit-dept-id">所属部门 <span class="required">*</span></label>
                        <select id="edit-dept-id">
                            <option value="">请选择部门</option>
                            <!-- 部门选项将通过JavaScript动态加载 -->
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-phone">联系电话</label>
                        <input type="text" id="edit-phone" pattern="[0-9]{11}" title="请输入11位手机号">
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-email">电子邮箱</label>
                        <input type="email" id="edit-email">
                    </div>
                    
                    <div class="form-group">
                        <label for="edit-status">状态 <span class="required">*</span></label>
                        <select id="edit-status" required>
                            <option value="1">启用</option>
                            <option value="0">禁用</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="updateAdmin()">保存</button>
                <button class="btn btn-cancel" onclick="closeModal('edit-admin-modal')">取消</button>
            </div>
        </div>
    </div>
    
    <!-- 重置密码弹窗 -->
    <div id="reset-password-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>重置密码</h3>
                <span class="close" onclick="closeModal('reset-password-modal')">&times;</span>
            </div>
            <div class="modal-body">
                <form id="reset-password-form">
                    <input type="hidden" id="reset-id">
                    
                    <div class="form-group">
                        <label for="reset-username">用户名</label>
                        <input type="text" id="reset-username" readonly autocomplete="username">
                    </div>
                    
                    <div class="form-group">
                        <label for="reset-password">新密码 <span class="required">*</span></label>
                        <input type="password" id="reset-password" required autocomplete="new-password">
                        <div id="reset-password-strength"></div>
                        <div id="reset-password-errors"></div>
                    </div>

                    <div class="form-group">
                        <label for="reset-confirm-password">确认密码 <span class="required">*</span></label>
                        <input type="password" id="reset-confirm-password" required autocomplete="new-password">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="resetPassword()">保存</button>
                <button class="btn btn-cancel" onclick="closeModal('reset-password-modal')">取消</button>
            </div>
        </div>
    </div>
    
    <!-- 模态框样式现在由全局 style.css 控制 -->
    <style>
        
        /* 表单样式 */
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #5a6c7d;
            font-weight: 500;
            font-size: 14px;
        }
        
        .form-group input,
        .form-group select {
            width: 100%;
            padding: 10px 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s ease;
        }
        
        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #34495e;
            box-shadow: 0 0 0 3px rgba(52, 73, 94, 0.1);
        }
        
        .form-group input[readonly] {
            background-color: #f8f9fa;
            cursor: not-allowed;
        }
        
        .required {
            color: #e74c3c;
        }
        
        /* 按钮样式 */
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s ease;
            font-weight: 500;
        }
        
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }
        
        .btn-primary {
            background-color: #34495e;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #2c3e50;
        }
        
        .btn-cancel {
            background-color: #95a5a6;
            color: white;
        }
        
        .btn-cancel:hover {
            background-color: #7f8c8d;
        }
        
        /* 响应式设计 */
        @media (max-width: 768px) {
            .action-buttons {
                flex-direction: column;
            }
        }
    </style>
    <script src="${pageContext.request.contextPath}/js/session-manager.js"></script>
    <script src="${pageContext.request.contextPath}/js/password-validator.js"></script>
    <script src="${pageContext.request.contextPath}/js/modalUtils.js"></script>
    <script src="${pageContext.request.contextPath}/js/sidebar.js"></script>
    <script src="${pageContext.request.contextPath}/js/admins.js"></script>

    <script>
        // 初始化密码强度检查
        document.addEventListener('DOMContentLoaded', function() {
            if (window.passwordValidator) {
                // 添加管理员密码验证
                passwordValidator.initPasswordInput('add-password', 'add-password-strength', 'add-password-errors');
                // 重置密码验证
                passwordValidator.initPasswordInput('reset-password', 'reset-password-strength', 'reset-password-errors');
            }
        });
    </script>
</body>
</html>
