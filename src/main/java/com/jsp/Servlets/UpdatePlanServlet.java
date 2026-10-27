package com.jsp.Servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.PlanDAO;
import com.jsp.Models.Plans;
import com.jsp.Utils.CSRFTokenUtil;
import com.jsp.Utils.InputValidator;

public class UpdatePlanServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String planIdStr = request.getParameter("planId");

        // Validate CSRF token
        if (!CSRFTokenUtil.isValid(request)) {
            response.sendRedirect("EditPlan.jsp?planId=" + (planIdStr != null ? planIdStr : "") + "&error=" +
                    URLEncoder.encode("Security validation failed (invalid CSRF token).", "UTF-8"));
            return;
        }

        String month = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String amountStr = request.getParameter("amount");

        if (planIdStr == null || month == null || yearStr == null || amountStr == null) {
            response.sendRedirect("dashboard?error=Missing+update+parameters");
            return;
        }

        try {
            int planId = Integer.parseInt(planIdStr.trim());
            int year = Integer.parseInt(yearStr.trim());
            BigDecimal amountDec = new BigDecimal(amountStr.trim()).setScale(2, RoundingMode.HALF_UP);

            if (!InputValidator.isValidMonth(month.trim())) {
                response.sendRedirect("EditPlan.jsp?planId=" + planId + "&error=Invalid+month+selected");
                return;
            }

            if (!InputValidator.isValidYear(year)) {
                response.sendRedirect("EditPlan.jsp?planId=" + planId + "&error=Year+must+be+between+2020+and+2099");
                return;
            }

            if (!InputValidator.isValidBudgetAmount(amountDec)) {
                response.sendRedirect("EditPlan.jsp?planId=" + planId + "&error=" +
                        URLEncoder.encode("Budget amount must be positive and not exceed ₹10,000,000.", "UTF-8"));
                return;
            }

            // Verify plan ownership
            Plans existingPlan = PlanDAO.getPlanById(planId, userId);
            if (existingPlan == null) {
                response.sendRedirect("dashboard?error=Unauthorized+access+or+plan+not+found");
                return;
            }

            Plans plan = new Plans();
            plan.setPlanId(planId);
            plan.setUserId(userId);
            plan.setMonth(month.trim());
            plan.setYear(year);
            plan.setAmount(amountDec.doubleValue());

            boolean updated = PlanDAO.updatePlan(plan, userId);
            if (updated) {
                response.sendRedirect("dashboard?msg=plan_updated");
            } else {
                response.sendRedirect("EditPlan.jsp?planId=" + planId + "&error=" +
                        URLEncoder.encode("Failed to update plan. A plan for that month and year may already exist.", "UTF-8"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("dashboard?error=Invalid+number+format");
        }
    }
}
