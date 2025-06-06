<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公众参观预约 - 校园通行码预约管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* 公众预约页面特定样式 */
        .main-container {
            max-width: 500px;
        }
        
        /* 步骤指示器 */
        .step-indicator {
            display: flex;
            justify-content: center;
            margin-bottom: 30px;
            padding: 0 20px;
        }
        
        .step {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            position: relative;
        }
        
        .step-number {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #e0e0e0;
            color: #999;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            margin-bottom: 8px;
            position: relative;
            z-index: 2;
        }
        
        .step.active .step-number {
            background: #1e88e5;
            color: white;
        }
        
        .step.completed .step-number {
            background: #27ae60;
            color: white;
        }
        
        .step-label {
            font-size: 0.8em;
            color: #666;
            white-space: nowrap;
            text-align: center;
        }
        
        .step.active .step-label {
            color: #1e88e5;
            font-weight: 600;
        }
        
        .step.completed .step-label {
            color: #27ae60;
            font-weight: 600;
        }
        
        .step-line {
            position: absolute;
            top: 20px;
            left: 50%;
            right: -50%;
            height: 2px;
            background: #e0e0e0;
            z-index: 1;
        }
        
        .step:last-child .step-line {
            display: none;
        }
        
        .step.completed .step-line {
            background: #27ae60;
        }
        
        /* 表单步骤 */
        .form-step {
            display: none;
            animation: fadeIn 0.3s ease;
        }
        
        .form-step.active {
            display: block;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateX(20px); }
            to { opacity: 1; transform: translateX(0); }
        }
        
        .step-title {
            font-size: 1.2em;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 20px;
            text-align: center;
        }
        
        /* 表单错误样式 */
        .form-control.error {
            border-color: #e74c3c;
        }
        
        .error-message {
            color: #e74c3c;
            font-size: 0.85em;
            margin-top: 5px;
            display: none;
        }
        

        
        /* 页面特定按钮样式 */
        .btn-group {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }
        
        .btn-group .btn {
            flex: 1;
        }
        
        .btn-success {
            background: #27ae60;
            color: white;
        }
        
        .btn-success:hover {
            background: #229954;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
        }
        
        /* 响应式设计 */
        @media (max-width: 768px) {
            .main-container {
                margin: 20px;
                padding: 20px;
            }
            
            .step-indicator {
                padding: 0 10px;
            }
            
            .step-label {
                font-size: 0.7em;
            }
            
            .btn-group {
                flex-direction: column;
            }
        }

        /* 成功消息样式 */
        .alert {
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        .alert-success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
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
            <h1 class="page-title">公众参观预约</h1>
            <p class="page-subtitle">请按步骤填写预约信息</p>
        </div>
        
        <!-- 步骤指示器 -->
        <div class="step-indicator">
            <div class="step active" data-step="1">
                <div class="step-number">1</div>
                <div class="step-label">基本信息</div>
                <div class="step-line"></div>
            </div>
            <div class="step" data-step="2">
                <div class="step-number">2</div>
                <div class="step-label">预约详情</div>
                <div class="step-line"></div>
            </div>
            <div class="step" data-step="3">
                <div class="step-number">3</div>
                <div class="step-label">交通信息</div>
                <div class="step-line"></div>
            </div>
            <div class="step" data-step="4">
                <div class="step-number">4</div>
                <div class="step-label">确认提交</div>
            </div>
        </div>
        
        <form id="appointmentForm">
            <!-- 步骤1：基本信息 -->
            <div class="form-step active" data-step="1">
                <h3 class="step-title">📝 基本信息</h3>
                
                <div class="form-group">
                    <label for="name">姓名<span class="required">*</span></label>
                    <input type="text" id="name" name="name" class="form-control" required>
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="idCard">身份证号<span class="required">*</span></label>
                    <input type="text" id="idCard" name="idCard" class="form-control" required
                           pattern="[0-9Xx]{18}" title="请输入18位身份证号">
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="phone">联系电话<span class="required">*</span></label>
                    <input type="text" id="phone" name="phone" class="form-control" required
                           pattern="[0-9]{11}" title="请输入11位手机号">
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="organization">单位/学校<span class="required">*</span></label>
                    <input type="text" id="organization" name="organization" class="form-control" required>
                    <div class="error-message"></div>
                </div>
            </div>
            
            <!-- 步骤2：预约详情 -->
            <div class="form-step" data-step="2">
                <h3 class="step-title">📅 预约详情</h3>
                
                <div class="form-group">
                    <label for="campus">校区<span class="required">*</span></label>
                    <select id="campus" name="campus" class="form-control" required>
                        <option value="">请选择校区</option>
                        <option value="朝晖校区">朝晖校区</option>
                        <option value="莫干山校区">莫干山校区</option>
                        <option value="屏峰校区">屏峰校区</option>
                    </select>
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="visitTime">参观时间<span class="required">*</span></label>
                    <input type="datetime-local" id="visitTime" name="visitTime" class="form-control" required min="">
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="visitors">参观人数<span class="required">*</span></label>
                    <input type="number" id="visitors" name="visitors" class="form-control" required min="1" max="50">
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group">
                    <label for="purpose">参观目的<span class="required">*</span></label>
                    <textarea id="purpose" name="purpose" class="form-control" rows="3" required></textarea>
                    <div class="error-message"></div>
                </div>
            </div>
            
            <!-- 步骤3：交通信息 -->
            <div class="form-step" data-step="3">
                <h3 class="step-title">🚗 交通信息</h3>
                
                <div class="form-group">
                    <label for="transportation">交通方式<span class="required">*</span></label>
                    <select id="transportation" name="transportation" class="form-control" required>
                        <option value="">请选择交通方式</option>
                        <option value="步行">步行</option>
                        <option value="公交车">公交车</option>
                        <option value="地铁">地铁</option>
                        <option value="出租车">出租车</option>
                        <option value="网约车">网约车</option>
                        <option value="私家车">私家车</option>
                        <option value="自行车">自行车</option>
                        <option value="电动车">电动车</option>
                        <option value="其他">其他</option>
                    </select>
                    <div class="error-message"></div>
                </div>
                
                <div class="form-group" id="plateNumberGroup" style="display: none;">
                    <label for="plateNumber">车牌号</label>
                    <input type="text" id="plateNumber" name="plateNumber" class="form-control" placeholder="请输入车牌号">
                    <div class="error-message"></div>
                </div>
                

                
                <div class="form-group">
                    <label for="remarks">备注信息</label>
                    <textarea id="remarks" name="remarks" class="form-control" rows="3"></textarea>
                    <div class="error-message"></div>
                </div>
            </div>
            
            <!-- 步骤4：确认提交 -->
            <div class="form-step" data-step="4">
                <h3 class="step-title">✅ 确认提交</h3>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
                    <h4 style="margin-bottom: 15px; color: #2c3e50;">请核对您的预约信息：</h4>
                    <div id="summary-content">
                        <!-- 预约信息摘要将在这里显示 -->
                    </div>
                </div>
                
                <div class="form-group">
                    <div class="agreement-section">
                        <label class="agreement-label">
                            <input type="checkbox" id="agreement" name="agreement" required>
                            <span class="checkmark"></span>
                            <span class="agreement-text">
                                我已阅读并同意
                                <a href="#" onclick="showAgreement()" class="agreement-link">《预约须知》</a>
                            </span>
                        </label>
                    </div>
                    <div class="error-message"></div>
                </div>
            </div>
        </form>
        
        <!-- 步骤导航按钮 -->
        <div class="btn-group">
            <button type="button" class="btn btn-secondary" id="prevBtn" onclick="changeStep(-1)" style="display: none;">上一步</button>
            <button type="button" class="btn btn-primary" id="nextBtn" onclick="changeStep(1)">下一步</button>
            <button type="button" class="btn btn-success" id="submitBtn" onclick="submitAppointment()" style="display: none;">提交预约</button>
        </div>
        
        <!-- 成功消息 -->
        <div id="success-message" style="display: none;">
            <div class="alert alert-success">
                <h3>预约提交成功！</h3>
                <p>您的预约已成功提交，预约编号：<span id="appointment-id"></span></p>
                <p>请保存此编号，您可以使用预约编号和手机号查询预约状态。</p>
                <div style="margin-top: 20px;">
                    <a href="index.jsp" class="btn btn-primary" style="margin-right: 10px;">返回首页</a>
                    <a href="appointment_query.jsp" class="btn btn-secondary">查询预约</a>
                </div>
            </div>
        </div>
    </div>

    <!-- 预约须知弹窗 -->
    <div id="agreement-modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); z-index: 1000;">
        <div style="background-color: white; width: 80%; max-width: 600px; margin: 100px auto; padding: 20px; border-radius: 8px; max-height: 80vh; overflow-y: auto;">
            <h2 style="margin-bottom: 20px; color: #2c3e50;">预约须知</h2>
            <div>
                <p style="margin-bottom: 10px;">1. 请如实填写个人信息，身份证号和手机号将用于身份验证和通知。</p>
                <p style="margin-bottom: 10px;">2. 预约成功后，请在预约时间前到达指定校区的接待中心进行签到。</p>
                <p style="margin-bottom: 10px;">3. 参观人数限制为1-50人，如需大型团体参观，请提前联系管理员。</p>
                <p style="margin-bottom: 10px;">4. 预约成功后如需取消，请至少提前24小时取消，否则将记入信用记录。</p>
                <p style="margin-bottom: 10px;">5. 校园内请遵守各项规章制度，不得影响正常教学秩序。</p>
                <p style="margin-bottom: 10px;">6. 参观过程中，请在指定区域活动，不得擅自进入未开放区域。</p>
                <p style="margin-bottom: 10px;">7. 学校有权对不遵守规定的访客终止参观并请出校园。</p>
                <p style="margin-bottom: 10px;">8. 您的个人信息将被严格保密，仅用于预约管理和必要的安保需求。</p>
            </div>
            <div style="text-align: center; margin-top: 20px;">
                <button class="btn btn-primary" onclick="closeAgreement()">我已阅读并同意</button>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/public_appointment.js"></script>
</body>
</html>