<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.jsp.Utils.CSRFTokenUtil, com.jsp.Utils.SecurityUtil" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("index.jsp");
        return;
    }
    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    int currentMonthValue = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create New Budget Plan - Daily Budget Planner</title>
    <meta name="description" content="Set up a new monthly budget with automated daily target tracking.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/AddPlanStyle.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <div class="form-wrapper">
            <div class="plan-form-card">
                <div class="form-header-box">
                    <span class="form-header-badge">📅 Monthly Budget Setup</span>
                    <h1>Create Budget Plan</h1>
                    <p>Allocate your budget for a month and our engine will calculate daily spending limits automatically.</p>
                </div>

                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert alert-error">
                        <span>⚠️</span> <%= SecurityUtil.escapeHtml((String) request.getAttribute("error")) %>
                    </div>
                <% } %>

                <form action="addPlan" method="post" class="plan-form" id="createPlanForm">
                    <input type="hidden" name="csrfToken" value="<%= CSRFTokenUtil.getToken(session) %>" />

                    <div class="form-row">
                        <div class="form-field">
                            <label for="month"><span>📆</span> Target Month</label>
                            <select name="month" id="month" class="field-select" onchange="calculateLiveBudget()" required>
                                <%
                                    for (int i = 1; i <= 12; i++) {
                                        String monthName = java.time.Month.of(i).name();
                                        String displayMonth = monthName.substring(0, 1) + monthName.substring(1).toLowerCase();
                                        boolean isSelected = (i == currentMonthValue);
                                %>
                                <option value="<%= monthName %>" <%= isSelected ? "selected" : "" %>><%= displayMonth %></option>
                                <%
                                    }
                                %>
                            </select>
                        </div>

                        <div class="form-field">
                            <label for="year"><span>🗓️</span> Target Year</label>
                            <input type="number" name="year" id="year" class="field-input" value="<%= currentYear %>" min="2020" max="2035" oninput="calculateLiveBudget()" required>
                        </div>
                    </div>

                    <div class="form-field">
                        <label for="amount"><span>💰</span> Total Monthly Budget (₹)</label>
                        <div class="field-input-box">
                            <span class="field-prefix">₹</span>
                            <input type="number" name="amount" id="amount" class="field-input has-prefix" placeholder="e.g. 5000" step="0.01" min="1" max="10000000" oninput="calculateLiveBudget()" required autofocus>
                        </div>
                    </div>

                    <!-- Live Calculated Preview -->
                    <div class="calc-preview-card" id="calc-preview">
                        <div class="calc-preview-label">
                            <span>Estimated Daily Spending Target</span>
                            <span id="days-info">Based on 30 days in month</span>
                        </div>
                        <div class="calc-preview-value" id="daily-estimate-val">₹0.00 / day</div>
                    </div>

                    <div class="form-buttons-row">
                        <button type="submit" class="btn btn-primary" id="btn-submit-create-plan">
                            <span>🚀</span> Create Budget Plan
                        </button>
                        <a href="dashboard" class="btn btn-secondary">
                            <span>Cancel</span>
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <jsp:include page="footer.jsp" />

    <script>
        function getDaysInMonth(monthName, year) {
            const months = {
                'JANUARY': 31, 'FEBRUARY': (year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0)) ? 29 : 28,
                'MARCH': 31, 'APRIL': 30, 'MAY': 31, 'JUNE': 30,
                'JULY': 31, 'AUGUST': 31, 'SEPTEMBER': 30, 'OCTOBER': 31,
                'NOVEMBER': 30, 'DECEMBER': 31
            };
            return months[monthName.toUpperCase()] || 30;
        }

        function calculateLiveBudget() {
            const month = document.getElementById('month').value;
            const year = parseInt(document.getElementById('year').value) || new Date().getFullYear();
            const amount = parseFloat(document.getElementById('amount').value) || 0;

            const days = getDaysInMonth(month, year);
            const daily = (days > 0 && amount > 0) ? (amount / days) : 0;

            document.getElementById('days-info').innerText = 'Based on ' + days + ' days in ' + month.toLowerCase();
            document.getElementById('daily-estimate-val').innerText = '₹' + daily.toFixed(2) + ' / day';
        }

        // Initialize calculation on load
        calculateLiveBudget();
    </script>
</body>
</html>
