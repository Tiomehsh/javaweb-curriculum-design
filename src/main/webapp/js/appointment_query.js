// 全局变量保存当前查询的预约信息
let currentAppointment = null;

// 全局变量保存原始手机号
let originalPhone = '';

// 查询预约
function queryAppointment() {
    const appointmentType = document.getElementById('appointment-type').value;
    const appointmentId = document.getElementById('appointment-id').value.trim();
    const contactPhone = document.getElementById('contact-phone').value.trim();
    
    if (!appointmentId || !contactPhone) {
        alert('请输入预约编号和联系电话');
        return;
    }
    
    // 保存原始手机号
    originalPhone = contactPhone;
    
    // 禁用查询按钮
    const queryBtn = document.querySelector('.query-btn');
    queryBtn.disabled = true;
    queryBtn.textContent = '查询中...';
    
    // 准备查询参数
    const queryParams = new URLSearchParams({
        id: appointmentId,
        phone: contactPhone
    });
    
    // 发送查询请求
    fetch(`../api/appointment/${appointmentType}/query?${queryParams}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 保存当前预约信息
                currentAppointment = {
                    type: appointmentType,
                    data: data.appointment
                };
                
                // 如果是已批准的预约，先获取通行码信息再显示
                if (data.appointment.status === 'APPROVED') {
                    loadPassCodeAndDisplay(data.appointment, appointmentType);
                } else {
                    // 直接显示预约信息
                    displayAppointmentDetails(data.appointment, appointmentType, null);
                }
                
                // 隐藏查询表单，显示预约详情
                document.getElementById('query-form-container').style.display = 'none';
                document.getElementById('appointment-details').style.display = 'block';
                document.getElementById('query-error').style.display = 'none';
            } else {
                // 显示错误信息
                document.getElementById('error-message').textContent = data.message || '未找到匹配的预约记录';
                document.getElementById('query-form-container').style.display = 'none';
                document.getElementById('appointment-details').style.display = 'none';
                document.getElementById('query-error').style.display = 'block';
            }
        })
        .catch(error => {
            console.error('查询预约失败:', error);
            document.getElementById('error-message').textContent = '查询失败，请稍后再试';
            document.getElementById('query-form-container').style.display = 'none';
            document.getElementById('appointment-details').style.display = 'none';
            document.getElementById('query-error').style.display = 'block';
        })
        .finally(() => {
            // 恢复查询按钮
            queryBtn.disabled = false;
            queryBtn.textContent = '查询预约';
        });
}

// 加载通行码信息并显示整合的内容
function loadPassCodeAndDisplay(appointment, type) {
    // 构建查询参数
    const queryParams = new URLSearchParams({
        id: appointment.id,
        phone: originalPhone
    });
    
    // 发送请求获取通行码
    fetch(`../api/appointment/${type}/pass-code?${queryParams}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // 显示整合了通行码的预约详情
                displayAppointmentDetails(appointment, type, data);
            } else {
                console.error('获取通行码失败:', data.message);
                // 如果获取通行码失败，只显示预约信息
                displayAppointmentDetails(appointment, type, null);
            }
        })
        .catch(error => {
            console.error('获取通行码失败:', error);
            // 如果获取通行码失败，只显示预约信息
            displayAppointmentDetails(appointment, type, null);
        });
}

