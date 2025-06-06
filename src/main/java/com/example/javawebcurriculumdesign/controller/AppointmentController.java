package com.example.javawebcurriculumdesign.controller;

import com.example.javawebcurriculumdesign.model.Admin;

import com.example.javawebcurriculumdesign.model.OfficialAppointment;
import com.example.javawebcurriculumdesign.model.PublicAppointment;
import com.example.javawebcurriculumdesign.service.AdminService;
import com.example.javawebcurriculumdesign.service.AppointmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 预约控制器
 * 处理预约的增删改查请求
 */
@WebServlet("/api/appointment/*")
public class AppointmentController extends HttpServlet {
    private final AppointmentService appointmentService = new AppointmentService();
    private final AdminService adminService = new AdminService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 角色常量
    private static final String ROLE_RECEPTION_ADMIN = "RECEPTION_ADMIN";
    
    /**
 * 处理GET请求
 * /api/appointment/public/list - 获取所有公众预约列表
 * /api/appointment/official/list - 获取所有公务预约列表
 * /api/appointment/public/{id} - 获取指定ID的公众预约信息
 * /api/appointment/official/{id} - 获取指定ID的公务预约信息
 * /api/appointment/public/campus/{campus} - 获取指定校区的公众预约列表
 * /api/appointment/official/dept/{deptId} - 获取指定部门的公务预约列表
 * /api/appointment/official/status/{status} - 获取指定状态的公务预约列表
 * /api/appointment/public/pass-code - 获取公众预约通行码
 * /api/appointment/official/pass-code - 获取公务预约通行码
 * /api/appointment/public/query - 查询公众预约
 * /api/appointment/official/query - 查询公务预约
 * /api/appointment/public/count - 获取公众预约数量统计
 * /api/appointment/official/count - 获取公务预约数量统计
 * /api/appointment/count - 获取预约数量统计
 * /api/appointment/recent - 获取最近预约列表
 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 无需登录的请求
        if (pathInfo.equals("/public/query") || pathInfo.equals("/official/query") || 
            pathInfo.equals("/public/pass-code") || pathInfo.equals("/official/pass-code")) {
            handlePublicRequests(request, response, pathInfo);
            return;
        }
        
        // 统计API - 公众预约数量
        if (pathInfo.equals("/public/count")) {
            handlePublicCountRequest(request, response);
            return;
        }
        
        // 统计API - 公务预约数量
        if (pathInfo.equals("/official/count")) {
            handleOfficialCountRequest(request, response);
            return;
        }
        
        // 统计API - 预约数量（按状态或月份）
        if (pathInfo.equals("/count")) {
            handleCountRequest(request, response);
            return;
        }
        
        // 获取最近预约
        if (pathInfo.equals("/recent")) {
            handleRecentAppointmentsRequest(request, response);
            return;
        }
        
        // 需要登录的请求
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        if (currentAdmin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        if (pathInfo.equals("/public/list")) {
            // 检查权限
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                return;
            }
            
            try {
                // 获取所有公众预约列表
                List<PublicAppointment> appointments = appointmentService.queryPublicAppointments(null, null, null, null);
                
                // 转换为前端需要的格式
                List<Map<String, Object>> result = appointments.stream().map(appointment -> {
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("purpose", "参观访问");
                    appointmentData.put("remarks", "");
                    
                    return appointmentData;
                }).collect(Collectors.toList());
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load appointments: " + e.getMessage());
            }
        } else if (pathInfo.equals("/official/list")) {
            // 检查权限
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN) &&
                !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                return;
            }
            
            try {
                // 获取所有公务预约列表
                List<OfficialAppointment> appointments = appointmentService.queryOfficialAppointments(null, null, null, null);
                
                // 转换为前端需要的格式
                List<Map<String, Object>> result = appointments.stream().map(appointment -> {
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    
                    // 公务预约特有字段
                    appointmentData.put("visitDeptId", appointment.getVisitDeptId());
                    appointmentData.put("visitDeptName", appointment.getVisitDeptName());
                    appointmentData.put("visitContact", appointment.getVisitContact());
                    appointmentData.put("purpose", appointment.getVisitReason());
                    appointmentData.put("approverId", appointment.getApproverId());
                    appointmentData.put("approveTime", appointment.getApproveTime());
                    appointmentData.put("approverName", appointment.getApproverName());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("remarks", "");
                    appointmentData.put("officialTitle", "");
                    
                    return appointmentData;
                }).collect(Collectors.toList());
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load appointments: " + e.getMessage());
            }
        } else if (pathInfo.startsWith("/public/")) {
            // 检查权限
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                return;
            }
            
            if (pathInfo.startsWith("/public/campus/")) {
                // 获取指定校区的公众预约列表
                String campus = pathInfo.substring(15);
                List<PublicAppointment> appointments = appointmentService.queryPublicAppointments(campus, null, null, null);
                objectMapper.writeValue(response.getOutputStream(), appointments);
            } else {
                try {
                    // 尝试获取路径中的ID
                    int appointmentId = Integer.parseInt(pathInfo.substring(8));
                    
                    // 获取指定ID的公众预约信息
                    PublicAppointment appointment = appointmentService.getPublicAppointmentById(appointmentId);
                    if (appointment == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                        return;
                    }
                    
                    // 转换为前端需要的格式
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("purpose", "参观访问");
                    appointmentData.put("remarks", "");
                    
                    objectMapper.writeValue(response.getOutputStream(), appointmentData);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
                }
            }
        } else if (pathInfo.startsWith("/official/")) {
            // 检查权限
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN) &&
                !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                return;
            }
            
            // 如果是部门管理员，只能查看自己部门的预约
            if (adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN) && 
                !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) &&
                !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                int deptId = currentAdmin.getDeptId();
                if (pathInfo.equals("/official/list")) {
                    List<OfficialAppointment> appointments = appointmentService.queryOfficialAppointments(deptId, null, null, null);
                    objectMapper.writeValue(response.getOutputStream(), appointments);
                    return;
                } else if (!pathInfo.startsWith("/official/dept/" + deptId)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to view other department's appointments");
                    return;
                }
            }
            
            if (pathInfo.startsWith("/official/dept/")) {
                // 获取指定部门的公务预约列表
                try {
                    int deptId = Integer.parseInt(pathInfo.substring(15));
                    List<OfficialAppointment> appointments = appointmentService.queryOfficialAppointments(deptId, null, null, null);
                    objectMapper.writeValue(response.getOutputStream(), appointments);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
                }
            } else if (pathInfo.startsWith("/official/status/")) {
                // 获取指定状态的公务预约列表
                try {
                    String status = pathInfo.substring(17);
                    List<OfficialAppointment> appointments = appointmentService.queryOfficialAppointments(null, status, null, null);
                    objectMapper.writeValue(response.getOutputStream(), appointments);
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
                }
            } else {
                try {
                    // 尝试获取路径中的ID
                    int appointmentId = Integer.parseInt(pathInfo.substring(10));
                    
                    // 获取指定ID的公务预约信息
                    OfficialAppointment appointment = appointmentService.getOfficialAppointmentById(appointmentId);
                    if (appointment == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                        return;
                    }
                    
                    // 如果是部门管理员，只能查看自己部门的预约
                    if (adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN) && 
                        !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) &&
                        !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                        if (appointment.getVisitDeptId() != currentAdmin.getDeptId()) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to view other department's appointments");
                            return;
                        }
                    }
                    
                    // 转换为前端需要的格式
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    
                    // 公务预约特有字段
                    appointmentData.put("visitDeptId", appointment.getVisitDeptId());
                    appointmentData.put("visitDeptName", appointment.getVisitDeptName());
                    appointmentData.put("visitContact", appointment.getVisitContact());
                    appointmentData.put("purpose", appointment.getVisitReason());
                    appointmentData.put("approverId", appointment.getApproverId());
                    appointmentData.put("approveTime", appointment.getApproveTime());
                    appointmentData.put("approverName", appointment.getApproverName());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("remarks", "");
                    appointmentData.put("officialTitle", "");
                    
                    objectMapper.writeValue(response.getOutputStream(), appointmentData);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
        }
    }
    
    /**
     * 处理POST请求
     * /api/appointment/public/add - 添加公众预约
     * /api/appointment/official/add - 添加公务预约
     * /api/appointment/official/approve/{id} - 批准公务预约
     * /api/appointment/official/reject/{id} - 拒绝公务预约
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/public/add")) {
            // 添加公众预约（不需要登录权限）
            try {
                // 解析前端发送的数据
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                // 创建PublicAppointment对象
                PublicAppointment appointment = new PublicAppointment();
                appointment.setCampus((String) requestData.get("campus"));
                appointment.setOrganization((String) requestData.get("organization"));
                appointment.setName((String) requestData.get("name"));
                
                // 设置交通方式默认值（前端可能没有提供）
                String transportation = (String) requestData.get("transportation");
                appointment.setTransportation(transportation != null ? transportation : "其他");
                
                appointment.setPlateNumber((String) requestData.get("plateNumber"));
                
                // 处理visitTime字段
                String visitTimeStr = (String) requestData.get("visitTime");
                if (visitTimeStr != null && !visitTimeStr.isEmpty()) {
                    try {
                        // 转换ISO格式时间为Timestamp
                        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(visitTimeStr.replace("T", " "), 
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        appointment.setVisitTime(Timestamp.valueOf(localDateTime));
                    } catch (Exception e) {
                        // 如果解析失败，尝试直接解析
                        appointment.setVisitTime(Timestamp.valueOf(visitTimeStr));
                    }
                }
                
                // 加密敏感信息
                String idCard = (String) requestData.get("idCard");
                String phone = (String) requestData.get("phone");
                
                if (idCard != null && !idCard.isEmpty()) {
                    String encryptedIdCard = com.example.javawebcurriculumdesign.util.SMUtil.sm4Encrypt(idCard);
                    appointment.setIdCardEncrypted(encryptedIdCard);
                }
                
                if (phone != null && !phone.isEmpty()) {
                    String encryptedPhone = com.example.javawebcurriculumdesign.util.SMUtil.sm4Encrypt(phone);
                    appointment.setPhoneEncrypted(encryptedPhone);
                }
                
                // 处理访问人数
                Object visitorsObj = requestData.get("visitors");
                if (visitorsObj != null) {
                    if (visitorsObj instanceof String) {
                        appointment.setVisitors(Integer.parseInt((String) visitorsObj));
                    } else if (visitorsObj instanceof Integer) {
                        appointment.setVisitors((Integer) visitorsObj);
                    }
                } else {
                    appointment.setVisitors(1); // 默认值
                }
                
                // 设置申请时间
                appointment.setApplyTime(new Timestamp(System.currentTimeMillis()));
                
                // 显式设置状态为待审核（覆盖PublicAppointment默认的APPROVED状态）
                appointment.setStatus(AppointmentService.STATUS_PENDING);
                
                // 添加公众预约
                int appointmentId = appointmentService.addPublicAppointment(appointment);
                
                Map<String, Object> result = new HashMap<>();
                if (appointmentId > 0) {
                    result.put("success", true);
                    result.put("appointmentId", appointmentId);
                    result.put("message", "预约提交成功");
                } else {
                    result.put("success", false);
                    result.put("message", "预约提交失败，请检查预约信息");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "预约提交失败：" + e.getMessage());
                objectMapper.writeValue(response.getOutputStream(), result);
            }
        } else if (pathInfo.equals("/official/add")) {
            // 添加公务预约（不需要登录权限）
            try {
                // 解析前端发送的数据
                @SuppressWarnings("unchecked")
                Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                
                // 创建OfficialAppointment对象
                OfficialAppointment appointment = new OfficialAppointment();
                appointment.setCampus((String) requestData.get("campus"));
                appointment.setOrganization((String) requestData.get("unitName")); // 前端字段是unitName
                appointment.setName((String) requestData.get("name"));
                
                // 设置交通方式默认值（前端可能没有提供）
                String transportation = (String) requestData.get("transportation");
                appointment.setTransportation(transportation != null ? transportation : "其他");
                
                appointment.setPlateNumber((String) requestData.get("plateNumber"));
                
                // 处理visitTime字段
                String visitTimeStr = (String) requestData.get("visitTime");
                if (visitTimeStr != null && !visitTimeStr.isEmpty()) {
                    try {
                        // 转换ISO格式时间为Timestamp
                        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(visitTimeStr.replace("T", " "), 
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        appointment.setVisitTime(Timestamp.valueOf(localDateTime));
                    } catch (Exception e) {
                        // 如果解析失败，尝试直接解析
                        appointment.setVisitTime(Timestamp.valueOf(visitTimeStr));
                    }
                }
                
                // 加密敏感信息
                String idCard = (String) requestData.get("idCard");
                String phone = (String) requestData.get("phone");
                
                if (idCard != null && !idCard.isEmpty()) {
                    String encryptedIdCard = com.example.javawebcurriculumdesign.util.SMUtil.sm4Encrypt(idCard);
                    appointment.setIdCardEncrypted(encryptedIdCard);
                }
                
                if (phone != null && !phone.isEmpty()) {
                    String encryptedPhone = com.example.javawebcurriculumdesign.util.SMUtil.sm4Encrypt(phone);
                    appointment.setPhoneEncrypted(encryptedPhone);
                }
                
                // 设置公务预约特有字段
                Object visitDeptIdObj = requestData.get("visitDeptId");
                if (visitDeptIdObj != null) {
                    if (visitDeptIdObj instanceof String) {
                        appointment.setVisitDeptId(Integer.parseInt((String) visitDeptIdObj));
                    } else if (visitDeptIdObj instanceof Integer) {
                        appointment.setVisitDeptId((Integer) visitDeptIdObj);
                    }
                }
                
                // 设置接待人（前端可能没有提供，设置默认值）
                String visitContact = (String) requestData.get("visitContact");
                appointment.setVisitContact(visitContact != null ? visitContact : "待指定");
                
                appointment.setVisitReason((String) requestData.get("purpose")); // 前端字段是purpose
                
                // 处理访问人数
                Object visitorsObj = requestData.get("visitors");
                if (visitorsObj != null) {
                    if (visitorsObj instanceof String) {
                        appointment.setVisitors(Integer.parseInt((String) visitorsObj));
                    } else if (visitorsObj instanceof Integer) {
                        appointment.setVisitors((Integer) visitorsObj);
                    }
                } else {
                    appointment.setVisitors(1); // 默认值
                }
                
                // 设置申请时间
                appointment.setApplyTime(new Timestamp(System.currentTimeMillis()));
                
                // 显式设置状态为待审核
                appointment.setStatus(AppointmentService.STATUS_PENDING);
                
                // 添加公务预约
                int appointmentId = appointmentService.addOfficialAppointment(appointment);
                
                Map<String, Object> result = new HashMap<>();
                if (appointmentId > 0) {
                    result.put("success", true);
                    result.put("appointmentId", appointmentId);
                    result.put("message", "预约提交成功");
                } else {
                    result.put("success", false);
                    result.put("message", "预约提交失败，请检查预约信息");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "预约提交失败：" + e.getMessage());
                objectMapper.writeValue(response.getOutputStream(), result);
            }
        } else if (pathInfo.startsWith("/official/approve/")) {
            // 批准公务预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(18));
                
                // 获取公务预约信息
                OfficialAppointment appointment = appointmentService.getOfficialAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限（系统管理员、接待管理员可以批准任何预约，部门管理员只能批准自己部门的预约）
                if (adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) &&
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    if (appointment.getVisitDeptId() != currentAdmin.getDeptId()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to approve other department's appointments");
                        return;
                    }
                }
                
                // 批准公务预约
                boolean success = appointmentService.approveAppointment(appointmentId, AppointmentService.TYPE_OFFICIAL, 
                        AppointmentService.STATUS_APPROVED, null, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已批准");
                } else {
                    result.put("message", "预约批准失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/official/reject/")) {
            // 拒绝公务预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(17));
                
                // 获取公务预约信息
                OfficialAppointment appointment = appointmentService.getOfficialAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限（系统管理员、接待管理员可以拒绝任何预约，部门管理员只能拒绝自己部门的预约）
                if (adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) &&
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    if (appointment.getVisitDeptId() != currentAdmin.getDeptId()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to reject other department's appointments");
                        return;
                    }
                }
                
                // 从请求中获取拒绝原因
                @SuppressWarnings("unchecked")
                Map<String, String> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                String rejectReason = requestData.get("rejectReason");
                
                // 拒绝公务预约
                boolean success = appointmentService.approveAppointment(appointmentId, AppointmentService.TYPE_OFFICIAL, 
                        AppointmentService.STATUS_REJECTED, rejectReason, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已拒绝");
                } else {
                    result.put("message", "预约拒绝失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/public/approve/")) {
            // 批准公众预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(16));
                
                // 获取公众预约信息
                PublicAppointment appointment = appointmentService.getPublicAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                // 批准公众预约
                boolean success = appointmentService.approveAppointment(appointmentId, AppointmentService.TYPE_PUBLIC, 
                        AppointmentService.STATUS_APPROVED, null, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已批准");
                } else {
                    result.put("message", "预约批准失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/public/reject/")) {
            // 拒绝公众预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(15));
                
                // 获取公众预约信息
                PublicAppointment appointment = appointmentService.getPublicAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                // 从请求中获取拒绝原因
                @SuppressWarnings("unchecked")
                Map<String, String> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                String rejectReason = requestData.get("rejectReason");
                
                // 拒绝公众预约
                boolean success = appointmentService.approveAppointment(appointmentId, AppointmentService.TYPE_PUBLIC, 
                        AppointmentService.STATUS_REJECTED, rejectReason, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已拒绝");
                } else {
                    result.put("message", "预约拒绝失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/public/complete/")) {
            // 完成公众预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(17));
                
                // 获取公众预约信息
                PublicAppointment appointment = appointmentService.getPublicAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                // 完成公众预约
                boolean success = appointmentService.completeAppointment(appointmentId, AppointmentService.TYPE_PUBLIC, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已完成");
                } else {
                    result.put("message", "预约完成失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/official/complete/")) {
            // 完成公务预约（需要登录权限）
            
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(19));
                
                // 获取公务预约信息
                OfficialAppointment appointment = appointmentService.getOfficialAppointmentById(appointmentId);
                if (appointment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
                    return;
                }
                
                // 检查权限（系统管理员、接待管理员可以完成任何预约，部门管理员只能完成自己部门的预约）
                if (adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_DEPARTMENT_ADMIN) && 
                    !adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) &&
                    !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_RECEPTION_ADMIN)) {
                    if (appointment.getVisitDeptId() != currentAdmin.getDeptId()) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to complete other department's appointments");
                        return;
                    }
                }
                
                // 完成公务预约
                boolean success = appointmentService.completeAppointment(appointmentId, AppointmentService.TYPE_OFFICIAL, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已完成");
                } else {
                    result.put("message", "预约完成失败，可能是预约状态不正确");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
        }
    }
    
    /**
     * 处理PUT请求
     * /api/appointment/public/cancel/{id} - 取消公众预约
     * /api/appointment/official/cancel/{id} - 取消公务预约
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.startsWith("/public/cancel/")) {
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(15));
                
                // 获取取消原因
                @SuppressWarnings("unchecked")
                Map<String, String> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                String cancelReason = requestData.get("cancelReason");
                
                // 取消公众预约
                boolean success = appointmentService.cancelAppointment(appointmentId, AppointmentService.TYPE_PUBLIC, cancelReason);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已取消");
                } else {
                    result.put("message", "预约取消失败，可能是预约状态不正确或已过取消时限");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else if (pathInfo.startsWith("/official/cancel/")) {
            try {
                // 尝试获取路径中的ID
                int appointmentId = Integer.parseInt(pathInfo.substring(17));
                
                // 获取取消原因
                @SuppressWarnings("unchecked")
                Map<String, String> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
                String cancelReason = requestData.get("cancelReason");
                
                // 取消公务预约
                boolean success = appointmentService.cancelAppointment(appointmentId, AppointmentService.TYPE_OFFICIAL, cancelReason);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "预约已取消");
                } else {
                    result.put("message", "预约取消失败，可能是预约状态不正确或已过取消时限");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
        }
    }
    
    /**
     * 处理DELETE请求（在本应用中不使用，预约不应该被删除，而是通过状态标记）
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "DELETE method is not supported for appointments");
    }
    
    /**
     * 处理公众请求（无需登录）
     * @param request HTTP请求
     * @param response HTTP响应
     * @param pathInfo 路径信息
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void handlePublicRequests(HttpServletRequest request, HttpServletResponse response, String pathInfo) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 获取请求参数
        String idStr = request.getParameter("id");
        String phone = request.getParameter("phone");
        
        if (idStr == null || phone == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "参数不完整，请提供预约ID和联系电话");
            objectMapper.writeValue(response.getOutputStream(), result);
            return;
        }
        
        try {
            int appointmentId = Integer.parseInt(idStr);
            
            if (pathInfo.equals("/public/query")) {
                // 查询公众预约
                PublicAppointment appointment = appointmentService.queryPublicAppointment(appointmentId, phone);
                
                Map<String, Object> result = new HashMap<>();
                if (appointment != null) {
                    // 转换为前端期望的格式
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("id", appointment.getAppointmentId());
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("purpose", "参观访问");
                    appointmentData.put("remarks", "");
                    
                    result.put("success", true);
                    result.put("appointment", appointmentData);
                } else {
                    result.put("success", false);
                    result.put("message", "未找到匹配的预约记录");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } else if (pathInfo.equals("/official/query")) {
                // 查询公务预约
                OfficialAppointment appointment = appointmentService.queryOfficialAppointment(appointmentId, phone);
                
                Map<String, Object> result = new HashMap<>();
                if (appointment != null) {
                    // 转换为前端期望的格式
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("id", appointment.getAppointmentId());
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("name", appointment.getName());
                    appointmentData.put("phone", appointment.getPhoneMasked());
                    appointmentData.put("organization", appointment.getOrganization());
                    appointmentData.put("campus", appointment.getCampus());
                    appointmentData.put("visitTime", appointment.getVisitTime());
                    appointmentData.put("transportation", appointment.getTransportation());
                    appointmentData.put("plateNumber", appointment.getPlateNumber());
                    appointmentData.put("applyTime", appointment.getApplyTime());
                    appointmentData.put("status", appointment.getStatus());
                    appointmentData.put("createTime", appointment.getCreateTime());
                    appointmentData.put("updateTime", appointment.getUpdateTime());
                    appointmentData.put("idCardMasked", appointment.getIdCardMasked());
                    
                    // 公务预约特有字段
                    appointmentData.put("visitDeptId", appointment.getVisitDeptId());
                    appointmentData.put("visitDeptName", appointment.getVisitDeptName());
                    appointmentData.put("visitContact", appointment.getVisitContact());
                    appointmentData.put("purpose", appointment.getVisitReason());
                    appointmentData.put("approverId", appointment.getApproverId());
                    appointmentData.put("approveTime", appointment.getApproveTime());
                    appointmentData.put("approverName", appointment.getApproverName());
                    
                    // 使用数据库中存储的访问人数
                    appointmentData.put("visitors", appointment.getVisitors());
                    appointmentData.put("remarks", "");
                    appointmentData.put("officialTitle", "");
                    appointmentData.put("unitName", appointment.getOrganization());
                    
                    result.put("success", true);
                    result.put("appointment", appointmentData);
                } else {
                    result.put("success", false);
                    result.put("message", "未找到匹配的预约记录");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } else if (pathInfo.equals("/public/pass-code")) {
                // 获取公众预约通行码
                PublicAppointment appointment = appointmentService.queryPublicAppointment(appointmentId, phone);
                
                Map<String, Object> result = new HashMap<>();
                if (appointment != null) {
                    if (appointment.getStatus().equals(AppointmentService.STATUS_APPROVED)) {
                        // 生成通行码数据
                        generatePassCode(result, appointment);
                    } else {
                        result.put("success", false);
                        result.put("message", "该预约尚未批准，无法生成通行码");
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "未找到匹配的预约记录");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } else if (pathInfo.equals("/official/pass-code")) {
                // 获取公务预约通行码
                OfficialAppointment appointment = appointmentService.queryOfficialAppointment(appointmentId, phone);
                
                Map<String, Object> result = new HashMap<>();
                if (appointment != null) {
                    if (appointment.getStatus().equals(AppointmentService.STATUS_APPROVED)) {
                        // 生成通行码数据
                        generatePassCode(result, appointment);
                    } else {
                        result.put("success", false);
                        result.put("message", "该预约尚未批准，无法生成通行码");
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "未找到匹配的预约记录");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            }
        } catch (NumberFormatException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无效的预约ID");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "生成通行码失败: " + e.getMessage());
            objectMapper.writeValue(response.getOutputStream(), result);
        }
    }
    
    /**
     * 生成通行码数据
     * @param result 结果Map
     * @param appointment 预约对象
     * @throws Exception 异常
     */
    private void generatePassCode(Map<String, Object> result, Object appointment) throws Exception {
        // 获取预约信息
        String nameMasked = "";
        String idCardMasked = "";
        String campus = "";
        java.sql.Timestamp visitTime = null;
        
        if (appointment instanceof PublicAppointment) {
            PublicAppointment pa = (PublicAppointment) appointment;
            nameMasked = pa.getNameMasked();
            idCardMasked = pa.getIdCardMasked();
            campus = pa.getCampus();
            visitTime = pa.getVisitTime();
        } else if (appointment instanceof OfficialAppointment) {
            OfficialAppointment oa = (OfficialAppointment) appointment;
            nameMasked = oa.getNameMasked();
            idCardMasked = oa.getIdCardMasked();
            campus = oa.getCampus();
            visitTime = oa.getVisitTime();
        }
        
        // 检查预约时间是否有效（允许提前24小时和延后6小时）
        boolean isValid = false;
        java.util.Date now = new java.util.Date();
        long visitTimeMillis = visitTime.getTime();
        long nowMillis = now.getTime();
        long diff = visitTimeMillis - nowMillis;
        long hoursDiff = diff / (1000 * 60 * 60);
        
        if (hoursDiff <= 24 && hoursDiff >= -6) {
            isValid = true;
        }
        
        // 生成二维码内容
        String qrCodeContent = "姓名: " + nameMasked + "\n" +
                               "身份证号: " + idCardMasked + "\n" +
                               "校区: " + campus + "\n" +
                               "预约时间: " + visitTime.toString() + "\n" +
                               "生成时间: " + now.toString();
        
        // 生成二维码图像
        String qrCodeBase64;
        if (isValid) {
            qrCodeBase64 = com.example.javawebcurriculumdesign.util.QRCodeUtil.generateValidQRCodeBase64(qrCodeContent);
        } else {
            qrCodeBase64 = com.example.javawebcurriculumdesign.util.QRCodeUtil.generateInvalidQRCodeBase64(qrCodeContent);
        }
        
        // 构建返回数据
        result.put("success", true);
        result.put("isValid", isValid);
        result.put("nameMasked", nameMasked);
        result.put("idCardMasked", idCardMasked);
        result.put("campus", campus);
        result.put("visitTime", visitTime);
        result.put("generateTime", now);
        result.put("qrCodeBase64", qrCodeBase64);
    }
    
