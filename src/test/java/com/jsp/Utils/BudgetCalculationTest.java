package com.jsp.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import com.jsp.DAO.PlanDAO;

public class BudgetCalculationTest {

    @Test
    public void testDaysInMonthStandardAndLeapYears() {
        // Standard months
        assertEquals(31, PlanDAO.getTotalDaysInMonth("JANUARY", 2025));
        assertEquals(31, PlanDAO.getTotalDaysInMonth("january", 2025));
        assertEquals(30, PlanDAO.getTotalDaysInMonth("APRIL", 2025));
        assertEquals(31, PlanDAO.getTotalDaysInMonth("JULY", 2025));
        assertEquals(30, PlanDAO.getTotalDaysInMonth("SEPTEMBER", 2025));
        assertEquals(31, PlanDAO.getTotalDaysInMonth("DECEMBER", 2025));

        // February in non-leap years
        assertEquals(28, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2023));
        assertEquals(28, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2025));
        assertEquals(28, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2026));

        // February in leap years
        assertEquals(29, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2024));
        assertEquals(29, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2028));
        assertEquals(29, PlanDAO.getTotalDaysInMonth("FEBRUARY", 2000));
        assertEquals(28, PlanDAO.getTotalDaysInMonth("FEBRUARY", 1900));

        // Invalid input fallback
        assertEquals(30, PlanDAO.getTotalDaysInMonth("", 2025));
        assertEquals(30, PlanDAO.getTotalDaysInMonth(null, 2025));
        assertEquals(30, PlanDAO.getTotalDaysInMonth("INVALID_MONTH", 2025));
    }

    @Test
    public void testInitialDailyBudgetAllocation() {
        // ₹3,000 for 30 days => ₹100.00 / day
        BigDecimal budget = new BigDecimal("3000.00");
        int days = 30;
        BigDecimal daily = budget.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("100.00"), daily);

        // ₹5,000 for 31 days => ₹161.29 / day
        budget = new BigDecimal("5000.00");
        days = 31;
        daily = budget.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("161.29"), daily);

        // ₹2,900 for 29 days (Feb leap year) => ₹100.00 / day
        budget = new BigDecimal("2900.00");
        days = 29;
        daily = budget.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("100.00"), daily);
    }

    @Test
    public void testRecalculationAfterSpending() {
        BigDecimal totalPlan = new BigDecimal("3000.00");
        int totalDays = 30;

        // Day 1 user spends ₹60.00
        BigDecimal day1Spend = new BigDecimal("60.00");
        BigDecimal remaining = totalPlan.subtract(day1Spend); // ₹2,940.00
        assertEquals(new BigDecimal("2940.00"), remaining);

        // Remaining days after Day 1 = 30 - 1 = 29 days
        int remainingDays = totalDays - 1;
        BigDecimal nextDailyBudget = remaining.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP);
        // 2940.00 / 29 = 101.3793... => 101.38
        assertEquals(new BigDecimal("101.38"), nextDailyBudget);

        // Day 2 user spends ₹150.00 (over budget for that day)
        BigDecimal day2Spend = new BigDecimal("150.00");
        remaining = remaining.subtract(day2Spend); // 2940 - 150 = ₹2,790.00
        assertEquals(new BigDecimal("2790.00"), remaining);

        // Remaining days after Day 2 = 30 - 2 = 28 days
        remainingDays = totalDays - 2;
        nextDailyBudget = remaining.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP);
        // 2790.00 / 28 = 99.6428... => 99.64
        assertEquals(new BigDecimal("99.64"), nextDailyBudget);
    }

    @Test
    public void testRecalculationAfterSkipDayRollover() {
        BigDecimal totalPlan = new BigDecimal("3000.00");
        int totalDays = 30;

        // Day 1 skipped (₹0 spend)
        BigDecimal day1Spend = BigDecimal.ZERO;
        BigDecimal remaining = totalPlan.subtract(day1Spend);
        assertEquals(new BigDecimal("3000.00"), remaining);

        // Budget for remaining 29 days rolls over and increases
        int remainingDays = totalDays - 1;
        BigDecimal nextDailyBudget = remaining.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.HALF_UP);
        // 3000 / 29 = 103.448... => 103.45
        assertEquals(new BigDecimal("103.45"), nextDailyBudget);
    }

    @Test
    public void testMultipleExpensesOnSameDay() {
        BigDecimal tx1 = new BigDecimal("45.50");
        BigDecimal tx2 = new BigDecimal("120.00");
        BigDecimal tx3 = new BigDecimal("34.50");

        BigDecimal dayTotal = tx1.add(tx2).add(tx3).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("200.00"), dayTotal);
    }

    @Test
    public void testRemainingBalanceValidation() {
        BigDecimal planTotal = new BigDecimal("3000.00");
        BigDecimal totalSpent = new BigDecimal("2500.00");
        BigDecimal remaining = planTotal.subtract(totalSpent);

        // Valid spend within remaining budget
        BigDecimal validSpend = new BigDecimal("400.00");
        assertTrue(validSpend.compareTo(remaining) <= 0);

        // Spend matching remaining budget exactly
        BigDecimal exactSpend = new BigDecimal("500.00");
        assertTrue(exactSpend.compareTo(remaining) <= 0);

        // Excessive spend exceeding remaining budget
        BigDecimal excessSpend = new BigDecimal("500.01");
        assertFalse(excessSpend.compareTo(remaining) <= 0);
    }
}
