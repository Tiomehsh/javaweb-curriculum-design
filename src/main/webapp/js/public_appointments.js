// 全局变量
let currentAppointmentId = null;
let currentPage = 1;
let pageSize = 10;
let totalPages = 0;

// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    loadAppointments();
    
    // 监听审批状态变化
    const approvalStatus = document.getElementById('approval-status');
    if (approvalStatus) {
        approvalStatus.addEventListener('change', function() {
            const rejectReasonGroup = document.getElementById('reject-reason-group');
            if (this.value === '2') { // 拒绝
                rejectReasonGroup.style.display = 'block';
            } else {
                rejectReasonGroup.style.display = 'none';
            }
        });
    }
});

// 加载预约列表
function loadAppointments(searchTerm = '', statusFilter = '', dateFilter = '') {
    document.getElementById('appointments-table-body').innerHTML = '<tr><td colspan="8" style="text-align: center;">加载中...</td></tr>';
    
    // 构建查询参数
    let url = '../api/appointment/public/list';
    let queryParams = [];
    
    if (searchTerm) {
        queryParams.push(`search=${encodeURIComponent(searchTerm)}`);
    }
    
    if (statusFilter) {
        queryParams.push(`status=${encodeURIComponent(statusFilter)}`);
    }
    
    if (dateFilter) {
        queryParams.push(`date=${encodeURIComponent(dateFilter)}`);
    }
    
    queryParams.push(`page=${currentPage}`);
    queryParams.push(`size=${pageSize}`);
    
    if (queryParams.length > 0) {
        url += '?' + queryParams.join('&');
    }
    
    // 发送请求
    fetch(url)
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('appointments-table-body');
            tbody.innerHTML = '';
            
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align: center;">暂无预约数据</td></tr>';
                return;
            }
            
            // 假设后端返回了总页数
            // totalPages = data.totalPages;
            totalPages = Math.ceil(data.length / pageSize);
            
            // 显示预约数据
            data.forEach(appointment => {
                const tr = document.createElement('tr');
                
                // 构建状态标签
                let statusHtml = '';
                switch(appointment.status) {
                    case 'PENDING':
                        statusHtml = '<span class="status-badge status-pending">待审核</span>';
                        break;
                    case 'APPROVED':
                        statusHtml = '<span class="status-badge status-approved">已批准</span>';
                        break;
                    case 'REJECTED':
                        statusHtml = '<span class="status-badge status-rejected">已拒绝</span>';
                        break;
                    case 'CANCELLED':
                        statusHtml = '<span class="status-badge status-cancelled">已取消</span>';
                        break;
                    case 'COMPLETED':
                        statusHtml = '<span class="status-badge status-completed">已完成</span>';
                        break;
                    default:
                        statusHtml = appointment.status;
                }
                
                // 构建操作按钮
                let actionsHtml = `<button class="btn btn-sm" onclick="viewAppointment(${appointment.appointmentId})">查看</button>`;
                
                if (appointment.status === 'PENDING') {
                    actionsHtml += `<button class="btn btn-sm btn-primary" onclick="openApprovalModal(${appointment.appointmentId})">审批</button>`;
                } else if (appointment.status === 'APPROVED') {
                    actionsHtml += `<button class="btn btn-sm btn-success" onclick="openCompleteModal(${appointment.appointmentId})">完成</button>`;
                }
                
                // 构建表格行
                tr.innerHTML = `
                    <td>${appointment.appointmentId}</td>
                    <td>${appointment.name}</td>
                    <td>${appointment.phone || '-'}</td>
                    <td>${appointment.organization || '-'}</td>
                    <td>${formatDateTime(appointment.visitTime)}</td>
                    <td>${appointment.visitors || 1}</td>
                    <td>${statusHtml}</td>
                    <td>${actionsHtml}</td>
                `;
                
                tbody.appendChild(tr);
            });
            
            // 更新分页控件
            updatePagination();
        })
        .catch(error => {
            console.error('加载预约列表失败:', error);
            document.getElementById('appointments-table-body').innerHTML = 
                '<tr><td colspan="8" style="text-align: center;">加载失败，请刷新重试</td></tr>';
        });
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

// 更新分页控件
function updatePagination() {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';
    
    if (totalPages <= 1) {
        return;
    }
    
    // 上一页按钮
    const prevButton = document.createElement('button');
    prevButton.className = 'btn' + (currentPage === 1 ? ' disabled' : '');
    prevButton.textContent = '上一页';
    prevButton.disabled = currentPage === 1;
    prevButton.addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            loadAppointments(
                document.getElementById('search-input').value,
                document.getElementById('status-filter').value,
                document.getElementById('date-filter').value
            );
        }
    });
    pagination.appendChild(prevButton);
    
    // 页码按钮
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);
    
    if (endPage - startPage < 4) {
        startPage = Math.max(1, endPage - 4);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageButton = document.createElement('button');
        pageButton.className = 'btn' + (i === currentPage ? ' btn-primary' : '');
        pageButton.textContent = i;
        pageButton.addEventListener('click', () => {
            currentPage = i;
            loadAppointments(
                document.getElementById('search-input').value,
                document.getElementById('status-filter').value,
                document.getElementById('date-filter').value
            );
        });
        pagination.appendChild(pageButton);
    }
    
    // 下一页按钮
    const nextButton = document.createElement('button');
    nextButton.className = 'btn' + (currentPage === totalPages ? ' disabled' : '');
    nextButton.textContent = '下一页';
    nextButton.disabled = currentPage === totalPages;
    nextButton.addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            loadAppointments(
                document.getElementById('search-input').value,
                document.getElementById('status-filter').value,
                document.getElementById('date-filter').value
            );
        }
    });
    pagination.appendChild(nextButton);
}

