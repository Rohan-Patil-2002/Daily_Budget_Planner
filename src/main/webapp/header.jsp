<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.jsp.Utils.SecurityUtil" %>
<%
    String currentURI = request.getRequestURI();
    String username = (String) session.getAttribute("username");
    Integer userId = (Integer) session.getAttribute("userId");
    String contextPath = request.getContextPath();
    String userInitial = (username != null && !username.trim().isEmpty()) ? username.trim().substring(0, 1).toUpperCase() : "U";
%>
<header class="header-wrapper">
    <div class="header">
        <a href="<%= contextPath %>/dashboard" class="brand-link" id="brand-logo-link">
            <div class="brand-icon">💰</div>
            <div class="brand-text">
                <span class="brand-title">DailyBudget</span>
                <span class="brand-sub">Smart Financial Planner</span>
            </div>
        </a>

        <nav class="header-nav">
            <a href="<%= contextPath %>/dashboard" class="nav-link <%= currentURI.endsWith("dashboard") || currentURI.endsWith("dashboard.jsp") ? "active" : "" %>" id="nav-dashboard">
                <span>📊</span> Dashboard
            </a>
            <a href="<%= contextPath %>/AddPlan.jsp" class="nav-link <%= currentURI.endsWith("AddPlan.jsp") ? "active" : "" %>" id="nav-create-plan">
                <span>➕</span> New Plan
            </a>
            <a href="<%= contextPath %>/menu.jsp" class="nav-link <%= currentURI.endsWith("menu.jsp") || currentURI.endsWith("menu") ? "active" : "" %>" id="nav-features">
                <span>✨</span> Features
            </a>
            <a href="<%= contextPath %>/About.jsp" class="nav-link <%= currentURI.endsWith("About.jsp") ? "active" : "" %>" id="nav-about">
                <span>📖</span> Guide
            </a>

            <% if (userId != null && username != null) { %>
                <div class="user-badge" id="user-profile-badge">
                    <span class="user-avatar"><%= userInitial %></span>
                    <span><%= SecurityUtil.escapeHtml(username) %></span>
                </div>
                <a href="<%= contextPath %>/logout" class="nav-link logout-link" id="nav-logout">
                    <span>🚪</span> Logout
                </a>
            <% } else { %>
                <a href="<%= contextPath %>/index.jsp" class="nav-link <%= currentURI.endsWith("index.jsp") ? "active" : "" %>" id="nav-login">
                    <span>🔐</span> Login
                </a>
                <a href="<%= contextPath %>/register.jsp" class="nav-link <%= currentURI.endsWith("register.jsp") ? "active" : "" %>" id="nav-register">
                    <span>📝</span> Register
                </a>
            <% } %>
        </nav>
    </div>
</header>