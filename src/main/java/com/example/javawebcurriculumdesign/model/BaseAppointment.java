package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 预约实体基类，社会公众预约和公务预约的共同属性
 */
public abstract class BaseAppointment {
    private Integer appointmentId;     // 预约ID
    private String campus;             // 预约校区
    private Timestamp visitTime;       // 预约进校时间
    private String organization;       // 所在单位
    private String name;               // 姓名
    private String idCardEncrypted;    // 加密后的身份证号
    private String phoneEncrypted;     // 加密后的手机号
    private String transportation;     // 交通方式
    private String plateNumber;        // 车牌号（可选）
    private Integer visitors;          // 访问人数
    private Timestamp applyTime;       // 申请时间
    private String status;             // 状态
    private Timestamp createTime;      // 创建时间
    private Timestamp updateTime;      // 更新时间
    
    // 非数据库字段，用于前端显示
    private String idCardMasked;       // 脱敏后的身份证号
    private String phoneMasked;        // 脱敏后的手机号
    private String nameMasked;         // 脱敏后的姓名
    
    // 状态常量
    public static final String STATUS_PENDING = "PENDING";    // 待审核
    public static final String STATUS_APPROVED = "APPROVED";  // 已通过
    public static final String STATUS_REJECTED = "REJECTED";  // 已拒绝
    
    // 默认构造函数
    public BaseAppointment() {
    }
    
    // 带参数的构造函数
    public BaseAppointment(String campus, Timestamp visitTime, String organization,
                           String name, String idCardEncrypted, String phoneEncrypted,
                           String transportation, String plateNumber) {
        this.campus = campus;
        this.visitTime = visitTime;
        this.organization = organization;
        this.name = name;
        this.idCardEncrypted = idCardEncrypted;
        this.phoneEncrypted = phoneEncrypted;
        this.transportation = transportation;
        this.plateNumber = plateNumber;
        this.applyTime = new Timestamp(System.currentTimeMillis());
    }
    
    // Getter和Setter方法
    public Integer getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getCampus() {
        return campus;
    }
    
    public void setCampus(String campus) {
        this.campus = campus;
    }
    
    public Timestamp getVisitTime() {
        return visitTime;
    }
    
    public void setVisitTime(Timestamp visitTime) {
        this.visitTime = visitTime;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIdCardEncrypted() {
        return idCardEncrypted;
    }
    
    public void setIdCardEncrypted(String idCardEncrypted) {
        this.idCardEncrypted = idCardEncrypted;
    }
    
    public String getPhoneEncrypted() {
        return phoneEncrypted;
    }
    
    public void setPhoneEncrypted(String phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }
    
    public String getTransportation() {
        return transportation;
    }
    
    public void setTransportation(String transportation) {
        this.transportation = transportation;
    }
    
    public String getPlateNumber() {
        return plateNumber;
    }
    
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Integer getVisitors() {
        return visitors;
    }

    public void setVisitors(Integer visitors) {
        this.visitors = visitors;
    }

    public Timestamp getApplyTime() {
        return applyTime;
    }
    
    public void setApplyTime(Timestamp applyTime) {
        this.applyTime = applyTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
    
    public String getIdCardMasked() {
        return idCardMasked;
    }
    
    public void setIdCardMasked(String idCardMasked) {
        this.idCardMasked = idCardMasked;
    }
    
    public String getPhoneMasked() {
        return phoneMasked;
    }
    
    public void setPhoneMasked(String phoneMasked) {
        this.phoneMasked = phoneMasked;
    }
    
    public String getNameMasked() {
        return nameMasked;
    }
    
    public void setNameMasked(String nameMasked) {
        this.nameMasked = nameMasked;
    }
    

    
    /**
     * 检查通行码是否有效（当前时间是否在预约时间前后2小时内）
     * @return true表示有效，false表示无效
     */
    public boolean isPassCodeValid() {
        if (visitTime == null || !STATUS_APPROVED.equals(status)) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long visitTimeMillis = visitTime.getTime();
        long twoHoursInMillis = 2L * 60 * 60 * 1000; // 2小时的毫秒数
        
        return currentTime >= (visitTimeMillis - twoHoursInMillis) && 
               currentTime <= (visitTimeMillis + twoHoursInMillis);
    }
    
    /**
     * 获取通行码内容
     * @return 通行码内容（用于生成二维码）
     */
    public String getPassCodeContent() {
        return "姓名: " + (nameMasked != null ? nameMasked : name) + 
               ", 身份证: " + (idCardMasked != null ? idCardMasked : idCardEncrypted) + 
               ", 校区: " + campus + 
               ", 时间: " + visitTime;
    }
} 