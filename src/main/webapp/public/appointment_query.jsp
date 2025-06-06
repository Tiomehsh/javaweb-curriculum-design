<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>预约查询 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* 预约查询页面特定样式 */
        .main-container {
            max-width: 600px;
        }
        
        /* 查询表单 */
        .query-form {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        
        .query-btn {
            width: 100%;
        }
        
        /* 预约详情卡片 */
        .appointment-card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 0;
            margin-bottom: 20px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        
        .card-header {
            background: #f8f9fa;
            padding: 20px;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .card-title {
            font-size: 1.2em;
            font-weight: 600;
            color: #2c3e50;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        
        .card-body {
            padding: 20px;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-bottom: 20px;
        }
        
        .info-item.full-width {
            grid-column: 1 / -1;
        }
        
        /* 随行人员 */
        .companions-section {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #e0e0e0;
        }
        
        .companions-title {
            font-size: 1em;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 15px;
        }
        
        .companion-list {
            background: #f8f9fa;
            border-radius: 6px;
            padding: 15px;
        }
        
        .companion-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .companion-item:last-child {
            border-bottom: none;
        }
        
        .companion-name {
            font-weight: 500;
            color: #333;
        }
        
        .companion-info {
            font-size: 0.85em;
            color: #666;
        }
        
        /* 操作按钮 */
        .action-buttons {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
        
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 6px;
            font-size: 0.9em;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s ease;
            text-decoration: none;
            text-align: center;
            flex: 1;
        }
        
        .btn-primary {
            background: #1e88e5;
            color: white;
        }
        
        .btn-primary:hover {
            background: #1976d2;
            transform: translateY(-1px);
        }
        
        .btn-danger {
            background: #f44336;
            color: white;
        }
        
        .btn-danger:hover {
            background: #d32f2f;
            transform: translateY(-1px);
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
        }
        
        /* 通行码显示样式 */
        .pass-code-section {
            margin-top: 30px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            border: 2px solid #e0e0e0;
        }
        
        .pass-code-section.valid {
            background: #e8f5e8;
            border-color: #8a2be2;
        }
        
        .pass-code-section.invalid {
            background: #f5f5f5;
            border-color: #999;
        }
        
        .pass-code-header {
            text-align: center;
            margin-bottom: 20px;
        }
        
        .pass-code-title {
            font-size: 1.2em;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 10px;
        }
        
        .pass-code-status {
            font-size: 1em;
            margin-bottom: 10px;
        }
        
        .pass-code-content {
            display: flex;
            gap: 20px;
            align-items: flex-start;
        }
        
        .qrcode-wrapper {
            flex-shrink: 0;
            text-align: center;
        }
        
        .qrcode-container {
            background: white;
            padding: 15px;
            border: 2px solid;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            display: inline-block;
        }
        
        .qrcode-container.valid {
            border-color: #8a2be2;
        }
        
        .qrcode-container.invalid {
            border-color: #999;
            opacity: 0.8;
        }
        
        .qrcode-label {
            margin-top: 10px;
            font-weight: 600;
            color: #666;
            font-size: 0.9em;
        }
        
        .pass-info-wrapper {
            flex: 1;
        }
        
        .pass-info-table {
            width: 100%;
        }
        
        .pass-info-table td {
            padding: 8px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .pass-info-table td:first-child {
            font-weight: 500;
            color: #666;
            width: 30%;
        }
        
        /* 错误提示 */
        .alert {
            padding: 15px;
            border-radius: 6px;
            margin-bottom: 20px;
        }
        
        .alert-danger {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        
        /* 响应式设计 */
        @media (max-width: 768px) {
            .main-container {
                margin: 20px;
                padding: 20px;
            }
            
            .info-grid {
                grid-template-columns: 1fr;
                gap: 10px;
            }
            
            .pass-code-content {
                flex-direction: column;
                align-items: center;
            }
            
            .qrcode-container img {
                width: 180px !important;
                height: 180px !important;
            }
            
            .action-buttons {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="main-container">
        <!-- 卡片顶部链接 -->
        <div class="card-top-links">
            <a href="index.jsp" class="card-header-link">← 返回首页</a>
        </div>
        <div class="page-header">
            <h1 class="page-title">预约查询</h1>
            <p class="page-subtitle">查询您的预约信息和通行码</p>
        </div>
        
        <!-- 查询表单 -->
        <div id="query-form-container">
            <div class="query-form">
                <div class="form-group">
                    <label for="appointment-type">预约类型<span class="required">*</span></label>
                    <select id="appointment-type" class="form-control">
                        <option value="public">公众参观预约</option>
                        <option value="official">公务来访预约</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="appointment-id">预约编号<span class="required">*</span></label>
                    <input type="text" id="appointment-id" class="form-control" placeholder="请输入预约编号">
                </div>
                
                <div class="form-group">
                    <label for="contact-phone">联系电话<span class="required">*</span></label>
                    <input type="text" id="contact-phone" class="form-control" placeholder="请输入预约时填写的联系电话">
                </div>
                
                <div class="form-group">
                    <button type="button" class="btn btn-primary query-btn" onclick="queryAppointment()">查询预约</button>
                </div>
            </div>
        </div>
        
        <!-- 预约详情 -->
        <div id="appointment-details" style="display: none;">
            <div class="appointment-card">
                <div class="card-header">
                    <h3 class="card-title">
                        <span id="appointment-title">预约信息</span>
                        <span id="appointment-status-badge"></span>
                    </h3>
                </div>
                <div class="card-body">
                    <div class="info-grid" id="appointment-info">
                        <!-- 预约信息将在这里动态显示 -->
                    </div>
                    
                    <!-- 随行人员信息 -->
                    <div id="companions-section" class="companions-section" style="display: none;">
                        <div class="companions-title">随行人员</div>
                        <div class="companion-list" id="companions-list">
                            <!-- 随行人员信息将在这里动态显示 -->
                        </div>
                    </div>
                    
                    <!-- 操作按钮 -->
                    <div class="action-buttons">
                        <button type="button" class="btn btn-secondary" onclick="resetQuery()">重新查询</button>
                        <button type="button" class="btn btn-danger" id="cancel-btn" onclick="showCancelModal()" style="display: none;">取消预约</button>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 错误提示 -->
        <div id="query-error" style="display: none;">
            <div class="alert alert-danger">
                <p id="error-message"></p>
            </div>
            <div style="text-align: center;">
                <button type="button" class="btn btn-primary" onclick="resetQuery()">重新查询</button>
            </div>
        </div>
    </div>
    
    <!-- 取消预约弹窗 -->
    <div id="cancel-modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); z-index: 1000;">
        <div style="background-color: white; width: 90%; max-width: 500px; margin: 100px auto; padding: 20px; border-radius: 8px;">
            <h3 style="margin-bottom: 15px; color: #2c3e50;">取消预约</h3>
            <p style="margin-bottom: 20px; color: #666;">您确定要取消此预约吗？取消后将无法恢复。</p>
            <div class="form-group">
                <label for="cancel-reason">取消原因<span class="required">*</span></label>
                <textarea id="cancel-reason" rows="3" class="form-control" placeholder="请简要说明取消原因" required></textarea>
            </div>
            <div style="display: flex; gap: 10px; margin-top: 20px;">
                <button type="button" class="btn btn-secondary" onclick="closeCancelModal()">返回</button>
                <button type="button" class="btn btn-danger" onclick="cancelAppointment()">确认取消</button>
            </div>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/js/appointment_query.js"></script>
</body>
</html>