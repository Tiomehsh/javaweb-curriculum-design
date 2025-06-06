// 全局变量
let currentStep = 1;
const totalSteps = 4;

// 页面初始化
document.addEventListener('DOMContentLoaded', function() {
    // 设置访问时间最小为当前时间后24小时
    const now = new Date();
    now.setDate(now.getDate() + 1);
    const minDateTime = now.toISOString().slice(0, 16);
    document.getElementById('visitTime').min = minDateTime;
    
    // 监听交通方式变化，显示/隐藏车牌号输入框
    const transportationSelect = document.getElementById('transportation');
    const plateNumberGroup = document.getElementById('plateNumberGroup');
    
    transportationSelect.addEventListener('change', function() {
        if (this.value === '私家车') {
            plateNumberGroup.style.display = 'block';
            document.getElementById('plateNumber').required = true;
        } else {
            plateNumberGroup.style.display = 'none';
            document.getElementById('plateNumber').required = false;
            document.getElementById('plateNumber').value = '';
        }
    });

    // 初始化步骤显示
    updateStepDisplay();
});

// 步骤切换功能
function changeStep(direction) {
    if (direction === 1) {
        // 下一步 - 验证当前步骤
        if (!validateCurrentStep()) {
            return;
        }
        
        if (currentStep < totalSteps) {
            // 标记当前步骤为完成
            markStepCompleted(currentStep);
            currentStep++;
            updateStepDisplay();
            
            // 如果到了第4步，生成预约摘要
            if (currentStep === 4) {
                generateSummary();
            }
        }
    } else if (direction === -1) {
        // 上一步
        if (currentStep > 1) {
            currentStep--;
            updateStepDisplay();
        }
    }
}

// 验证当前步骤
function validateCurrentStep() {
    const currentFormStep = document.querySelector(`.form-step[data-step="${currentStep}"]`);
    const requiredInputs = currentFormStep.querySelectorAll('input[required], select[required], textarea[required]');
    
    let isValid = true;
    
    requiredInputs.forEach(input => {
        clearError(input);
        
        if (!input.value.trim()) {
            showError(input, '此字段为必填项');
            isValid = false;
        } else {
            // 特殊验证
            if (input.type === 'text' && input.pattern) {
                const regex = new RegExp(input.pattern);
                if (!regex.test(input.value)) {
                    showError(input, input.title || '格式不正确');
                    isValid = false;
                }
            }
        }
    });
    
    return isValid;
}

// 验证所有步骤
function validateAllSteps() {
    let allValid = true;
    
    for (let step = 1; step <= totalSteps; step++) {
        const formStep = document.querySelector(`.form-step[data-step="${step}"]`);
        const requiredInputs = formStep.querySelectorAll('input[required], select[required], textarea[required]');
        
        requiredInputs.forEach(input => {
            if (!input.value.trim()) {
                allValid = false;
                console.log(`步骤 ${step} 中的字段 ${input.name || input.id} 未填写`);
            } else if (input.type === 'text' && input.pattern) {
                const regex = new RegExp(input.pattern);
                if (!regex.test(input.value)) {
                    allValid = false;
                    console.log(`步骤 ${step} 中的字段 ${input.name || input.id} 格式不正确`);
                }
            }
        });
    }
    
    return allValid;
}

