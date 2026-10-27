package com.jsp.Servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.PlanDAO;
import com.jsp.Models.Plans;
import com.jsp.Utils.CSRFTokenUtil;
import com.jsp.Utils.DBConnection;
import com.jsp.Utils.InputValidator;

public class AddPlanServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AddPlanServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        response.sendRedirect("AddPlan.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Validate CSRF token
        if (!CSRFTokenUtil.isValid(request)) {
            request.setAttribute("error", "Security validation failed (invalid CSRF token). Please try submitting again.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
            dispatcher.forward(request, response);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String month = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String amountStr = request.getParameter("amount");

        if (month == null || month.trim().isEmpty() || yearStr == null || amountStr == null) {
            request.setAttribute("error", "All fields are required.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
            dispatcher.forward(request, response);
            return;
        }

        month = month.trim();

        if (!InputValidator.isValidMonth(month)) {
            request.setAttribute("error", "Invalid month selected.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
            dispatcher.forward(request, response);
            return;
        }

        try {
            int year = Integer.parseInt(yearStr.trim());
            if (!InputValidator.isValidYear(year)) {
                request.setAttribute("error", "Year must be between 2020 and 2099.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
                dispatcher.forward(request, response);
                return;
            }

            BigDecimal amountDec = new BigDecimal(amountStr.trim()).setScale(2, RoundingMode.HALF_UP);
            if (!InputValidator.isValidBudgetAmount(amountDec)) {
                request.setAttribute("error", "Budget amount must be positive and not exceed ₹10,000,000.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
                dispatcher.forward(request, response);
                return;
            }

            // Check if plan already exists for this user, month, and year
            if (PlanDAO.planExists(userId, month, year)) {
                request.setAttribute("error", "A plan for " + month + " " + year + " already exists.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
                dispatcher.forward(request, response);
                return;
            }

            Plans plan = new Plans();
            plan.setUserId(userId);
            plan.setMonth(month);
            plan.setYear(year);
            plan.setAmount(amountDec.doubleValue());

            try (Connection conn = DBConnection.getConnection()) {
                int planId = PlanDAO.insertPlanAndReturnId(conn, plan);
                if (planId > 0) {
                    // Initialize Day 1 spend_amount
                    PlanDAO dao = new PlanDAO();
                    dao.initializeSpendAmounts(conn, planId, amountDec.doubleValue(), month, year);

                    response.sendRedirect("dashboard?msg=plan_created");
                } else {
                    request.setAttribute("error", "A plan for " + month + " " + year + " already exists.");
                    RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
                    dispatcher.forward(request, response);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Database error creating plan for user: " + userId, e);
                request.setAttribute("error", "Database error occurred while creating plan.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
                dispatcher.forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid numeric values entered for year or amount.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("AddPlan.jsp");
            dispatcher.forward(request, response);
        }
    }
}
