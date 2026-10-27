<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.jsp.Models.Plans, com.jsp.Utils.SecurityUtil, com.jsp.Utils.CSRFTokenUtil"%>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("index.jsp");
        return;
    }
    if (request.getAttribute("plans") == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    String username = (String) session.getAttribute("username");
    String csrfToken = CSRFTokenUtil.getToken(session);
    List<Plans> plans = (List<Plans>) request.getAttribute("plans");
    Map<Integer, Map<String, Object>> planStats = (Map<Integer, Map<String, Object>>) request.getAttribute("planStats");
    
    Double totalBudgetAllocated = (Double) request.getAttribute("totalBudgetAllocated");
    Double totalSpentAll = (Double) request.getAttribute("totalSpentAll");
    Double totalRemainingAll = (Double) request.getAttribute("totalRemainingAll");
    
    if (totalBudgetAllocated == null) totalBudgetAllocated = 0.0;
    if (totalSpentAll == null) totalSpentAll = 0.0;
    if (totalRemainingAll == null) totalRemainingAll = 0.0;
    
    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Financial Dashboard - Daily Budget Planner</title>
    <meta name="description" content="Manage your monthly budgets, review daily limits, and track spending progress.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/dashboardStyle.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <!-- Hero Greeting & Actions -->
        <div class="dashboard-hero">
            <div class="hero-text">
                <h1>Hello, <%= (username != null) ? SecurityUtil.escapeHtml(username) : "Planner" %>! 👋</h1>
                <p>Welcome to your personal budget command center. Here is your spending health overview.</p>
            </div>
            <div class="hero-actions">
                <a href="AddPlan.jsp" class="btn btn-primary" id="btn-create-plan">
                    <span>➕</span> Create New Budget Plan
                </a>
            </div>
        </div>

        <!-- Alert Notifications -->
        <% if ("plan_created".equals(msg)) { %>
            <div class="alert alert-success">
                <span>🎉</span> <strong>Success!</strong> New budget plan has been created and initialized.
            </div>
        <% } else if ("plan_updated".equals(msg)) { %>
            <div class="alert alert-success">
                <span>✅</span> <strong>Updated!</strong> Plan details and daily limits recalculated successfully.
            </div>
        <% } else if ("plan_deleted".equals(msg)) { %>
            <div class="alert alert-success">
                <span>🗑️</span> Plan and its records were deleted successfully.
            </div>
        <% } %>

        <% if (error != null && !error.isEmpty()) { %>
            <div class="alert alert-error">
                <span>⚠️</span> <%= SecurityUtil.escapeHtml(error) %>
            </div>
        <% } %>

        <!-- Key Metrics Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon emerald">💳</div>
                <div class="stat-details">
                    <span class="stat-label">Total Allocated</span>
                    <span class="stat-value">₹<%= String.format("%,.2f", totalBudgetAllocated) %></span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon rose">💸</div>
                <div class="stat-details">
                    <span class="stat-label">Total Spent</span>
                    <span class="stat-value">₹<%= String.format("%,.2f", totalSpentAll) %></span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon indigo">💰</div>
                <div class="stat-details">
                    <span class="stat-label">Total Remaining</span>
                    <span class="stat-value">₹<%= String.format("%,.2f", totalRemainingAll) %></span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon amber">📅</div>
                <div class="stat-details">
                    <span class="stat-label">Active Plans</span>
                    <span class="stat-value"><%= (plans != null) ? plans.size() : 0 %></span>
                </div>
            </div>
        </div>

        <!-- Plans Section Header & Toggle -->
        <div class="section-header">
            <h2 class="section-title">
                <span>📋</span> Your Monthly Budget Plans
            </h2>
            <div class="view-toggle">
                <button type="button" class="toggle-btn active" id="btn-grid-view" onclick="switchView('grid')">Cards View</button>
                <button type="button" class="toggle-btn" id="btn-table-view" onclick="switchView('table')">Table View</button>
            </div>
        </div>

        <% if (plans != null && !plans.isEmpty()) { %>
            <!-- 1. Card Grid View -->
            <div class="plans-grid" id="plans-card-view">
                <% 
                    for (Plans plan : plans) { 
                        Map<String, Object> stats = (planStats != null) ? planStats.get(plan.getPlanId()) : null;
                        double spent = (stats != null && stats.get("spent") != null) ? (Double) stats.get("spent") : 0.0;
                        double remaining = (stats != null && stats.get("remaining") != null) ? (Double) stats.get("remaining") : plan.getAmount();
                        double dailyBudget = (stats != null && stats.get("dailyBudget") != null) ? (Double) stats.get("dailyBudget") : (plan.getAmount() / 30.0);
                        long percentSpent = (stats != null && stats.get("percentSpent") != null) ? (Long) stats.get("percentSpent") : 0;
                        
                        String badgeClass = "on-track";
                        String badgeText = "On Track";
                        String progressClass = "";
                        if (percentSpent >= 90) {
                            badgeClass = "danger";
                            badgeText = "Critically Low";
                            progressClass = "danger";
                        } else if (percentSpent >= 70) {
                            badgeClass = "warning";
                            badgeText = "Careful";
                            progressClass = "warning";
                        }
                %>
                <div class="plan-card">
                    <div class="plan-card-header">
                        <div class="plan-title-box">
                            <h3 class="plan-month"><%= SecurityUtil.escapeHtml(plan.getMonth()) %> <%= plan.getYear() %></h3>
                            <span class="plan-id-tag">Plan #<%= plan.getPlanId() %></span>
                        </div>
                        <span class="plan-badge <%= badgeClass %>"><%= badgeText %></span>
                    </div>

                    <div class="plan-amount-box">
                        <span class="plan-total-sub">Total Monthly Budget</span>
                        <div class="plan-total-amount">₹<%= String.format("%,.2f", plan.getAmount()) %></div>
                    </div>

                    <div class="progress-container">
                        <div class="progress-label-bar">
                            <span><%= percentSpent %>% Spent</span>
                            <span>Remaining: ₹<%= String.format("%,.2f", remaining) %></span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill <%= progressClass %>" style="width: <%= percentSpent %>%;"></div>
                        </div>
                    </div>

                    <div class="plan-metrics-row">
                        <div class="metric-col">
                            <span>Total Spent</span>
                            <span>₹<%= String.format("%,.2f", spent) %></span>
                        </div>
                        <div class="metric-col">
                            <span>Daily Limit</span>
                            <span>₹<%= String.format("%,.2f", dailyBudget) %>/day</span>
                        </div>
                    </div>

                    <div class="plan-actions-group">
                        <a href="SpendAmount.jsp?planId=<%= plan.getPlanId() %>" class="btn-card-action btn-spend" id="btn-spend-<%= plan.getPlanId() %>">
                            <span>⚡</span> Spend Today
                        </a>
                        <a href="<%= request.getContextPath() %>/history?planId=<%= plan.getPlanId() %>" class="btn-card-action btn-history" id="btn-history-<%= plan.getPlanId() %>">
                            <span>📜</span> History
                        </a>
                        <a href="EditPlan.jsp?planId=<%= plan.getPlanId() %>" class="btn-card-action btn-edit" id="btn-edit-<%= plan.getPlanId() %>">
                            <span>✏️</span> Edit
                        </a>
                        <form action="deletePlan" method="post" style="display:inline; margin:0;" onsubmit="return confirm('Are you sure you want to permanently delete the budget plan for <%= SecurityUtil.escapeHtml(plan.getMonth()) %> <%= plan.getYear() %>?');">
                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>" />
                            <input type="hidden" name="planId" value="<%= plan.getPlanId() %>" />
                            <button type="submit" class="btn-card-action btn-delete" id="btn-delete-<%= plan.getPlanId() %>" style="border:none; cursor:pointer;" title="Delete Plan">
                                <span>🗑️</span>
                            </button>
                        </form>
                    </div>
                </div>
                <% } %>
            </div>

            <!-- 2. Table View (Hidden by default) -->
            <div class="table-card" id="plans-table-view" style="display: none;">
                <table class="budget-table">
                    <thead>
                        <tr>
                            <th>Plan ID</th>
                            <th>Month & Year</th>
                            <th>Total Budget</th>
                            <th>Total Spent</th>
                            <th>Remaining</th>
                            <th>Daily Target</th>
                            <th>Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Plans plan : plans) { 
                            Map<String, Object> stats = (planStats != null) ? planStats.get(plan.getPlanId()) : null;
                            double spent = (stats != null && stats.get("spent") != null) ? (Double) stats.get("spent") : 0.0;
                            double remaining = (stats != null && stats.get("remaining") != null) ? (Double) stats.get("remaining") : plan.getAmount();
                            double dailyBudget = (stats != null && stats.get("dailyBudget") != null) ? (Double) stats.get("dailyBudget") : (plan.getAmount() / 30.0);
                            long percentSpent = (stats != null && stats.get("percentSpent") != null) ? (Long) stats.get("percentSpent") : 0;
                            
                            String badgeClass = "on-track";
                            String badgeText = "On Track";
                            if (percentSpent >= 90) {
                                badgeClass = "danger";
                                badgeText = "Low Balance";
                            } else if (percentSpent >= 70) {
                                badgeClass = "warning";
                                badgeText = "Careful";
                            }
                        %>
                        <tr>
                            <td>#<%= plan.getPlanId() %></td>
                            <td><strong><%= SecurityUtil.escapeHtml(plan.getMonth()) %> <%= plan.getYear() %></strong></td>
                            <td class="table-amount">₹<%= String.format("%,.2f", plan.getAmount()) %></td>
                            <td>₹<%= String.format("%,.2f", spent) %></td>
                            <td><strong>₹<%= String.format("%,.2f", remaining) %></strong></td>
                            <td>₹<%= String.format("%,.2f", dailyBudget) %>/day</td>
                            <td><span class="plan-badge <%= badgeClass %>"><%= badgeText %></span></td>
                            <td style="text-align: right;">
                                <a href="SpendAmount.jsp?planId=<%= plan.getPlanId() %>" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;">Spend</a>
                                <a href="<%= request.getContextPath() %>/history?planId=<%= plan.getPlanId() %>" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">History</a>
                                <a href="EditPlan.jsp?planId=<%= plan.getPlanId() %>" class="btn btn-secondary" style="padding: 6px 10px; font-size: 12px;">Edit</a>
                                <form action="deletePlan" method="post" style="display:inline; margin:0;" onsubmit="return confirm('Delete this plan?');">
                                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>" />
                                    <input type="hidden" name="planId" value="<%= plan.getPlanId() %>" />
                                    <button type="submit" class="btn btn-danger" style="padding: 6px 10px; font-size: 12px; border:none; cursor:pointer;" title="Delete Plan">Delete</button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

        <% } else { %>
            <!-- Empty State -->
            <div class="empty-state">
                <div class="empty-icon">📂</div>
                <h3 class="empty-title">No Budget Plans Found</h3>
                <p class="empty-desc">You haven't set up any monthly budgets yet. Create your first budget plan to unlock automatic daily limits and smart financial control.</p>
                <a href="AddPlan.jsp" class="btn btn-primary">
                    <span>➕</span> Create First Budget Plan
                </a>
            </div>
        <% } %>
    </main>

    <jsp:include page="footer.jsp" />

    <script>
        function switchView(view) {
            const cardView = document.getElementById('plans-card-view');
            const tableView = document.getElementById('plans-table-view');
            const btnGrid = document.getElementById('btn-grid-view');
            const btnTable = document.getElementById('btn-table-view');

            if (view === 'grid') {
                if (cardView) cardView.style.display = 'grid';
                if (tableView) tableView.style.display = 'none';
                btnGrid.classList.add('active');
                btnTable.classList.remove('active');
            } else {
                if (cardView) cardView.style.display = 'none';
                if (tableView) tableView.style.display = 'block';
                btnTable.classList.add('active');
                btnGrid.classList.remove('active');
            }
        }
    </script>
</body>
</html>