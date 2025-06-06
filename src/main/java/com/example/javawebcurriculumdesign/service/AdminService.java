package com.example.javawebcurriculumdesign.service;

import com.example.javawebcurriculumdesign.dao.AdminDao;
import com.example.javawebcurriculumdesign.dao.AdminPermissionDao;
import com.example.javawebcurriculumdesign.dao.SystemLogDao;
import com.example.javawebcurriculumdesign.model.Admin;
import com.example.javawebcurriculumdesign.model.AdminPermission;
import com.example.javawebcurriculumdesign.model.SystemLog;
import com.example.javawebcurriculumdesign.util.SMUtil;
import com.example.javawebcurriculumdesign.util.PasswordValidator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务类
 * 提供管理员登录、权限验证、密码修改等功能
 */
public class AdminService {
    private final AdminDao adminDao = new AdminDao();
    private final SystemLogDao systemLogDao = new SystemLogDao();
    private final AdminPermissionDao adminPermissionDao = new AdminPermissionDao();
    
    /**
     * 管理员登录
     * @param loginName 登录名
     * @param password 密码
     * @return 登录成功返回管理员对象，失败返回null
     */
    public Admin login(String loginName, String password) {
        if (loginName == null || password == null || loginName.isEmpty() || password.isEmpty()) {
            return null;
        }

        Admin admin = adminDao.getByLoginName(loginName);
        if (admin == null) {
            return null;
        }

        // 检查账户是否被锁定
        if (isAccountLocked(admin)) {
            return null;
        }

        // 验证密码 (密码以SM3哈希存储)
        String passwordHash = SMUtil.sm3(password);

        // 忽略大小写比较哈希值
        if (!passwordHash.equalsIgnoreCase(admin.getPasswordHash())) {
            // 密码错误，增加失败次数
            incrementLoginAttempts(admin.getAdminId());
            return null;
        }

        // 登录成功，重置失败次数
        resetLoginAttempts(admin.getAdminId());

        return admin;
    }

    /**
     * 检查账户是否被锁定
     * @param admin 管理员对象
     * @return 是否被锁定
     */
    public boolean isAccountLocked(Admin admin) {
        if (admin.getLockedUntil() == null) {
            return false;
        }

        // 检查锁定时间是否已过
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (now.after(admin.getLockedUntil())) {
            // 锁定时间已过，解除锁定
            adminDao.unlockAccount(admin.getAdminId());
            return false;
        }

        return true;
    }

    /**
     * 增加登录失败次数
     * @param adminId 管理员ID
     */
    private void incrementLoginAttempts(Integer adminId) {
        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return;
        }

        int attempts = admin.getLoginAttempts() + 1;

        if (attempts >= 5) {
            // 达到5次失败，锁定30分钟
            Timestamp lockUntil = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);
            adminDao.lockAccount(adminId, lockUntil);

