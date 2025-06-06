package com.example.javawebcurriculumdesign.filter;

import com.example.javawebcurriculumdesign.model.Admin;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

/**
 * 管理员权限过滤器
 * 用于验证用户是否登录，以及是否有权限访问特定页面
 */
public class AdminAuthFilter implements Filter {
    
    // 页面权限映射
    private static final Map<String, List<String>> PAGE_PERMISSIONS = new HashMap<>();
    
    static {
        // 所有管理员都可以访问的页面
        PAGE_PERMISSIONS.put("dashboard.jsp", Arrays.asList("SYSTEM_ADMIN", "DEPARTMENT_ADMIN", "RECEPTION_ADMIN", "AUDIT_ADMIN"));
        PAGE_PERMISSIONS.put("profile.jsp", Arrays.asList("SYSTEM_ADMIN", "DEPARTMENT_ADMIN", "RECEPTION_ADMIN", "AUDIT_ADMIN"));
        
        // 系统管理员可以访问的页面
        PAGE_PERMISSIONS.put("departments.jsp", Arrays.asList("SYSTEM_ADMIN"));
        PAGE_PERMISSIONS.put("admins.jsp", Arrays.asList("SYSTEM_ADMIN"));
        
        // 系统管理员和审计管理员可以访问的页面
        PAGE_PERMISSIONS.put("logs.jsp", Arrays.asList("SYSTEM_ADMIN", "AUDIT_ADMIN"));
        
        // 系统管理员和接待管理员可以访问的页面
        PAGE_PERMISSIONS.put("public_appointments.jsp", Arrays.asList("SYSTEM_ADMIN", "RECEPTION_ADMIN"));
        
        // 系统管理员、部门管理员和接待管理员可以访问的页面
        PAGE_PERMISSIONS.put("official_appointments.jsp", Arrays.asList("SYSTEM_ADMIN", "DEPARTMENT_ADMIN", "RECEPTION_ADMIN"));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // 获取请求的URI
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = uri.substring(contextPath.length());
        
        // 如果是访问API接口，则直接放行（API接口应该有自己的权限控制）
        if (relativePath.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 如果是访问静态资源，则直接放行
        if (relativePath.startsWith("/css/") || relativePath.startsWith("/js/") || relativePath.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 如果请求的是管理员登录页面，直接放行
        if (relativePath.equals("/admin/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 如果请求的是根路径，重定向到公众服务页面
        if (relativePath.equals("/") || relativePath.equals("")) {
            httpResponse.sendRedirect(contextPath + "/public/index.jsp");
            return;
        }
        
        // 检查用户是否已登录
        Admin admin = (session != null) ? (Admin) session.getAttribute("admin") : null;
        if (admin == null) {
            // 用户未登录，重定向到管理员登录页面
            httpResponse.sendRedirect(contextPath + "/admin/login.jsp");
            return;
        }
        
        // 检查用户是否有权限访问特定页面
        if (relativePath.startsWith("/admin/")) {
            String pageName = relativePath.substring("/admin/".length());
            
            // 如果是空路径或者/，则默认为dashboard.jsp
            if (pageName.isEmpty() || pageName.equals("/")) {
                pageName = "dashboard.jsp";
            }
            
            List<String> allowedRoles = PAGE_PERMISSIONS.get(pageName);
            if (allowedRoles != null && !allowedRoles.contains(admin.getRole())) {
                // 用户没有权限访问该页面，重定向到控制面板
                httpResponse.sendRedirect(contextPath + "/admin/dashboard.jsp");
                return;
            }
        }
        
        // 将管理员信息存储在request中，方便JSP页面使用EL表达式获取
        request.setAttribute("admin", admin);
        
        // 权限验证通过，继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
} 