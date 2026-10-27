<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.jsp.Models.Plans, com.jsp.Models.SpendAmount, com.jsp.Utils.SecurityUtil" %>
<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect("index.jsp");
        return;
    }
    if (request.getAttribute("plan") == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }

    Plans plan = (Plans) request.getAttribute("plan");
    List<SpendAmount> spendList = (List<SpendAmount>) request.getAttribute("spend_amount");
    List<Map<String, Object>> spendedDetails = (List<Map<String, Object>>) request.getAttribute("spended_day");
    List<Map<String, Object>> allExpenses = (List<Map<String, Object>>) request.getAttribute("all_expenses");
    Integer selectedDay = (Integer) request.getAttribute("day");

    int planId = (plan != null) ? plan.getPlanId() : 0;
    String month = (plan != null) ? plan.getMonth() : "";
    int year = (plan != null) ? plan.getYear() : 2025;
    double totalBudget = (plan != null) ? plan.getAmount() : 0.0;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spend History - <%= SecurityUtil.escapeHtml(month) %> <%= year %> - Daily Budget Planner</title>
    <meta name="description" content="Day-by-day spend history, expense itemization, and remaining budget tracking.">
    <link rel="stylesheet" href="Css/headerfooter.css">
    <link rel="stylesheet" href="Css/dashboardStyle.css">
    <style>
        .history-hero {
            background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
            border: 1px solid var(--border-light);
            border-radius: var(--radius-lg);
            padding: 24px 28px;
            margin-bottom: 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 20px;
        }
        .history-title-box h1 {
            font-family: 'Outfit', sans-serif;
            font-size: 26px;
            font-weight: 800;
            color: var(--text-main);
            letter-spacing: -0.02em;
        }
        .history-title-box p {
            color: var(--text-muted);
            font-size: 14px;
        }
        .history-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }
        .day-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 34px;
            height: 34px;
            border-radius: 8px;
            background: #eef2ff;
            color: var(--indigo-600);
            font-weight: 800;
            font-size: 14px;
            border: 1px solid #c7d2fe;
        }
        .detail-card {
            background: #ffffff;
            border: 1.5px solid var(--primary-300);
            border-radius: var(--radius-lg);
            padding: 24px 28px;
            margin-bottom: 30px;
            box-shadow: 0 8px 24px rgba(16, 185, 129, 0.12);
            animation: slideDown 0.3s ease;
        }
        .detail-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--border-light);
        }
        .detail-header h3 {
            font-family: 'Outfit', sans-serif;
            font-size: 20px;
            font-weight: 800;
            color: var(--text-main);
        }
        .note-tag {
            background: var(--bg-surface-alt);
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 13px;
            color: var(--text-main);
            font-weight: 500;
        }
        @media print {
            .header-wrapper, .footer-wrapper, .history-actions, .btn-action {
                display: none !important;
            }
            .page-container {
                max-width: 100% !important;
                padding: 0 !important;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="page-container">
        <!-- Hero Header -->
        <div class="history-hero">
            <div class="history-title-box">
                <span class="plan-badge on-track" style="margin-bottom: 8px; display: inline-block;">Plan #<%= planId %> History</span>
                <h1><%= SecurityUtil.escapeHtml(month) %> <%= year %> Spend History</h1>
                <p>Day-by-day record of actual expenses, daily budget adjustments, and transaction notes.</p>
            </div>

            <div class="history-actions">
                <a href="<%= request.getContextPath() %>/SpendAmount.jsp?planId=<%= planId %>" class="btn btn-primary">
                    <span>⚡</span> Record Spend
                </a>
                <button type="button" class="btn btn-secondary" onclick="window.print()">
                    <span>🖨️</span> Print / Export
                </button>
                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">
                    <span>←</span> Dashboard
                </a>
            </div>
        </div>

        <!-- Selected Day Detailed Itemization Drawer -->
        <% if (spendedDetails != null && selectedDay != null) { %>
            <div class="detail-card">
                <div class="detail-header">
                    <h3><span>🔍</span> Detailed Expenses for Day <%= selectedDay %></h3>
                    <a href="<%= request.getContextPath() %>/history?planId=<%= planId %>" class="btn btn-secondary" style="padding: 4px 10px; font-size: 12px;">Close Details ✕</a>
                </div>

                <div class="table-card" style="margin-bottom: 0;">
                    <table class="budget-table">
                        <thead>
                            <tr>
                                <th>Transaction #</th>
                                <th>Expense Amount</th>
                                <th>Category / Note</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                                double dayTotalSum = 0;
                                int txNum = 1;
                                for (Map<String, Object> entry : spendedDetails) { 
                                    double amt = (Double) entry.get("spended_amount");
                                    dayTotalSum += amt;
                                    String rawNote = (String) entry.get("note");
                            %>
                            <tr>
                                <td>#<%= txNum++ %></td>
                                <td class="table-amount">₹<%= String.format("%,.2f", amt) %></td>
                                <td>
                                    <span class="note-tag"><%= SecurityUtil.escapeHtml(rawNote) %></span>
                                </td>
                            </tr>
                            <% } %>
                            <tr style="background: var(--primary-50); font-weight: 700;">
                                <td>Day <%= selectedDay %> Total</td>
                                <td class="table-amount" style="color: var(--primary-800);">₹<%= String.format("%,.2f", dayTotalSum) %></td>
                                <td>Total for this day</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>

        <!-- Day-wise Running Budget Table -->
        <div class="section-header">
            <h2 class="section-title">
                <span>📆</span> Day-Wise Budget & Spending Summary
            </h2>
        </div>

        <div class="table-card">
            <table class="budget-table">
                <thead>
                    <tr>
                        <th style="width: 80px;">Day</th>
                        <th>Spent Today</th>
                        <th>Remaining Balance</th>
                        <th>Calculated Daily Target</th>
                        <th>Day Status</th>
                        <th style="text-align: right;">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        if (spendList != null && !spendList.isEmpty()) {
                            for (SpendAmount sa : spendList) {
                                boolean isSelected = (selectedDay != null && selectedDay == sa.getDay());
                                double dayAmt = sa.getDayAmount();
                                double dailyTarget = sa.getDailyBudget();
                                String statusBadge = "on-track";
                                String statusText = "On Target";

                                if (dayAmt == 0.0) {
                                    statusBadge = "warning";
                                    statusText = "Skipped / Zero";
                                } else if (dayAmt > dailyTarget && dailyTarget > 0) {
                                    statusBadge = "danger";
                                    statusText = "Above Target";
                                }
                    %>
                    <tr style="<%= isSelected ? "background-color: var(--primary-50);" : "" %>">
                        <td>
                            <span class="day-badge"><%= sa.getDay() %></span>
                        </td>
                        <td class="table-amount">
                            ₹<%= String.format("%,.2f", sa.getDayAmount()) %>
                        </td>
                        <td>
                            <strong>₹<%= String.format("%,.2f", sa.getCurrentAmount()) %></strong>
                        </td>
                        <td>
                            ₹<%= String.format("%,.2f", sa.getDailyBudget()) %>/day
                        </td>
                        <td>
                            <span class="plan-badge <%= statusBadge %>"><%= statusText %></span>
                        </td>
                        <td style="text-align: right;">
                            <a href="<%= request.getContextPath() %>/history?planId=<%= sa.getPlanId() %>&day=<%= sa.getDay() %>" class="btn btn-secondary" style="padding: 6px 12px; font-size: 13px;" id="btn-view-day-<%= sa.getDay() %>">
                                <span>🔍</span> View Notes
                            </a>
                        </td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 40px; color: var(--text-muted);">
                            No spend data recorded yet for this plan. Click <strong>Record Spend</strong> to log your first expenditure!
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>

        <!-- All Transactions Itemized Table -->
        <% if (allExpenses != null && !allExpenses.isEmpty()) { %>
            <div class="section-header" style="margin-top: 40px;">
                <h2 class="section-title">
                    <span>🧾</span> Complete Itemized Expense Log
                </h2>
            </div>

            <div class="table-card">
                <table class="budget-table">
                    <thead>
                        <tr>
                            <th style="width: 80px;">Day</th>
                            <th>Amount</th>
                            <th>Category / Note</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Map<String, Object> tx : allExpenses) { 
                            String noteVal = (String) tx.get("note");
                        %>
                        <tr>
                            <td><span class="day-badge"><%= tx.get("day") %></span></td>
                            <td class="table-amount">₹<%= String.format("%,.2f", (Double) tx.get("spended_amount")) %></td>
                            <td><span class="note-tag"><%= SecurityUtil.escapeHtml(noteVal) %></span></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>

        <div style="margin-top: 30px; text-align: center;">
            <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-secondary">
                <span>←</span> Return to Dashboard
            </a>
        </div>
    </main>

    <jsp:include page="footer.jsp" />
</body>
</html>
