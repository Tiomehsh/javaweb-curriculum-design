package com.example.javawebcurriculumdesign.service;


import com.example.javawebcurriculumdesign.dao.OfficialAppointmentDao;
import com.example.javawebcurriculumdesign.dao.PublicAppointmentDao;
import com.example.javawebcurriculumdesign.dao.SystemLogDao;

import com.example.javawebcurriculumdesign.model.OfficialAppointment;
import com.example.javawebcurriculumdesign.model.PublicAppointment;
import com.example.javawebcurriculumdesign.model.SystemLog;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务类
 * 处理预约相关的业务逻辑，包括公众预约和官方预约
 */
public class AppointmentService {
    private final PublicAppointmentDao publicAppointmentDao = new PublicAppointmentDao();
    private final OfficialAppointmentDao officialAppointmentDao = new OfficialAppointmentDao();

    private final SystemLogDao systemLogDao = new SystemLogDao();
    
    // 预约类型常量
    public static final String TYPE_PUBLIC = "PUBLIC";
    public static final String TYPE_OFFICIAL = "OFFICIAL";
    
    // 预约状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    
    /**
     * 添加公众预约
     * @param appointment 预约对象
     * @return 新增预约的ID，失败返回-1
     */
    public int addPublicAppointment(PublicAppointment appointment) {
        if (appointment == null || appointment.getName() == null || 
            appointment.getVisitTime() == null || appointment.getOrganization() == null) {
            return -1;
        }
        
        try {
            // 设置默认状态为待审核
            if (appointment.getStatus() == null) {
                appointment.setStatus(STATUS_PENDING);
            }
            
            // 添加预约
            int appointmentId = publicAppointmentDao.add(appointment);
            if (appointmentId <= 0) {
                return -1;
            }
            
            return appointmentId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 添加官方预约
     * @param appointment 预约对象
     * @return 新增预约的ID，失败返回-1
     */
    public int addOfficialAppointment(OfficialAppointment appointment) {
        if (appointment == null || appointment.getName() == null || 
            appointment.getVisitTime() == null || appointment.getOrganization() == null || 
            appointment.getVisitDeptId() == null || appointment.getVisitContact() == null || 
            appointment.getVisitReason() == null) {
            return -1;
        }
        
        try {
            // 设置默认状态为待审核
            if (appointment.getStatus() == null) {
                appointment.setStatus(STATUS_PENDING);
            }
            
            // 添加预约
            int appointmentId = officialAppointmentDao.add(appointment);
            if (appointmentId <= 0) {
                return -1;
            }
            
            return appointmentId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 取消预约
     * @param appointmentId 预约ID
     * @param appointmentType 预约类型
     * @param reason 取消原因
     * @return 是否成功
     */
    public boolean cancelAppointment(Integer appointmentId, String appointmentType, String reason) {
        if (appointmentId == null || appointmentId <= 0 || 
            appointmentType == null || reason == null) {
            return false;
        }
        
        boolean success = false;
        
        if (TYPE_PUBLIC.equals(appointmentType)) {
            PublicAppointment appointment = publicAppointmentDao.getById(appointmentId);
            if (appointment == null || STATUS_CANCELLED.equals(appointment.getStatus())) {
                return false;
            }
            
            appointment.setStatus(STATUS_CANCELLED);
            // 在这里记录取消原因，通常需要在数据库中添加一个cancel_reason字段
            success = publicAppointmentDao.update(appointment) > 0;
        } else if (TYPE_OFFICIAL.equals(appointmentType)) {
            OfficialAppointment appointment = officialAppointmentDao.getById(appointmentId);
            if (appointment == null || STATUS_CANCELLED.equals(appointment.getStatus())) {
                return false;
            }
            
            appointment.setStatus(STATUS_CANCELLED);
            // 在这里记录取消原因，通常需要在数据库中添加一个cancel_reason字段
            success = officialAppointmentDao.update(appointment) > 0;
        }
        
        return success;
    }
    
    /**
     * 审核预约
     * @param appointmentId 预约ID
     * @param appointmentType 预约类型
     * @param status 审核状态
     * @param rejectReason 拒绝原因（如果状态为拒绝）
     * @param approverId 审核人ID
     * @return 是否成功
     */
    public boolean approveAppointment(Integer appointmentId, String appointmentType,
                                      String status, String rejectReason, Integer approverId) {
        if (appointmentId == null || appointmentId <= 0 || 
            appointmentType == null || status == null || approverId == null) {
            return false;
        }
        
        if (STATUS_REJECTED.equals(status) && (rejectReason == null || rejectReason.isEmpty())) {
            return false;
        }
        
        boolean success = false;
        
        if (TYPE_PUBLIC.equals(appointmentType)) {
            PublicAppointment appointment = publicAppointmentDao.getById(appointmentId);
            if (appointment == null) {
                return false;
            }
            
            if (STATUS_APPROVED.equals(status)) {
                appointment.setStatus(STATUS_APPROVED);
            } else if (STATUS_REJECTED.equals(status)) {
                appointment.setStatus(STATUS_REJECTED);
                // 在这里记录拒绝原因，通常需要在数据库中添加一个reject_reason字段
            }
            
            success = publicAppointmentDao.update(appointment) > 0;
        } else if (TYPE_OFFICIAL.equals(appointmentType)) {
            OfficialAppointment appointment = officialAppointmentDao.getById(appointmentId);
            if (appointment == null) {
                return false;
            }
            
            if (STATUS_APPROVED.equals(status)) {
                appointment.approve(approverId);
            } else if (STATUS_REJECTED.equals(status)) {
                appointment.reject(approverId);
                // 在这里记录拒绝原因，通常需要在数据库中添加一个reject_reason字段
            }
            
            success = officialAppointmentDao.update(appointment) > 0;
        }
        
        // 记录审核日志
        if (success) {
            String operation = STATUS_APPROVED.equals(status) ? "批准预约" : "拒绝预约";
            SystemLog log = new SystemLog();
            log.setAdminId(approverId);
            log.setOperation(operation);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("预约ID: " + appointmentId + ", 预约类型: " + appointmentType + 
                    (STATUS_REJECTED.equals(status) ? ", 拒绝原因: " + rejectReason : ""));
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 标记预约为已完成
     * @param appointmentId 预约ID
     * @param appointmentType 预约类型
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    public boolean completeAppointment(Integer appointmentId, String appointmentType, Integer operatorId) {
        if (appointmentId == null || appointmentId <= 0 || 
            appointmentType == null || operatorId == null) {
            return false;
        }
        
        boolean success = false;
        
        if (TYPE_PUBLIC.equals(appointmentType)) {
            PublicAppointment appointment = publicAppointmentDao.getById(appointmentId);
            if (appointment == null || !STATUS_APPROVED.equals(appointment.getStatus())) {
                return false;
            }
            
            appointment.setStatus(STATUS_COMPLETED);
            success = publicAppointmentDao.update(appointment) > 0;
        } else if (TYPE_OFFICIAL.equals(appointmentType)) {
            OfficialAppointment appointment = officialAppointmentDao.getById(appointmentId);
            if (appointment == null || !STATUS_APPROVED.equals(appointment.getStatus())) {
                return false;
            }
            
            appointment.setStatus(STATUS_COMPLETED);
            success = officialAppointmentDao.update(appointment) > 0;
        }
        
        // 记录完成预约日志
        if (success) {
            SystemLog log = new SystemLog();
            log.setAdminId(operatorId);
            log.setOperation("完成预约");
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            log.setDescription("预约ID: " + appointmentId + ", 预约类型: " + appointmentType);
            systemLogDao.add(log);
        }
        
        return success;
    }
    
    /**
     * 根据ID获取公众预约
     * @param appointmentId 预约ID
     * @return 预约对象
     */
    public PublicAppointment getPublicAppointmentById(Integer appointmentId) {
        if (appointmentId == null || appointmentId <= 0) {
            return null;
        }
        return publicAppointmentDao.getById(appointmentId);
    }
    
    /**
     * 根据ID获取官方预约
     * @param appointmentId 预约ID
     * @return 预约对象
     */
    public OfficialAppointment getOfficialAppointmentById(Integer appointmentId) {
        if (appointmentId == null || appointmentId <= 0) {
            return null;
        }
        return officialAppointmentDao.getById(appointmentId);
    }
    
    /**
     * 根据加密的身份证号和手机号查询公众预约
     * @param idCardEncrypted 加密的身份证号
     * @param phoneEncrypted 加密的手机号
     * @return 预约列表
     */
    public List<PublicAppointment> getPublicAppointmentsByIdCardAndPhoneEncrypted(String idCardEncrypted, String phoneEncrypted) {
        if (idCardEncrypted == null || phoneEncrypted == null) {
            return null;
        }
        return publicAppointmentDao.getByIdCardAndPhone(idCardEncrypted, phoneEncrypted);
    }
    
    /**
     * 根据加密的身份证号和手机号查询官方预约
     * @param idCardEncrypted 加密的身份证号
     * @param phoneEncrypted 加密的手机号
     * @return 预约列表
     */
    public List<OfficialAppointment> getOfficialAppointmentsByIdCardAndPhoneEncrypted(String idCardEncrypted, String phoneEncrypted) {
        if (idCardEncrypted == null || phoneEncrypted == null) {
            return null;
        }
        return officialAppointmentDao.getByIdCardAndPhone(idCardEncrypted, phoneEncrypted);
    }
    
    /**
     * 查询公众预约
     * @param campus 校区
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约列表
     */
    public List<PublicAppointment> queryPublicAppointments(String campus, String status, 
                                                         Timestamp startDate, Timestamp endDate) {
        // 调整参数以匹配DAO的query方法
        // startApplyTime, endApplyTime, startVisitTime, endVisitTime, campus, organization, name, idCardEncrypted
        List<PublicAppointment> appointments = publicAppointmentDao.query(null, null, null, null, campus, null, null, null);
        
        // 如果指定了状态，进行状态过滤
        if (status != null && !status.isEmpty()) {
            appointments = appointments.stream()
                    .filter(appointment -> status.equals(appointment.getStatus()))
                    .collect(Collectors.toList());
        }
        
        // 如果指定了日期范围，进行日期过滤（按申请时间）
        if (startDate != null || endDate != null) {
            appointments = appointments.stream()
                    .filter(appointment -> {
                        Timestamp applyTime = appointment.getApplyTime();
                        if (applyTime == null) return false;

                        boolean afterStart = startDate == null || applyTime.compareTo(startDate) >= 0;
                        boolean beforeEnd = endDate == null || applyTime.compareTo(endDate) <= 0;

                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        return appointments;
    }
    
    /**
     * 查询官方预约
     * @param visitDeptId 访问部门ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约列表
     */
    public List<OfficialAppointment> queryOfficialAppointments(Integer visitDeptId, String status, 
                                                             Timestamp startDate, Timestamp endDate) {
        // 调整参数以匹配DAO的query方法
        // startApplyTime, endApplyTime, startVisitTime, endVisitTime, campus, organization, name,
        // visitDeptId, visitContact, status
        List<OfficialAppointment> appointments = officialAppointmentDao.query(null, null, null, null, 
                                           null, null, null, visitDeptId, null, status);
        
        // 如果指定了日期范围，进行日期过滤（按申请时间）
        if (startDate != null || endDate != null) {
            appointments = appointments.stream()
                    .filter(appointment -> {
                        Timestamp applyTime = appointment.getApplyTime();
                        if (applyTime == null) return false;

                        boolean afterStart = startDate == null || applyTime.compareTo(startDate) >= 0;
                        boolean beforeEnd = endDate == null || applyTime.compareTo(endDate) <= 0;

                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        return appointments;
    }
    

    
    /**
     * 统计公众预约数量
     * @param campus 校区
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约数量
     */
    public long countPublicAppointments(String campus, String status, 
                                       Timestamp startDate, Timestamp endDate) {
        // 按申请时间统计，调整参数以匹配DAO的countAppointments方法
        // startApplyTime, endApplyTime, startVisitTime, endVisitTime, campus, status
        return publicAppointmentDao.countAppointments(startDate, endDate, null, null, campus, status);
    }
    
    /**
     * 统计官方预约数量
     * @param visitDeptId 访问部门ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约数量
     */
    public long countOfficialAppointments(Integer visitDeptId, String status, 
                                         Timestamp startDate, Timestamp endDate) {
        // 按申请时间统计，调整参数以匹配DAO的countAppointments方法
        // startApplyTime, endApplyTime, startVisitTime, endVisitTime, campus, visitDeptId, status
        return officialAppointmentDao.countAppointments(startDate, endDate, null, null, 
                                                       null, visitDeptId, status);
    }
    
    /**
     * 按校区统计预约数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 校区到预约数量的映射
     */
    public Map<String, Long> countAppointmentsByCampus(Timestamp startDate, Timestamp endDate) {
        // 使用统一的查询方法，按申请时间过滤
        List<PublicAppointment> publicAppointments = queryPublicAppointments(null, null, startDate, endDate);
        List<OfficialAppointment> officialAppointments = queryOfficialAppointments(null, null, startDate, endDate);
        
        // 合并两种预约类型的校区统计
        Map<String, Long> campusStats = publicAppointments.stream()
                .collect(Collectors.groupingBy(PublicAppointment::getCampus, Collectors.counting()));
        
        officialAppointments.forEach(appointment -> {
            String campus = appointment.getCampus();
            campusStats.put(campus, campusStats.getOrDefault(campus, 0L) + 1);
        });
        
        return campusStats;
    }
    
    /**
     * 按状态统计预约数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 状态到预约数量的映射
     */
    public Map<String, Long> countAppointmentsByStatus(Timestamp startDate, Timestamp endDate) {
        // 使用统一的查询方法，按申请时间过滤
        List<PublicAppointment> publicAppointments = queryPublicAppointments(null, null, startDate, endDate);
        List<OfficialAppointment> officialAppointments = queryOfficialAppointments(null, null, startDate, endDate);
        
        // 合并两种预约类型的状态统计
        Map<String, Long> statusStats = publicAppointments.stream()
                .collect(Collectors.groupingBy(PublicAppointment::getStatus, Collectors.counting()));
        
        officialAppointments.forEach(appointment -> {
            String status = appointment.getStatus();
            statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
        });
        
        return statusStats;
    }
    
    /**
     * 根据ID和手机号查询公众预约
     * @param appointmentId 预约ID
     * @param phone 手机号（明文）
     * @return 预约对象，如果未找到则返回null
     */
    public PublicAppointment queryPublicAppointment(int appointmentId, String phone) {
        try {
            // 首先查询预约
            PublicAppointment appointment = publicAppointmentDao.getById(appointmentId);
            
            if (appointment == null) {
                return null;
            }
            
            // 获取手机号密文
            String phoneEncrypted = appointment.getPhoneEncrypted();
            
            // 解密手机号密文
            String decryptedPhone = com.example.javawebcurriculumdesign.util.SMUtil.sm4Decrypt(phoneEncrypted);
            
            // 比较解密后的手机号与提供的手机号是否一致
            if (decryptedPhone != null && decryptedPhone.equals(phone)) {
                return appointment;
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据ID和手机号查询公务预约
     * @param appointmentId 预约ID
     * @param phone 手机号（明文）
     * @return 预约对象，如果未找到则返回null
     */
    public OfficialAppointment queryOfficialAppointment(int appointmentId, String phone) {
        try {
            // 首先查询预约
            OfficialAppointment appointment = officialAppointmentDao.getById(appointmentId);
            
            if (appointment == null) {
                return null;
            }
            
            // 获取手机号密文
            String phoneEncrypted = appointment.getPhoneEncrypted();
            
            // 解密手机号密文
            String decryptedPhone = com.example.javawebcurriculumdesign.util.SMUtil.sm4Decrypt(phoneEncrypted);
            
            // 比较解密后的手机号与提供的手机号是否一致
            if (decryptedPhone != null && decryptedPhone.equals(phone)) {
                return appointment;
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 