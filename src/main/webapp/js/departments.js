// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    loadDepartments();
});

// 获取上下文路径
function getContextPath() {
    return window.contextPath || '';
}

// 加载部门列表
function loadDepartments() {
    // 显示加载状态
    showLoadingState();
    
    fetch(`${getContextPath()}/api/department/list`)
        .then(response => {
            console.log('API响应状态:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('API响应数据:', data);
            if (data.success) {
                displayDepartments(data.departments || []);
            } else {
                showError('加载部门列表失败: ' + (data.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('加载部门列表失败:', error);
            showError('加载部门列表失败: ' + error.message);
        });
}

// 显示加载状态
function showLoadingState() {
    const containers = ['admin-departments', 'direct-departments', 'college-departments'];
    containers.forEach(containerId => {
        const container = document.getElementById(containerId);
        container.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>加载中...</p></div>';
    });
    
    // 重置计数
    document.getElementById('admin-count').textContent = '0';
    document.getElementById('direct-count').textContent = '0';
    document.getElementById('college-count').textContent = '0';
}

// 显示部门列表
function displayDepartments(departments) {
    // 按部门类型分组
    const adminDepts = departments.filter(dept => dept.deptType === '行政部门');
    const directDepts = departments.filter(dept => dept.deptType === '直属部门');
    const collegeDepts = departments.filter(dept => dept.deptType === '学院');
    
    // 显示各类型部门
    displayDepartmentGroup('admin-departments', adminDepts, 'admin-count');
    displayDepartmentGroup('direct-departments', directDepts, 'direct-count');
    displayDepartmentGroup('college-departments', collegeDepts, 'college-count');
}

// 显示部门分组
function displayDepartmentGroup(containerId, departments, countId) {
    const container = document.getElementById(containerId);
    const countElement = document.getElementById(countId);
    
    // 更新计数
    countElement.textContent = departments.length;
    
    if (departments.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-building"></i>
                <p>暂无部门数据</p>
            </div>
        `;
        return;
    }
    
    // 生成部门卡片
    container.innerHTML = departments.map(dept => createDepartmentCard(dept)).join('');
}

// 创建部门卡片
function createDepartmentCard(department) {
    const contactPerson = department.contactPerson || '未设置';
    const contactPhone = department.contactPhone || '未设置';
    
    return `
        <div class="department-card">
            <div class="card-header">
                <h3 class="card-title">${department.deptName}</h3>
                <span class="card-type">${department.deptType}</span>
            </div>
            
            <div class="card-info">
                <div class="info-item">
                    <i class="fas fa-user"></i>
                    <span>联系人：${contactPerson}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-phone"></i>
                    <span>联系电话：${contactPhone}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-calendar"></i>
                    <span>创建时间：${formatDateTime(department.createTime)}</span>
                </div>
            </div>
            
            <div class="card-actions">
                <button class="btn btn-edit" onclick="editDepartment(${department.deptId})">
                    <i class="fas fa-edit"></i>
                    编辑
                </button>
                <button class="btn btn-delete" onclick="deleteDepartment(${department.deptId})">
                    <i class="fas fa-trash"></i>
                    删除
                </button>
            </div>
        </div>
    `;
}

// 格式化日期时间
function formatDateTime(timestamp) {
    if (!timestamp) return '未知';
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 打开添加部门弹窗
function openAddDepartmentModal() {
    // 重置表单
    document.getElementById('add-department-form').reset();
    
    // 打开弹窗
    openModal('add-department-modal');
}

// 添加部门
function addDepartment() {
    const deptType = document.getElementById('add-dept-type').value;
    const deptName = document.getElementById('add-dept-name').value.trim();
    const contactPerson = document.getElementById('add-contact-person').value.trim();
    const contactPhone = document.getElementById('add-contact-phone').value.trim();
    
    // 表单验证
    if (!deptType) {
        showAlert('请选择部门类型', 'warning');
        return;
    }
    
    if (!deptName) {
        showAlert('请输入部门名称', 'warning');
        return;
    }
    
    const departmentData = {
        deptType: deptType,
        deptName: deptName,
        contactPerson: contactPerson || null,
        contactPhone: contactPhone || null
    };
    
    fetch(`${getContextPath()}/api/department/add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(departmentData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('添加部门成功', 'success');
            closeModal('add-department-modal');
            loadDepartments();
        } else {
            showAlert('添加部门失败: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('添加部门失败:', error);
        showAlert('添加部门失败，请稍后再试', 'error');
    });
}

// 编辑部门
function editDepartment(deptId) {
    fetch(`${getContextPath()}/api/department/detail?id=${deptId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const department = data.department;
                
                // 填充表单数据
                document.getElementById('edit-dept-id').value = department.deptId;
                document.getElementById('edit-dept-type').value = department.deptType;
                document.getElementById('edit-dept-name').value = department.deptName;
                document.getElementById('edit-contact-person').value = department.contactPerson || '';
                document.getElementById('edit-contact-phone').value = department.contactPhone || '';
                
                // 打开弹窗
                openModal('edit-department-modal');
            } else {
                showAlert('获取部门详情失败: ' + data.message, 'error');
            }
        })
        .catch(error => {
            console.error('获取部门详情失败:', error);
            showAlert('获取部门详情失败，请稍后再试', 'error');
        });
}

// 更新部门
function updateDepartment() {
    const deptId = document.getElementById('edit-dept-id').value;
    const deptType = document.getElementById('edit-dept-type').value;
    const deptName = document.getElementById('edit-dept-name').value.trim();
    const contactPerson = document.getElementById('edit-contact-person').value.trim();
    const contactPhone = document.getElementById('edit-contact-phone').value.trim();
    
    // 表单验证
    if (!deptType) {
        showAlert('请选择部门类型', 'warning');
        return;
    }
    
    if (!deptName) {
        showAlert('请输入部门名称', 'warning');
        return;
    }
    
    const departmentData = {
        deptId: parseInt(deptId),
        deptType: deptType,
        deptName: deptName,
        contactPerson: contactPerson || null,
        contactPhone: contactPhone || null
    };
    
    fetch(`${getContextPath()}/api/department/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(departmentData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('更新部门成功', 'success');
            closeModal('edit-department-modal');
            loadDepartments();
        } else {
            showAlert('更新部门失败: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('更新部门失败:', error);
        showAlert('更新部门失败，请稍后再试', 'error');
    });
}

// 删除部门
function deleteDepartment(deptId) {
    if (!confirm('确定要删除该部门吗？此操作不可恢复！')) {
        return;
    }
    
    fetch(`${getContextPath()}/api/department/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ deptId: deptId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('删除部门成功', 'success');
            loadDepartments();
        } else {
            showAlert('删除部门失败: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('删除部门失败:', error);
        showAlert('删除部门失败，请稍后再试', 'error');
    });
}


// 显示提示消息
function showAlert(message, type = 'info') {
    // 创建提示元素
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `
        <i class="fas fa-${getAlertIcon(type)}"></i>
        <span>${message}</span>
        <button class="alert-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // 添加到页面
    document.body.appendChild(alertDiv);
    
    // 自动移除
    setTimeout(() => {
        if (alertDiv.parentElement) {
            alertDiv.remove();
        }
    }, 5000);
}

// 获取提示图标
function getAlertIcon(type) {
    const icons = {
        success: 'check-circle',
        error: 'exclamation-circle',
        warning: 'exclamation-triangle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

// 显示错误消息
function showError(message) {
    showAlert(message, 'error');
}

// 退出登录
function logout() {
    if (!confirm('确定要退出登录吗？')) {
        return;
    }
    
    fetch(`${getContextPath()}/api/admin/logout`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.location.href = 'login.jsp';
        } else {
            showAlert('退出登录失败', 'error');
        }
    })
    .catch(error => {
        console.error('退出登录失败:', error);
        showAlert('退出登录失败，请稍后再试', 'error');
    });
}