// 显示错误信息
function showError(input, message) {
    input.classList.add('error');
    const errorDiv = input.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

// 清除错误信息
function clearError(input) {
    input.classList.remove('error');
    const errorDiv = input.parentElement.querySelector('.error-message');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
}

// 标记步骤为完成
function markStepCompleted(stepNumber) {
    const stepElement = document.querySelector(`.step[data-step="${stepNumber}"]`);
    if (stepElement) {
        stepElement.classList.add('completed');
        stepElement.classList.remove('active');
    }
}

// 更新步骤显示
function updateStepDisplay() {
    // 更新步骤指示器
    document.querySelectorAll('.step').forEach(step => {
        const stepNumber = parseInt(step.dataset.step);
        step.classList.remove('active');
        
        if (stepNumber === currentStep) {
            step.classList.add('active');
        }
    });
    
    // 更新表单步骤显示
    document.querySelectorAll('.form-step').forEach(step => {
        const stepNumber = parseInt(step.dataset.step);
        step.classList.remove('active');
        
        if (stepNumber === currentStep) {
            step.classList.add('active');
        }
    });
    
    // 更新按钮显示
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');
    
    // 上一步按钮
    if (currentStep === 1) {
        prevBtn.style.display = 'none';
    } else {
        prevBtn.style.display = 'block';
    }
    
    // 下一步/提交按钮
    if (currentStep === totalSteps) {
        nextBtn.style.display = 'none';
        submitBtn.style.display = 'block';
    } else {
        nextBtn.style.display = 'block';
        submitBtn.style.display = 'none';
    }
}

// 生成预约信息摘要
function generateSummary() {
    const summaryContent = document.getElementById('summary-content');
    
    const name = document.getElementById('name').value;
    const phone = document.getElementById('phone').value;
    const organization = document.getElementById('organization').value;
    const campus = document.getElementById('campus').value;
    const visitTime = document.getElementById('visitTime').value;
    const visitors = document.getElementById('visitors').value;
    const transportation = document.getElementById('transportation').value;
    const purpose = document.getElementById('purpose').value;
    
    // 格式化时间
    const formattedTime = new Date(visitTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
    
    summaryContent.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 0.9em;">
            <div><strong>姓名：</strong>${name}</div>
            <div><strong>联系电话：</strong>${phone}</div>
            <div><strong>单位/学校：</strong>${organization}</div>
            <div><strong>校区：</strong>${campus}</div>
            <div><strong>参观时间：</strong>${formattedTime}</div>
            <div><strong>参观人数：</strong>${visitors}人</div>
            <div><strong>交通方式：</strong>${transportation}</div>
            <div style="grid-column: 1 / -1;"><strong>参观目的：</strong>${purpose}</div>
        </div>
    `;
}





// 显示预约须知
function showAgreement() {
    document.getElementById('agreement-modal').style.display = 'block';
}

// 关闭预约须知
function closeAgreement() {
    document.getElementById('agreement-modal').style.display = 'none';
    document.getElementById('agreement').checked = true;
}

// 提交预约
function submitAppointment() {
    // 最终验证
    if (!document.getElementById('agreement').checked) {
        alert('请阅读并同意预约须知');
        return;
    }
    
    // 验证所有步骤的必填字段
    if (!validateAllSteps()) {
        alert('请检查并完善所有必填信息');
        return;
    }
    
    // 收集主要预约信息
    const appointmentData = {
        name: document.getElementById('name').value,
        idCard: document.getElementById('idCard').value,
        phone: document.getElementById('phone').value,
        organization: document.getElementById('organization').value,
        campus: document.getElementById('campus').value,
        visitTime: document.getElementById('visitTime').value,
        visitors: document.getElementById('visitors').value,
        transportation: document.getElementById('transportation').value,
        plateNumber: document.getElementById('plateNumber').value || null,
        purpose: document.getElementById('purpose').value,
        remarks: document.getElementById('remarks').value
    };
    
    // 准备发送的数据（移除随行人员）
    const requestData = appointmentData;
    
    // 禁用提交按钮
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = '提交中...';
    
    // 发送预约请求
    fetch('../api/appointment/public/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 隐藏表单，显示成功信息
            document.querySelector('.step-indicator').style.display = 'none';
            document.getElementById('appointmentForm').style.display = 'none';
            document.querySelector('.btn-group').style.display = 'none';
            document.getElementById('success-message').style.display = 'block';
            document.getElementById('appointment-id').textContent = data.appointmentId;
        } else {
            alert('预约提交失败：' + data.message);
        }
    })
    .catch(error => {
        console.error('预约提交失败:', error);
        alert('预约提交失败，请稍后再试');
    })
    .finally(() => {
        // 恢复提交按钮
        submitBtn.disabled = false;
        submitBtn.textContent = '提交预约';
    });
}