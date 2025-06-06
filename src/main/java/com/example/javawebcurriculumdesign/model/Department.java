package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 部门实体类
 */
public class Department {
    private Integer deptId;       // 部门ID
    private String deptType;      // 部门类型（行政部门、直属部门、学院）
    private String deptName;      // 部门名称
    private String contactPerson; // 联系人
    private String contactPhone;  // 联系电话
    private Timestamp createTime; // 创建时间
    private Timestamp updateTime; // 更新时间
    
    // 部门类型常量
    public static final String TYPE_ADMINISTRATION = "行政部门";
    public static final String TYPE_DIRECT = "直属部门";
    public static final String TYPE_COLLEGE = "学院";
    
    // 默认构造函数
    public Department() {
    }
    
    // 带参数的构造函数
    public Department(Integer deptId, String deptType, String deptName) {
        this.deptId = deptId;
        this.deptType = deptType;
        this.deptName = deptName;
    }
    
    // 完整的带参数构造函数
    public Department(Integer deptId, String deptType, String deptName, String contactPerson, String contactPhone) {
        this.deptId = deptId;
        this.deptType = deptType;
        this.deptName = deptName;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
    }
    
    // Getter和Setter方法
    public Integer getDeptId() {
        return deptId;
    }
    
    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }
    
    public String getDeptType() {
        return deptType;
    }
    
    public void setDeptType(String deptType) {
        this.deptType = deptType;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
    
    @Override
    public String toString() {
        return "Department{" +
                "deptId=" + deptId +
                ", deptType='" + deptType + '\'' +
                ", deptName='" + deptName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 