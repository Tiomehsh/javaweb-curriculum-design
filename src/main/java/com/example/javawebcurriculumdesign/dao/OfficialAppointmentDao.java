package com.example.javawebcurriculumdesign.dao;

import com.example.javawebcurriculumdesign.model.OfficialAppointment;
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
 * 公务预约数据访问对象
 */
public class OfficialAppointmentDao extends BaseDao {
    
    /**
     * 添加公务预约
     * @param appointment 公务预约对象
     * @return 新增记录的ID，如果添加失败则返回-1
     */
    public int add(OfficialAppointment appointment) {
        String sql = "INSERT INTO official_appointment (campus, visit_time, organization, name, " +
                "id_card_encrypted, phone_encrypted, transportation, plate_number, visitors, visit_dept_id, " +
                "visit_contact, visit_reason, apply_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
            ps.setInt(10, appointment.getVisitDeptId());
            ps.setString(11, appointment.getVisitContact());
            ps.setString(12, appointment.getVisitReason());
            ps.setTimestamp(13, appointment.getApplyTime() != null ? appointment.getApplyTime() : new Timestamp(System.currentTimeMillis()));
            ps.setString(14, appointment.getStatus());
            
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
            throw new RuntimeException("添加公务预约失败", e);
        } finally {
            com.example.javawebcurriculumdesign.util.DBUtil.close(conn, ps, rs);
        }
    }
    
    /**
     * 根据ID删除公务预约
     * @param appointmentId 预约ID
     * @return 影响的行数
     */
    public int deleteById(Integer appointmentId) {
        String sql = "DELETE FROM official_appointment WHERE appointment_id = ?";
        return executeUpdate(sql, appointmentId);
    }
    
    /**
     * 更新公务预约
     * @param appointment 公务预约对象
     * @return 影响的行数
     */
    public int update(OfficialAppointment appointment) {
        String sql = "UPDATE official_appointment SET campus = ?, visit_time = ?, organization = ?, " +
                "name = ?, id_card_encrypted = ?, phone_encrypted = ?, transportation = ?, " +
                "plate_number = ?, visitors = ?, visit_dept_id = ?, visit_contact = ?, visit_reason = ?, " +
                "status = ? WHERE appointment_id = ?";
        
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
                appointment.getVisitDeptId(),
                appointment.getVisitContact(),
                appointment.getVisitReason(),
                appointment.getStatus(),
                appointment.getAppointmentId());
    }
    
    /**
     * 审核公务预约
     * @param appointmentId 预约ID
     * @param status 状态（已通过/已拒绝）
     * @param approverId 审核人ID
     * @return 影响的行数
     */
    public int approve(Integer appointmentId, String status, Integer approverId) {
        String sql = "UPDATE official_appointment SET status = ?, approver_id = ?, approve_time = ? " +
                "WHERE appointment_id = ?";
        return executeUpdate(sql, status, approverId, new Timestamp(System.currentTimeMillis()), appointmentId);
    }
    
    /**
     * 根据ID查询公务预约
     * @param appointmentId 预约ID
     * @return 公务预约对象
     */
    public OfficialAppointment getById(Integer appointmentId) {
        String sql = "SELECT o.*, d.dept_name AS visit_dept_name, a.real_name AS approver_name " +
                "FROM official_appointment o " +
                "LEFT JOIN department d ON o.visit_dept_id = d.dept_id " +
                "LEFT JOIN admin a ON o.approver_id = a.admin_id " +
                "WHERE o.appointment_id = ?";
        
        OfficialAppointment appointment = querySingle(sql, new OfficialAppointmentRowMapper(), appointmentId);
        if (appointment != null) {
            maskSensitiveInfo(appointment);
        }
        return appointment;
    }
    
    /**
     * 根据身份证号和手机号查询公务预约
     * @param idCardEncrypted 加密后的身份证号
     * @param phoneEncrypted 加密后的手机号
     * @return 公务预约列表
     */
    public List<OfficialAppointment> getByIdCardAndPhone(String idCardEncrypted, String phoneEncrypted) {
        String sql = "SELECT o.*, d.dept_name AS visit_dept_name, a.real_name AS approver_name " +
                "FROM official_appointment o " +
                "LEFT JOIN department d ON o.visit_dept_id = d.dept_id " +
                "LEFT JOIN admin a ON o.approver_id = a.admin_id " +
                "WHERE o.id_card_encrypted = ? AND o.phone_encrypted = ? " +
                "ORDER BY o.apply_time DESC";
        
        List<OfficialAppointment> appointments = executeQuery(sql, new OfficialAppointmentRowMapper(), 
                idCardEncrypted, phoneEncrypted);
        
        // 对敏感信息进行脱敏处理
        for (OfficialAppointment appointment : appointments) {
            maskSensitiveInfo(appointment);
        }
        
        return appointments;
    }
    
    /**
     * 根据部门ID查询公务预约
     * @param deptId 部门ID
     * @param status 状态（可选）
     * @return 公务预约列表
     */
    public List<OfficialAppointment> getByDeptId(Integer deptId, String status) {
        StringBuilder sql = new StringBuilder("SELECT o.*, d.dept_name AS visit_dept_name, a.real_name AS approver_name " +
                "FROM official_appointment o " +
                "LEFT JOIN department d ON o.visit_dept_id = d.dept_id " +
                "LEFT JOIN admin a ON o.approver_id = a.admin_id " +
                "WHERE o.visit_dept_id = ?");
        
        List<Object> params = new ArrayList<>();
        params.add(deptId);
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        
        sql.append(" ORDER BY o.apply_time DESC");
        
        List<OfficialAppointment> appointments = executeQuery(sql.toString(), new OfficialAppointmentRowMapper(), 
                params.toArray());
        
        // 对敏感信息进行脱敏处理
        for (OfficialAppointment appointment : appointments) {
            maskSensitiveInfo(appointment);
        }
        
        return appointments;
    }
    
    /**
     * 根据条件查询公务预约
     * @param startApplyTime 申请开始时间
     * @param endApplyTime 申请结束时间
     * @param startVisitTime 预约开始时间
     * @param endVisitTime 预约结束时间
     * @param campus 校区
     * @param organization 单位
     * @param name 姓名
     * @param visitDeptId 访问部门ID
     * @param visitContact 访问接待人
     * @param status 状态
     * @return 公务预约列表
     */
    public List<OfficialAppointment> query(Timestamp startApplyTime, Timestamp endApplyTime,
                                     Timestamp startVisitTime, Timestamp endVisitTime,
                                     String campus, String organization, String name,
                                     Integer visitDeptId, String visitContact, String status) {
        StringBuilder sql = new StringBuilder("SELECT o.*, d.dept_name AS visit_dept_name, a.real_name AS approver_name " +
                "FROM official_appointment o " +
                "LEFT JOIN department d ON o.visit_dept_id = d.dept_id " +
                "LEFT JOIN admin a ON o.approver_id = a.admin_id " +
                "WHERE 1=1");
        
        List<Object> params = new ArrayList<>();
        
        if (startApplyTime != null) {
            sql.append(" AND o.apply_time >= ?");
            params.add(startApplyTime);
        }
        
        if (endApplyTime != null) {
            sql.append(" AND o.apply_time <= ?");
            params.add(endApplyTime);
        }
        
        if (startVisitTime != null) {
            sql.append(" AND o.visit_time >= ?");
            params.add(startVisitTime);
        }
        
        if (endVisitTime != null) {
            sql.append(" AND o.visit_time <= ?");
            params.add(endVisitTime);
        }
        
        if (campus != null && !campus.isEmpty()) {
            sql.append(" AND o.campus = ?");
            params.add(campus);
        }
        
        if (organization != null && !organization.isEmpty()) {
            sql.append(" AND o.organization LIKE ?");
            params.add("%" + organization + "%");
        }
        
        if (name != null && !name.isEmpty()) {
            sql.append(" AND o.name = ?");
            params.add(name);
        }
        
        if (visitDeptId != null) {
            sql.append(" AND o.visit_dept_id = ?");
            params.add(visitDeptId);
        }
        
        if (visitContact != null && !visitContact.isEmpty()) {
            sql.append(" AND o.visit_contact LIKE ?");
            params.add("%" + visitContact + "%");
        }
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        
        sql.append(" ORDER BY o.apply_time DESC");
        
        List<OfficialAppointment> appointments = executeQuery(sql.toString(), new OfficialAppointmentRowMapper(), 
                params.toArray());
        
        // 对敏感信息进行脱敏处理
        for (OfficialAppointment appointment : appointments) {
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
     * @param visitDeptId 访问部门ID
     * @param status 状态
     * @return 预约数量
     */
    public long countAppointments(Timestamp startApplyTime, Timestamp endApplyTime,
                               Timestamp startVisitTime, Timestamp endVisitTime,
                               String campus, Integer visitDeptId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM official_appointment WHERE 1=1");
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
        
        if (visitDeptId != null) {
            sql.append(" AND visit_dept_id = ?");
            params.add(visitDeptId);
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
    private void maskSensitiveInfo(OfficialAppointment appointment) {
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
     * 公务预约行映射器
     */
    private static class OfficialAppointmentRowMapper implements RowMapper<OfficialAppointment> {
        @Override
        public OfficialAppointment mapRow(ResultSet rs) throws SQLException {
            OfficialAppointment appointment = new OfficialAppointment();
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
            appointment.setVisitDeptId(rs.getInt("visit_dept_id"));
            appointment.setVisitContact(rs.getString("visit_contact"));
            appointment.setVisitReason(rs.getString("visit_reason"));
            appointment.setApplyTime(rs.getTimestamp("apply_time"));
            appointment.setStatus(rs.getString("status"));
            appointment.setApproverId(rs.getInt("approver_id"));
            appointment.setApproveTime(rs.getTimestamp("approve_time"));
            appointment.setCreateTime(rs.getTimestamp("create_time"));
            appointment.setUpdateTime(rs.getTimestamp("update_time"));
            
            // 关联字段
            try {
                appointment.setVisitDeptName(rs.getString("visit_dept_name"));
                appointment.setApproverName(rs.getString("approver_name"));
            } catch (SQLException e) {
                // 忽略可能不存在的字段
            }
            
            return appointment;
        }
    }
} 