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
import com.jsp.Models.SpendAmount;
import com.jsp.Utils.InputValidator;

public class SpendAmountDAO {
    private static final Logger LOGGER = Logger.getLogger(SpendAmountDAO.class.getName());
    private final Connection conn;

    public SpendAmountDAO(Connection conn) {
        this.conn = conn;
    }

    // Get latest spend_amount for plan, with auto-initialization if none exists yet
    public SpendAmount getLatestSpendAmountForPlan(int planId) {
        SpendAmount spend = null;
        String sql = "SELECT spendId, planId, day, day_amount, current_amount, daily_budget FROM spend_amount WHERE planId = ? ORDER BY day DESC, spendId DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    spend = new SpendAmount();
                    spend.setSpendId(rs.getInt("spendId"));
                    spend.setPlanId(rs.getInt("planId"));
                    spend.setDay(rs.getInt("day"));
                    spend.setDayAmount(rs.getDouble("day_amount"));
                    spend.setCurrentAmount(rs.getDouble("current_amount"));
                    spend.setDailyBudget(rs.getDouble("daily_budget"));
                    return spend;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching latest spend_amount for planId: " + planId, e);
        }

        // If no record found, auto-initialize Day 1
        try {
            Plans plan = PlanDAO.getPlanById(planId);
            if (plan != null) {
                PlanDAO planDAO = new PlanDAO();
                planDAO.initializeSpendAmounts(conn, planId, plan.getAmount(), plan.getMonth(), plan.getYear());

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, planId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            spend = new SpendAmount();
                            spend.setSpendId(rs.getInt("spendId"));
                            spend.setPlanId(rs.getInt("planId"));
                            spend.setDay(rs.getInt("day"));
                            spend.setDayAmount(rs.getDouble("day_amount"));
                            spend.setCurrentAmount(rs.getDouble("current_amount"));
                            spend.setDailyBudget(rs.getDouble("daily_budget"));
                            return spend;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error auto-initializing spend_amount for planId: " + planId, e);
        }

        return null;
    }

