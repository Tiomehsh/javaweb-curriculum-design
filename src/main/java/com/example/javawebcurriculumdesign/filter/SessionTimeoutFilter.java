package com.example.javawebcurriculumdesign.filter;

import com.example.javawebcurriculumdesign.model.Admin;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话超时过滤器
 * 检查管理员会话是否超时（30分钟）
 */
@WebFilter(urlPatterns = {"/admin/*", "/api/admin/*", "/api/department/*", "/api/appointment/*", "/api/log/*"})
public class SessionTimeoutFilter implements Filter {
    
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化过滤器
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // 排除不需要检查的路径
        if (isExcludedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = httpRequest.getSession(false);
        
        // 检查会话是否存在
        if (session == null) {
            handleSessionTimeout(httpRequest, httpResponse);
            return;
        }
        
        // 检查管理员是否已登录
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            handleSessionTimeout(httpRequest, httpResponse);
            return;
        }
        
        // 检查会话是否超时
        long lastAccessTime = session.getLastAccessedTime();
        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - lastAccessTime;
        long timeoutMillis = SESSION_TIMEOUT_MINUTES * 60 * 1000;
        
        if (sessionAge > timeoutMillis) {
            // 会话超时，清除会话
            session.invalidate();
            handleSessionTimeout(httpRequest, httpResponse);
            return;
        }
        
        // 会话有效，继续处理请求
        chain.doFilter(request, response);
    }
    
    /**
     * 检查是否为排除路径
     * @param requestURI 请求URI
     * @return 是否排除
     */
    private boolean isExcludedPath(String requestURI) {
        return requestURI.endsWith("/login.jsp") ||
               requestURI.endsWith("/api/admin/login") ||
               requestURI.endsWith("/api/admin/logout") ||
               requestURI.contains("/public/") ||
               requestURI.contains("/css/") ||
               requestURI.contains("/js/") ||
               requestURI.contains("/images/");
    }
    
    /**
     * 处理会话超时
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    private void handleSessionTimeout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String requestURI = request.getRequestURI();
        
        // 如果是AJAX请求，返回JSON响应
        if (isAjaxRequest(request) || requestURI.startsWith("/api/")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "会话已超时，请重新登录");
            result.put("sessionTimeout", true);
            
            objectMapper.writeValue(response.getOutputStream(), result);
        } else {
            // 普通请求，重定向到登录页面
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?timeout=1");
        }
    }
    
    /**
     * 检查是否为AJAX请求
     * @param request HTTP请求
     * @return 是否为AJAX请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWith);
    }
    
    @Override
    public void destroy() {
        // 清理资源
    }
}
