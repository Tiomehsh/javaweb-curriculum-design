package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 管理员实体类
 */
public class Admin {
    private Integer adminId;           // 管理员ID
    private String loginName;          // 登录名
    private String passwordHash;       // SM3加密后的密码
    private String realName;           // 真实姓名
    private Integer deptId;            // 所属部门ID
    private String phone;              // 联系电话
    private String role;               // 角色
    private Integer loginAttempts;     // 登录失败尝试次数
    private Timestamp lockedUntil;     // 账户锁定截止时间
    private Timestamp lastPasswordChange; // 最后一次密码修改时间
    private Timestamp createTime;      // 创建时间
    private Timestamp updateTime;      // 更新时间
    private Integer status;            // 状态(1:启用 0:禁用)
    
    // 角色常量
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ROLE_DEPARTMENT_ADMIN = "DEPARTMENT_ADMIN";
    public static final String ROLE_AUDIT_ADMIN = "AUDIT_ADMIN";
    
    // 状态常量
    public static final Integer STATUS_ENABLED = 1;
    public static final Integer STATUS_DISABLED = 0;
    
    // 部门名称（非数据库字段，用于显示）
    private String deptName;
    
    // 默认构造函数
    public Admin() {
    }
    
    // 带参数的构造函数
    public Admin(String loginName, String passwordHash, String realName, Integer deptId, String role) {
        this.loginName = loginName;
        this.passwordHash = passwordHash;
        this.realName = realName;
        this.deptId = deptId;
        this.role = role;
        this.loginAttempts = 0;
        this.status = STATUS_ENABLED;
    }
    
    // Getter和Setter方法
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public String getLoginName() {
        return loginName;
    }
    
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public Integer getDeptId() {
        return deptId;
    }
    
    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Integer getLoginAttempts() {
        return loginAttempts;
    }
    
    public void setLoginAttempts(Integer loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
    
    public Timestamp getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(Timestamp lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public Timestamp getLastPasswordChange() {
        return lastPasswordChange;
    }
    
    public void setLastPasswordChange(Timestamp lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }
    
    public Timestamp getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public Timestamp getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    
    /**
     * 检查账户是否被锁定
     * @return true表示被锁定，false表示未锁定
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return lockedUntil.after(new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * 检查密码是否过期（90天）
     * @return true表示已过期，false表示未过期
     */
    public boolean isPasswordExpired() {
        if (lastPasswordChange == null) {
            return false;
        }
        
        long passwordAgeInMillis = System.currentTimeMillis() - lastPasswordChange.getTime();
        long daysInMillis = 90L * 24 * 60 * 60 * 1000; // 90天的毫秒数
        
        return passwordAgeInMillis > daysInMillis;
    }
    
    /**
     * 增加登录失败尝试次数
     */
    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts == null) ? 1 : this.loginAttempts + 1;
    }
    
    /**
     * 重置登录失败尝试次数
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }
    
    /**
     * 设置账户锁定（30分钟）
     */
    public void lockAccount() {
        long thirtyMinutesInMillis = 30L * 60 * 1000; // 30分钟的毫秒数
        this.lockedUntil = new Timestamp(System.currentTimeMillis() + thirtyMinutesInMillis);
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", loginName='" + loginName + '\'' +
                ", realName='" + realName + '\'' +
                ", deptId=" + deptId +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                ", deptName='" + deptName + '\'' +
                '}';
    }
} 