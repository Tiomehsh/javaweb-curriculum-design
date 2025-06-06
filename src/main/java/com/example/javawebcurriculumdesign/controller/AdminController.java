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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 管理员控制器
 * 处理管理员登录、登出、获取信息等请求
 */
@WebServlet("/api/admin/*")
public class AdminController extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final SystemLogService systemLogService = new SystemLogService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理GET请求
     * /api/admin/current - 获取当前登录管理员信息
     * /api/admin/list - 获取所有管理员列表（需要系统管理员权限）
     * /api/admin/{id} - 获取指定ID的管理员信息（需要系统管理员权限）
     * /api/admin/detail - 获取管理员详情
     * /api/admin/profile - 获取当前登录管理员的个人资料信息
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        // 获取当前登录的管理员
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        // 如果未登录，则返回401错误
        if (currentAdmin == null && !pathInfo.equals("/login")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/current")) {
            // 获取当前登录管理员信息
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("adminId", currentAdmin.getAdminId());
            adminInfo.put("loginName", currentAdmin.getLoginName());
            adminInfo.put("realName", currentAdmin.getRealName());
            adminInfo.put("role", currentAdmin.getRole());
            adminInfo.put("deptId", currentAdmin.getDeptId());
            adminInfo.put("deptName", currentAdmin.getDeptName());

            objectMapper.writeValue(response.getOutputStream(), adminInfo);
        } else if (pathInfo.equals("/session-check")) {
            // 会话检查API
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionValid", true);
            result.put("adminId", currentAdmin.getAdminId());
            result.put("loginName", currentAdmin.getLoginName());

            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/validate-password")) {
            // 密码复杂度验证API
            String password = request.getParameter("password");
            if (password == null || password.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing password parameter");
                return;
            }

            var validationResult = adminService.validatePassword(password);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", validationResult.isValid());
            result.put("errors", validationResult.getErrors());
            result.put("strength", com.example.javawebcurriculumdesign.util.PasswordValidator.getPasswordStrength(password));
            result.put("strengthDescription", com.example.javawebcurriculumdesign.util.PasswordValidator.getPasswordStrengthDescription(password));

            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/profile")) {
            // 获取当前登录管理员的个人资料信息
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("admin", currentAdmin);
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/detail")) {
            // 获取管理员详情
            String idParam = request.getParameter("id");
            if (idParam == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
                return;
            }
            
            try {
                int adminId = Integer.parseInt(idParam);
                
                // 检查权限（只有系统管理员或查询自己的信息才允许）
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    currentAdmin.getAdminId() != adminId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                Admin admin = adminService.getAdminById(adminId);
                
                Map<String, Object> result = new HashMap<>();
                if (admin != null) {
                    result.put("success", true);
                    result.put("admin", admin);
                } else {
                    result.put("success", false);
                    result.put("message", "管理员不存在");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid admin ID");
            }
        } else if (pathInfo.equals("/list")) {
            // 检查权限（只有系统管理员可以查看所有管理员）
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "没有权限查看管理员列表");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            try {
                // 获取所有管理员列表
                List<Admin> admins = adminService.getAllAdmins();
                
                // 创建响应对象
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("admins", admins);
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "获取管理员列表失败: " + e.getMessage());
                objectMapper.writeValue(response.getOutputStream(), result);
            }
        } else {
            try {
                // 尝试获取路径中的ID
                int adminId = Integer.parseInt(pathInfo.substring(1));
                
                // 检查权限（只有系统管理员或查询自己的信息才允许）
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    currentAdmin.getAdminId() != adminId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                // 获取指定ID的管理员信息
                Admin admin = adminService.getAdminById(adminId);
                if (admin == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Admin not found");
                    return;
                }
                
                objectMapper.writeValue(response.getOutputStream(), admin);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid admin ID");
            }
        }
    }
    
    /**
     * 处理POST请求
     * /api/admin/login - 管理员登录
     * /api/admin/logout - 管理员登出
     * /api/admin/add - 添加管理员（需要系统管理员权限）
     * /api/admin/reset-password - 重置密码
     * /api/admin/toggle-status - 切换状态
     * /api/admin/update - 更新管理员信息
     * /api/admin/update-profile - 更新管理员个人资料
     * /api/admin/change-password - 修改密码
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/login")) {
            // 管理员登录
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            String loginName = (String) requestData.get("loginName");
            String password = (String) requestData.get("password");
            
            if (loginName == null || password == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing login name or password");
                return;
            }
            
            // 获取客户端IP地址
            String ipAddress = request.getRemoteAddr();
            
            // 首先检查用户是否存在
            Admin existingAdmin = adminService.getAdminByLoginName(loginName);
            
            // 检查账户是否被锁定
            if (existingAdmin != null && adminService.isAccountLocked(existingAdmin)) {
                SystemLog log = new SystemLog();
                log.setAdminId(existingAdmin.getAdminId());
                log.setOperation("管理员登录: " + loginName);
                log.setDescription("状态: 失败, 账户被锁定");
                log.setIpAddress(ipAddress);
                log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
                systemLogService.addLog(log);

                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "账户已被锁定，请30分钟后再试");
                result.put("accountLocked", true);
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }

            // 调用登录服务
            Admin admin = adminService.login(loginName, password);

            if (admin == null) {
                // 登录失败，记录详细的日志
                SystemLog log = new SystemLog();
                String failureReason;

                if (existingAdmin == null) {
                    // 用户名不存在
                    log.setAdminId(null);
                    failureReason = "用户名不存在";
                } else {
                    // 用户名存在但密码错误或账户被锁定
                    log.setAdminId(existingAdmin.getAdminId());
                    if (adminService.isAccountLocked(existingAdmin)) {
                        failureReason = "账户被锁定";
                    } else {
                        failureReason = "密码错误";
                    }
                }

                log.setOperation("管理员登录: " + loginName);
                log.setDescription("状态: 失败, " + failureReason);
                log.setIpAddress(ipAddress);
                log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
                systemLogService.addLog(log);

                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 登录成功，记录日志
            SystemLog log = new SystemLog();
            log.setAdminId(admin.getAdminId());
            log.setOperation("管理员登录: " + loginName);
            log.setDescription("状态: 成功, 登录成功");
            log.setIpAddress(ipAddress);
            log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
            systemLogService.addLog(log);

            // 将管理员信息存入session
            HttpSession session = request.getSession(true);
            session.setAttribute("admin", admin);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("adminId", admin.getAdminId());
            result.put("loginName", admin.getLoginName());
            result.put("realName", admin.getRealName());
            result.put("role", admin.getRole());

            // 检查密码是否过期或即将过期
            if (adminService.isPasswordExpired(admin)) {
                result.put("passwordExpired", true);
                result.put("message", "密码已过期，请立即修改密码");
            } else if (adminService.needPasswordExpiryWarning(admin)) {
                long remainingDays = adminService.getPasswordRemainingDays(admin);
                result.put("passwordWarning", true);
                result.put("message", "密码将在" + remainingDays + "天后过期，建议及时修改");
                result.put("remainingDays", remainingDays);
            }

            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/logout")) {
            // 管理员登出
            HttpSession session = request.getSession(false);
            Admin admin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (admin != null) {
                // 记录登出日志
                String ipAddress = request.getRemoteAddr();
                SystemLog log = new SystemLog();
                log.setAdminId(admin.getAdminId());
                log.setOperation("登出");
                log.setDescription("管理员登出: " + admin.getLoginName());
                log.setIpAddress(ipAddress);
                log.setOperationTime(Timestamp.valueOf(LocalDateTime.now()));
                systemLogService.addLog(log);
                
                // 清除session
                session.invalidate();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/extend-session")) {
            // 延长会话
            HttpSession session = request.getSession(false);
            Admin admin = (session != null) ? (Admin) session.getAttribute("admin") : null;

            Map<String, Object> result = new HashMap<>();
            if (admin != null) {
                // 会话存在，延长会话时间
                session.setMaxInactiveInterval(30 * 60); // 30分钟
                result.put("success", true);
                result.put("message", "会话已延长");
            } else {
                result.put("success", false);
                result.put("message", "会话不存在");
                result.put("sessionTimeout", true);
            }

            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/add")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
                return;
            }
            
            // 检查权限（只有系统管理员可以添加管理员）
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                return;
            }
            
            // 解析请求数据
            Admin newAdmin = objectMapper.readValue(request.getInputStream(), Admin.class);
            
            // 添加管理员
            boolean success = adminService.addAdmin(newAdmin, currentAdmin.getAdminId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "管理员添加成功");
            } else {
                result.put("message", "管理员添加失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/reset-password")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未登录");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 检查权限（只有系统管理员可以重置密码）
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "没有权限重置密码");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 解析请求数据
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            Integer adminId = (Integer) requestData.get("adminId");
            String newPassword = (String) requestData.get("passwordHash");
            
            if (adminId == null || newPassword == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "参数不完整");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 重置密码
            boolean success = adminService.resetPassword(adminId, newPassword);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "密码重置成功");
            } else {
                result.put("message", "密码重置失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/toggle-status")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未登录");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 检查权限（只有系统管理员可以切换状态）
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "没有权限修改管理员状态");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 解析请求数据
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            Integer adminId = (Integer) requestData.get("adminId");
            Integer status = (Integer) requestData.get("status");
            
            if (adminId == null || status == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "参数不完整");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 不允许修改自己的状态
            if (currentAdmin.getAdminId().equals(adminId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "不能修改自己的状态");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 切换状态
            boolean success = adminService.updateAdminStatus(adminId, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "状态修改成功");
            } else {
                result.put("message", "状态修改失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/update")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未登录");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 解析请求数据
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            
            Integer adminId = (Integer) requestData.get("adminId");
            if (adminId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "管理员ID不能为空");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 检查权限（只有系统管理员或修改自己的信息才允许）
            if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                !currentAdmin.getAdminId().equals(adminId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "没有权限修改管理员信息");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 获取现有管理员信息
            Admin existingAdmin = adminService.getAdminById(adminId);
            if (existingAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "管理员不存在");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 更新管理员信息
            existingAdmin.setRealName((String) requestData.get("realName"));
            existingAdmin.setRole((String) requestData.get("role"));
            existingAdmin.setDeptId((Integer) requestData.get("deptId"));
            existingAdmin.setPhone((String) requestData.get("phone"));
            existingAdmin.setStatus((Integer) requestData.get("status"));
            
            boolean success = adminService.updateAdmin(existingAdmin, currentAdmin.getAdminId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "管理员信息更新成功");
            } else {
                result.put("message", "管理员信息更新失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/update-profile")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未登录");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 解析请求数据
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            
            // 更新管理员信息
            Admin updatedAdmin = new Admin();
            updatedAdmin.setAdminId(currentAdmin.getAdminId());
            updatedAdmin.setRealName((String) requestData.get("realName"));
            updatedAdmin.setPhone((String) requestData.get("phone"));
            
            // 保留原有信息
            updatedAdmin.setLoginName(currentAdmin.getLoginName());
            updatedAdmin.setPasswordHash(currentAdmin.getPasswordHash());
            updatedAdmin.setRole(currentAdmin.getRole());
            updatedAdmin.setDeptId(currentAdmin.getDeptId());
            updatedAdmin.setDeptName(currentAdmin.getDeptName());
            
            boolean success = adminService.updateAdmin(updatedAdmin, currentAdmin.getAdminId());
            
            // 如果更新成功，更新session中的管理员信息
            if (success) {
                session.setAttribute("admin", adminService.getAdminById(currentAdmin.getAdminId()));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "个人资料更新成功");
            } else {
                result.put("message", "个人资料更新失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/change-password")) {
            // 获取当前登录的管理员
            HttpSession session = request.getSession(false);
            Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
            
            if (currentAdmin == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未登录");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 解析请求数据
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            String oldPassword = (String) requestData.get("oldPassword");
            String newPassword = (String) requestData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "参数不完整");
                objectMapper.writeValue(response.getOutputStream(), result);
                return;
            }
            
            // 调用修改密码服务
            boolean success = adminService.changePassword(currentAdmin.getAdminId(), oldPassword, newPassword);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "密码修改成功");
            } else {
                result.put("message", "密码修改失败，请检查旧密码是否正确");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
        }
    }
    
    /**
     * 处理PUT请求
     * /api/admin/{id} - 更新管理员信息
     * /api/admin/password - 修改密码
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        // 获取当前登录的管理员
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        if (currentAdmin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/password")) {
            // 修改密码
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            String oldPassword = (String) requestData.get("oldPassword");
            String newPassword = (String) requestData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing old or new password");
                return;
            }
            
            // 调用修改密码服务
            boolean success = adminService.changePassword(currentAdmin.getAdminId(), oldPassword, newPassword);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "密码修改成功");
            } else {
                result.put("message", "密码修改失败，请检查旧密码是否正确");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else {
            try {
                // 尝试获取路径中的ID
                int adminId = Integer.parseInt(pathInfo.substring(1));
                
                // 检查权限（只有系统管理员或修改自己的信息才允许）
                if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN) && 
                    currentAdmin.getAdminId() != adminId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
                    return;
                }
                
                // 解析请求数据
                Admin updatedAdmin = objectMapper.readValue(request.getInputStream(), Admin.class);
                updatedAdmin.setAdminId(adminId); // 确保ID正确
                
                // 更新管理员信息
                boolean success = adminService.updateAdmin(updatedAdmin, currentAdmin.getAdminId());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                if (success) {
                    result.put("message", "管理员信息更新成功");
                } else {
                    result.put("message", "管理员信息更新失败");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid admin ID");
            }
        }
    }
    
    /**
     * 处理DELETE请求
     * /api/admin/{id} - 删除管理员
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }
        
        // 获取当前登录的管理员
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        if (currentAdmin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        // 检查权限（只有系统管理员可以删除管理员）
        if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
            return;
        }
        
        try {
            // 尝试获取路径中的ID
            int adminId = Integer.parseInt(pathInfo.substring(1));
            
            // 不允许删除自己
            if (currentAdmin.getAdminId() == adminId) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot delete yourself");
                return;
            }
            
            // 删除管理员
            boolean success = adminService.deleteAdmin(adminId, currentAdmin.getAdminId());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "管理员删除成功");
            } else {
                result.put("message", "管理员删除失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid admin ID");
        }
    }
} 