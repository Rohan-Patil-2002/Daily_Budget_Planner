package com.jsp.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jsp.Models.SpendAmount;
import com.jsp.Utils.DBConnection;

public class HistoryDAO {
    private static final Logger LOGGER = Logger.getLogger(HistoryDAO.class.getName());

    // Fetch all spend_amount rows for a given planId
    public List<SpendAmount> getSpendAmountsByPlanId(int planId) {
        List<SpendAmount> list = new ArrayList<>();
        String sql = "SELECT spendId, planId, day, day_amount, current_amount, daily_budget FROM spend_amount WHERE planId = ? ORDER BY day ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SpendAmount sa = new SpendAmount();
                    sa.setSpendId(rs.getInt("spendId"));
                    sa.setPlanId(rs.getInt("planId"));
                    sa.setDay(rs.getInt("day"));
                    sa.setDayAmount(rs.getDouble("day_amount"));
                    sa.setCurrentAmount(rs.getDouble("current_amount"));
                    sa.setDailyBudget(rs.getDouble("daily_budget"));
                    list.add(sa);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching spend amounts for planId: " + planId, e);
        }
        return list;
    }

    // Fetch the spendId for a given planId and day
    public int getSpendIdByPlanIdAndDay(int planId, int day) {
        int spendId = -1;
        String sql = "SELECT spendId FROM spend_amount WHERE planId = ? AND day = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, planId);
            ps.setInt(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    spendId = rs.getInt("spendId");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching spendId for planId: " + planId + " and day: " + day, e);
        }
        return spendId;
    }

    // Fetch all spended_day rows for a given spendId
    public List<Map<String, Object>> getSpendedDayBySpendId(int spendId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT dayId, day, spended_amount, note FROM spended_day WHERE spendId = ? ORDER BY dayId ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, spendId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dayId", rs.getInt("dayId"));
                    entry.put("day", rs.getInt("day"));
                    entry.put("spended_amount", rs.getDouble("spended_amount"));
                    entry.put("note", rs.getString("note"));
                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching spended_day for spendId: " + spendId, e);
        }
        return list;
    }

    // Fetch all itemized expenses for an entire plan
    public List<Map<String, Object>> getAllSpendedDaysByPlanId(int planId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sd.dayId, sd.day, sd.spended_amount, sd.note " +
                     "FROM spended_day sd " +
                     "JOIN spend_amount sa ON sd.spendId = sa.spendId " +
                     "WHERE sa.planId = ? ORDER BY sd.day ASC, sd.dayId ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dayId", rs.getInt("dayId"));
                    entry.put("day", rs.getInt("day"));
                    entry.put("spended_amount", rs.getDouble("spended_amount"));
                    entry.put("note", rs.getString("note"));
                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all spended days for planId: " + planId, e);
        }
        return list;
    }
}