// 搜索预约
function searchAppointments() {
    const searchTerm = document.getElementById('search-input').value;
    currentPage = 1;
    loadAppointments(
        searchTerm,
        document.getElementById('status-filter').value,
        document.getElementById('date-filter').value
    );
}

// 筛选预约
function filterAppointments() {
    const statusFilter = document.getElementById('status-filter').value;
    const dateFilter = document.getElementById('date-filter').value;
    currentPage = 1;
    loadAppointments(
        document.getElementById('search-input').value,
        statusFilter,
        dateFilter
    );
}

// 查看预约详情
function viewAppointment(appointmentId) {
    // 发送请求获取预约详情
    fetch(`../api/appointment/public/${appointmentId}`)
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
                            <th>申请时间</th>
                            <td>${formatDateTime(appointment.applyTime)}</td>
                        </tr>
                        <tr>
                            <th>状态</th>
                            <td colspan="3">
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
            
            // 如果有拒绝原因，显示拒绝原因
            if (appointment.status === 'REJECTED' && appointment.rejectReason) {
                detailsHtml += `<br><small>拒绝原因: ${appointment.rejectReason}</small>`;
            }
            
            // 如果有取消原因，显示取消原因
            if (appointment.status === 'CANCELLED' && appointment.cancelReason) {
                detailsHtml += `<br><small>取消原因: ${appointment.cancelReason}</small>`;
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
                    <h4>参观信息</h4>
                    <table class="detail-table">
                        <tr>
                            <th>参观校区</th>
                            <td>${appointment.campus || '-'}</td>
                            <th>参观时间</th>
                            <td>${formatDateTime(appointment.visitTime)}</td>
                        </tr>
                        <tr>
                            <th>参观人数</th>
                            <td>${appointment.visitors || 1}</td>
                            <th>参观目的</th>
                            <td>${appointment.purpose || '-'}</td>
                        </tr>
                        <tr>
                            <th>备注</th>
                            <td colspan="3">${appointment.remarks || '-'}</td>
                        </tr>
                    </table>
                </div>
            `;
            
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
            
            // 更新操作按钮
            const actionButtons = document.getElementById('action-buttons');
            actionButtons.innerHTML = '';
            
            if (appointment.status === 'PENDING') {
                const approveButton = document.createElement('button');
                approveButton.className = 'btn btn-primary';
                approveButton.textContent = '审批';
                approveButton.onclick = function() {
                    closeModal('appointment-modal');
                    openApprovalModal(appointment.appointmentId);
                };
                actionButtons.appendChild(approveButton);
            } else if (appointment.status === 'APPROVED') {
                const completeButton = document.createElement('button');
                completeButton.className = 'btn btn-success';
                completeButton.textContent = '标记完成';
                completeButton.onclick = function() {
                    closeModal('appointment-modal');
                    openCompleteModal(appointment.appointmentId);
                };
                actionButtons.appendChild(completeButton);
            }
            
            // 显示预约详情弹窗
            openModal('appointment-modal');
            
            // 保存当前预约ID
            currentAppointmentId = appointmentId;
        })
        .catch(error => {
            console.error('获取预约详情失败:', error);
            alert('获取预约详情失败，请稍后再试');
        });
}

// 打开审批弹窗
function openApprovalModal(appointmentId) {
    document.getElementById('approval-status').value = '1'; // 默认选择批准
    document.getElementById('reject-reason-group').style.display = 'none';
    document.getElementById('reject-reason').value = '';
    document.getElementById('approval-remarks').value = '';
    
    currentAppointmentId = appointmentId;
    openModal('approval-modal');
}

// 提交审批
function submitApproval() {
    const status = document.getElementById('approval-status').value;
    const isReject = status === '2';
    const rejectReason = isReject ? document.getElementById('reject-reason').value : null;
    const remarks = document.getElementById('approval-remarks').value;
    
    if (isReject && !rejectReason) {
        alert('请输入拒绝原因');
        return;
    }
    
    // 构建请求数据
    const requestData = {
        rejectReason: rejectReason,
        remarks: remarks
    };
    
    // 发送审批请求
    fetch(`../api/appointment/public/${isReject ? 'reject' : 'approve'}/${currentAppointmentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(isReject ? '已拒绝预约' : '已批准预约');
            closeModal('approval-modal');
            loadAppointments(
                document.getElementById('search-input').value,
                document.getElementById('status-filter').value,
                document.getElementById('date-filter').value
            );
        } else {
            alert('操作失败: ' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('提交审批失败:', error);
        alert('提交审批失败，请稍后再试');
    });
}

// 打开完成预约弹窗
function openCompleteModal(appointmentId) {
    document.getElementById('complete-remarks').value = '';
    
    currentAppointmentId = appointmentId;
    openModal('complete-modal');
}

// 确认完成预约
function confirmComplete() {
    const remarks = document.getElementById('complete-remarks').value;
    
    // 构建请求数据
    const requestData = {
        remarks: remarks
    };
    
    // 发送完成预约请求
    fetch(`../api/appointment/public/complete/${currentAppointmentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('预约已标记为完成');
            closeModal('complete-modal');
            loadAppointments(
                document.getElementById('search-input').value,
                document.getElementById('status-filter').value,
                document.getElementById('date-filter').value
            );
        } else {
            alert('操作失败: ' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('完成预约失败:', error);
        alert('完成预约失败，请稍后再试');
    });
}

// 注意：弹窗打开和关闭功能已由modalUtils.js提供，不再需要在这里定义

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
