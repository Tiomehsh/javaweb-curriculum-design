package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.AdminPermission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 管理员权限数据访问对象
 */
public class AdminPermissionDao extends BaseDao {
    
    /**
     * 添加权限
     * @param permission 权限对象
     * @return 影响的行数
     */
    public int add(AdminPermission permission) {
        String sql = "INSERT INTO admin_permission (admin_id, permission_type, permission_value, " +
                "granted_by, granted_time, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        return executeUpdate(sql,
                permission.getAdminId(),
                permission.getPermissionType(),
                permission.getPermissionValue(),
                permission.getGrantedBy(),
                permission.getGrantedTime() != null ? permission.getGrantedTime() : new Timestamp(System.currentTimeMillis()),
                permission.getStatus() != null ? permission.getStatus() : AdminPermission.STATUS_ACTIVE);
    }
    
    /**
     * 根据ID删除权限
     * @param permissionId 权限ID
     * @return 影响的行数
     */
    public int deleteById(Integer permissionId) {
        String sql = "DELETE FROM admin_permission WHERE permission_id = ?";
        return executeUpdate(sql, permissionId);
    }
    
    /**
     * 更新权限状态
     * @param permissionId 权限ID
     * @param status 新状态
     * @return 影响的行数
     */
    public int updateStatus(Integer permissionId, Integer status) {
        String sql = "UPDATE admin_permission SET status = ? WHERE permission_id = ?";
        return executeUpdate(sql, status, permissionId);
    }
    
    /**
     * 根据ID查询权限
     * @param permissionId 权限ID
     * @return 权限对象
     */
    public AdminPermission getById(Integer permissionId) {
        String sql = "SELECT ap.*, a1.real_name as admin_name, a2.real_name as granted_by_name " +
                "FROM admin_permission ap " +
                "LEFT JOIN admin a1 ON ap.admin_id = a1.admin_id " +
                "LEFT JOIN admin a2 ON ap.granted_by = a2.admin_id " +
                "WHERE ap.permission_id = ?";
        return querySingle(sql, new AdminPermissionRowMapper(), permissionId);
    }
    
    /**
     * 根据管理员ID查询权限列表
     * @param adminId 管理员ID
     * @return 权限列表
     */
    public List<AdminPermission> getByAdminId(Integer adminId) {
        String sql = "SELECT ap.*, a1.real_name as admin_name, a2.real_name as granted_by_name " +
                "FROM admin_permission ap " +
                "LEFT JOIN admin a1 ON ap.admin_id = a1.admin_id " +
                "LEFT JOIN admin a2 ON ap.granted_by = a2.admin_id " +
                "WHERE ap.admin_id = ? ORDER BY ap.create_time DESC";
        return executeQuery(sql, new AdminPermissionRowMapper(), adminId);
    }
    
    /**
     * 根据管理员ID和权限类型查询权限
     * @param adminId 管理员ID
     * @param permissionType 权限类型
     * @return 权限对象
     */
    public AdminPermission getByAdminIdAndType(Integer adminId, String permissionType) {
        String sql = "SELECT ap.*, a1.real_name as admin_name, a2.real_name as granted_by_name " +
                "FROM admin_permission ap " +
                "LEFT JOIN admin a1 ON ap.admin_id = a1.admin_id " +
                "LEFT JOIN admin a2 ON ap.granted_by = a2.admin_id " +
                "WHERE ap.admin_id = ? AND ap.permission_type = ? AND ap.status = 1";
        return querySingle(sql, new AdminPermissionRowMapper(), adminId, permissionType);
    }
    
    /**
     * 查询所有权限
     * @return 权限列表
     */
    public List<AdminPermission> getAll() {
        String sql = "SELECT ap.*, a1.real_name as admin_name, a2.real_name as granted_by_name " +
                "FROM admin_permission ap " +
                "LEFT JOIN admin a1 ON ap.admin_id = a1.admin_id " +
                "LEFT JOIN admin a2 ON ap.granted_by = a2.admin_id " +
                "ORDER BY ap.create_time DESC";
        return executeQuery(sql, new AdminPermissionRowMapper());
    }
    
    /**
     * 根据权限类型查询权限列表
     * @param permissionType 权限类型
     * @return 权限列表
     */
    public List<AdminPermission> getByType(String permissionType) {
        String sql = "SELECT ap.*, a1.real_name as admin_name, a2.real_name as granted_by_name " +
                "FROM admin_permission ap " +
                "LEFT JOIN admin a1 ON ap.admin_id = a1.admin_id " +
                "LEFT JOIN admin a2 ON ap.granted_by = a2.admin_id " +
                "WHERE ap.permission_type = ? ORDER BY ap.create_time DESC";
        return executeQuery(sql, new AdminPermissionRowMapper(), permissionType);
    }
    
    /**
     * 检查管理员是否有指定权限
     * @param adminId 管理员ID
     * @param permissionType 权限类型
     * @return 是否有权限
     */
    public boolean hasPermission(Integer adminId, String permissionType) {
        String sql = "SELECT COUNT(*) FROM admin_permission " +
                "WHERE admin_id = ? AND permission_type = ? AND status = 1";
        return queryCount(sql, adminId, permissionType) > 0;
    }
    
    /**
     * 撤销权限（设置状态为无效）
     * @param adminId 管理员ID
     * @param permissionType 权限类型
     * @return 影响的行数
     */
    public int revokePermission(Integer adminId, String permissionType) {
        String sql = "UPDATE admin_permission SET status = 0 WHERE admin_id = ? AND permission_type = ?";
        return executeUpdate(sql, adminId, permissionType);
    }
    
    /**
     * 权限行映射器
     */
    private static class AdminPermissionRowMapper implements RowMapper<AdminPermission> {
        @Override
        public AdminPermission mapRow(ResultSet rs) throws SQLException {
            AdminPermission permission = new AdminPermission();
            permission.setPermissionId(rs.getInt("permission_id"));
            permission.setAdminId(rs.getInt("admin_id"));
            permission.setPermissionType(rs.getString("permission_type"));
            permission.setPermissionValue(rs.getString("permission_value"));
            permission.setGrantedBy(rs.getInt("granted_by"));
            permission.setGrantedTime(rs.getTimestamp("granted_time"));
            permission.setStatus(rs.getInt("status"));
            permission.setCreateTime(rs.getTimestamp("create_time"));
            permission.setUpdateTime(rs.getTimestamp("update_time"));
            
            // 关联字段
            try {
                permission.setAdminName(rs.getString("admin_name"));
                permission.setGrantedByName(rs.getString("granted_by_name"));
            } catch (SQLException e) {
                // 忽略可能不存在的字段
            }
            
            return permission;
        }
    }
}
