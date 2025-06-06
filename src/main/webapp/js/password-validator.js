/**
 * 前端密码强度检查器
 */
class PasswordValidator {
    constructor() {
        this.strengthColors = {
            0: '#e74c3c',  // 非常弱 - 红色
            1: '#e74c3c',  // 非常弱 - 红色
            2: '#f39c12',  // 弱 - 橙色
            3: '#f1c40f',  // 一般 - 黄色
            4: '#27ae60',  // 强 - 绿色
            5: '#2ecc71'   // 非常强 - 深绿色
        };
        
        this.strengthTexts = {
            0: '非常弱',
            1: '非常弱',
            2: '弱',
            3: '一般',
            4: '强',
            5: '非常强'
        };
    }
    
    /**
     * 初始化密码输入框的强度检查
     * @param {string} inputId 密码输入框ID
     * @param {string} strengthContainerId 强度显示容器ID
     * @param {string} errorContainerId 错误信息容器ID
     */
    initPasswordInput(inputId, strengthContainerId, errorContainerId) {
        const passwordInput = document.getElementById(inputId);
        const strengthContainer = document.getElementById(strengthContainerId);
        const errorContainer = document.getElementById(errorContainerId);
        
        if (!passwordInput) {
            console.error('Password input not found:', inputId);
            return;
        }
        
        // 创建强度显示元素
        if (strengthContainer) {
            this.createStrengthIndicator(strengthContainer);
        }
        
        // 绑定输入事件
        passwordInput.addEventListener('input', (e) => {
            const password = e.target.value;
            this.checkPasswordStrength(password, strengthContainer, errorContainer);
        });
        
        // 绑定失焦事件，进行服务器端验证
        passwordInput.addEventListener('blur', (e) => {
            const password = e.target.value;
            if (password) {
                this.validatePasswordOnServer(password, errorContainer);
            }
        });
    }
    
    /**
     * 创建强度指示器
     * @param {HTMLElement} container 容器元素
     */
    createStrengthIndicator(container) {
        container.innerHTML = `
            <div class="password-strength-container">
                <div class="strength-bars">
                    <div class="strength-bar" data-level="1"></div>
                    <div class="strength-bar" data-level="2"></div>
                    <div class="strength-bar" data-level="3"></div>
                    <div class="strength-bar" data-level="4"></div>
                    <div class="strength-bar" data-level="5"></div>
                </div>
                <div class="strength-text">请输入密码</div>
            </div>
        `;
        
        // 添加样式
        this.addPasswordStrengthStyles();
    }
    
    /**
     * 添加密码强度样式
     */
    addPasswordStrengthStyles() {
        if (document.getElementById('password-strength-styles')) {
            return;
        }
        
        const style = document.createElement('style');
        style.id = 'password-strength-styles';
        style.textContent = `
            .password-strength-container {
                margin-top: 8px;
            }
            
            .strength-bars {
                display: flex;
                gap: 4px;
                margin-bottom: 5px;
            }
            
            .strength-bar {
                height: 4px;
                flex: 1;
                background-color: #e0e0e0;
                border-radius: 2px;
                transition: background-color 0.3s ease;
            }
            
            .strength-bar.active {
                background-color: var(--strength-color);
            }
            
            .strength-text {
                font-size: 12px;
                color: #666;
                margin-top: 4px;
            }
            
            .password-errors {
                margin-top: 8px;
            }
            
            .password-error {
                font-size: 12px;
                color: #e74c3c;
                margin-bottom: 4px;
            }
            
            .password-error:last-child {
                margin-bottom: 0;
            }
        `;
        
        document.head.appendChild(style);
    }
    
    /**
     * 检查密码强度（客户端）
     * @param {string} password 密码
     * @param {HTMLElement} strengthContainer 强度显示容器
     * @param {HTMLElement} errorContainer 错误信息容器
     */
    checkPasswordStrength(password, strengthContainer, errorContainer) {
        if (!password) {
            this.updateStrengthDisplay(0, '请输入密码', strengthContainer);
            if (errorContainer) {
                errorContainer.innerHTML = '';
            }
            return;
        }
        
        const strength = this.calculatePasswordStrength(password);
        const strengthText = this.strengthTexts[strength];
        
        this.updateStrengthDisplay(strength, strengthText, strengthContainer);
        
        // 客户端基础验证
        const errors = this.getBasicValidationErrors(password);
        if (errorContainer) {
            this.displayErrors(errors, errorContainer);
        }
    }
    
