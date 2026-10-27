package com.jsp.Servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsp.DAO.PlanDAO;
import com.jsp.Models.Plans;
import com.jsp.Utils.CSRFTokenUtil;

public class DeletePlanServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Enforce security: Data deletion MUST NOT be performed via HTTP GET
        response.sendRedirect("dashboard");
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
            response.sendRedirect("dashboard?error=Security+validation+failed+(invalid+CSRF+token)");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String planIdParam = request.getParameter("planId");

        if (planIdParam != null && !planIdParam.trim().isEmpty()) {
            try {
                int planId = Integer.parseInt(planIdParam.trim());

                // Verify ownership before deleting
                Plans plan = PlanDAO.getPlanById(planId, userId);
                if (plan != null) {
                    boolean deleted = PlanDAO.deletePlanById(planId, userId);
                    if (deleted) {
                        response.sendRedirect("dashboard?msg=plan_deleted");
                    } else {
                        response.sendRedirect("dashboard?error=Could+not+delete+plan");
                    }
                    return;
                } else {
                    response.sendRedirect("dashboard?error=Unauthorized+or+plan+not+found");
                    return;
                }
            } catch (NumberFormatException e) {
                response.sendRedirect("dashboard?error=Invalid+plan+id");
                return;
            }
        }

        response.sendRedirect("dashboard");
    }
}
