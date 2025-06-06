// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    loadAdmins();
    loadDepartments();
});

// 获取上下文路径
function getContextPath() {
    return window.contextPath || '';
}

// 加载管理员列表
function loadAdmins() {
    fetch(`${getContextPath()}/api/admin/list`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayAdmins(data.admins);
            } else {
                showError('加载管理员列表失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('加载管理员列表失败:', error);
            showError('加载管理员列表失败，请稍后再试');
        });
}

// 加载部门列表
function loadDepartments() {
    fetch(`${getContextPath()}/api/department/list`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const departments = data.departments;
                
                // 添加到添加管理员弹窗的部门选择器
                const addDeptSelect = document.getElementById('add-dept-id');
                addDeptSelect.innerHTML = '<option value="">请选择部门</option>';
                departments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.deptId;
                    option.textContent = dept.deptName;
                    addDeptSelect.appendChild(option);
                });
                
                // 添加到编辑管理员弹窗的部门选择器
                const editDeptSelect = document.getElementById('edit-dept-id');
                editDeptSelect.innerHTML = '<option value="">请选择部门</option>';
                departments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.deptId;
                    option.textContent = dept.deptName;
                    editDeptSelect.appendChild(option);
                });
            } else {
                showError('加载部门列表失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('加载部门列表失败:', error);
            showError('加载部门列表失败，请稍后再试');
        });
}

// 显示管理员列表
function displayAdmins(admins) {
    const tableBody = document.getElementById('admins-table-body');
    tableBody.innerHTML = '';
    
    if (!admins || admins.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="7" style="text-align: center;">暂无管理员数据</td>';
        tableBody.appendChild(row);
        return;
    }
    
    // 部门映射（id -> 名称）
    const deptMap = {};
    
    // 获取部门名称映射
    fetch(`${getContextPath()}/api/department/list`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                data.departments.forEach(dept => {
                    deptMap[dept.deptId] = dept.deptName;
                });
                
                // 渲染管理员列表
                admins.forEach(admin => {
                    const row = document.createElement('tr');
                    
                    // 获取部门名称（如果有）
                    const deptName = admin.deptId ? (deptMap[admin.deptId] || '未知部门') : '-';
                    
                    // 角色显示名称和样式
                    let roleName, roleClass;
                    switch (admin.role) {
                        case 'SYSTEM_ADMIN':
                            roleName = '系统管理员';
                            roleClass = 'role-system';
                            break;
                        case 'DEPARTMENT_ADMIN':
                            roleName = '部门管理员';
                            roleClass = 'role-department';
                            break;
                        case 'RECEPTION_ADMIN':
                            roleName = '接待管理员';
                            roleClass = 'role-reception';
                            break;
                        case 'AUDIT_ADMIN':
                            roleName = '审计管理员';
                            roleClass = 'role-audit';
                            break;
                        default:
                            roleName = admin.role;
                            roleClass = '';
                    }
                    
                    row.innerHTML = `
                        <td>${admin.adminId}</td>
                        <td>${admin.loginName}</td>
                        <td>${admin.realName}</td>
                        <td><span class="role-badge ${roleClass}">${roleName}</span></td>
                        <td>${deptName}</td>
                        <td>${admin.status === 1 ? '<span class="status-badge status-active">启用</span>' : '<span class="status-badge status-inactive">禁用</span>'}</td>
                        <td>
                            <div class="action-buttons">
                                <button class="btn-sm btn-edit" onclick="editAdmin(${admin.adminId})">编辑</button>
                                <button class="btn-sm btn-password" onclick="resetAdminPassword(${admin.adminId})">重置密码</button>
                                <button class="btn-sm ${admin.status === 1 ? 'btn-delete' : 'btn-edit'}" onclick="toggleAdminStatus(${admin.adminId}, ${admin.status})">${admin.status === 1 ? '禁用' : '启用'}</button>
                            </div>
                        </td>
                    `;
                    tableBody.appendChild(row);
                });
            }
        });
}

// 打开添加管理员弹窗
function openAddAdminModal() {
    // 重置表单
    document.getElementById('add-admin-form').reset();
    document.getElementById('add-dept-group').style.display = 'none';
    
    // 打开弹窗
    openModal('add-admin-modal');
}

