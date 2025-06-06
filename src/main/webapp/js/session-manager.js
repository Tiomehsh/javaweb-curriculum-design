/**
 * 会话管理器
 * 负责检查会话超时和自动退出
 */
class SessionManager {
    constructor() {
        this.checkInterval = 5 * 60 * 1000; // 5分钟检查一次
        this.warningTime = 5 * 60 * 1000; // 提前5分钟警告
        this.sessionTimeout = 30 * 60 * 1000; // 30分钟超时
        this.lastActivity = Date.now();
        this.warningShown = false;
        this.checkTimer = null;
        this.warningTimer = null;
        
        this.init();
    }

    getContextPath() {
        // 获取上下文路径
        const path = window.location.pathname;
        const contextPath = path.substring(0, path.indexOf('/', 1));
        return contextPath || '';
    }

    init() {
        // 监听用户活动
        this.bindActivityEvents();
        
        // 开始定期检查
        this.startSessionCheck();
        
        // 监听页面可见性变化
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden) {
                this.checkSessionStatus();
            }
        });
    }
    
    bindActivityEvents() {
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        
        events.forEach(event => {
            document.addEventListener(event, () => {
                this.updateActivity();
            }, true);
        });
    }
    
    updateActivity() {
        this.lastActivity = Date.now();
        this.warningShown = false;
        
        // 清除警告定时器
        if (this.warningTimer) {
            clearTimeout(this.warningTimer);
            this.warningTimer = null;
        }
        
        // 设置新的警告定时器
        this.setWarningTimer();
    }
    
    setWarningTimer() {
        const timeToWarning = this.sessionTimeout - this.warningTime;
        
        this.warningTimer = setTimeout(() => {
            this.showSessionWarning();
        }, timeToWarning);
    }
    
    startSessionCheck() {
        this.checkTimer = setInterval(() => {
            this.checkSessionStatus();
        }, this.checkInterval);
        
        // 设置初始警告定时器
        this.setWarningTimer();
    }
    
    checkSessionStatus() {
        // 发送AJAX请求检查会话状态
        const contextPath = this.getContextPath();
        fetch(`${contextPath}/api/admin/session-check`, {
            method: 'GET',
            credentials: 'same-origin'
        })
        .then(response => {
            if (response.status === 401) {
                // 会话已过期
                this.handleSessionTimeout();
                return;
            }
            return response.json();
        })
        .then(data => {
            if (data && data.sessionTimeout) {
                this.handleSessionTimeout();
            }
        })
        .catch(error => {
            console.warn('会话检查失败:', error);
        });
    }
    
    showSessionWarning() {
        if (this.warningShown) {
            return;
        }
        
        this.warningShown = true;
        
        const modal = this.createWarningModal();
        document.body.appendChild(modal);
        
        // 5分钟后自动退出
        setTimeout(() => {
            this.handleSessionTimeout();
        }, this.warningTime);
    }
    
    createWarningModal() {
        const modal = document.createElement('div');
        modal.className = 'session-warning-modal';
        modal.innerHTML = `
            <div class="session-warning-overlay"></div>
            <div class="session-warning-content">
                <div class="session-warning-header">
                    <h3>会话即将过期</h3>
                </div>
                <div class="session-warning-body">
                    <p>您的会话将在5分钟后过期，请点击"继续工作"保持登录状态。</p>
                    <div class="session-warning-countdown">
                        <span id="countdown-timer">5:00</span>
                    </div>
                </div>
                <div class="session-warning-footer">
                    <button class="btn btn-primary" onclick="sessionManager.extendSession()">继续工作</button>
                    <button class="btn btn-secondary" onclick="sessionManager.logout()">退出登录</button>
                </div>
            </div>
        `;
        
        // 添加样式
        this.addWarningModalStyles();
        
        // 开始倒计时
        this.startCountdown();
        
        return modal;
    }
    
    addWarningModalStyles() {
        if (document.getElementById('session-warning-styles')) {
            return;
        }
        
        const style = document.createElement('style');
        style.id = 'session-warning-styles';
        style.textContent = `
            .session-warning-modal {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                z-index: 10000;
            }
            
            .session-warning-overlay {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
            }
            
            .session-warning-content {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: white;
                border-radius: 8px;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
                min-width: 400px;
                max-width: 500px;
            }
            
            .session-warning-header {
                padding: 20px 20px 0;
                text-align: center;
            }
            
            .session-warning-header h3 {
                margin: 0;
                color: #e74c3c;
                font-size: 1.2em;
            }
            
            .session-warning-body {
                padding: 20px;
                text-align: center;
            }
            
            .session-warning-countdown {
                margin-top: 15px;
                font-size: 2em;
                font-weight: bold;
                color: #e74c3c;
            }
            
            .session-warning-footer {
                padding: 0 20px 20px;
                text-align: center;
            }
            
            .session-warning-footer .btn {
                margin: 0 10px;
                padding: 10px 20px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 14px;
            }
            
            .session-warning-footer .btn-primary {
                background: #3498db;
                color: white;
            }
            
            .session-warning-footer .btn-secondary {
                background: #95a5a6;
                color: white;
            }
        `;
        
        document.head.appendChild(style);
    }
    
    startCountdown() {
        let timeLeft = 300; // 5分钟 = 300秒
        
        const countdownElement = document.getElementById('countdown-timer');
        if (!countdownElement) return;
        
        const timer = setInterval(() => {
            const minutes = Math.floor(timeLeft / 60);
            const seconds = timeLeft % 60;
            countdownElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
            
            timeLeft--;
            
            if (timeLeft < 0) {
                clearInterval(timer);
                this.handleSessionTimeout();
            }
        }, 1000);
    }
    
    extendSession() {
        // 发送请求延长会话
        const contextPath = this.getContextPath();
        fetch(`${contextPath}/api/admin/extend-session`, {
            method: 'POST',
            credentials: 'same-origin'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                this.updateActivity();
                this.closeWarningModal();
            } else {
                this.handleSessionTimeout();
            }
        })
        .catch(error => {
            console.error('延长会话失败:', error);
            this.handleSessionTimeout();
        });
    }
    
    closeWarningModal() {
        const modal = document.querySelector('.session-warning-modal');
        if (modal) {
            modal.remove();
        }
        this.warningShown = false;
    }
    
    logout() {
        // 发送登出请求
        const contextPath = this.getContextPath();
        fetch(`${contextPath}/api/admin/logout`, {
            method: 'POST',
            credentials: 'same-origin'
        })
        .finally(() => {
            window.location.href = 'login.jsp?timeout=1';
        });
    }
    
    handleSessionTimeout() {
        // 清除定时器
        if (this.checkTimer) {
            clearInterval(this.checkTimer);
        }
        if (this.warningTimer) {
            clearTimeout(this.warningTimer);
        }
        
        // 显示超时消息并跳转
        alert('会话已超时，请重新登录');
        window.location.href = 'login.jsp?timeout=1';
    }
    
    destroy() {
        if (this.checkTimer) {
            clearInterval(this.checkTimer);
        }
        if (this.warningTimer) {
            clearTimeout(this.warningTimer);
        }
    }
}

// 创建全局会话管理器实例
let sessionManager;

// 页面加载完成后初始化会话管理器
document.addEventListener('DOMContentLoaded', function() {
    // 只在管理员页面启用会话管理
    if (window.location.pathname.includes('/admin/') && 
        !window.location.pathname.includes('/login.jsp')) {
        sessionManager = new SessionManager();
    }
});

// 页面卸载时清理
window.addEventListener('beforeunload', function() {
    if (sessionManager) {
        sessionManager.destroy();
    }
});
