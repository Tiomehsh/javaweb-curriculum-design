package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.PublicAppointment;
import com.example.javawebcurriculumdesign.util.SMUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 社会公众预约数据访问对象
 */
public class PublicAppointmentDao extends BaseDao {
    
    /**
     * 添加社会公众预约
     * @param appointment 社会公众预约对象
     * @return 新增记录的ID，如果添加失败则返回-1
     */
    public int add(PublicAppointment appointment) {
        String sql = "INSERT INTO public_appointment (campus, visit_time, organization, name, " +
                "id_card_encrypted, phone_encrypted, transportation, plate_number, visitors, apply_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = com.example.javawebcurriculumdesign.util.DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, appointment.getCampus());
            ps.setTimestamp(2, appointment.getVisitTime());
            ps.setString(3, appointment.getOrganization());
            ps.setString(4, appointment.getName());
            ps.setString(5, appointment.getIdCardEncrypted());
            ps.setString(6, appointment.getPhoneEncrypted());
            ps.setString(7, appointment.getTransportation());
            ps.setString(8, appointment.getPlateNumber());
            ps.setInt(9, appointment.getVisitors() != null ? appointment.getVisitors() : 1);
            ps.setTimestamp(10, appointment.getApplyTime() != null ? appointment.getApplyTime() : new Timestamp(System.currentTimeMillis()));
            ps.setString(11, appointment.getStatus());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加社会公众预约失败", e);
        } finally {
            com.example.javawebcurriculumdesign.util.DBUtil.close(conn, ps, rs);
        }
    }
    
    /**
     * 根据ID删除社会公众预约
     * @param appointmentId 预约ID
     * @return 影响的行数
     */
    public int deleteById(Integer appointmentId) {
        String sql = "DELETE FROM public_appointment WHERE appointment_id = ?";
        return executeUpdate(sql, appointmentId);
    }
    
    /**
     * 更新社会公众预约
     * @param appointment 社会公众预约对象
     * @return 影响的行数
     */
    public int update(PublicAppointment appointment) {
        String sql = "UPDATE public_appointment SET campus = ?, visit_time = ?, organization = ?, " +
                "name = ?, id_card_encrypted = ?, phone_encrypted = ?, transportation = ?, " +
                "plate_number = ?, visitors = ?, status = ? WHERE appointment_id = ?";
        
        return executeUpdate(sql,
                appointment.getCampus(),
                appointment.getVisitTime(),
                appointment.getOrganization(),
                appointment.getName(),
                appointment.getIdCardEncrypted(),
                appointment.getPhoneEncrypted(),
                appointment.getTransportation(),
                appointment.getPlateNumber(),
                appointment.getVisitors(),
                appointment.getStatus(),
                appointment.getAppointmentId());
    }
    
    /**
     * 根据ID查询社会公众预约
     * @param appointmentId 预约ID
     * @return 社会公众预约对象
     */
    public PublicAppointment getById(Integer appointmentId) {
        String sql = "SELECT * FROM public_appointment WHERE appointment_id = ?";
        PublicAppointment appointment = querySingle(sql, new PublicAppointmentRowMapper(), appointmentId);
        if (appointment != null) {
            maskSensitiveInfo(appointment);
        }
        return appointment;
    }
    
    /**
     * 根据身份证号和手机号查询社会公众预约
     * @param idCardEncrypted 加密后的身份证号
     * @param phoneEncrypted 加密后的手机号
     * @return 社会公众预约列表
     */
    public List<PublicAppointment> getByIdCardAndPhone(String idCardEncrypted, String phoneEncrypted) {
        String sql = "SELECT * FROM public_appointment WHERE id_card_encrypted = ? AND phone_encrypted = ? " +
                "ORDER BY apply_time DESC";
        
        List<PublicAppointment> appointments = executeQuery(sql, new PublicAppointmentRowMapper(), 
                idCardEncrypted, phoneEncrypted);
        
        // 对敏感信息进行脱敏处理
        for (PublicAppointment appointment : appointments) {
            maskSensitiveInfo(appointment);
        }
        
        return appointments;
    }
    
    /**
     * 根据条件查询社会公众预约
     * @param startApplyTime 申请开始时间
     * @param endApplyTime 申请结束时间
     * @param startVisitTime 预约开始时间
     * @param endVisitTime 预约结束时间
     * @param campus 校区
     * @param organization 单位
     * @param name 姓名
     * @param idCardEncrypted 加密后的身份证号
     * @return 社会公众预约列表
     */
    public List<PublicAppointment> query(Timestamp startApplyTime, Timestamp endApplyTime,
                                     Timestamp startVisitTime, Timestamp endVisitTime,
                                     String campus, String organization, String name, String idCardEncrypted) {
        StringBuilder sql = new StringBuilder("SELECT * FROM public_appointment WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (startApplyTime != null) {
            sql.append(" AND apply_time >= ?");
            params.add(startApplyTime);
        }
        
        if (endApplyTime != null) {
            sql.append(" AND apply_time <= ?");
            params.add(endApplyTime);
        }
        
        if (startVisitTime != null) {
            sql.append(" AND visit_time >= ?");
            params.add(startVisitTime);
        }
        
        if (endVisitTime != null) {
            sql.append(" AND visit_time <= ?");
            params.add(endVisitTime);
        }
        
        if (campus != null && !campus.isEmpty()) {
            sql.append(" AND campus = ?");
            params.add(campus);
        }
        
        if (organization != null && !organization.isEmpty()) {
            sql.append(" AND organization LIKE ?");
            params.add("%" + organization + "%");
        }
        
        if (name != null && !name.isEmpty()) {
            sql.append(" AND name = ?");
            params.add(name);
        }
        
        if (idCardEncrypted != null && !idCardEncrypted.isEmpty()) {
            sql.append(" AND id_card_encrypted = ?");
            params.add(idCardEncrypted);
        }
        
        sql.append(" ORDER BY apply_time DESC");
        
        List<PublicAppointment> appointments = executeQuery(sql.toString(), new PublicAppointmentRowMapper(), 
                params.toArray());
        
        // 对敏感信息进行脱敏处理
        for (PublicAppointment appointment : appointments) {
            maskSensitiveInfo(appointment);
        }
        
        return appointments;
    }
    
    /**
     * 统计预约数量
     * @param startApplyTime 申请开始时间
     * @param endApplyTime 申请结束时间
     * @param startVisitTime 预约开始时间
     * @param endVisitTime 预约结束时间
     * @param campus 校区
     * @param status 状态
     * @return 预约数量
     */
    public long countAppointments(Timestamp startApplyTime, Timestamp endApplyTime,
                                 Timestamp startVisitTime, Timestamp endVisitTime,
                                 String campus, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM public_appointment WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (startApplyTime != null) {
            sql.append(" AND apply_time >= ?");
            params.add(startApplyTime);
        }
        
        if (endApplyTime != null) {
            sql.append(" AND apply_time <= ?");
            params.add(endApplyTime);
        }
        
        if (startVisitTime != null) {
            sql.append(" AND visit_time >= ?");
            params.add(startVisitTime);
        }
        
        if (endVisitTime != null) {
            sql.append(" AND visit_time <= ?");
            params.add(endVisitTime);
        }
        
        if (campus != null && !campus.isEmpty()) {
            sql.append(" AND campus = ?");
            params.add(campus);
        }
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        return queryCount(sql.toString(), params.toArray());
    }
    
    /**
     * 对敏感信息进行脱敏处理
     * @param appointment 预约对象
     */
    private void maskSensitiveInfo(PublicAppointment appointment) {
        try {
            // 解密身份证号和手机号
            String idCard = SMUtil.sm4Decrypt(appointment.getIdCardEncrypted());
            String phone = SMUtil.sm4Decrypt(appointment.getPhoneEncrypted());
            
            // 脱敏处理
            appointment.setIdCardMasked(SMUtil.maskIdCard(idCard));
            appointment.setPhoneMasked(SMUtil.maskPhone(phone));
            appointment.setNameMasked(SMUtil.maskName(appointment.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解密失败，直接使用加密后的数据（已经是不可读的）
            appointment.setIdCardMasked(appointment.getIdCardEncrypted());
            appointment.setPhoneMasked(appointment.getPhoneEncrypted());
            appointment.setNameMasked(appointment.getName());
        }
    }
    
    /**
     * 社会公众预约行映射器
     */
    private static class PublicAppointmentRowMapper implements RowMapper<PublicAppointment> {
        @Override
        public PublicAppointment mapRow(ResultSet rs) throws SQLException {
            PublicAppointment appointment = new PublicAppointment();
            appointment.setAppointmentId(rs.getInt("appointment_id"));
            appointment.setCampus(rs.getString("campus"));
            appointment.setVisitTime(rs.getTimestamp("visit_time"));
            appointment.setOrganization(rs.getString("organization"));
            appointment.setName(rs.getString("name"));
            appointment.setIdCardEncrypted(rs.getString("id_card_encrypted"));
            appointment.setPhoneEncrypted(rs.getString("phone_encrypted"));
            appointment.setTransportation(rs.getString("transportation"));
            appointment.setPlateNumber(rs.getString("plate_number"));
            appointment.setVisitors(rs.getInt("visitors"));
            appointment.setApplyTime(rs.getTimestamp("apply_time"));
            appointment.setStatus(rs.getString("status"));
            appointment.setCreateTime(rs.getTimestamp("create_time"));
            appointment.setUpdateTime(rs.getTimestamp("update_time"));
            return appointment;
        }
    }
} 