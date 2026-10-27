<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String planId = request.getParameter("planId");
    if (planId == null) {
        planId = request.getParameter("plan_id");
    }
    if (planId != null && !planId.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/history?planId=" + planId);
    } else {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
%>
