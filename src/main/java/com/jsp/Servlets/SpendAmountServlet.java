package com.jsp.Servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.PlanDAO;
import com.jsp.DAO.SpendAmountDAO;
import com.jsp.Models.Plans;
import com.jsp.Utils.CSRFTokenUtil;
import com.jsp.Utils.DBConnection;
import com.jsp.Utils.InputValidator;

public class SpendAmountServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SpendAmountServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String planId = request.getParameter("planId");
        if (planId != null) {
            response.sendRedirect("SpendAmount.jsp?planId=" + planId);
        } else {
            response.sendRedirect("dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String planIdStr = request.getParameter("planId");

        // Validate CSRF token
        if (!CSRFTokenUtil.isValid(request)) {
            response.sendRedirect("SpendAmount.jsp?planId=" + (planIdStr != null ? planIdStr : "") + "&error=" +
                    URLEncoder.encode("Security validation failed (invalid CSRF token).", "UTF-8"));
            return;
        }

        String dayStr = request.getParameter("day");
        String spendAmountStr = request.getParameter("spendAmount");
        String note = request.getParameter("note");
        String isSkipDay = request.getParameter("skipDay");

        if (planIdStr == null || dayStr == null) {
            response.sendRedirect("dashboard?error=Missing+plan+or+day+information");
            return;
        }

        try {
            int planId = Integer.parseInt(planIdStr.trim());
            int day = Integer.parseInt(dayStr.trim());

            // Verify ownership
            Plans plan = PlanDAO.getPlanById(planId, userId);
            if (plan == null) {
                response.sendRedirect("dashboard?error=Unauthorized+access+to+plan");
                return;
            }

            int totalDaysInMonth = PlanDAO.getTotalDaysInMonth(plan.getMonth(), plan.getYear());
            if (day < 1 || day > totalDaysInMonth) {
                response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&error=" +
                        URLEncoder.encode("Day must be between 1 and " + totalDaysInMonth + " for " + plan.getMonth() + ".", "UTF-8"));
                return;
            }

            boolean isSkipping = "true".equalsIgnoreCase(isSkipDay) || "skip".equalsIgnoreCase(request.getParameter("action"));
            BigDecimal spendAmountDec = BigDecimal.ZERO;

            if (isSkipping) {
                spendAmountDec = BigDecimal.ZERO;
                note = (note != null && !note.trim().isEmpty()) ? note.trim() : "Skipped Day";
            } else {
                if (spendAmountStr == null || spendAmountStr.trim().isEmpty()) {
                    response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&error=Spend+amount+is+required");
                    return;
                }
                spendAmountDec = new BigDecimal(spendAmountStr.trim()).setScale(2, RoundingMode.HALF_UP);
                if (spendAmountDec.compareTo(BigDecimal.ZERO) < 0) {
                    response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&error=Spend+amount+cannot+be+negative");
                    return;
                }
                if (!InputValidator.isValidSpendAmount(spendAmountDec)) {
                    response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&error=Invalid+spend+amount");
                    return;
                }
                if (note == null || note.trim().isEmpty()) {
                    note = "Daily Expense";
                }
            }

            try (Connection conn = DBConnection.getConnection()) {
                SpendAmountDAO dao = new SpendAmountDAO(conn);

                // Validation - check spendAmount is not greater than remaining balance
                if (spendAmountDec.compareTo(BigDecimal.ZERO) > 0 && !dao.isSpendAmountValid(planId, spendAmountDec.doubleValue())) {
                    response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&error=" +
                            URLEncoder.encode("Spend amount exceeds your remaining balance for this month.", "UTF-8"));
                    return;
                }

                // Insert spend record and recalculate future days
                dao.insertSpendedDay(planId, day, spendAmountDec.doubleValue(), note);
                dao.recalculateDayAndFuture(planId, day);

                String msg = isSkipping ? "day_skipped" : "spend_success";
                response.sendRedirect("SpendAmount.jsp?planId=" + planId + "&msg=" + msg);
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("SpendAmount.jsp?planId=" + planIdStr + "&error=" +
                    URLEncoder.encode("Please enter valid numbers for day and amount.", "UTF-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing spend in SpendAmountServlet", e);
            response.sendRedirect("SpendAmount.jsp?planId=" + planIdStr + "&error=" +
                    URLEncoder.encode("Error processing spend: " + e.getMessage(), "UTF-8"));
        }
    }
}