    /**
     * 计算密码强度（客户端简化版）
     * @param {string} password 密码
     * @returns {number} 强度等级 (0-5)
     */
    calculatePasswordStrength(password) {
        let score = 0;
        
        // 长度评分
        if (password.length >= 8) score++;
        if (password.length >= 12) score++;
        
        // 字符类型评分
        if (/\d/.test(password)) score++;           // 数字
        if (/[a-z]/.test(password)) score++;        // 小写字母
        if (/[A-Z]/.test(password)) score++;        // 大写字母
        if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score++; // 特殊字符
        
        // 复杂度评分
        if (!this.hasConsecutiveChars(password)) score++;
        if (!this.isCommonWeakPassword(password)) score++;
        
        return Math.min(5, score);
    }
    
    /**
     * 检查是否包含连续字符
     * @param {string} password 密码
     * @returns {boolean} 是否包含连续字符
     */
    hasConsecutiveChars(password) {
        for (let i = 0; i < password.length - 2; i++) {
            if (password[i] === password[i + 1] && password[i + 1] === password[i + 2]) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否为常见弱密码
     * @param {string} password 密码
     * @returns {boolean} 是否为弱密码
     */
    isCommonWeakPassword(password) {
        const lowerPassword = password.toLowerCase();
        const weakPasswords = [
            'password', '123456', '12345678', 'qwerty', 'abc123',
            'password123', 'admin', 'root', 'user', 'test'
        ];
        
        return weakPasswords.some(weak => lowerPassword.includes(weak));
    }
    
    /**
     * 获取基础验证错误
     * @param {string} password 密码
     * @returns {Array} 错误列表
     */
    getBasicValidationErrors(password) {
        const errors = [];
        
        if (password.length < 8) {
            errors.push('密码长度至少需要8位');
        }
        
        if (!/\d/.test(password)) {
            errors.push('密码必须包含至少一个数字');
        }
        
        if (!/[a-z]/.test(password)) {
            errors.push('密码必须包含至少一个小写字母');
        }
        
        if (!/[A-Z]/.test(password)) {
            errors.push('密码必须包含至少一个大写字母');
        }
        
        if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
            errors.push('密码必须包含至少一个特殊字符');
        }
        
        return errors;
    }
    
    /**
     * 更新强度显示
     * @param {number} strength 强度等级
     * @param {string} text 强度文本
     * @param {HTMLElement} container 容器元素
     */
    updateStrengthDisplay(strength, text, container) {
        if (!container) return;
        
        const bars = container.querySelectorAll('.strength-bar');
        const strengthText = container.querySelector('.strength-text');
        const color = this.strengthColors[strength];
        
        // 更新强度条
        bars.forEach((bar, index) => {
            if (index < strength) {
                bar.classList.add('active');
                bar.style.setProperty('--strength-color', color);
            } else {
                bar.classList.remove('active');
            }
        });
        
        // 更新文本
        if (strengthText) {
            strengthText.textContent = text;
            strengthText.style.color = color;
        }
    }
    
    /**
     * 显示错误信息
     * @param {Array} errors 错误列表
     * @param {HTMLElement} container 容器元素
     */
    displayErrors(errors, container) {
        if (!container) return;
        
        if (errors.length === 0) {
            container.innerHTML = '';
            return;
        }
        
        const errorHtml = errors.map(error => 
            `<div class="password-error">${error}</div>`
        ).join('');
        
        container.innerHTML = `<div class="password-errors">${errorHtml}</div>`;
    }
    
    /**
     * 获取上下文路径
     * @returns {string} 上下文路径
     */
    getContextPath() {
        const path = window.location.pathname;
        const contextPath = path.substring(0, path.indexOf('/', 1));
        return contextPath || '';
    }

    /**
     * 服务器端密码验证
     * @param {string} password 密码
     * @param {HTMLElement} errorContainer 错误容器
     */
    validatePasswordOnServer(password, errorContainer) {
        const contextPath = this.getContextPath();
        fetch(`${contextPath}/api/admin/validate-password?password=` + encodeURIComponent(password))
            .then(response => response.json())
            .then(data => {
                if (errorContainer) {
                    this.displayErrors(data.errors || [], errorContainer);
                }
            })
            .catch(error => {
                console.error('密码验证失败:', error);
            });
    }
}

// 创建全局实例
window.passwordValidator = new PasswordValidator();
