package com.example.javawebcurriculumdesign.controller;

import com.example.javawebcurriculumdesign.model.Admin;
import com.example.javawebcurriculumdesign.model.SystemLog;
import com.example.javawebcurriculumdesign.service.AdminService;
import com.example.javawebcurriculumdesign.service.SystemLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 系统日志控制器
 * 处理系统日志的查询请求
 */
@WebServlet("/api/log/*")
public class SystemLogController extends HttpServlet {
    private final SystemLogService systemLogService = new SystemLogService();
    private final AdminService adminService = new AdminService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 角色常量
    private static final String ROLE_AUDIT_ADMIN = "AUDIT_ADMIN";
    
    /**
     * 处理GET请求
     * /api/log/list - 获取所有系统日志
     * /api/log/{id} - 获取指定ID的系统日志
     * /api/log/admin/{adminId} - 获取指定管理员的系统日志
     * /api/log/operation/{operation} - 获取指定操作类型的系统日志
     * /api/log/date - 根据时间范围查询系统日志
     * /api/log/verify/{id} - 验证指定ID的系统日志完整性
     * /api/log/verify/batch - 批量验证系统日志完整性
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            pathInfo = "/list"; // 默认获取所有日志
        }
        
        // 获取当前登录的管理员
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        if (currentAdmin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        // 检查权限（只有系统管理员和审计管理员可以查看系统日志）
        if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
            !adminService.hasPermission(currentAdmin.getAdminId(), ROLE_AUDIT_ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/list")) {
            // 获取所有系统日志
            List<SystemLog> logs = systemLogService.getAllLogs();
            
            // 创建响应对象
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("logs", logs);
            result.put("total", logs.size());
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/detail")) {
            // 获取日志详情
            String idParam = request.getParameter("id");
            if (idParam == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
                return;
            }
            
            try {
                int logId = Integer.parseInt(idParam);
                SystemLog log = systemLogService.getLogById(logId);
                
                Map<String, Object> result = new HashMap<>();
                if (log != null) {
                    result.put("success", true);
                    result.put("log", log);
                } else {
                    result.put("success", false);
                    result.put("message", "日志不存在");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid log ID");
            }
        } else if (pathInfo.startsWith("/admin/")) {
            try {
                // 获取指定管理员的系统日志
                int adminId = Integer.parseInt(pathInfo.substring(7));
                List<SystemLog> logs = systemLogService.queryLogs(adminId, null, null, null);
                objectMapper.writeValue(response.getOutputStream(), logs);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid admin ID");
            }
        } else if (pathInfo.startsWith("/operation/")) {
            // 获取指定操作类型的系统日志
            String operation = pathInfo.substring(11);
            List<SystemLog> logs = systemLogService.queryLogs(null, operation, null, null);
            objectMapper.writeValue(response.getOutputStream(), logs);
        } else if (pathInfo.equals("/date")) {
            // 根据时间范围查询系统日志
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            
            try {
                Timestamp startDate = null;
                Timestamp endDate = null;
                
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    startDate = new Timestamp(dateFormat.parse(startDateStr).getTime());
                }
                
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    endDate = new Timestamp(dateFormat.parse(endDateStr).getTime());
                }
                
                List<SystemLog> logs = systemLogService.queryLogs(null, null, startDate, endDate);
                objectMapper.writeValue(response.getOutputStream(), logs);
            } catch (ParseException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format");
            }
        } else if (pathInfo.startsWith("/verify/")) {
            if (pathInfo.equals("/verify/batch")) {
                // 批量验证系统日志完整性
                // 直接使用null参数调用batchVerifyLogIntegrity，不需要先获取logs
                Map<Integer, Boolean> verifyResults = systemLogService.batchVerifyLogIntegrity(null, null);
                objectMapper.writeValue(response.getOutputStream(), verifyResults);
            } else {
                try {
                    // 验证指定ID的系统日志完整性
                    int logId = Integer.parseInt(pathInfo.substring(8));
                    
                    boolean isValid = systemLogService.verifyLogIntegrity(logId);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("logId", logId);
                    result.put("isValid", isValid);
                    
                    objectMapper.writeValue(response.getOutputStream(), result);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid log ID");
                }
            }
        } else {
            try {
                // 尝试获取路径中的ID
                int logId = Integer.parseInt(pathInfo.substring(1));
                
                // 获取指定ID的系统日志
                SystemLog log = systemLogService.getLogById(logId);
                if (log == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Log not found");
                    return;
                }
                
                objectMapper.writeValue(response.getOutputStream(), log);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid log ID");
            }
        }
    }
    
    /**
     * 处理POST请求（在本应用中不使用，日志只能查询不能添加）
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method is not supported for logs");
    }
    
    /**
     * 处理PUT请求（在本应用中不使用，日志只能查询不能修改）
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "PUT method is not supported for logs");
    }
    
    /**
     * 处理DELETE请求（在本应用中不使用，日志只能查询不能删除）
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "DELETE method is not supported for logs");
    }
} 