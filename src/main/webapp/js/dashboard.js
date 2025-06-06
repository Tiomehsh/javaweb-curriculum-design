// 加载控制面板数据
function loadDashboardData() {
    // 加载今日公众预约数
    fetch('../api/appointment/public/count?today=true')
        .then(response => response.json())
        .then(data => {
            document.getElementById('public-count').textContent = data.count;
        })
        .catch(error => {
            console.error('加载今日公众预约数失败:', error);
            document.getElementById('public-count').textContent = '加载失败';
        });
        
    // 加载今日公务预约数
    fetch('../api/appointment/official/count?today=true')
        .then(response => response.json())
        .then(data => {
            document.getElementById('official-count').textContent = data.count;
        })
        .catch(error => {
            console.error('加载今日公务预约数失败:', error);
            document.getElementById('official-count').textContent = '加载失败';
        });
        
    // 加载待处理预约数
    fetch('../api/appointment/count?status=PENDING')
        .then(response => response.json())
        .then(data => {
            document.getElementById('pending-count').textContent = data.count;
        })
        .catch(error => {
            console.error('加载待处理预约数失败:', error);
            document.getElementById('pending-count').textContent = '加载失败';
        });
        
    // 加载本月总预约数
    fetch('../api/appointment/count?month=true')
        .then(response => response.json())
        .then(data => {
            document.getElementById('monthly-count').textContent = data.count;
        })
        .catch(error => {
            console.error('加载本月总预约数失败:', error);
            document.getElementById('monthly-count').textContent = '加载失败';
        });
        
    // 加载最近预约
    fetch('../api/appointment/recent')
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('recent-appointments-data');
            tbody.innerHTML = '';
            
            if (data.length === 0) {
                const tr = document.createElement('tr');
                tr.innerHTML = '<td colspan="6" style="text-align: center;">暂无预约数据</td>';
                tbody.appendChild(tr);
                return;
            }
            
            data.forEach(appointment => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>#${appointment.appointmentId || appointment.id}</td>
                    <td><span class="appointment-type ${appointment.type === 'PUBLIC' ? 'type-public' : 'type-official'}">${appointment.type === 'PUBLIC' ? '公众预约' : '公务预约'}</span></td>
                    <td>${appointment.name}</td>
                    <td>${formatDateTime(appointment.visitTime)}</td>
                    <td>${getStatusBadge(appointment.status)}</td>
                    <td>
                        <button class="view-btn" onclick="viewAppointment(${appointment.id}, '${appointment.type}')">查看</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error('加载最近预约失败:', error);
            const tbody = document.getElementById('recent-appointments-data');
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">加载失败</td></tr>';
        });
}

// 获取状态文本
function getStatusText(status) {
    switch(status) {
        case 'PENDING': return '<span style="color: #f57c00;">待审核</span>';
        case 'APPROVED': return '<span style="color: #43a047;">已批准</span>';
        case 'REJECTED': return '<span style="color: #e53935;">已拒绝</span>';
        case 'CANCELLED': return '<span style="color: #757575;">已取消</span>';
        case 'COMPLETED': return '<span style="color: #1e88e5;">已完成</span>';
        default: return status;
    }
}

// 获取状态徽章
function getStatusBadge(status) {
    switch(status) {
        case 'PENDING': return '<span class="status-badge status-pending">待审核</span>';
        case 'APPROVED': return '<span class="status-badge status-approved">已批准</span>';
        case 'REJECTED': return '<span class="status-badge status-rejected">已拒绝</span>';
        case 'CANCELLED': return '<span class="status-badge status-cancelled">已取消</span>';
        case 'COMPLETED': return '<span class="status-badge status-completed">已完成</span>';
        default: return status;
    }
}