// 切换部门字段显示/隐藏
function toggleDepartmentField(prefix) {
    const roleSelect = document.getElementById(`${prefix}-role`);
    const deptGroup = document.getElementById(`${prefix}-dept-group`);
    const deptSelect = document.getElementById(`${prefix}-dept-id`);
    
    if (roleSelect.value === 'DEPARTMENT_ADMIN') {
        deptGroup.style.display = 'block';
        deptSelect.required = true;
    } else {
        deptGroup.style.display = 'none';
        deptSelect.required = false;
        deptSelect.value = '';
    }
}

// 添加管理员
function addAdmin() {
    const usernameInput = document.getElementById('add-username');
    const passwordInput = document.getElementById('add-password');
    const confirmPasswordInput = document.getElementById('add-confirm-password');
    const realNameInput = document.getElementById('add-real-name');
    const roleSelect = document.getElementById('add-role');
    const deptSelect = document.getElementById('add-dept-id');
    
    // 表单验证
    if (!usernameInput.value.trim()) {
        alert('请输入用户名');
        usernameInput.focus();
        return;
    }
    
    if (!passwordInput.value) {
        alert('请输入密码');
        passwordInput.focus();
        return;
    }

    // 使用密码验证器验证密码
    if (window.passwordValidator) {
        const validationResult = passwordValidator.validatePassword ?
            passwordValidator.validatePassword(passwordInput.value) :
            { isValid: () => true, getErrors: () => [] };

        if (!validationResult.isValid()) {
            alert('密码复杂度不符合要求：' + validationResult.getErrors().join(', '));
            passwordInput.focus();
            return;
        }
    }

    if (passwordInput.value !== confirmPasswordInput.value) {
        alert('两次输入的密码不一致');
        confirmPasswordInput.focus();
        return;
    }
    
    if (!realNameInput.value.trim()) {
        alert('请输入姓名');
        realNameInput.focus();
        return;
    }
    
    if (!roleSelect.value) {
        alert('请选择角色');
        roleSelect.focus();
        return;
    }
    
    if (roleSelect.value === 'DEPARTMENT_ADMIN' && !deptSelect.value) {
        alert('请选择所属部门');
        deptSelect.focus();
        return;
    }
    
    const adminData = {
        loginName: usernameInput.value.trim(),
        passwordHash: passwordInput.value,
        realName: realNameInput.value.trim(),
        role: roleSelect.value,
        deptId: roleSelect.value === 'DEPARTMENT_ADMIN' ? parseInt(deptSelect.value) : null,
        phone: document.getElementById('add-phone').value.trim() || null,
        email: document.getElementById('add-email').value.trim() || null,
        status: 1 // 默认启用
    };
    
    fetch(`${getContextPath()}/api/admin/add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(adminData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('添加管理员成功');
            closeModal('add-admin-modal');
            loadAdmins();
        } else {
            alert('添加管理员失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('添加管理员失败:', error);
        alert('添加管理员失败，请稍后再试');
    });
}

// 编辑管理员
function editAdmin(id) {
    fetch(`${getContextPath()}/api/admin/detail?id=${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const admin = data.admin;
                
                // 填充表单数据
                document.getElementById('edit-id').value = admin.adminId;
                document.getElementById('edit-username').value = admin.loginName;
                document.getElementById('edit-real-name').value = admin.realName;
                document.getElementById('edit-role').value = admin.role;
                document.getElementById('edit-phone').value = admin.phone || '';
                document.getElementById('edit-email').value = admin.email || '';
                document.getElementById('edit-status').value = admin.status;
                
                // 处理部门字段
                if (admin.role === 'DEPARTMENT_ADMIN') {
                    document.getElementById('edit-dept-group').style.display = 'block';
                    document.getElementById('edit-dept-id').value = admin.deptId || '';
                } else {
                    document.getElementById('edit-dept-group').style.display = 'none';
                }
                
                // 打开弹窗
                openModal('edit-admin-modal');
            } else {
                showError('获取管理员详情失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('获取管理员详情失败:', error);
            showError('获取管理员详情失败，请稍后再试');
        });
}

// 更新管理员
function updateAdmin() {
    const idInput = document.getElementById('edit-id');
    const realNameInput = document.getElementById('edit-real-name');
    const roleSelect = document.getElementById('edit-role');
    const deptSelect = document.getElementById('edit-dept-id');
    const statusSelect = document.getElementById('edit-status');
    
    // 表单验证
    if (!realNameInput.value.trim()) {
        alert('请输入姓名');
        realNameInput.focus();
        return;
    }
    
    if (!roleSelect.value) {
        alert('请选择角色');
        roleSelect.focus();
        return;
    }
    
    if (roleSelect.value === 'DEPARTMENT_ADMIN' && !deptSelect.value) {
        alert('请选择所属部门');
        deptSelect.focus();
        return;
    }
    
    const adminData = {
        adminId: parseInt(idInput.value),
        realName: realNameInput.value.trim(),
        role: roleSelect.value,
        deptId: roleSelect.value === 'DEPARTMENT_ADMIN' ? parseInt(deptSelect.value) : null,
        phone: document.getElementById('edit-phone').value.trim() || null,
        status: parseInt(statusSelect.value)
    };
    
    fetch(`${getContextPath()}/api/admin/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(adminData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('更新管理员成功');
            closeModal('edit-admin-modal');
            loadAdmins();
        } else {
            alert('更新管理员失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('更新管理员失败:', error);
        alert('更新管理员失败，请稍后再试');
    });
}

// 重置管理员密码
function resetAdminPassword(id) {
    fetch(`${getContextPath()}/api/admin/detail?id=${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const admin = data.admin;
                
                // 填充表单数据
                document.getElementById('reset-id').value = admin.adminId;
                document.getElementById('reset-username').value = admin.loginName;
                document.getElementById('reset-password').value = '';
                document.getElementById('reset-confirm-password').value = '';
                
                // 打开弹窗
                openModal('reset-password-modal');
            } else {
                showError('获取管理员详情失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('获取管理员详情失败:', error);
            showError('获取管理员详情失败，请稍后再试');
        });
}

// 重置密码
function resetPassword() {
    const idInput = document.getElementById('reset-id');
    const passwordInput = document.getElementById('reset-password');
    const confirmPasswordInput = document.getElementById('reset-confirm-password');
    
    // 表单验证
    if (!passwordInput.value) {
        alert('请输入新密码');
        passwordInput.focus();
        return;
    }

    // 使用密码验证器验证密码
    if (window.passwordValidator) {
        const validationResult = passwordValidator.validatePassword ?
            passwordValidator.validatePassword(passwordInput.value) :
            { isValid: () => true, getErrors: () => [] };

        if (!validationResult.isValid()) {
            alert('密码复杂度不符合要求：' + validationResult.getErrors().join(', '));
            passwordInput.focus();
            return;
        }
    }

    if (passwordInput.value !== confirmPasswordInput.value) {
        alert('两次输入的密码不一致');
        confirmPasswordInput.focus();
        return;
    }
    
    const resetData = {
        adminId: parseInt(idInput.value),
        passwordHash: passwordInput.value
    };
    
    fetch(`${getContextPath()}/api/admin/reset-password`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(resetData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('重置密码成功');
            closeModal('reset-password-modal');
        } else {
            alert('重置密码失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('重置密码失败:', error);
        alert('重置密码失败，请稍后再试');
    });
}

// 切换管理员状态（启用/禁用）
function toggleAdminStatus(id, currentStatus) {
    const newStatus = currentStatus === 1 ? 0 : 1;
    const action = newStatus === 1 ? '启用' : '禁用';
    
    if (confirm(`确定要${action}该管理员吗？`)) {
        fetch(`${getContextPath()}/api/admin/toggle-status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                adminId: id,
                status: newStatus
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(`${action}管理员成功`);
                loadAdmins();
            } else {
                alert(`${action}管理员失败: ` + data.message);
            }
        })
        .catch(error => {
            console.error(`${action}管理员失败:`, error);
            alert(`${action}管理员失败，请稍后再试`);
        });
    }
}

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