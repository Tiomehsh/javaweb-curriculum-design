package com.example.javawebcurriculumdesign.service;

import com.example.javawebcurriculumdesign.dao.SystemLogDao;
import com.example.javawebcurriculumdesign.model.SystemLog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务类
 * 提供系统日志管理相关的业务逻辑
 */
public class SystemLogService {
    private final SystemLogDao systemLogDao = new SystemLogDao();
    
    /**
     * 添加系统日志
     * @param log 日志对象
     * @return 新增日志的ID，失败返回-1
     */
    public int addLog(SystemLog log) {
        if (log == null || log.getOperation() == null || log.getAdminId() == null) {
            return -1;
        }
        
        // 如果没有设置操作时间，则使用当前时间
        if (log.getOperationTime() == null) {
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
        }
        
        return systemLogDao.add(log);
    }
    
    /**
     * 根据ID获取日志
     * @param logId 日志ID
     * @return 日志对象
     */
    public SystemLog getLogById(Integer logId) {
        if (logId == null || logId <= 0) {
            return null;
        }
        return systemLogDao.getById(logId);
    }
    
    /**
     * 获取所有日志
     * @return 日志列表
     */
    public List<SystemLog> getAllLogs() {
        return systemLogDao.getAll();
    }
    
    /**
     * 按条件查询日志
     * @param adminId 管理员ID
     * @param operation 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    public List<SystemLog> queryLogs(Integer adminId, String operation, Timestamp startTime, Timestamp endTime) {
        return systemLogDao.query(adminId, operation, startTime, endTime);
    }
    
    /**
     * 统计日志数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数量
     */
    public long countLogs(Timestamp startTime, Timestamp endTime) {
        return systemLogDao.countLogs(startTime, endTime);
    }
    
    /**
     * 验证日志完整性
     * @param logId 日志ID
     * @return 是否完整
     */
    public boolean verifyLogIntegrity(Integer logId) {
        if (logId == null || logId <= 0) {
            return false;
        }
        
        SystemLog log = systemLogDao.getById(logId);
        if (log == null) {
            return false;
        }
        
        return systemLogDao.verifyLogIntegrity(log);
    }
    
    /**
     * 批量验证日志完整性
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志ID到完整性状态的映射
     */
    public Map<Integer, Boolean> batchVerifyLogIntegrity(Timestamp startTime, Timestamp endTime) {
        List<SystemLog> logs = systemLogDao.query(null, null, startTime, endTime);
        return systemLogDao.batchVerifyLogIntegrity(logs);
    }
    
    /**
     * 按操作类型统计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作类型到数量的映射
     */
    public Map<String, Long> countLogsByOperation(Timestamp startTime, Timestamp endTime) {
        List<SystemLog> logs = systemLogDao.query(null, null, startTime, endTime);
        
        // 使用Java 8 Stream API进行统计
        return logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SystemLog::getOperation,
                        java.util.stream.Collectors.counting()
                ));
    }
    
    /**
     * 按管理员统计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 管理员ID到数量的映射
     */
    public Map<Integer, Long> countLogsByAdmin(Timestamp startTime, Timestamp endTime) {
        List<SystemLog> logs = systemLogDao.query(null, null, startTime, endTime);
        
        // 使用Java 8 Stream API进行统计
        return logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SystemLog::getAdminId,
                        java.util.stream.Collectors.counting()
                ));
    }
    
    /**
     * 获取近期日志
     * @param days 天数
     * @return 日志列表
     */
    public List<SystemLog> getRecentLogs(int days) {
        if (days <= 0) {
            days = 7; // 默认7天
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        return systemLogDao.query(
                null, 
                null, 
                Timestamp.valueOf(startTime), 
                Timestamp.valueOf(endTime)
        );
    }
} 