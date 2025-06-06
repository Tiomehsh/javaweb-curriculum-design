package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 系统日志实体类
 */
public class SystemLog {
    private Integer logId;             // 日志ID
    private Integer adminId;           // 操作人ID
    private String operation;          // 操作类型
    private String description;        // 操作描述
    private String ipAddress;          // 操作IP地址
    private Timestamp operationTime;   // 操作时间
    private String logHash;            // 日志记录的HMAC-SM3值
    private Timestamp createTime;      // 创建时间
    
    // 非数据库字段，用于前端显示
    private String adminName;          // 操作人姓名
    
    // 操作类型常量
    public static final String OPERATION_LOGIN = "LOGIN";
    public static final String OPERATION_LOGOUT = "LOGOUT";
    public static final String OPERATION_ADD = "ADD";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_QUERY = "QUERY";
    public static final String OPERATION_APPROVE = "APPROVE";
    public static final String OPERATION_REJECT = "REJECT";
    
    // 默认构造函数
    public SystemLog() {
    }
    
    // 带参数的构造函数
    public SystemLog(Integer adminId, String operation, String description, String ipAddress) {
        this.adminId = adminId;
        this.operation = operation;
        this.description = description;
        this.ipAddress = ipAddress;
        this.operationTime = new Timestamp(System.currentTimeMillis());
    }
    
    // Getter和Setter方法
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }
    
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Timestamp getOperationTime() {
        return operationTime;
    }
    
    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }
    
    public String getLogHash() {
        return logHash;
    }
    
    public void setLogHash(String logHash) {
        this.logHash = logHash;
    }
    
    public Timestamp getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * 获取日志内容（用于计算HMAC-SM3值）
     * @return 日志内容字符串
     */
    public String getLogContent() {
        return "LogId:" + logId +
                ",AdminId:" + adminId +
                ",Operation:" + operation +
                ",Description:" + description +
                ",IpAddress:" + ipAddress +
                ",OperationTime:" + operationTime;
    }
    
    @Override
    public String toString() {
        return "SystemLog{" +
                "logId=" + logId +
                ", adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", operation='" + operation + '\'' +
                ", description='" + description + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", operationTime=" + operationTime +
                '}';
    }
} 