<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="com.jsp.Utils.SecurityUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Daily Budget Planner</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/Css/headerfooter.css">
    <style>
        .error-card {
            background: white;
            max-width: 550px;
            margin: 80px auto;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
            text-align: center;
        }
        .error-icon {
            font-size: 64px;
            margin-bottom: 20px;
        }
        .error-title {
            font-size: 26px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 12px;
        }
        .error-desc {
            color: #64748b;
            font-size: 15px;
            line-height: 1.6;
            margin-bottom: 30px;
        }
        .btn-group {
            display: flex;
            gap: 15px;
            justify-content: center;
        }
        .btn-primary {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            padding: 12px 24px;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.2s;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 15px rgba(16, 185, 129, 0.35);
        }
        .btn-secondary {
            background: #f1f5f9;
            color: #334155;
            padding: 12px 24px;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.2s;
        }
        .btn-secondary:hover {
            background: #e2e8f0;
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <div class="error-card">
            <div class="error-icon">🧭</div>
            <h1 class="error-title">Notice</h1>
            <p class="error-desc">
                <% 
                    String errorMsg = (String) request.getAttribute("error");
                    if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                %>
                    <%= SecurityUtil.escapeHtml(errorMsg) %>
                <% } else { %>
                    The page you are looking for might have been moved, removed, or had its name changed. Let's get you back on track with your budget!
                <% } %>
            </p>
            <div class="btn-group">
                <a href="<%= request.getContextPath() %>/dashboard" class="btn-primary">Go to Dashboard</a>
                <a href="<%= request.getContextPath() %>/menu.jsp" class="btn-secondary">Overview</a>
            </div>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
</body>
</html>