    // Checks if any transactions exist in spended_day for a given spendId and day
    public boolean hasTransactionsForDay(int spendId, int day) throws SQLException {
        String sql = "SELECT 1 FROM spended_day WHERE spendId = ? AND day = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spendId);
            ps.setInt(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Total spend amount for that day
    public double getTotalSpendForDay(int spendId, int day) throws SQLException {
        String sql = "SELECT COALESCE(SUM(spended_amount), 0) AS total FROM spended_day WHERE spendId = ? AND day = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spendId);
            ps.setInt(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getDouble("total"))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }
            }
        }
        return 0.0;
    }

    // Insert spended_day data
    public void insertSpendedDay(int planId, int day, double amount, String note) throws SQLException {
        int spendId = getSpendId(planId, day);
        String sql = "INSERT INTO spended_day (spendId, day, spended_amount, note) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spendId);
            ps.setInt(2, day);
            ps.setDouble(3, BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue());
            ps.setString(4, InputValidator.sanitizeNote(note));
            ps.executeUpdate();
        }
    }

    // Recalculate day amounts, remaining balance, and daily budgets across the plan
    public void recalculateDayAndFuture(int planId, int fromDay) throws SQLException {
        String getSql = "SELECT spendId, day FROM spend_amount WHERE planId = ? ORDER BY day ASC, spendId ASC";
        List<SpendAmount> days = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(getSql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SpendAmount s = new SpendAmount();
                    s.setSpendId(rs.getInt("spendId"));
                    s.setDay(rs.getInt("day"));
                    days.add(s);
                }
            }
        }

        BigDecimal planTotal = BigDecimal.valueOf(getPlanTotalAmount(planId)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal runningAmount = planTotal;
        int totalDays = getTotalDaysInMonth(planId);

        String update = "UPDATE spend_amount SET day_amount = ?, current_amount = ?, daily_budget = ? WHERE spendId = ?";
        try (PreparedStatement updatePs = conn.prepareStatement(update)) {
            for (SpendAmount s : days) {
                double dayAmtRaw = getTotalSpendForDay(s.getSpendId(), s.getDay());
                BigDecimal dayAmt = BigDecimal.valueOf(dayAmtRaw).setScale(2, RoundingMode.HALF_UP);
                runningAmount = runningAmount.subtract(dayAmt).setScale(2, RoundingMode.HALF_UP);

                boolean hasTx = hasTransactionsForDay(s.getSpendId(), s.getDay());
                int remainingDays;
                if (hasTx) {
                    // Spending has completed for this day; budget distributes over upcoming days
                    remainingDays = totalDays - s.getDay();
                } else {
                    // This day has not had any transactions yet; budget covers this day plus upcoming days
                    remainingDays = totalDays - s.getDay() + 1;
                }

                BigDecimal dailyBudget;
                if (remainingDays > 0) {
                    dailyBudget = runningAmount.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP);
                } else {
                    dailyBudget = runningAmount;
                }

                if (dailyBudget.compareTo(BigDecimal.ZERO) < 0) {
                    dailyBudget = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }

                updatePs.setDouble(1, dayAmt.doubleValue());
                updatePs.setDouble(2, runningAmount.doubleValue());
                updatePs.setDouble(3, dailyBudget.doubleValue());
                updatePs.setInt(4, s.getSpendId());
                updatePs.executeUpdate();
            }
        }
    }

    // Check if spend amount is valid (cannot exceed overall remaining balance for the plan)
    public boolean isSpendAmountValid(int planId, double spendAmount) throws SQLException {
        if (spendAmount < 0) {
            return false;
        }
        BigDecimal planTotal = BigDecimal.valueOf(getPlanTotalAmount(planId)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalSpent = BigDecimal.valueOf(getTotalSpentForPlan(planId)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = planTotal.subtract(totalSpent).setScale(2, RoundingMode.HALF_UP);

        BigDecimal requested = BigDecimal.valueOf(spendAmount).setScale(2, RoundingMode.HALF_UP);
        return requested.compareTo(remaining) <= 0;
    }

    // Total days in month
    public int getTotalDaysInMonth(int planId) throws SQLException {
        String sql = "SELECT month, year FROM plans WHERE planId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String month = rs.getString("month");
                    int year = rs.getInt("year");
                    if (month != null && !month.trim().isEmpty()) {
                        Month monthEnum = Month.valueOf(month.trim().toUpperCase());
                        YearMonth ym = YearMonth.of(year, monthEnum.getValue());
                        return ym.lengthOfMonth();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error resolving days in month for planId: " + planId, e);
        }
        return 30;
    }

    // Total plan amount
    public double getPlanTotalAmount(int planId) throws SQLException {
        String sql = "SELECT amount FROM plans WHERE planId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getDouble("amount"))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }
            }
        }
        return 0.0;
    }

    // Total spent so far for a plan (calculated authoritatively across all transactions)
    public double getTotalSpentForPlan(int planId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(sd.spended_amount), 0) AS total " +
                     "FROM spended_day sd " +
                     "JOIN spend_amount sa ON sd.spendId = sa.spendId " +
                     "WHERE sa.planId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return BigDecimal.valueOf(rs.getDouble("total"))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }
            }
        }
        return 0.0;
    }

    // Get or create spendId for a specific plan and day atomically
    public int getSpendId(int planId, int day) throws SQLException {
        String sql = "SELECT spendId FROM spend_amount WHERE planId = ? AND day = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setInt(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("spendId");
                }
            }
        }

        // Insert new day entry
        String insertSql = "INSERT INTO spend_amount (planId, day, day_amount, current_amount, daily_budget) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, planId);
            ps.setInt(2, day);
            ps.setDouble(3, 0.0);
            ps.setDouble(4, getPlanTotalAmount(planId));
            ps.setDouble(5, 0.0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            // In case of race condition with unique constraint, query once more
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, planId);
                ps.setInt(2, day);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("spendId");
                    }
                }
            }
            throw e;
        }

        throw new SQLException("Failed to create spendId for planId=" + planId + " and day=" + day);
    }
}
