package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.SystemLog;
import com.example.javawebcurriculumdesign.util.SMUtil;
import com.example.javawebcurriculumdesign.util.CryptoConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志数据访问对象
 */
public class SystemLogDao extends BaseDao {
    
    /**
     * 添加系统日志
     * @param log 系统日志对象
     * @return 影响的行数
     */
    public int add(SystemLog log) {
        String sql = "INSERT INTO system_log (admin_id, operation, description, ip_address, operation_time, log_hash) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        // 如果日志内容没有哈希值，生成一个
        if (log.getLogHash() == null || log.getLogHash().isEmpty()) {
            // 使用HMAC-SM3计算日志的哈希值
            String logContent = log.getLogContent();
            String logHash = SMUtil.hmacSM3(logContent, CryptoConfig.getLogHmacKey());
            log.setLogHash(logHash);
        }
        
        return executeUpdate(sql,
                log.getAdminId(),
                log.getOperation(),
                log.getDescription(),
                log.getIpAddress(),
                log.getOperationTime() != null ? log.getOperationTime() : new Timestamp(System.currentTimeMillis()),
                log.getLogHash());
    }
    
    /**
     * 根据ID查询系统日志
     * @param logId 日志ID
     * @return 系统日志对象
     */
    public SystemLog getById(Integer logId) {
        String sql = "SELECT l.*, a.real_name AS admin_name FROM system_log l " +
                "LEFT JOIN admin a ON l.admin_id = a.admin_id " +
                "WHERE l.log_id = ?";
        return querySingle(sql, new SystemLogRowMapper(), logId);
    }
    
    /**
     * 查询所有系统日志
     * @return 系统日志列表
     */
    public List<SystemLog> getAll() {
        String sql = "SELECT l.*, a.real_name AS admin_name FROM system_log l " +
                "LEFT JOIN admin a ON l.admin_id = a.admin_id " +
                "ORDER BY l.operation_time DESC";
        return executeQuery(sql, new SystemLogRowMapper());
    }
    
    /**
     * 根据条件查询系统日志
     * @param adminId 管理员ID
     * @param operation 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 系统日志列表
     */
    public List<SystemLog> query(Integer adminId, String operation, Timestamp startTime, Timestamp endTime) {
        StringBuilder sql = new StringBuilder("SELECT l.*, a.real_name AS admin_name FROM system_log l " +
                "LEFT JOIN admin a ON l.admin_id = a.admin_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (adminId != null) {
            sql.append(" AND l.admin_id = ?");
            params.add(adminId);
        }
        
        if (operation != null && !operation.isEmpty()) {
            sql.append(" AND l.operation = ?");
            params.add(operation);
        }
        
        if (startTime != null) {
            sql.append(" AND l.operation_time >= ?");
            params.add(startTime);
        }
        
        if (endTime != null) {
            sql.append(" AND l.operation_time <= ?");
            params.add(endTime);
        }
        
        sql.append(" ORDER BY l.operation_time DESC");
        
        return executeQuery(sql.toString(), new SystemLogRowMapper(), params.toArray());
    }
    
    /**
     * 验证日志完整性
     * @param log 系统日志对象
     * @return 是否完整
     */
    public boolean verifyLogIntegrity(SystemLog log) {
        if (log == null || log.getLogHash() == null) {
            return false;
        }
        
        String logContent = log.getLogContent();
        String calculatedHash = SMUtil.hmacSM3(logContent, CryptoConfig.getLogHmacKey());

        return calculatedHash.equals(log.getLogHash());
    }
    
    /**
     * 批量验证日志完整性
     * @param logs 系统日志列表
     * @return 完整性验证结果（日志ID -> 是否完整）
     */
    public java.util.Map<Integer, Boolean> batchVerifyLogIntegrity(List<SystemLog> logs) {
        java.util.Map<Integer, Boolean> result = new java.util.HashMap<>();
        
        for (SystemLog log : logs) {
            result.put(log.getLogId(), verifyLogIntegrity(log));
        }
        
        return result;
    }
    
    /**
     * 查询指定时间段内的日志数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数量
     */
    public long countLogs(Timestamp startTime, Timestamp endTime) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM system_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (startTime != null) {
            sql.append(" AND operation_time >= ?");
            params.add(startTime);
        }
        
        if (endTime != null) {
            sql.append(" AND operation_time <= ?");
            params.add(endTime);
        }
        
        return queryCount(sql.toString(), params.toArray());
    }
    
    /**
     * 系统日志行映射器
     */
    private static class SystemLogRowMapper implements RowMapper<SystemLog> {
        @Override
        public SystemLog mapRow(ResultSet rs) throws SQLException {
            SystemLog log = new SystemLog();
            log.setLogId(rs.getInt("log_id"));
            log.setAdminId(rs.getInt("admin_id"));
            log.setOperation(rs.getString("operation"));
            log.setDescription(rs.getString("description"));
            log.setIpAddress(rs.getString("ip_address"));
            log.setOperationTime(rs.getTimestamp("operation_time"));
            log.setLogHash(rs.getString("log_hash"));
            log.setCreateTime(rs.getTimestamp("create_time"));
            
            // 关联字段
            try {
                log.setAdminName(rs.getString("admin_name"));
            } catch (SQLException e) {
                // 忽略可能不存在的字段
            }
            
            return log;
        }
    }
} 