// 显示预约详情（整合通行码信息）
function displayAppointmentDetails(appointment, type, passCodeData) {
    // 设置状态标签
    const statusBadge = document.getElementById('appointment-status-badge');
    let statusText = '';
    let statusClass = '';
    
    switch (appointment.status) {
        case 'PENDING':
            statusText = '待审核';
            statusClass = 'status-pending';
            break;
        case 'APPROVED':
            statusText = '已批准';
            statusClass = 'status-approved';
            break;
        case 'REJECTED':
            statusText = '已拒绝';
            statusClass = 'status-rejected';
            break;
        case 'CANCELLED':
            statusText = '已取消';
            statusClass = 'status-cancelled';
            break;
        case 'COMPLETED':
            statusText = '已完成';
            statusClass = 'status-completed';
            break;
        default:
            statusText = '未知状态';
            statusClass = '';
    }
    
    statusBadge.innerHTML = `<span class="status-badge ${statusClass}">${statusText}</span>`;
    
    // 显示取消按钮（只有待审核和已批准的预约才能取消）
    const cancelBtn = document.getElementById('cancel-btn');
    if (appointment.status === 'PENDING' || appointment.status === 'APPROVED') {
        cancelBtn.style.display = 'block';
    } else {
        cancelBtn.style.display = 'none';
    }
    
    // 如果有通行码数据，显示整合的信息
    if (passCodeData) {
        displayIntegratedInfo(appointment, type, passCodeData);
    } else {
        displayBasicInfo(appointment, type);
    }
    
    // 显示随行人员信息
    displayCompanions(appointment.companions, type);
}

// 显示整合了通行码的信息
function displayIntegratedInfo(appointment, type, passCodeData) {
    const appointmentInfo = document.getElementById('appointment-info');
    appointmentInfo.innerHTML = '';
    
    // 计算通行码状态
    const now = new Date();
    const visitTime = new Date(passCodeData.visitTime);
    const hoursToVisit = (visitTime.getTime() - now.getTime()) / (1000 * 3600);
    
    let statusIcon, statusText, timeInfo;
    if (passCodeData.isValid) {
        statusIcon = '✅';
        statusText = '通行码有效';
        timeInfo = '通行码当前有效，可以使用';
    } else {
        statusIcon = '⏰';
        statusText = '通行码未激活';
        if (hoursToVisit > 24) {
            timeInfo = `通行码将在预约前24小时激活（还需等待约 ${Math.ceil(hoursToVisit - 24)} 小时）`;
        } else if (hoursToVisit >= -6) {
            timeInfo = '通行码应该已激活，如有问题请联系管理员';
        } else {
            timeInfo = '通行码已过期（预约时间后6小时失效）';
        }
    }
    
    // 脱敏处理
    const maskedName = maskName(passCodeData.name || passCodeData.nameMasked);
    const maskedIdCard = maskIdCard(passCodeData.idCard || passCodeData.idCardMasked);
    
    // 收集所有额外的预约信息
    const extraFields = [];
    
    if (type === 'public') {
        extraFields.push(
            { label: '单位/学校', value: appointment.organization },
            { label: '参观人数', value: appointment.visitors + '人' }
        );
    } else {
        extraFields.push(
            { label: '职务', value: appointment.officialTitle },
            { label: '单位名称', value: appointment.unitName },
            { label: '访问部门', value: appointment.visitDeptName },
            { label: '访问人数', value: appointment.visitors + '人' }
        );
        
        if (appointment.visitContact) {
            extraFields.push({ label: '接待人', value: appointment.visitContact });
        }
    }
    
    extraFields.push(
        { label: '预约目的', value: appointment.purpose, fullWidth: true },
        { label: '备注', value: appointment.remarks || '无', fullWidth: true }
    );
    
    // 添加状态相关信息
    if (appointment.status === 'REJECTED' && appointment.rejectReason) {
        extraFields.push({ label: '拒绝原因', value: appointment.rejectReason, fullWidth: true });
    }
    
    if (appointment.status === 'CANCELLED' && appointment.cancelReason) {
        extraFields.push({ label: '取消原因', value: appointment.cancelReason, fullWidth: true });
    }

    // 生成额外字段的HTML
    let extraFieldsHTML = '';
    extraFields.forEach(field => {
        const className = field.fullWidth ? 'full-width' : '';
        extraFieldsHTML += `<div class="${className}" style="padding: 8px; border-bottom: 1px solid #eee;"><strong>${field.label}：</strong>${field.value || '未设置'}</div>`;
    });

    // 创建整合信息的布局
    appointmentInfo.innerHTML = `
        <div style="grid-column: 1 / -1; margin-bottom: 20px;">
            <!-- 第一行：二维码 + 通行码状态 -->
            <div style="display: flex; gap: 20px; align-items: center; margin-bottom: 20px; padding: 15px; background: ${passCodeData.isValid ? '#e8f5e8' : '#f5f5f5'}; border-radius: 8px; border: 2px solid ${passCodeData.isValid ? '#8a2be2' : '#999'};">
                <div style="flex-shrink: 0; text-align: center;">
                    <div style="background: white; padding: 15px; border: 2px solid ${passCodeData.isValid ? '#8a2be2' : '#999'}; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                        <img src="${passCodeData.qrCodeBase64}" alt="通行码" style="width: 160px; height: 160px;">
                    </div>
                    <div style="margin-top: 8px; font-weight: 600; color: #666; font-size: 0.85em;">${passCodeData.isValid ? '扫码验证' : '暂不可用'}</div>
                </div>
                <div style="flex: 1;">
                    <h4 style="margin: 0 0 10px 0; color: #2c3e50; font-size: 1.3em;">校园通行码</h4>
                    <div style="font-size: 1.2em; font-weight: 600; color: ${passCodeData.isValid ? '#155724' : '#856404'};">${statusIcon} ${statusText}</div>
                </div>
            </div>
            
            <!-- 第二行：所有详细信息 -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; font-size: 0.95em;">
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>预约编号：</strong>${appointment.id}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>联系人：</strong>${maskedName}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>身份证号：</strong>${maskedIdCard}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>联系电话：</strong>${appointment.phone}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>预约校区：</strong>${passCodeData.campus || appointment.campus}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>预约时间：</strong>${formatDateTime(passCodeData.visitTime)}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>生成时间：</strong>${formatDateTime(passCodeData.generateTime)}</div>
                <div style="padding: 8px; border-bottom: 1px solid #eee;"><strong>有效时间：</strong>预约前24小时至预约后6小时</div>
                ${extraFieldsHTML}
            </div>
        </div>
    `;
}

