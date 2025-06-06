package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 公务预约实体类
 */
public class OfficialAppointment extends BaseAppointment {
    private Integer visitDeptId;      // 公务访问部门ID
    private String visitContact;      // 公务访问接待人
    private String visitReason;       // 来访事由
    private Integer approverId;       // 审核人ID
    private Timestamp approveTime;    // 审核时间
    
    // 非数据库字段，用于前端显示
    private String visitDeptName;     // 公务访问部门名称
    private String approverName;      // 审核人姓名
    
    // 默认构造函数
    public OfficialAppointment() {
        super();
        setStatus(STATUS_PENDING); // 公务预约默认待审核
    }
    
    // 带参数的构造函数
    public OfficialAppointment(String campus, Timestamp visitTime, String organization,
                              String name, String idCardEncrypted, String phoneEncrypted,
                              String transportation, String plateNumber,
                              Integer visitDeptId, String visitContact, String visitReason) {
        super(campus, visitTime, organization, name, idCardEncrypted, phoneEncrypted,
              transportation, plateNumber);
        this.visitDeptId = visitDeptId;
        this.visitContact = visitContact;
        this.visitReason = visitReason;
        setStatus(STATUS_PENDING); // 公务预约默认待审核
    }
    
    // Getter和Setter方法
    public Integer getVisitDeptId() {
        return visitDeptId;
    }
    
    public void setVisitDeptId(Integer visitDeptId) {
        this.visitDeptId = visitDeptId;
    }
    
    public String getVisitContact() {
        return visitContact;
    }
    
    public void setVisitContact(String visitContact) {
        this.visitContact = visitContact;
    }
    
    public String getVisitReason() {
        return visitReason;
    }
    
    public void setVisitReason(String visitReason) {
        this.visitReason = visitReason;
    }
    
    public Integer getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }
    
    public Timestamp getApproveTime() {
        return approveTime;
    }
    
    public void setApproveTime(Timestamp approveTime) {
        this.approveTime = approveTime;
    }
    
    public String getVisitDeptName() {
        return visitDeptName;
    }
    
    public void setVisitDeptName(String visitDeptName) {
        this.visitDeptName = visitDeptName;
    }
    
    public String getApproverName() {
        return approverName;
    }
    
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }
    
    /**
     * 审核通过
     * @param approverId 审核人ID
     */
    public void approve(Integer approverId) {
        this.approverId = approverId;
        this.approveTime = new Timestamp(System.currentTimeMillis());
        setStatus(STATUS_APPROVED);
    }
    
    /**
     * 审核拒绝
     * @param approverId 审核人ID
     */
    public void reject(Integer approverId) {
        this.approverId = approverId;
        this.approveTime = new Timestamp(System.currentTimeMillis());
        setStatus(STATUS_REJECTED);
    }
    
    @Override
    public String toString() {
        return "OfficialAppointment{" +
                "appointmentId=" + getAppointmentId() +
                ", campus='" + getCampus() + '\'' +
                ", visitTime=" + getVisitTime() +
                ", organization='" + getOrganization() + '\'' +
                ", name='" + getName() + '\'' +
                ", visitDeptId=" + visitDeptId +
                ", visitContact='" + visitContact + '\'' +
                ", visitReason='" + visitReason + '\'' +
                ", status='" + getStatus() + '\'' +
                ", approverId=" + approverId +
                ", approveTime=" + approveTime +
                ", applyTime=" + getApplyTime() +
                '}';
    }
} 