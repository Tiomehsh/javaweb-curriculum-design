// 页面加载时执行
document.addEventListener('DOMContentLoaded', function() {
    // 加载用户信息
    loadUserInfo();
    
    // 默认激活第一个标签页
    switchTab('info');
});

// 加载用户信息
function loadUserInfo() {
    fetch('../api/admin/profile')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                fillUserInfo(data.admin);
            } else {
                showError('加载用户信息失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('加载用户信息失败:', error);
            showError('加载用户信息失败，请稍后再试');
        });
}

// 填充用户信息
function fillUserInfo(admin) {
    document.getElementById('username').value = admin.loginName;
    document.getElementById('real-name').value = admin.realName;
    document.getElementById('role').value = getRoleName(admin.role);
    
    // 如果用户是部门管理员并且有所属部门，则显示部门信息
    if (admin.role === 'DEPARTMENT_ADMIN' && admin.deptId) {
        document.getElementById('department-group').style.display = 'block';
        loadDepartmentInfo(admin.deptId);
    } else {
        document.getElementById('department-group').style.display = 'none';
    }
    
    // 填充联系方式
    document.getElementById('phone').value = admin.phone || '';
}

// 加载部门信息
function loadDepartmentInfo(deptId) {
    fetch(`../api/department/detail?id=${deptId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('department').value = data.department.deptName;
            }
        })
        .catch(error => {
            console.error('加载部门信息失败:', error);
        });
}

// 获取角色名称
function getRoleName(role) {
    switch(role) {
        case 'SYSTEM_ADMIN': return '系统管理员';
        case 'DEPARTMENT_ADMIN': return '部门管理员';
        case 'RECEPTION_ADMIN': return '接待管理员';
        case 'AUDIT_ADMIN': return '审计管理员';
        default: return role;
    }
}

// 切换标签页
function switchTab(tabName) {
    // 隐藏所有标签页内容
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 取消所有标签按钮的激活状态
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 激活选中的标签页
    document.getElementById(`${tabName}-tab-content`).classList.add('active');
    document.getElementById(`${tabName}-tab`).classList.add('active');
}

// 更新个人信息
function updateProfile() {
    const realNameInput = document.getElementById('real-name');
    const phoneInput = document.getElementById('phone');
    
    // 表单验证
    if (!realNameInput.value.trim()) {
        alert('请输入姓名');
        realNameInput.focus();
        return;
    }
    
    // 手机号验证
    if (phoneInput.value.trim() && !/^[0-9]{11}$/.test(phoneInput.value.trim())) {
        alert('请输入有效的11位手机号');
        phoneInput.focus();
        return;
    }
    
    const profileData = {
        realName: realNameInput.value.trim(),
        phone: phoneInput.value.trim() || null
    };
    
    fetch('../api/admin/update-profile', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(profileData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('个人信息更新成功');
            // 刷新页面以显示更新后的信息
            window.location.reload();
        } else {
            alert('个人信息更新失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('个人信息更新失败:', error);
        alert('个人信息更新失败，请稍后再试');
    });
}

// 更新密码
function updatePassword() {
    const oldPasswordInput = document.getElementById('old-password');
    const newPasswordInput = document.getElementById('new-password');
    const confirmPasswordInput = document.getElementById('confirm-password');
    
    // 表单验证
    if (!oldPasswordInput.value) {
        alert('请输入当前密码');
        oldPasswordInput.focus();
        return;
    }
    
    if (!newPasswordInput.value) {
        alert('请输入新密码');
        newPasswordInput.focus();
        return;
    }
    
    // 使用密码验证器验证新密码
    if (window.passwordValidator) {
        const validationResult = passwordValidator.validatePassword ?
            passwordValidator.validatePassword(newPasswordInput.value) :
            { isValid: () => true, getErrors: () => [] };

        if (!validationResult.isValid()) {
            alert('密码复杂度不符合要求：' + validationResult.getErrors().join(', '));
            newPasswordInput.focus();
            return;
        }
    } else if (newPasswordInput.value.length < 6) {
        alert('新密码长度至少为6位');
        newPasswordInput.focus();
        return;
    }
    
    if (newPasswordInput.value !== confirmPasswordInput.value) {
        alert('两次输入的新密码不一致');
        confirmPasswordInput.focus();
        return;
    }
    
    const passwordData = {
        oldPassword: oldPasswordInput.value,
        newPassword: newPasswordInput.value
    };
    
    // 获取上下文路径
    const contextPath = getContextPath();

    fetch(`${contextPath}/api/admin/change-password`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(passwordData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('密码修改成功，请重新登录');
            logout();
        } else {
            alert('密码修改失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('密码修改失败:', error);
        alert('密码修改失败，请稍后再试');
    });
}

// 获取上下文路径
function getContextPath() {
    const path = window.location.pathname;
    const contextPath = path.substring(0, path.indexOf('/', 1));
    return contextPath || '';
}

// 显示错误消息
function showError(message) {
    alert(message);
}

// 退出登录
function logout() {
    const contextPath = getContextPath();
    fetch(`${contextPath}/api/admin/logout`, {
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
        window.location.href = 'login.jsp'; // 即使API调用失败，也强制跳转到登录页
    });
} 