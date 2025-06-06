package com.example.javawebcurriculumdesign.model;

import java.sql.Timestamp;

/**
 * 社会公众预约实体类
 */
public class PublicAppointment extends BaseAppointment {
    
    // 默认构造函数
    public PublicAppointment() {
        super();
        setStatus(STATUS_APPROVED); // 社会公众预约默认自动通过
    }
    
    // 带参数的构造函数
    public PublicAppointment(String campus, Timestamp visitTime, String organization,
                            String name, String idCardEncrypted, String phoneEncrypted,
                            String transportation, String plateNumber) {
        super(campus, visitTime, organization, name, idCardEncrypted, phoneEncrypted,
              transportation, plateNumber);
        setStatus(STATUS_APPROVED); // 社会公众预约默认自动通过
    }
    
    @Override
    public String toString() {
        return "PublicAppointment{" +
                "appointmentId=" + getAppointmentId() +
                ", campus='" + getCampus() + '\'' +
                ", visitTime=" + getVisitTime() +
                ", organization='" + getOrganization() + '\'' +
                ", name='" + getName() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", applyTime=" + getApplyTime() +
                '}';
    }
} 