package com.jsp.Servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.HistoryDAO;
import com.jsp.DAO.PlanDAO;
import com.jsp.Models.Plans;
import com.jsp.Models.SpendAmount;

public class HistoryServlet extends HttpServlet {
    private HistoryDAO historyDAO;

    @Override
    public void init() {
        historyDAO = new HistoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String planIdStr = request.getParameter("planId");
        String dayStr = request.getParameter("day");

        if (planIdStr == null || planIdStr.trim().isEmpty()) {
            response.sendRedirect("dashboard");
            return;
        }

        try {
            int planId = Integer.parseInt(planIdStr.trim());

            // Verify plan ownership
            Plans plan = PlanDAO.getPlanById(planId, userId);
            if (plan == null) {
                response.sendRedirect("dashboard?error=Unauthorized+access+to+plan");
                return;
            }

            request.setAttribute("plan", plan);

            // Get all spend records for the given plan
            List<SpendAmount> spendList = historyDAO.getSpendAmountsByPlanId(planId);
            request.setAttribute("spend_amount", spendList);

            // Get all itemized expenses for quick reference
            List<Map<String, Object>> allExpenses = historyDAO.getAllSpendedDaysByPlanId(planId);
            request.setAttribute("all_expenses", allExpenses);

            // If day is specified, get detailed spending for that day
            if (dayStr != null && !dayStr.trim().isEmpty()) {
                int day = Integer.parseInt(dayStr.trim());
                int spendId = historyDAO.getSpendIdByPlanIdAndDay(planId, day);

                if (spendId > 0) {
                    List<Map<String, Object>> spendedDay = historyDAO.getSpendedDayBySpendId(spendId);
                    request.setAttribute("spended_day", spendedDay);
                    request.setAttribute("day", day);
                }
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("History.jsp");
            dispatcher.forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect("dashboard?error=Invalid+plan+id");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
