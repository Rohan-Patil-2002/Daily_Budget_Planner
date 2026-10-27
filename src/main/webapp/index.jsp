<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.jsp.Utils.SecurityUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Daily Budget Planner</title>
    <meta name="description" content="Sign in to your Daily Budget Planner to manage monthly budgets and daily expenses effectively.">
    <link rel="stylesheet" href="Css/indexStyle.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-header">
            <div class="auth-badge">✨ Smart Financial Manager</div>
            <div class="auth-logo">💰</div>
            <h1 class="auth-title">Welcome Back</h1>
            <p class="auth-subtitle">Sign in to track your budget and daily spends</p>
        </div>

        <% 
            String msg = request.getParameter("msg");
            if ("logged_out".equals(msg)) {
        %>
            <div class="msg-alert msg-success">
                <span>👋</span> You have been securely logged out.
            </div>
        <% } else if ("registered".equals(msg)) { %>
            <div class="msg-alert msg-success">
                <span>🎉</span> Registration successful! You can now log in.
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="msg-alert msg-error">
                <span>⚠️</span> <%= SecurityUtil.escapeHtml((String) request.getAttribute("error")) %>
            </div>
        <% } %>

        <form method="post" action="login" class="auth-form" id="loginForm">
            <div class="form-group">
                <label for="username" class="form-label">Username</label>
                <div class="input-wrapper">
                    <span class="input-icon">👤</span>
                    <input type="text" id="username" name="username" class="form-input" placeholder="Enter your username" required autofocus autocomplete="username" />
                </div>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <div class="input-wrapper">
                    <span class="input-icon">🔒</span>
                    <input type="password" id="password" name="password" class="form-input" placeholder="Enter your password" required autocomplete="current-password" />
                </div>
            </div>

            <button type="submit" class="btn-submit" id="btn-login-submit">
                <span>Sign In to Dashboard</span>
                <span>→</span>
            </button>
        </form>

        <div class="auth-footer">
            Don't have an account? <a href="register.jsp">Create an account</a>
        </div>
    </div>
</body>
</html>