    /**
     * 处理公众预约数量统计请求
     * /api/appointment/public/count?today=true - 获取今日公众预约数量
     */
    private void handlePublicCountRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String todayParam = request.getParameter("today");
        boolean isToday = todayParam != null && todayParam.equals("true");
        
        try {
            Map<String, Object> result = new HashMap<>();
            long count = 0;
            
            if (isToday) {
                // 获取今天的开始和结束时间
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Timestamp startDate = new Timestamp(cal.getTimeInMillis());
                
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp endDate = new Timestamp(cal.getTimeInMillis());
                
                // 统计今日公众预约数量
                count = appointmentService.countPublicAppointments(null, null, startDate, endDate);
            } else {
                // 统计所有公众预约数量
                count = appointmentService.countPublicAppointments(null, null, null, null);
            }
            
            result.put("count", count);
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "统计公众预约数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理公务预约数量统计请求
     * /api/appointment/official/count?today=true - 获取今日公务预约数量
     */
    private void handleOfficialCountRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String todayParam = request.getParameter("today");
        boolean isToday = todayParam != null && todayParam.equals("true");
        
        try {
            Map<String, Object> result = new HashMap<>();
            long count = 0;
            
            if (isToday) {
                // 获取今天的开始和结束时间
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Timestamp startDate = new Timestamp(cal.getTimeInMillis());
                
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp endDate = new Timestamp(cal.getTimeInMillis());
                
                // 统计今日公务预约数量
                count = appointmentService.countOfficialAppointments(null, null, startDate, endDate);
            } else {
                // 统计所有公务预约数量
                count = appointmentService.countOfficialAppointments(null, null, null, null);
            }
            
            result.put("count", count);
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "统计公务预约数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理预约数量统计请求
     * /api/appointment/count?status=PENDING - 获取待处理预约数量
     * /api/appointment/count?month=true - 获取本月预约数量
     */
    private void handleCountRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String statusParam = request.getParameter("status");
        String monthParam = request.getParameter("month");
        boolean isMonth = monthParam != null && monthParam.equals("true");
        
        try {
            Map<String, Object> result = new HashMap<>();
            long count = 0;
            
            if (statusParam != null && !statusParam.isEmpty()) {
                // 统计指定状态的预约数量
                long publicCount = appointmentService.countPublicAppointments(null, statusParam, null, null);
                long officialCount = appointmentService.countOfficialAppointments(null, statusParam, null, null);
                count = publicCount + officialCount;
            } else if (isMonth) {
                // 获取本月的开始和结束时间
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Timestamp startDate = new Timestamp(cal.getTimeInMillis());
                
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp endDate = new Timestamp(cal.getTimeInMillis());
                
                // 统计本月预约数量
                long publicCount = appointmentService.countPublicAppointments(null, null, startDate, endDate);
                long officialCount = appointmentService.countOfficialAppointments(null, null, startDate, endDate);
                count = publicCount + officialCount;
            } else {
                // 统计所有预约数量
                long publicCount = appointmentService.countPublicAppointments(null, null, null, null);
                long officialCount = appointmentService.countOfficialAppointments(null, null, null, null);
                count = publicCount + officialCount;
            }
            
            result.put("count", count);
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "统计预约数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理最近预约请求
     * /api/appointment/recent - 获取最近预约列表（默认10条）
     */
    private void handleRecentAppointmentsRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 限制返回数量，默认10条
            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                } catch (NumberFormatException e) {
                    // 忽略错误，使用默认值
                }
            }
            
            // 获取所有预约
            List<PublicAppointment> publicAppointments = appointmentService.queryPublicAppointments(null, null, null, null);
            List<OfficialAppointment> officialAppointments = appointmentService.queryOfficialAppointments(null, null, null, null);
            
            // 创建组合结果集
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 处理公众预约
            for (PublicAppointment appointment : publicAppointments) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", appointment.getAppointmentId());
                item.put("type", "PUBLIC");
                item.put("name", appointment.getName());
                item.put("visitTime", appointment.getVisitTime());
                item.put("status", appointment.getStatus());
                result.add(item);
            }
            
            // 处理公务预约
            for (OfficialAppointment appointment : officialAppointments) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", appointment.getAppointmentId());
                item.put("type", "OFFICIAL");
                item.put("name", appointment.getName());
                item.put("visitTime", appointment.getVisitTime());
                item.put("status", appointment.getStatus());
                result.add(item);
            }
            
            // 按预约时间排序（降序，最近的排在前面）
            result.sort((a, b) -> {
                Timestamp timeA = (Timestamp) a.get("visitTime");
                Timestamp timeB = (Timestamp) b.get("visitTime");
                return timeB.compareTo(timeA);
            });
            
            // 限制返回数量
            if (result.size() > limit) {
                result = result.subList(0, limit);
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取最近预约失败: " + e.getMessage());
        }
    }
} 