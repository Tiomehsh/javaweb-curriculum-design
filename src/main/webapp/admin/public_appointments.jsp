<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公众预约管理 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // 定义上下文路径供JS文件使用
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <style>
        /* 页面特有样式 - 通用的管理端样式已移至 style.css */
        
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
        
        /* 详情表格样式已移至全局CSS文件 */
        
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
                    <a href="public_appointments.jsp" class="nav-item active">
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
                    <h1 class="page-title">公众预约管理</h1>
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
                        <h2 class="card-title">公众预约管理</h2>
                        
                        <div class="filter-actions">
                            <div class="search-group">
                                <input type="text" id="search-input" class="form-control" placeholder="搜索预约编号/姓名/手机号">
                                <button class="search-btn" onclick="searchAppointments()">
                                    🔍 搜索
                                </button>
                            </div>
                            <div class="filter-group">
                                <select id="status-filter" class="form-control">
                                    <option value="">全部状态</option>
                                    <option value="0">待审核</option>
                                    <option value="1">已批准</option>
                                    <option value="2">已拒绝</option>
                                    <option value="3">已取消</option>
                                    <option value="4">已完成</option>
                                </select>
                                <input type="date" id="date-filter" class="form-control" placeholder="按日期筛选">
                                <button class="filter-btn" onclick="filterAppointments()">
                                    📊 筛选
                                </button>
                            </div>
                        </div>
                    </div>
                
                    <div class="card-body">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>预约编号</th>
                                    <th>姓名</th>
                                    <th>联系电话</th>
                                    <th>单位/学校</th>
                                    <th>参观时间</th>
                                    <th>参观人数</th>
                                    <th>状态</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody id="appointments-table-body">
                                <!-- 数据将通过JavaScript动态加载 -->
                                <tr>
                                    <td colspan="8" style="text-align: center; padding: 40px;">
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
                <div id="action-buttons">
                    <!-- 操作按钮将根据预约状态动态显示 -->
                </div>
                <button class="btn btn-secondary" onclick="closeModal('appointment-modal')">关闭</button>
            </div>
        </div>
    </div>
    
    <!-- 审批弹窗 -->
    <div id="approval-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="approval-title">审批预约</h3>
                <span class="close" onclick="closeModal('approval-modal')">&times;</span>
            </div>
            <!-- 只需要一个modal-body，这是重复的内容 -->
            <div class="modal-body">
                <form id="approval-form">
                    <div class="form-group">
                        <label for="approval-status">审批结果</label>
                        <select id="approval-status" required>
                            <option value="1">批准</option>
                            <option value="2">拒绝</option>
                        </select>
                    </div>
                    
                    <div class="form-group" id="reject-reason-group" style="display: none;">
                        <label for="reject-reason">拒绝原因</label>
                        <textarea id="reject-reason" rows="3" placeholder="请输入拒绝原因"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="approval-remarks">审批备注</label>
                        <textarea id="approval-remarks" rows="3" placeholder="可选"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="submitApproval()">提交</button>
                <button class="btn" onclick="closeModal('approval-modal')">取消</button>
            </div>
        </div>
    </div>
    
    <!-- 完成预约弹窗 -->
    <div id="complete-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>完成预约</h3>
                <span class="close" onclick="closeModal('complete-modal')">&times;</span>
            </div>
            <div class="modal-body">
                <p>确认该预约已完成？</p>
                <div class="form-group">
                    <label for="complete-remarks">备注</label>
                    <textarea id="complete-remarks" rows="3" placeholder="可选"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="confirmComplete()">确认完成</button>
                <button class="btn" onclick="closeModal('complete-modal')">取消</button>
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
    <script src="${pageContext.request.contextPath}/js/public_appointments.js"></script>
    
    <!-- 页面特有样式已移至页面顶部style标签内 -->
</body>
</html>