            // 记录锁定日志
            SystemLog log = new SystemLog();
            log.setAdminId(adminId);
            log.setOperation("账户锁定");
            log.setDescription("登录失败5次，账户被锁定30分钟");
            log.setOperationTime(new Timestamp(System.currentTimeMillis()));
            systemLogDao.add(log);
        } else {
            // 更新失败次数
            adminDao.updateLoginAttempts(adminId, attempts);
        }
    }

    /**
     * 重置登录失败次数
     * @param adminId 管理员ID
     */
    private void resetLoginAttempts(Integer adminId) {
        adminDao.updateLoginAttempts(adminId, 0);
    }

    /**
     * 检查密码是否过期（90天）
     * @param admin 管理员对象
     * @return 是否过期
     */
    public boolean isPasswordExpired(Admin admin) {
        if (admin.getLastPasswordChange() == null) {
            return true; // 如果没有密码修改记录，认为已过期
        }

        long daysSinceChange = (System.currentTimeMillis() - admin.getLastPasswordChange().getTime()) / (1000 * 60 * 60 * 24);
        return daysSinceChange >= 90;
    }

    /**
     * 获取密码剩余有效天数
     * @param admin 管理员对象
     * @return 剩余天数，负数表示已过期
     */
    public long getPasswordRemainingDays(Admin admin) {
        if (admin.getLastPasswordChange() == null) {
            return -1; // 已过期
        }

        long daysSinceChange = (System.currentTimeMillis() - admin.getLastPasswordChange().getTime()) / (1000 * 60 * 60 * 24);
        return 90 - daysSinceChange;
    }

    /**
     * 检查是否需要密码过期提醒（剩余7天内）
     * @param admin 管理员对象
     * @return 是否需要提醒
     */
    public boolean needPasswordExpiryWarning(Admin admin) {
        long remainingDays = getPasswordRemainingDays(admin);
        return remainingDays > 0 && remainingDays <= 7;
    }
    
    /**
     * 获取管理员信息
     * @param adminId 管理员ID
     * @return 管理员对象
     */
    public Admin getAdminById(Integer adminId) {
        if (adminId == null || adminId <= 0) {
            return null;
        }
        return adminDao.getById(adminId);
    }
    
    /**
     * 根据登录名获取管理员信息
     * @param loginName 登录名
     * @return 管理员对象
     */
    public Admin getAdminByLoginName(String loginName) {
        if (loginName == null || loginName.isEmpty()) {
            return null;
        }
        return adminDao.getByLoginName(loginName);
    }
    
    /**
     * 验证管理员是否有指定权限
     * @param adminId 管理员ID
     * @param requiredRole 需要的权限级别
     * @return 是否有权限
     */
    public boolean hasPermission(Integer adminId, String requiredRole) {
        if (adminId == null || adminId <= 0 || requiredRole == null) {
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 基于角色的权限检查
        // 系统管理员拥有所有权限
        if (Admin.ROLE_SYSTEM_ADMIN.equals(admin.getRole())) {
            return true;
        }

        // 部门管理员可以访问部门管理员和审核员权限
        if (Admin.ROLE_DEPARTMENT_ADMIN.equals(admin.getRole())) {
            return !Admin.ROLE_SYSTEM_ADMIN.equals(requiredRole);
        }

        // 审核员只能访问审核员权限
        if (Admin.ROLE_AUDIT_ADMIN.equals(admin.getRole())) {
            return Admin.ROLE_AUDIT_ADMIN.equals(requiredRole);
        }

        return false;
    }

    /**
     * 检查管理员是否可以访问指定部门的数据
     * @param adminId 管理员ID
     * @param targetDeptId 目标部门ID
     * @return 是否有权限
     */
    public boolean canAccessDepartment(Integer adminId, Integer targetDeptId) {
        if (adminId == null || adminId <= 0) {
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 系统管理员可以访问所有部门
        if (Admin.ROLE_SYSTEM_ADMIN.equals(admin.getRole())) {
            return true;
        }

        // 部门管理员只能访问自己的部门
        if (Admin.ROLE_DEPARTMENT_ADMIN.equals(admin.getRole())) {
            return admin.getDeptId() != null && admin.getDeptId().equals(targetDeptId);
        }

        // 审计管理员可以查看所有部门（只读）
        if (Admin.ROLE_AUDIT_ADMIN.equals(admin.getRole())) {
            return true;
        }

        return false;
    }

    /**
     * 检查管理员是否可以管理指定部门的公务预约
     * @param adminId 管理员ID
     * @param targetDeptId 目标部门ID
     * @return 是否有权限
     */
    public boolean canManageOfficialAppointment(Integer adminId, Integer targetDeptId) {
        if (adminId == null || adminId <= 0) {
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 系统管理员可以管理所有部门的公务预约
        if (Admin.ROLE_SYSTEM_ADMIN.equals(admin.getRole())) {
            return true;
        }

        // 部门管理员只能管理自己部门的公务预约
        if (Admin.ROLE_DEPARTMENT_ADMIN.equals(admin.getRole())) {
            return admin.getDeptId() != null && admin.getDeptId().equals(targetDeptId);
        }

        // 审计管理员不能管理预约，只能查看
        return false;
    }

    /**
     * 检查管理员是否可以查看社会公众预约
     * @param adminId 管理员ID
     * @return 是否有权限
     */
    public boolean canViewPublicAppointment(Integer adminId) {
        if (adminId == null || adminId <= 0) {
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 系统管理员可以查看所有社会公众预约
        if (Admin.ROLE_SYSTEM_ADMIN.equals(admin.getRole())) {
            return true;
        }

        // 审计管理员可以查看所有社会公众预约
        if (Admin.ROLE_AUDIT_ADMIN.equals(admin.getRole())) {
            return true;
        }

        // 部门管理员需要被授权才能查看社会公众预约
        if (Admin.ROLE_DEPARTMENT_ADMIN.equals(admin.getRole())) {
            return adminPermissionDao.hasPermission(adminId, AdminPermission.TYPE_VIEW_PUBLIC_APPOINTMENT);
        }

        return false;
    }

    /**
     * 授权管理员查看社会公众预约
     * @param adminId 被授权的管理员ID
     * @param grantedBy 授权人ID
     * @return 是否成功
     */
    public boolean grantPublicAppointmentPermission(Integer adminId, Integer grantedBy) {
        if (adminId == null || grantedBy == null) {
            return false;
        }

        // 检查是否已有权限
        if (adminPermissionDao.hasPermission(adminId, AdminPermission.TYPE_VIEW_PUBLIC_APPOINTMENT)) {
            return true; // 已有权限
        }

        AdminPermission permission = new AdminPermission(adminId, AdminPermission.TYPE_VIEW_PUBLIC_APPOINTMENT, grantedBy);
        boolean success = adminPermissionDao.add(permission) > 0;

        // 记录授权日志
        if (grantedBy != null) {
            Admin targetAdmin = adminDao.getById(adminId);
            String operation = "授权查看社会公众预约: " + (targetAdmin != null ? targetAdmin.getRealName() : "ID:" + adminId);
            SystemLog log = new SystemLog();
            log.setAdminId(grantedBy);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("结果: " + (success ? "成功" : "失败"));
            systemLogDao.add(log);
        }

        return success;
    }

    /**
     * 撤销管理员查看社会公众预约权限
     * @param adminId 被撤销权限的管理员ID
     * @param revokedBy 撤销人ID
     * @return 是否成功
     */
    public boolean revokePublicAppointmentPermission(Integer adminId, Integer revokedBy) {
        if (adminId == null || revokedBy == null) {
            return false;
        }

        boolean success = adminPermissionDao.revokePermission(adminId, AdminPermission.TYPE_VIEW_PUBLIC_APPOINTMENT) > 0;

        // 记录撤销日志
        if (revokedBy != null) {
            Admin targetAdmin = adminDao.getById(adminId);
            String operation = "撤销查看社会公众预约权限: " + (targetAdmin != null ? targetAdmin.getRealName() : "ID:" + adminId);
            SystemLog log = new SystemLog();
            log.setAdminId(revokedBy);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("结果: " + (success ? "成功" : "失败"));
            systemLogDao.add(log);
        }

        return success;
    }

    /**
     * 获取管理员的权限列表
     * @param adminId 管理员ID
     * @return 权限列表
     */
    public List<AdminPermission> getAdminPermissions(Integer adminId) {
        if (adminId == null || adminId <= 0) {
            return null;
        }
        return adminPermissionDao.getByAdminId(adminId);
    }
    
    /**
     * 修改密码
     * @param adminId 管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    public boolean changePassword(Integer adminId, String oldPassword, String newPassword) {
        if (adminId == null || oldPassword == null || newPassword == null ||
            oldPassword.isEmpty() || newPassword.isEmpty()) {
            return false;
        }

        // 验证新密码复杂度
        PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            // 记录密码修改失败日志
            recordPasswordChangeLog(adminId, false, "密码复杂度不符合要求: " + validationResult.getErrorMessage());
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 验证旧密码
        String oldPasswordHash = SMUtil.sm3(oldPassword);
        if (!oldPasswordHash.equalsIgnoreCase(admin.getPasswordHash())) {
            // 记录密码修改失败日志
            recordPasswordChangeLog(adminId, false, "旧密码错误");
            return false;
        }

        // 检查新密码是否与旧密码相同
        String newPasswordHash = SMUtil.sm3(newPassword);
        if (newPasswordHash.equalsIgnoreCase(admin.getPasswordHash())) {
            recordPasswordChangeLog(adminId, false, "新密码不能与旧密码相同");
            return false;
        }

        // 更新密码
        boolean success = adminDao.updatePassword(adminId, newPasswordHash) > 0;

        // 记录密码修改日志
        recordPasswordChangeLog(adminId, success, success ? "密码修改成功" : "密码修改失败");

        return success;
    }

    /**
     * 验证密码复杂度
     * @param password 密码
     * @return 验证结果
     */
    public PasswordValidator.PasswordValidationResult validatePassword(String password) {
        return PasswordValidator.validate(password);
    }
    
    /**
     * 获取所有管理员列表
     * @return 管理员列表
     */
    public List<Admin> getAllAdmins() {
        return adminDao.getAll();
    }
    
    /**
     * 添加管理员
     * @param admin 管理员对象
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    public boolean addAdmin(Admin admin, Integer operatorId) {
        if (admin == null || admin.getLoginName() == null || admin.getPasswordHash() == null ||
            admin.getRealName() == null || admin.getRole() == null) {
            return false;
        }

        // 检查用户名是否已存在
        if (adminDao.getByLoginName(admin.getLoginName()) != null) {
            return false;
        }

        // 如果密码是明文，验证密码复杂度并进行哈希处理
        if (admin.getPasswordHash().length() != 64) { // SM3哈希值长度为64
            // 验证密码复杂度
            PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(admin.getPasswordHash());
            if (!validationResult.isValid()) {
                // 记录密码验证失败日志
                if (operatorId != null) {
                    String operation = "添加管理员失败: " + admin.getLoginName() + "(" + admin.getRealName() + ")";
                    SystemLog log = new SystemLog();
                    log.setAdminId(operatorId);
                    log.setOperation(operation);
                    log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
                    log.setDescription("密码复杂度不符合要求: " + validationResult.getErrorMessage());
                    systemLogDao.add(log);
                }
                return false;
            }
            admin.setPasswordHash(SMUtil.sm3(admin.getPasswordHash()));
        }
        
        boolean success = adminDao.add(admin) > 0;
        
        // 记录添加管理员日志
        if (operatorId != null) {
            String operation = "添加管理员: " + admin.getLoginName() + "(" + admin.getRealName() + ")";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("角色: " + admin.getRole() + ", 结果: " + (success ? "成功" : "失败"));
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 删除管理员
     * @param adminId 要删除的管理员ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    public boolean deleteAdmin(Integer adminId, Integer operatorId) {
        if (adminId == null || adminId <= 0) {
            return false;
        }
        
        // 获取要删除的管理员信息（用于日志记录）
        Admin adminToDelete = adminDao.getById(adminId);
        if (adminToDelete == null) {
            return false;
        }
        
        boolean success = adminDao.deleteById(adminId) > 0;
        
        // 记录删除管理员日志
        if (operatorId != null) {
            String operation = "删除管理员: " + adminToDelete.getLoginName() + "(" + adminToDelete.getRealName() + ")";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("管理员ID: " + adminId + ", 结果: " + (success ? "成功" : "失败"));
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 更新管理员信息
     * @param admin 管理员对象
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    public boolean updateAdmin(Admin admin, Integer operatorId) {
        if (admin == null || admin.getAdminId() == null || admin.getAdminId() <= 0) {
            return false;
        }
        
        // 获取原始管理员信息（用于日志记录和密码处理）
        Admin originalAdmin = adminDao.getById(admin.getAdminId());
        if (originalAdmin == null) {
            return false;
        }
        
        // 如果密码字段为空，保留原密码
        if (admin.getPasswordHash() == null || admin.getPasswordHash().isEmpty()) {
            admin.setPasswordHash(originalAdmin.getPasswordHash());
        } else if (admin.getPasswordHash().length() != 64) { // 如果密码不是哈希值，则进行哈希处理
            admin.setPasswordHash(SMUtil.sm3(admin.getPasswordHash()));
        }
        
        boolean success = adminDao.update(admin) > 0;
        
        // 记录更新管理员日志
        if (operatorId != null) {
            String operation = "更新管理员信息: " + admin.getLoginName() + "(" + admin.getRealName() + ")";
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("管理员ID: " + admin.getAdminId() + ", 结果: " + (success ? "成功" : "失败"));
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 重置管理员密码
     * @param adminId 管理员ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    public boolean resetPassword(Integer adminId, String newPassword) {
        if (adminId == null || adminId <= 0 || newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }

        // 如果密码不是哈希值，验证密码复杂度并进行哈希处理
        String passwordHash;
        if (newPassword.length() == 64) {
            passwordHash = newPassword;
        } else {
            // 验证密码复杂度
            PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(newPassword);
            if (!validationResult.isValid()) {
                // 记录密码验证失败日志
                recordPasswordChangeLog(adminId, false, "密码重置失败，密码复杂度不符合要求: " + validationResult.getErrorMessage());
                return false;
            }
            passwordHash = SMUtil.sm3(newPassword);
        }

        boolean success = adminDao.updatePassword(adminId, passwordHash) > 0;

        // 记录密码重置日志
        recordPasswordChangeLog(adminId, success, success ? "管理员密码重置成功" : "管理员密码重置失败");

        return success;
    }
    
    /**
     * 更新管理员状态
     * @param adminId 管理员ID
     * @param status 新状态
     * @return 是否成功
     */
    public boolean updateAdminStatus(Integer adminId, Integer status) {
        if (adminId == null || adminId <= 0 || status == null) {
            return false;
        }
        
        Admin admin = adminDao.getById(adminId);
        if (admin == null) {
            return false;
        }
        
        admin.setStatus(status);
        boolean success = adminDao.update(admin) > 0;
        
        // 记录状态修改日志
        SystemLog log = new SystemLog();
        log.setAdminId(adminId);
        log.setOperation("管理员状态修改");
        log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
        log.setDescription("管理员: " + admin.getLoginName() + ", 新状态: " + (status == 1 ? "启用" : "禁用") + ", 结果: " + (success ? "成功" : "失败"));
        systemLogDao.add(log);
        
        return success;
    }
    

    
    /**
     * 记录密码修改日志
     * @param adminId 管理员ID
     * @param success 是否成功
     * @param details 详细信息
     */
    private void recordPasswordChangeLog(Integer adminId, boolean success, String details) {
        SystemLog log = new SystemLog();
        log.setAdminId(adminId);
        log.setOperation("密码修改");
        log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
        log.setDescription("状态: " + (success ? "成功" : "失败") + ", " + details);
        systemLogDao.add(log);
    }
} 
