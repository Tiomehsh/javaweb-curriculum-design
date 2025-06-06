// 页面加载时检查是否有记住的用户名
document.addEventListener('DOMContentLoaded', function() {
    const savedUsername = localStorage.getItem('rememberUsername');
    if (savedUsername) {
        document.getElementById('loginName').value = savedUsername;
        document.getElementById('rememberMe').checked = true;
    }
    
    // 聚焦到适当的输入框
    if (savedUsername) {
        document.getElementById('password').focus();
    } else {
        document.getElementById('loginName').focus();
    }
});

function submitLogin() {
    const form = document.getElementById('loginForm');
    const loginName = document.getElementById('loginName').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;
    
    // 简单的前端验证
    if (!loginName || !password) {
        showError('请输入用户名和密码');
        return;
    }
    
    // 处理记住用户名
    if (rememberMe) {
        localStorage.setItem('rememberUsername', loginName);
    } else {
        localStorage.removeItem('rememberUsername');
    }
    
    // 禁用登录按钮，防止重复提交
    const loginBtn = document.querySelector('.login-btn');
    loginBtn.disabled = true;
    loginBtn.innerHTML = '<span class="btn-text">登录中...</span>';
    
    // 使用fetch API发送登录请求
    fetch('../api/admin/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            loginName: loginName,
            password: password
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 登录成功，检查是否有密码相关警告
            if (data.passwordExpired) {
                // 密码已过期，强制跳转到修改密码页面
                alert('密码已过期，请立即修改密码！');
                window.location.href = 'profile.jsp?forceChangePassword=1';
            } else if (data.passwordWarning) {
                // 密码即将过期，显示警告但允许继续
                if (confirm(data.message + '\n是否现在修改密码？')) {
                    window.location.href = 'profile.jsp?changePassword=1';
                } else {
                    window.location.href = 'dashboard.jsp';
                }
            } else {
                // 正常登录，跳转到仪表板
                window.location.href = 'dashboard.jsp';
            }
        } else {
            // 登录失败，显示具体错误信息
            if (data.accountLocked) {
                showError('账户已被锁定，请30分钟后再试');
            } else {
                showError(data.message || '登录失败，请检查用户名和密码');
            }
            // 恢复登录按钮
            loginBtn.disabled = false;
            loginBtn.innerHTML = '<span class="btn-text">登 录</span><i class="icon-arrow-right"></i>';
        }
    })
    .catch(error => {
        console.error('登录请求失败:', error);
        showError('网络错误，请稍后再试');
        // 恢复登录按钮
        loginBtn.disabled = false;
        loginBtn.innerHTML = '<span class="btn-text">登 录</span><i class="icon-arrow-right"></i>';
    });
}

// 显示错误信息
function showError(message) {
    // 创建错误提示元素
    const alertDiv = document.createElement('div');
    alertDiv.className = 'login-alert login-alert-error';
    alertDiv.innerHTML = '<i class="icon-error"></i>' + message;
    
    // 查找表单元素
    const form = document.querySelector('.login-form');
    
    // 移除现有的错误提示
    const existingAlert = document.querySelector('.login-alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    // 在表单前插入错误提示
    form.parentNode.insertBefore(alertDiv, form);
    
    // 3秒后自动移除错误提示
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// 支持按Enter键登录
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        submitLogin();
    }
});