// 显示基本预约信息（无通行码）
function displayBasicInfo(appointment, type) {
    const appointmentInfo = document.getElementById('appointment-info');
    appointmentInfo.innerHTML = '';
    
    // 基本信息字段
    const fields = [
        { label: '预约编号', value: appointment.id },
        { label: '联系人', value: appointment.name },
        { label: '联系电话', value: appointment.phone },
        { label: '预约时间', value: formatDateTime(appointment.visitTime) }
    ];
    
    // 根据类型添加特定字段
    if (type === 'public') {
        fields.push(
            { label: '单位/学校', value: appointment.organization },
            { label: '校区', value: appointment.campus },
            { label: '参观人数', value: appointment.visitors + '人' }
        );
    } else {
        fields.push(
            { label: '职务', value: appointment.officialTitle },
            { label: '单位名称', value: appointment.unitName },
            { label: '访问部门', value: appointment.visitDeptName },
            { label: '校区', value: appointment.campus },
            { label: '访问人数', value: appointment.visitors + '人' }
        );
        
        if (appointment.visitContact) {
            fields.push({ label: '接待人', value: appointment.visitContact });
        }
    }
    
    // 添加目的和备注
    fields.push(
        { label: '目的', value: appointment.purpose, fullWidth: true },
        { label: '备注', value: appointment.remarks || '无', fullWidth: true }
    );
    
    // 添加状态相关信息
    if (appointment.status === 'REJECTED' && appointment.rejectReason) {
        fields.push({ label: '拒绝原因', value: appointment.rejectReason, fullWidth: true });
    }
    
    if (appointment.status === 'CANCELLED' && appointment.cancelReason) {
        fields.push({ label: '取消原因', value: appointment.cancelReason, fullWidth: true });
    }
    
    // 渲染字段
    fields.forEach(field => {
        const infoItem = document.createElement('div');
        infoItem.className = `info-item ${field.fullWidth ? 'full-width' : ''}`;
        infoItem.innerHTML = `
            <div class="info-label">${field.label}</div>
            <div class="info-value">${field.value || '未设置'}</div>
        `;
        appointmentInfo.appendChild(infoItem);
    });
}

