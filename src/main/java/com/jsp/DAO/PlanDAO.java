package com.jsp.DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jsp.Models.Plans;
import com.jsp.Utils.DBConnection;

public class PlanDAO {
    private static final Logger LOGGER = Logger.getLogger(PlanDAO.class.getName());

    // Insert plan and return generated planId
    public static int insertPlanAndReturnId(Connection conn, Plans plan) throws SQLException {
        if (plan == null) {
            return -1;
        }

        // Check if plan already exists for this user/month/year
        String checkSql = "SELECT planId FROM plans WHERE userId = ? AND LOWER(TRIM(month)) = LOWER(TRIM(?)) AND year = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, plan.getUserId());
            checkStmt.setString(2, plan.getMonth());
            checkStmt.setInt(3, plan.getYear());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return -1; // Plan already exists
                }
            }
        }

        // Insert new plan
        String sql = "INSERT INTO plans (userId, month, year, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getMonth().trim());
            stmt.setInt(3, plan.getYear());
            stmt.setDouble(4, plan.getAmount());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    // Check if plan exists for user, month, year
    public static boolean planExists(int userId, String month, int year) {
        if (month == null) {
            return false;
        }
        String sql = "SELECT planId FROM plans WHERE userId = ? AND LOWER(TRIM(month)) = LOWER(TRIM(?)) AND year = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, month.trim());
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if plan exists for user: " + userId, e);
            return false;
        }
    }

    // Fetch all plans for a user
    public static List<Plans> getPlansByUserId(int userId) {
        List<Plans> plansList = new ArrayList<>();
        String sql = "SELECT planId, userId, month, year, amount FROM plans WHERE userId = ? ORDER BY year DESC, planId DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Plans plan = new Plans();
                    plan.setPlanId(rs.getInt("planId"));
                    plan.setUserId(rs.getInt("userId"));
                    plan.setMonth(rs.getString("month"));
                    plan.setYear(rs.getInt("year"));
                    plan.setAmount(rs.getDouble("amount"));
                    plansList.add(plan);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to fetch plans for user: " + userId, e);
            throw new RuntimeException("Unable to fetch plans.", e);
        }

        return plansList;
    }

    // Update plan details with user ownership verification
    public static boolean updatePlan(Plans plan, int userId) {
        if (plan == null) {
            return false;
        }

        // Check if updating month/year causes a duplicate for this user
        String checkDuplicateSql = "SELECT planId FROM plans WHERE userId = ? AND LOWER(TRIM(month)) = LOWER(TRIM(?)) AND year = ? AND planId != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setString(2, plan.getMonth().trim());
            checkStmt.setInt(3, plan.getYear());
            checkStmt.setInt(4, plan.getPlanId());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    LOGGER.warning("Duplicate plan detected for update: " + plan.getMonth() + " " + plan.getYear());
                    return false;
                }
            }

            String sql = "UPDATE plans SET month = ?, year = ?, amount = ? WHERE planId = ? AND userId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plan.getMonth().trim());
                stmt.setInt(2, plan.getYear());
                stmt.setDouble(3, plan.getAmount());
                stmt.setInt(4, plan.getPlanId());
                stmt.setInt(5, userId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    // Recalculate spend amounts after updating plan total amount
                    SpendAmountDAO spendDAO = new SpendAmountDAO(conn);
                    spendDAO.recalculateDayAndFuture(plan.getPlanId(), 1);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating plan: " + plan.getPlanId(), e);
            return false;
        }
    }

    // Overload for backward compatibility
    public static boolean updatePlan(Plans plan) {
        if (plan == null) return false;
        return updatePlan(plan, plan.getUserId());
    }

    // Delete plan and its associated spend entries with ownership verification
    public static boolean deletePlanById(int planId, int userId) {
        String selectSpendIdsSql = "SELECT spendId FROM spend_amount WHERE planId = ?";
        String deleteSpendedDaySql = "DELETE FROM spended_day WHERE spendId = ?";
        String deleteSpendAmountSql = "DELETE FROM spend_amount WHERE planId = ?";
        String deletePlanSql = "DELETE FROM plans WHERE planId = ? AND userId = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (
                PreparedStatement selectSpendIdsStmt = conn.prepareStatement(selectSpendIdsSql);
                PreparedStatement deleteSpendedDayStmt = conn.prepareStatement(deleteSpendedDaySql);
                PreparedStatement deleteSpendAmountStmt = conn.prepareStatement(deleteSpendAmountSql);
                PreparedStatement deletePlanStmt = conn.prepareStatement(deletePlanSql)
            ) {
                // Get all spendIds for this planId
                selectSpendIdsStmt.setInt(1, planId);
                try (ResultSet rs = selectSpendIdsStmt.executeQuery()) {
                    while (rs.next()) {
                        int spendId = rs.getInt("spendId");
                        deleteSpendedDayStmt.setInt(1, spendId);
                        deleteSpendedDayStmt.executeUpdate();
                    }
                }

                // Delete from spend_amount
                deleteSpendAmountStmt.setInt(1, planId);
                deleteSpendAmountStmt.executeUpdate();

                // Delete from plans with ownership check
                deletePlanStmt.setInt(1, planId);
                deletePlanStmt.setInt(2, userId);
                int rowsAffected = deletePlanStmt.executeUpdate();

                conn.commit();
                return rowsAffected > 0;
            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Failed transaction while deleting plan: " + planId, e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error connecting to delete plan: " + planId, e);
            return false;
        }
    }

    // Overload for backward compatibility
    public static boolean deletePlanById(int planId) {
        Plans plan = getPlanById(planId);
        if (plan == null) {
            return false;
        }
        return deletePlanById(planId, plan.getUserId());
    }

    // Get a specific plan by planId
    public static Plans getPlanById(int planId) {
        String sql = "SELECT planId, userId, month, year, amount FROM plans WHERE planId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Plans plan = new Plans();
                    plan.setPlanId(rs.getInt("planId"));
                    plan.setMonth(rs.getString("month"));
                    plan.setYear(rs.getInt("year"));
                    plan.setAmount(rs.getDouble("amount"));
                    plan.setUserId(rs.getInt("userId"));
                    return plan;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching plan by ID: " + planId, e);
        }
        return null;
    }

    // Get a specific plan by planId with ownership check
    public static Plans getPlanById(int planId, int userId) {
        Plans plan = getPlanById(planId);
        if (plan != null && plan.getUserId() == userId) {
            return plan;
        }
        return null;
    }

    // Initialize day 1 spend_amount for a newly created plan
    public void initializeSpendAmounts(Connection conn, int planId, double currentAmount, String month, int year) throws SQLException {
        int totalDays = getTotalDaysInMonth(month, year);
        BigDecimal budgetDec = BigDecimal.valueOf(currentAmount);
        BigDecimal daysDec = BigDecimal.valueOf(totalDays);
        BigDecimal dailyBudgetDec = budgetDec.divide(daysDec, 2, RoundingMode.HALF_UP);
        double dailyBudget = dailyBudgetDec.doubleValue();

        String checkSql = "SELECT spendId FROM spend_amount WHERE planId = ? AND day = 1";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, planId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return; // Already initialized
                }
            }
        }

        String sql = "INSERT INTO spend_amount (planId, day, day_amount, current_amount, daily_budget) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setInt(2, 1);
            ps.setDouble(3, 0.0);
            ps.setDouble(4, currentAmount);
            ps.setDouble(5, dailyBudget);
            ps.executeUpdate();
        }
    }

    // Total number of days in that month (handles leap years accurately)
    public static int getTotalDaysInMonth(String month, int year) {
        if (month == null || month.trim().isEmpty()) {
            return 30;
        }
        try {
            Month monthEnum = Month.valueOf(month.trim().toUpperCase());
            YearMonth ym = YearMonth.of(year, monthEnum.getValue());
            return ym.lengthOfMonth();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid month: " + month + " or year: " + year + ", defaulting to 30 days.");
            return 30; // Fallback
        }
    }
}
