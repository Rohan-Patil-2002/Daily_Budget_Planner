package com.jsp.Servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.PlanDAO;
import com.jsp.DAO.SpendAmountDAO;
import com.jsp.Models.Plans;
import com.jsp.Models.SpendAmount;
import com.jsp.Utils.CSRFTokenUtil;
import com.jsp.Utils.DBConnection;

public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        CSRFTokenUtil.getToken(session); // Ensure CSRF token is available for forms

        try {
            List<Plans> plans = PlanDAO.getPlansByUserId(userId);
            request.setAttribute("plans", plans);

            BigDecimal totalBudgetAllocated = BigDecimal.ZERO;
            BigDecimal totalSpentAll = BigDecimal.ZERO;
            BigDecimal totalRemainingAll = BigDecimal.ZERO;

            Map<Integer, Map<String, Object>> planStats = new HashMap<>();

            try (Connection conn = DBConnection.getConnection()) {
                SpendAmountDAO spendDAO = new SpendAmountDAO(conn);

                for (Plans p : plans) {
                    BigDecimal planAmount = BigDecimal.valueOf(p.getAmount()).setScale(2, RoundingMode.HALF_UP);
                    totalBudgetAllocated = totalBudgetAllocated.add(planAmount);

                    SpendAmount latestSpend = spendDAO.getLatestSpendAmountForPlan(p.getPlanId());
                    double spentRaw = spendDAO.getTotalSpentForPlan(p.getPlanId());
                    BigDecimal spent = BigDecimal.valueOf(spentRaw).setScale(2, RoundingMode.HALF_UP);
                    
                    BigDecimal remaining;
                    if (latestSpend != null) {
                        remaining = BigDecimal.valueOf(latestSpend.getCurrentAmount()).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        remaining = planAmount;
                    }

                    int totalDays = PlanDAO.getTotalDaysInMonth(p.getMonth(), p.getYear());
                    double dailyBudgetRaw = (latestSpend != null) ? latestSpend.getDailyBudget() : (p.getAmount() / (double) totalDays);
                    BigDecimal dailyBudget = BigDecimal.valueOf(dailyBudgetRaw).setScale(2, RoundingMode.HALF_UP);

                    totalSpentAll = totalSpentAll.add(spent);
                    totalRemainingAll = totalRemainingAll.add(remaining);

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("spent", spent.doubleValue());
                    stats.put("remaining", remaining.doubleValue());
                    stats.put("dailyBudget", dailyBudget.doubleValue());

                    double percentSpent = 0.0;
                    if (planAmount.compareTo(BigDecimal.ZERO) > 0) {
                        percentSpent = spent.divide(planAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                    }
                    if (percentSpent > 100.0) percentSpent = 100.0;
                    stats.put("percentSpent", Math.round(percentSpent));

                    planStats.put(p.getPlanId(), stats);
                }
            }

            request.setAttribute("planStats", planStats);
            request.setAttribute("totalBudgetAllocated", totalBudgetAllocated.doubleValue());
            request.setAttribute("totalSpentAll", totalSpentAll.doubleValue());
            request.setAttribute("totalRemainingAll", totalRemainingAll.doubleValue());

            RequestDispatcher dispatcher = request.getRequestDispatcher("dashboard.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to load dashboard for user: " + userId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load dashboard.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}