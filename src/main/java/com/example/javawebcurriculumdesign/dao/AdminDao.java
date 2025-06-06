package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.Admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 管理员数据访问对象
 */
public class AdminDao extends BaseDao {
    
    /**
     * 添加管理员
     * @param admin 管理员对象
     * @return 影响的行数
     */
    public int add(Admin admin) {
        String sql = "INSERT INTO admin (login_name, password_hash, real_name, dept_id, phone, role, " +
                "login_attempts, last_password_change, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        return executeUpdate(sql,
                admin.getLoginName(),
                admin.getPasswordHash(),
                admin.getRealName(),
                admin.getDeptId(),
                admin.getPhone(),
                admin.getRole(),
                admin.getLoginAttempts(),
                new Timestamp(System.currentTimeMillis()),
                admin.getStatus());
    }
    
    /**
     * 根据ID删除管理员
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    public int deleteById(Integer adminId) {
        String sql = "DELETE FROM admin WHERE admin_id = ?";
        return executeUpdate(sql, adminId);
    }
    
    /**
     * 更新管理员
     * @param admin 管理员对象
     * @return 影响的行数
     */
    public int update(Admin admin) {
        String sql = "UPDATE admin SET real_name = ?, dept_id = ?, phone = ?, role = ?, status = ? " +
                "WHERE admin_id = ?";
        
        return executeUpdate(sql,
                admin.getRealName(),
                admin.getDeptId(),
                admin.getPhone(),
                admin.getRole(),
                admin.getStatus(),
                admin.getAdminId());
    }
    
    /**
     * 更新管理员密码
     * @param adminId 管理员ID
     * @param passwordHash 加密后的密码
     * @return 影响的行数
     */
    public int updatePassword(Integer adminId, String passwordHash) {
        String sql = "UPDATE admin SET password_hash = ?, last_password_change = ? WHERE admin_id = ?";
        return executeUpdate(sql, passwordHash, new Timestamp(System.currentTimeMillis()), adminId);
    }
    
    /**
     * 更新登录失败次数
     * @param adminId 管理员ID
     * @param loginAttempts 登录失败次数
     * @return 影响的行数
     */
    public int updateLoginAttempts(Integer adminId, Integer loginAttempts) {
        String sql = "UPDATE admin SET login_attempts = ? WHERE admin_id = ?";
        return executeUpdate(sql, loginAttempts, adminId);
    }
    
    /**
     * 更新账户锁定时间
     * @param adminId 管理员ID
     * @param lockedUntil 账户锁定截止时间
     * @return 影响的行数
     */
    public int updateLockedUntil(Integer adminId, Timestamp lockedUntil) {
        String sql = "UPDATE admin SET locked_until = ? WHERE admin_id = ?";
        return executeUpdate(sql, lockedUntil, adminId);
    }

    /**
     * 锁定账户
     * @param adminId 管理员ID
     * @param lockedUntil 锁定截止时间
     * @return 影响的行数
     */
    public int lockAccount(Integer adminId, Timestamp lockedUntil) {
        String sql = "UPDATE admin SET locked_until = ?, login_attempts = 5 WHERE admin_id = ?";
        return executeUpdate(sql, lockedUntil, adminId);
    }

    /**
     * 解锁账户
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    public int unlockAccount(Integer adminId) {
        String sql = "UPDATE admin SET locked_until = NULL, login_attempts = 0 WHERE admin_id = ?";
        return executeUpdate(sql, adminId);
    }
    
    /**
     * 根据ID查询管理员
     * @param adminId 管理员ID
     * @return 管理员对象
     */
    public Admin getById(Integer adminId) {
        String sql = "SELECT a.*, d.dept_name FROM admin a " +
                "LEFT JOIN department d ON a.dept_id = d.dept_id " +
                "WHERE a.admin_id = ?";
        return querySingle(sql, new AdminRowMapper(), adminId);
    }
    
    /**
     * 根据登录名查询管理员
     * @param loginName 登录名
     * @return 管理员对象
     */
    public Admin getByLoginName(String loginName) {
        String sql = "SELECT a.*, d.dept_name FROM admin a " +
                "LEFT JOIN department d ON a.dept_id = d.dept_id " +
                "WHERE a.login_name = ?";
        return querySingle(sql, new AdminRowMapper(), loginName);
    }
    
    /**
     * 查询所有管理员
     * @return 管理员列表
     */
    public List<Admin> getAll() {
        String sql = "SELECT a.*, d.dept_name FROM admin a " +
                "LEFT JOIN department d ON a.dept_id = d.dept_id " +
                "ORDER BY a.admin_id";
        return executeQuery(sql, new AdminRowMapper());
    }
    
    /**
     * 根据部门ID查询管理员
     * @param deptId 部门ID
     * @return 管理员列表
     */
    public List<Admin> getByDeptId(Integer deptId) {
        String sql = "SELECT a.*, d.dept_name FROM admin a " +
                "LEFT JOIN department d ON a.dept_id = d.dept_id " +
                "WHERE a.dept_id = ? ORDER BY a.admin_id";
        return executeQuery(sql, new AdminRowMapper(), deptId);
    }
    
    /**
     * 根据角色查询管理员
     * @param role 角色
     * @return 管理员列表
     */
    public List<Admin> getByRole(String role) {
        String sql = "SELECT a.*, d.dept_name FROM admin a " +
                "LEFT JOIN department d ON a.dept_id = d.dept_id " +
                "WHERE a.role = ? ORDER BY a.admin_id";
        return executeQuery(sql, new AdminRowMapper(), role);
    }
    
    /**
     * 查询管理员总数
     * @return 管理员总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM admin";
        return queryCount(sql);
    }
    
    /**
     * 管理员行映射器
     */
    private static class AdminRowMapper implements RowMapper<Admin> {
        @Override
        public Admin mapRow(ResultSet rs) throws SQLException {
            Admin admin = new Admin();
            admin.setAdminId(rs.getInt("admin_id"));
            admin.setLoginName(rs.getString("login_name"));
            admin.setPasswordHash(rs.getString("password_hash"));
            admin.setRealName(rs.getString("real_name"));
            admin.setDeptId(rs.getInt("dept_id"));
            admin.setPhone(rs.getString("phone"));
            admin.setRole(rs.getString("role"));
            admin.setLoginAttempts(rs.getInt("login_attempts"));
            admin.setLockedUntil(rs.getTimestamp("locked_until"));
            admin.setLastPasswordChange(rs.getTimestamp("last_password_change"));
            admin.setCreateTime(rs.getTimestamp("create_time"));
            admin.setUpdateTime(rs.getTimestamp("update_time"));
            admin.setStatus(rs.getInt("status"));
            
            // 关联字段
            try {
                admin.setDeptName(rs.getString("dept_name"));
            } catch (SQLException e) {
                // 忽略可能不存在的字段
            }
            
            return admin;
        }
    }
} 