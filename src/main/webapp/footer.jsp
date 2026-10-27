<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String contextPath = request.getContextPath();
%>
<footer class="footer-wrapper">
    <div class="footer">
        <div class="footer-brand">
            <span>💰 Daily Budget Planner</span>
            <span class="footer-status">
                <span class="status-dot"></span> Active & Secure
            </span>
        </div>

        <div class="footer-links">
            <a href="<%= contextPath %>/dashboard">Dashboard</a>
            <a href="<%= contextPath %>/menu.jsp">Features</a>
            <a href="<%= contextPath %>/About.jsp">User Guide</a>
            <span>•</span>
            <span>Support: rohandpatil24@gmail.com</span>
        </div>

        <div class="footer-copy">
            © <%= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) %> Developed by <strong>Patil Rohan</strong>. All rights reserved.
        </div>
    </div>
</footer>