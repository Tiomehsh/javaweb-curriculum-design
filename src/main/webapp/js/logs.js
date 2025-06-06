// 全局变量
let currentPage = 1;
let pageSize = 10;
let totalPages = 0;
let currentLog = null;

// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    loadLogs();
    
    // 设置筛选器的变化事件
    document.getElementById('type-filter').addEventListener('change', filterLogs);
    document.getElementById('date-filter').addEventListener('change', filterLogs);
});

// 获取上下文路径
function getContextPath() {
    return window.contextPath || '';
}

// 加载日志列表
function loadLogs() {
    const typeFilter = document.getElementById('type-filter').value;
    const dateFilter = document.getElementById('date-filter').value;
    const searchQuery = document.getElementById('search-input').value;
    
    let url = `${getContextPath()}/api/log/list?page=${currentPage}&size=${pageSize}`;
    
    if (typeFilter) {
        url += `&type=${typeFilter}`;
    }
    
    if (dateFilter) {
        url += `&date=${dateFilter}`;
    }
    
    if (searchQuery) {
        url += `&query=${encodeURIComponent(searchQuery)}`;
    }
    
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayLogs(data.logs);
                totalPages = Math.ceil(data.total / pageSize);
                updatePagination();
            } else {
                showError('加载日志列表失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('加载日志列表失败:', error);
            showError('加载日志列表失败，请稍后再试');
        });
}

// 显示日志列表
function displayLogs(logs) {
    const tableBody = document.getElementById('logs-table-body');
    tableBody.innerHTML = '';
    
    if (!logs || logs.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="7" style="text-align: center;">暂无日志数据</td>';
        tableBody.appendChild(row);
        return;
    }
    
    logs.forEach(log => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${log.logId}</td>
            <td>${getLogTypeHtml(log.operation)}</td>
            <td>系统管理员</td>
            <td>${log.description}</td>
            <td>${log.ipAddress}</td>
            <td>${formatDateTime(log.operationTime)}</td>
            <td>
                <button class="btn btn-sm" onclick="viewLog(${log.logId})">详情</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// 搜索日志
function searchLogs() {
    currentPage = 1;
    loadLogs();
}

// 筛选日志
function filterLogs() {
    currentPage = 1;
    loadLogs();
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
    prevButton.textContent = '上一页';
    prevButton.className = 'btn btn-sm';
    prevButton.disabled = currentPage === 1;
    prevButton.onclick = () => {
        if (currentPage > 1) {
            currentPage--;
            loadLogs();
        }
    };
    pagination.appendChild(prevButton);
    
    // 页码按钮
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, startPage + 4);
    
    for (let i = startPage; i <= endPage; i++) {
        const pageButton = document.createElement('button');
        pageButton.textContent = i;
        pageButton.className = i === currentPage ? 'btn btn-sm btn-primary' : 'btn btn-sm';
        pageButton.onclick = () => {
            currentPage = i;
            loadLogs();
        };
        pagination.appendChild(pageButton);
    }
    
    // 下一页按钮
    const nextButton = document.createElement('button');
    nextButton.textContent = '下一页';
    nextButton.className = 'btn btn-sm';
    nextButton.disabled = currentPage === totalPages;
    nextButton.onclick = () => {
        if (currentPage < totalPages) {
            currentPage++;
            loadLogs();
        }
    };
    pagination.appendChild(nextButton);
}

// 查看日志详情
function viewLog(id) {
    fetch(`${getContextPath()}/api/log/detail?id=${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                currentLog = data.log;
                displayLogDetails(data.log);
                openModal('log-modal');
            } else {
                showError('获取日志详情失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('获取日志详情失败:', error);
            showError('获取日志详情失败，请稍后再试');
        });
}

// 显示日志详情
function displayLogDetails(log) {
    const detailsContainer = document.getElementById('log-details');
    
    // 清空容器
    detailsContainer.innerHTML = '';
    
    // 创建基本信息部分
    const basicInfo = document.createElement('div');
    basicInfo.className = 'detail-section';
    basicInfo.innerHTML = `
        <h4>基本信息</h4>
        <table class="detail-table">
            <tr>
                <td>日志ID:</td>
                <td>${log.logId}</td>
                <td>操作类型:</td>
                <td>${getLogTypeHtml(log.operation)}</td>
            </tr>
            <tr>
                <td>操作人:</td>
                <td>系统管理员</td>
                <td>操作人ID:</td>
                <td>${log.adminId}</td>
            </tr>
            <tr>
                <td>IP地址:</td>
                <td>${log.ipAddress}</td>
                <td>操作时间:</td>
                <td>${formatDateTime(log.operationTime)}</td>
            </tr>
        </table>
    `;
    
    // 创建操作内容部分
    const contentSection = document.createElement('div');
    contentSection.className = 'detail-section';
    contentSection.innerHTML = `
        <h4>操作内容</h4>
        <p>${log.description}</p>
    `;
    
    // 创建详细数据部分（如果有）
    if (log.logHash) {
        const detailSection = document.createElement('div');
        detailSection.className = 'detail-section';
        
        detailSection.innerHTML = `
            <h4>日志哈希</h4>
            <p>${log.logHash}</p>
        `;
        
        detailsContainer.appendChild(detailSection);
    }
    
    // 添加所有部分到容器
    detailsContainer.appendChild(basicInfo);
    detailsContainer.appendChild(contentSection);
}

// 格式化详细数据的值
function formatDetailValue(value) {
    if (value === null || value === undefined) {
        return '无';
    }
    
    if (typeof value === 'object') {
        return JSON.stringify(value);
    }
    
    return value.toString();
}

// 获取日志类型HTML
function getLogTypeHtml(type) {
    let typeName;
    let typeClass;
    
    switch (type) {
        case 'LOGIN':
            typeName = '登录';
            typeClass = 'login';
            break;
        case 'LOGOUT':
            typeName = '登出';
            typeClass = 'logout';
            break;
        case 'ADD':
            typeName = '添加';
            typeClass = 'add';
            break;
        case 'UPDATE':
            typeName = '更新';
            typeClass = 'update';
            break;
        case 'DELETE':
            typeName = '删除';
            typeClass = 'delete';
            break;
        case 'APPROVE':
            typeName = '审批';
            typeClass = 'approve';
            break;
        case 'REJECT':
            typeName = '拒绝';
            typeClass = 'reject';
            break;
        case 'COMPLETE':
            typeName = '完成';
            typeClass = 'complete';
            break;
        default:
            typeName = type;
            typeClass = '';
    }
    
    return `<span class="log-type log-type-${typeClass}">${typeName}</span>`;
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
        second: '2-digit',
        hour12: false
    });
}

// 注意：弹窗打开和关闭功能已由modalUtils.js提供，不再需要在这里定义

// 显示错误消息
function showError(message) {
    alert(message);
}

// 退出登录
function logout() {
    fetch(`${getContextPath()}/api/admin/logout`, {
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
