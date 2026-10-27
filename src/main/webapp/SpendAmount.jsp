<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection, com.jsp.DAO.PlanDAO, com.jsp.DAO.SpendAmountDAO, com.jsp.Models.Plans, com.jsp.Models.SpendAmount, com.jsp.Utils.DBConnection, com.jsp.Utils.CSRFTokenUtil, com.jsp.Utils.SecurityUtil" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    int userId = (Integer) session.getAttribute("userId");
    String planIdParam = request.getParameter("planId");

    if (planIdParam == null || planIdParam.trim().isEmpty()) {
        response.sendRedirect("dashboard?error=Missing+plan+id");
        return;
    }

    int planId = 0;
    try {
        planId = Integer.parseInt(planIdParam.trim());
    } catch (NumberFormatException e) {
        response.sendRedirect("dashboard?error=Invalid+plan+id");
        return;
    }

    Plans plan = PlanDAO.getPlanById(planId, userId);
    if (plan == null) {
        response.sendRedirect("dashboard?error=Unauthorized+access+or+plan+not+found");
        return;
    }

    SpendAmount spend = null;
    int currentDay = 1;
    double currentAmount = plan.getAmount();
    double dailyBudget = plan.getAmount() / 30.0;
    int totalDays = PlanDAO.getTotalDaysInMonth(plan.getMonth(), plan.getYear());

    try (Connection conn = DBConnection.getConnection()) {
        SpendAmountDAO dao = new SpendAmountDAO(conn);
        spend = dao.getLatestSpendAmountForPlan(planId);

        if (spend != null) {
            currentDay = spend.getDay();
            currentAmount = spend.getCurrentAmount();
            dailyBudget = spend.getDailyBudget();
        } else {
            // Auto initialize Day 1
            PlanDAO planDAO = new PlanDAO();
            planDAO.initializeSpendAmounts(conn, planId, plan.getAmount(), plan.getMonth(), plan.getYear());
            spend = dao.getLatestSpendAmountForPlan(planId);
            if (spend != null) {
                currentDay = spend.getDay();
                currentAmount = spend.getCurrentAmount();
                dailyBudget = spend.getDailyBudget();
            }
        }
    } catch (Exception e) {
        // Handled silently with logging inside DAO
    }

    String error = request.getParameter("error");
    String msg = request.getParameter("msg");
    String csrfToken = CSRFTokenUtil.getToken(session);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Record Daily Spend - Daily Budget Planner</title>
    <meta name="description" content="Record daily expenses or skip days to adjust remaining monthly budgets automatically.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/AddPlanStyle.css">
    <link rel="stylesheet" href="Css/spendAmount.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <!-- Toast Alerts -->
        <% if ("spend_success".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✅</span> <strong>Spend Recorded!</strong> Remaining balance and future daily targets updated.
            </div>
        <% } else if ("day_skipped".equals(msg)) { %>
            <div class="alert alert-info">
                <span>🔄</span> <strong>Day Skipped!</strong> Day budget rolled over into remaining balance for upcoming days.
            </div>
        <% } %>

        <% if (error != null && !error.isEmpty()) { %>
            <div class="alert alert-error">
                <span>⚠️</span> <%= SecurityUtil.escapeHtml(error) %>
            </div>
        <% } %>

        <div class="spend-layout">
            <!-- Left: Plan Health Card -->
            <div class="spend-summary-card">
                <div class="summary-header">
                    <div>
                        <span class="form-header-badge">Plan #<%= plan.getPlanId() %></span>
                        <h2 class="summary-plan-title"><%= SecurityUtil.escapeHtml(plan.getMonth()) %> <%= plan.getYear() %></h2>
                    </div>
                    <span class="plan-badge on-track">Active Plan</span>
                </div>

                <div class="balance-widget">
                    <span class="balance-label">Current Remaining Balance</span>
                    <div class="balance-amount">₹<%= String.format("%,.2f", currentAmount) %></div>

                    <div class="balance-stats-sub">
                        <div class="balance-sub-item">
                            <span>Today's Daily Target</span>
                            <span>₹<%= String.format("%,.2f", dailyBudget) %>/day</span>
                        </div>
                        <div class="balance-sub-item">
                            <span>Total Plan Budget</span>
                            <span>₹<%= String.format("%,.2f", plan.getAmount()) %></span>
                        </div>
                    </div>
                </div>

                <div class="tips-box">
                    <div class="tips-title">
                        <span>💡</span> Smart Spending Insight
                    </div>
                    <p class="tips-text">
                        Spending less than <strong>₹<%= String.format("%,.2f", dailyBudget) %></strong> today will increase your daily budget for the rest of <%= SecurityUtil.escapeHtml(plan.getMonth()) %>. If you spend ₹0, use the <strong>Skip Day</strong> feature!
                    </p>
                </div>

                <div class="nav-links-row">
                    <a href="dashboard">← Back to Dashboard</a>
                    <a href="<%= request.getContextPath() %>/history?planId=<%= plan.getPlanId() %>">View Complete History →</a>
                </div>
            </div>

            <!-- Right: Spend Entry Form -->
            <div class="spend-form-card">
                <div class="spend-form-header">
                    <h2>Record Spend</h2>
                    <p>Log your daily expenditure for Day <%= currentDay %> of <%= totalDays %>.</p>
                </div>

                <form action="SpendAmountServlet" method="post" class="spend-form" id="spendEntryForm">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="planId" value="<%= plan.getPlanId() %>">

                    <div class="field-group">
                        <label for="day" class="field-label">
                            <span>📅</span> Month Day (1 to <%= totalDays %>)
                        </label>
                        <input type="number" name="day" id="day" class="field-input" value="<%= currentDay %>" min="1" max="<%= totalDays %>" required>
                    </div>

                    <div class="field-group">
                        <label for="spendAmount" class="field-label">
                            <span>💸</span> Amount to Spend (₹)
                        </label>
                        <div class="field-input-box">
                            <span class="field-prefix">₹</span>
                            <input type="number" name="spendAmount" id="spendAmount" class="field-input has-prefix" placeholder="0.00" step="0.01" min="0" max="10000000" required autofocus>
                        </div>

                        <!-- Quick Amount Presets -->
                        <span class="chips-label">Quick Add:</span>
                        <div class="chips-row">
                            <button type="button" class="chip-btn" onclick="addAmount(50)">+₹50</button>
                            <button type="button" class="chip-btn" onclick="addAmount(100)">+₹100</button>
                            <button type="button" class="chip-btn" onclick="addAmount(200)">+₹200</button>
                            <button type="button" class="chip-btn" onclick="addAmount(500)">+₹500</button>
                            <button type="button" class="chip-btn" onclick="addAmount(1000)">+₹1000</button>
                        </div>
                    </div>

                    <div class="field-group">
                        <label for="note" class="field-label">
                            <span>📝</span> Expense Note / Category
                        </label>
                        <input type="text" name="note" id="note" class="field-input" placeholder="e.g. Lunch with friends, groceries, commute" maxlength="255" required>

                        <!-- Quick Note Chips -->
                        <span class="chips-label">Common Categories:</span>
                        <div class="chips-row">
                            <button type="button" class="chip-btn" onclick="setNote('🍕 Food / Dining')">🍕 Food</button>
                            <button type="button" class="chip-btn" onclick="setNote('🛒 Groceries')">🛒 Groceries</button>
                            <button type="button" class="chip-btn" onclick="setNote('☕ Coffee / Tea')">☕ Coffee</button>
                            <button type="button" class="chip-btn" onclick="setNote('🚗 Travel / Transport')">🚗 Travel</button>
                            <button type="button" class="chip-btn" onclick="setNote('🛍️ Shopping')">🛍️ Shopping</button>
                            <button type="button" class="chip-btn" onclick="setNote('💡 Bills & Utilities')">💡 Utilities</button>
                        </div>
                    </div>

                    <div class="spend-actions-row">
                        <button type="submit" class="btn btn-primary btn-record-spend" id="btn-submit-spend">
                            <span>⚡</span> Record Spend
                        </button>

                        <button type="button" class="btn-skip-day" onclick="submitSkipDay()" id="btn-skip-day">
                            <span>🔄</span> Skip This Day (₹0 Spend)
                        </button>
                    </div>
                </form>

                <!-- Hidden form for skip day action -->
                <form id="skipDayForm" action="SpendAmountServlet" method="post" style="display: none;">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="planId" value="<%= plan.getPlanId() %>">
                    <input type="hidden" name="day" id="skipDayVal" value="<%= currentDay %>">
                    <input type="hidden" name="skipDay" value="true">
                    <input type="hidden" name="note" value="Skipped Day">
                </form>
            </div>
        </div>
    </main>

    <jsp:include page="footer.jsp" />

    <script>
        function addAmount(val) {
            const input = document.getElementById('spendAmount');
            const current = parseFloat(input.value) || 0;
            input.value = (current + val).toFixed(2);
        }

        function setNote(category) {
            document.getElementById('note').value = category;
        }

        function submitSkipDay() {
            const currentDay = document.getElementById('day').value;
            if (confirm('Skip Day ' + currentDay + '? This logs ₹0 spend and rolls the budget forward to increase future daily allowances.')) {
                document.getElementById('skipDayVal').value = currentDay;
                document.getElementById('skipDayForm').submit();
            }
        }
    </script>
</body>
</html>
