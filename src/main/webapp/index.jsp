<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 重定向到public目录的主页
    response.sendRedirect(request.getContextPath() + "/public/index.jsp");
%>