// 格式化日期时间
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    
    const date = new Date(dateTimeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 查看预约详情
function viewAppointment(appointmentId, type) {
    const apiUrl = type === 'PUBLIC' ? `../api/appointment/public/${appointmentId}` : `../api/appointment/official/${appointmentId}`;
    
    // 发送请求获取预约详情
    fetch(apiUrl)
        .then(response => response.json())
        .then(appointment => {
            const detailsContainer = document.getElementById('appointment-details');
            
            // 构建预约详情HTML
            let detailsHtml = `
                <div class="detail-section">
                    <h4>基本信息</h4>
                    <table class="detail-table">
                        <tr>
                            <th>预约编号</th>
                            <td>${appointment.appointmentId}</td>
                            <th>预约类型</th>
                            <td>${type === 'PUBLIC' ? '公众预约' : '公务预约'}</td>
                        </tr>
                        <tr>
                            <th>申请时间</th>
                            <td>${formatDateTime(appointment.applyTime)}</td>
                            <th>状态</th>
                            <td>
            `;
            
            // 添加状态信息
            switch(appointment.status) {
                case 'PENDING':
                    detailsHtml += '<span class="status-badge status-pending">待审核</span>';
                    break;
                case 'APPROVED':
                    detailsHtml += '<span class="status-badge status-approved">已批准</span>';
                    break;
                case 'REJECTED':
                    detailsHtml += '<span class="status-badge status-rejected">已拒绝</span>';
                    break;
                case 'CANCELLED':
                    detailsHtml += '<span class="status-badge status-cancelled">已取消</span>';
                    break;
                case 'COMPLETED':
                    detailsHtml += '<span class="status-badge status-completed">已完成</span>';
                    break;
                default:
                    detailsHtml += appointment.status;
            }
            
            detailsHtml += `
                            </td>
                        </tr>
                    </table>
                </div>
                
                <div class="detail-section">
                    <h4>预约人信息</h4>
                    <table class="detail-table">
                        <tr>
                            <th>姓名</th>
                            <td>${appointment.name}</td>
                            <th>身份证号</th>
                            <td>${appointment.idCardMasked || '-'}</td>
                        </tr>
                        <tr>
                            <th>联系电话</th>
                            <td>${appointment.phone || '-'}</td>
                            <th>单位/学校</th>
                            <td>${appointment.organization || '-'}</td>
                        </tr>
                    </table>
                </div>
                
                <div class="detail-section">
                    <h4>访问信息</h4>
                    <table class="detail-table">
                        <tr>
                            <th>访问校区</th>
                            <td>${appointment.campus || '-'}</td>
                            <th>访问时间</th>
                            <td>${formatDateTime(appointment.visitTime)}</td>
                        </tr>
                        <tr>
                            <th>访问人数</th>
                            <td>${appointment.visitors || 1}</td>
                            <th>交通方式</th>
                            <td>${appointment.transportation || '-'}</td>
                        </tr>
            `;
            
            // 如果有车牌号，显示车牌号
            if (appointment.plateNumber) {
                detailsHtml += `
                        <tr>
                            <th>车牌号</th>
                            <td colspan="3">${appointment.plateNumber}</td>
                        </tr>
                `;
            }
            
            detailsHtml += `
                        <tr>
                            <th>访问目的</th>
                            <td colspan="3">${appointment.purpose || '-'}</td>
                        </tr>
                    </table>
                </div>
            `;
            
            // 公务预约特有信息
            if (type === 'OFFICIAL') {
                detailsHtml += `
                    <div class="detail-section">
                        <h4>公务信息</h4>
                        <table class="detail-table">
                            <tr>
                                <th>访问部门</th>
                                <td>${appointment.visitDeptName || '-'}</td>
                                <th>接待人</th>
                                <td>${appointment.visitContact || '-'}</td>
                            </tr>
                `;
                
                if (appointment.status === 'APPROVED' && appointment.approverName) {
                    detailsHtml += `
                            <tr>
                                <th>审批人</th>
                                <td>${appointment.approverName}</td>
                                <th>审批时间</th>
                                <td>${formatDateTime(appointment.approveTime)}</td>
                            </tr>
                    `;
                }
                
                detailsHtml += `
                        </table>
                    </div>
                `;
            }
            
            // 如果有随行人员，显示随行人员信息
            if (appointment.companions && appointment.companions.length > 0) {
                detailsHtml += `
                    <div class="detail-section">
                        <h4>随行人员（${appointment.companions.length}人）</h4>
                        <table class="detail-table">
                            <tr>
                                <th>姓名</th>
                                <th>身份证号</th>
                                <th>联系电话</th>
                            </tr>
                `;
                
                appointment.companions.forEach(companion => {
                    detailsHtml += `
                        <tr>
                            <td>${companion.name}</td>
                            <td>${companion.idCardMasked || '-'}</td>
                            <td>${companion.phone || '-'}</td>
                        </tr>
                    `;
                });
                
                detailsHtml += `
                        </table>
                    </div>
                `;
            }
            
            // 更新预约详情
            detailsContainer.innerHTML = detailsHtml;
            
            // 显示预约详情弹窗
            openModal('appointment-modal');
        })
        .catch(error => {
            console.error('获取预约详情失败:', error);
            alert('获取预约详情失败，请稍后再试');
        });
}

// 退出登录
function logout() {
    fetch('../api/admin/logout', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.location.href = 'login.jsp';
        }
    })
    .catch(error => {
        console.error('退出登录失败:', error);
        alert('退出登录失败，请稍后再试');
    });
}

// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
}); 