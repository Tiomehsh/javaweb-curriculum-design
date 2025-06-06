package com.example.javawebcurriculumdesign.controller;

import com.example.javawebcurriculumdesign.model.Admin;
import com.example.javawebcurriculumdesign.model.Department;
import com.example.javawebcurriculumdesign.service.AdminService;
import com.example.javawebcurriculumdesign.service.DepartmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 部门控制器
 * 处理部门的增删改查请求
 */
@WebServlet("/api/department/*")
public class DepartmentController extends HttpServlet {
    private final DepartmentService departmentService = new DepartmentService();
    private final AdminService adminService = new AdminService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理GET请求
     * /api/department/list - 获取所有部门列表
     * /api/department/{id} - 获取指定ID的部门信息
     * /api/department/type/{type} - 获取指定类型的部门列表
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            pathInfo = "/list"; // 默认获取所有部门
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo.equals("/list")) {
            // 获取所有部门列表
            List<Department> departments = departmentService.getAllDepartments();
            
            // 创建响应对象
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("departments", departments);
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/detail")) {
            // 获取部门详情
            String idParam = request.getParameter("id");
            if (idParam == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
                return;
            }
            
            try {
                int deptId = Integer.parseInt(idParam);
                Department department = departmentService.getDepartmentById(deptId);
                
                Map<String, Object> result = new HashMap<>();
                if (department != null) {
                    result.put("success", true);
                    result.put("department", department);
                } else {
                    result.put("success", false);
                    result.put("message", "部门不存在");
                }
                
                objectMapper.writeValue(response.getOutputStream(), result);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
            }
        } else if (pathInfo.startsWith("/type/")) {
            // 获取指定类型的部门列表
            String type = pathInfo.substring(6);
            List<Department> departments = departmentService.getDepartmentsByType(type);
            objectMapper.writeValue(response.getOutputStream(), departments);
        } else {
            try {
                // 尝试获取路径中的ID
                int deptId = Integer.parseInt(pathInfo.substring(1));
                
                // 获取指定ID的部门信息
                Department department = departmentService.getDepartmentById(deptId);
                if (department == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
                    return;
                }
                
                objectMapper.writeValue(response.getOutputStream(), department);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
            }
        }
    }
    
    /**
     * 处理POST请求
     * /api/department/add - 添加部门
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        // 获取当前登录的管理员
        HttpSession session = request.getSession(false);
        Admin currentAdmin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        
        if (currentAdmin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        // 检查权限（只有系统管理员可以添加部门）
        if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/add")) {
            // 解析请求数据
            Department department = objectMapper.readValue(request.getInputStream(), Department.class);
            
            // 添加部门
            int deptId = departmentService.addDepartment(department, currentAdmin.getAdminId());
            
            Map<String, Object> result = new HashMap<>();
            if (deptId > 0) {
                result.put("success", true);
                result.put("deptId", deptId);
                result.put("message", "部门添加成功");
            } else {
                result.put("success", false);
                result.put("message", "部门添加失败，可能是部门名称已存在");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/update")) {
            // 更新部门
            Department department = objectMapper.readValue(request.getInputStream(), Department.class);
            
            // 更新部门信息
            boolean success = departmentService.updateDepartment(department, currentAdmin.getAdminId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "部门信息更新成功");
            } else {
                result.put("message", "部门信息更新失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else if (pathInfo.equals("/delete")) {
            // 删除部门
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getInputStream(), Map.class);
            int deptId = (Integer) requestData.get("deptId");
            
            // 删除部门
            boolean success = departmentService.deleteDepartment(deptId, currentAdmin.getAdminId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "部门删除成功");
            } else {
                result.put("message", "部门删除失败，可能是该部门下有关联的预约或管理员");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
        }
    }
    
    /**
     * 处理PUT请求
     * /api/department/{id} - 更新部门信息
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
        
        // 检查权限（只有系统管理员可以更新部门）
        if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
            return;
        }
        
        try {
            // 尝试获取路径中的ID
            int deptId = Integer.parseInt(pathInfo.substring(1));
            
            // 解析请求数据
            Department department = objectMapper.readValue(request.getInputStream(), Department.class);
            department.setDeptId(deptId); // 确保ID正确
            
            // 更新部门信息
            boolean success = departmentService.updateDepartment(department, currentAdmin.getAdminId());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "部门信息更新成功");
            } else {
                result.put("message", "部门信息更新失败");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        }
    }
    
    /**
     * 处理DELETE请求
     * /api/department/{id} - 删除部门
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
        
        // 检查权限（只有系统管理员可以删除部门）
        if (!adminService.hasPermission(currentAdmin.getAdminId(), Admin.ROLE_SYSTEM_ADMIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission");
            return;
        }
        
        try {
            // 尝试获取路径中的ID
            int deptId = Integer.parseInt(pathInfo.substring(1));
            
            // 删除部门
            boolean success = departmentService.deleteDepartment(deptId, currentAdmin.getAdminId());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "部门删除成功");
            } else {
                result.put("message", "部门删除失败，可能是该部门下有关联的预约或管理员");
            }
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid department ID");
        }
    }
} 