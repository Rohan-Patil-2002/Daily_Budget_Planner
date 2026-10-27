<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.jsp.Utils.SecurityUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Daily Budget Planner</title>
    <meta name="description" content="Create a new account on Daily Budget Planner to start tracking your daily expenses and monthly budgets.">
    <link rel="stylesheet" href="Css/indexStyle.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-header">
            <div class="auth-badge">🚀 Get Started Free</div>
            <div class="auth-logo">📝</div>
            <h1 class="auth-title">Create Account</h1>
            <p class="auth-subtitle">Join Daily Budget Planner and master your finances</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="msg-alert msg-error">
                <span>⚠️</span> <%= SecurityUtil.escapeHtml((String) request.getAttribute("error")) %>
            </div>
        <% } %>

        <form method="post" action="register" class="auth-form" id="registerForm" onsubmit="return validateRegister()">
            <div class="form-group">
                <label for="username" class="form-label">Choose Username</label>
                <div class="input-wrapper">
                    <span class="input-icon">👤</span>
                    <input type="text" id="username" name="username" class="form-input" placeholder="e.g. rohan_patil" required autofocus autocomplete="username" minlength="3" maxlength="50" />
                </div>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Create Password</label>
                <div class="input-wrapper">
                    <span class="input-icon">🔒</span>
                    <input type="password" id="password" name="password" class="form-input" placeholder="At least 6 characters" required autocomplete="new-password" minlength="6" />
                </div>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <div class="input-wrapper">
                    <span class="input-icon">🔑</span>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-input" placeholder="Re-enter your password" required autocomplete="new-password" minlength="6" />
                </div>
            </div>

            <button type="submit" class="btn-submit" id="btn-register-submit">
                <span>Create Account</span>
                <span>→</span>
            </button>
        </form>

        <div class="auth-footer">
            Already have an account? <a href="index.jsp">Log in here</a>
        </div>
    </div>

    <script>
        function validateRegister() {
            var pwd = document.getElementById('password').value;
            var cpwd = document.getElementById('confirmPassword').value;
            if (pwd !== cpwd) {
                alert('Passwords do not match. Please re-enter.');
                return false;
            }
            return true;
        }
    </script>
</body>
</html>
