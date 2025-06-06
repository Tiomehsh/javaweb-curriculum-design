package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 管理员权限实体类
 */
public class AdminPermission {
    private Integer permissionId;      // 权限ID
    private Integer adminId;           // 管理员ID
    private String permissionType;     // 权限类型
    private String permissionValue;    // 权限值
    private Integer grantedBy;         // 授权人ID
    private Timestamp grantedTime;     // 授权时间
    private Integer status;            // 状态(1:有效 0:无效)
    private Timestamp createTime;      // 创建时间
    private Timestamp updateTime;      // 更新时间
    
    // 权限类型常量
    public static final String TYPE_VIEW_PUBLIC_APPOINTMENT = "VIEW_PUBLIC_APPOINTMENT";
    public static final String TYPE_MANAGE_PUBLIC_APPOINTMENT = "MANAGE_PUBLIC_APPOINTMENT";
    public static final String TYPE_VIEW_ALL_DEPARTMENTS = "VIEW_ALL_DEPARTMENTS";
    public static final String TYPE_MANAGE_DEPARTMENT = "MANAGE_DEPARTMENT";
    
    // 状态常量
    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_INACTIVE = 0;
    
    // 关联字段
    private String adminName;          // 管理员姓名
    private String grantedByName;      // 授权人姓名
    
    // 默认构造函数
    public AdminPermission() {
    }
    
    // 带参数的构造函数
    public AdminPermission(Integer adminId, String permissionType, Integer grantedBy) {
        this.adminId = adminId;
        this.permissionType = permissionType;
        this.grantedBy = grantedBy;
        this.status = STATUS_ACTIVE;
    }
    
    // Getter和Setter方法
    public Integer getPermissionId() {
        return permissionId;
    }
    
    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
    
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public String getPermissionType() {
        return permissionType;
    }
    
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
    
    public String getPermissionValue() {
        return permissionValue;
    }
    
    public void setPermissionValue(String permissionValue) {
        this.permissionValue = permissionValue;
    }
    
    public Integer getGrantedBy() {
        return grantedBy;
    }
    
    public void setGrantedBy(Integer grantedBy) {
        this.grantedBy = grantedBy;
    }
    
    public Timestamp getGrantedTime() {
        return grantedTime;
    }
    
    public void setGrantedTime(Timestamp grantedTime) {
        this.grantedTime = grantedTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
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
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    public String getGrantedByName() {
        return grantedByName;
    }
    
    public void setGrantedByName(String grantedByName) {
        this.grantedByName = grantedByName;
    }
    
    @Override
    public String toString() {
        return "AdminPermission{" +
                "permissionId=" + permissionId +
                ", adminId=" + adminId +
                ", permissionType='" + permissionType + '\'' +
                ", permissionValue='" + permissionValue + '\'' +
                ", grantedBy=" + grantedBy +
                ", grantedTime=" + grantedTime +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", adminName='" + adminName + '\'' +
                ", grantedByName='" + grantedByName + '\'' +
                '}';
    }
}