// 显示随行人员信息
function displayCompanions(companions, type) {
    const companionsSection = document.getElementById('companions-section');
    const companionsList = document.getElementById('companions-list');
    
    if (companions && companions.length > 0) {
        companionsSection.style.display = 'block';
        companionsList.innerHTML = '';
        
        companions.forEach((companion, index) => {
            const companionItem = document.createElement('div');
            companionItem.className = 'companion-item';
            
            let companionInfo = `${companion.idCard}`;
            if (companion.phone) {
                companionInfo += ` · ${companion.phone}`;
            }
            if (type === 'official' && companion.title) {
                companionInfo += ` · ${companion.title}`;
            }
            
            companionItem.innerHTML = `
                <div class="companion-name">${companion.name}</div>
                <div class="companion-info">${companionInfo}</div>
            `;
            companionsList.appendChild(companionItem);
        });
    } else {
        companionsSection.style.display = 'none';
    }
}

// 姓名脱敏函数
function maskName(name) {
    if (!name || name.length < 2) return name;
    
    if (name.length === 2) {
        // 两个字：李*
        return name[0] + '*';
    } else if (name.length === 3) {
        // 三个字：李*明
        return name[0] + '*' + name[2];
    } else {
        // 四个字或更多：李**华
        const stars = '*'.repeat(name.length - 2);
        return name[0] + stars + name[name.length - 1];
    }
}

// 身份证脱敏函数
function maskIdCard(idCard) {
    if (!idCard || idCard.length < 10) return idCard;
    
    // 显示前6位和后4位，中间用*替换
    const start = idCard.substring(0, 6);
    const end = idCard.substring(idCard.length - 4);
    const middle = '*'.repeat(idCard.length - 10);
    return start + middle + end;
}

// 格式化日期时间
function formatDateTime(timestamp) {
    if (!timestamp) return '未设置';
    
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
}

// 重置查询表单
function resetQuery() {
    document.getElementById('query-form-container').style.display = 'block';
    document.getElementById('appointment-details').style.display = 'none';
    document.getElementById('query-error').style.display = 'none';
    
    // 清空表单
    document.getElementById('appointment-id').value = '';
    document.getElementById('contact-phone').value = '';
    
    currentAppointment = null;
    originalPhone = '';
}

// 显示取消预约弹窗
function showCancelModal() {
    document.getElementById('cancel-modal').style.display = 'block';
}

// 关闭取消预约弹窗
function closeCancelModal() {
    document.getElementById('cancel-modal').style.display = 'none';
    document.getElementById('cancel-reason').value = '';
}

// 取消预约
function cancelAppointment() {
    if (!currentAppointment) {
        alert('无法获取当前预约信息');
        return;
    }
    
    const cancelReason = document.getElementById('cancel-reason').value.trim();
    if (!cancelReason) {
        alert('请填写取消原因');
        return;
    }
    
    // 准备请求数据
    const requestData = {
        id: currentAppointment.data.id,
        phone: originalPhone,
        cancelReason: cancelReason
    };
    
    // 发送取消请求
    fetch(`../api/appointment/${currentAppointment.type}/cancel`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('预约已成功取消');
            closeCancelModal();
            
            // 重新查询以更新状态
            document.getElementById('appointment-id').value = currentAppointment.data.id;
            document.getElementById('contact-phone').value = originalPhone;
            document.getElementById('appointment-type').value = currentAppointment.type;
            queryAppointment();
        } else {
            alert('取消预约失败：' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('取消预约失败:', error);
        alert('取消预约失败，请稍后再试');
    });
}