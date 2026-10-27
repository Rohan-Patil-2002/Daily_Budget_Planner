package com.jsp.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import com.jsp.DAO.UserDAO;

public class SecurityAndValidationTest {

    @Test
    public void testBCryptHashingAndVerification() {
        UserDAO userDAO = new UserDAO();
        String plainPassword = "SecretPassword123!";

        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        assertNotNull(hash);
        assertTrue(UserDAO.isBCryptHash(hash));

        // Correct password matches
        assertTrue(userDAO.verifyPassword(plainPassword, hash));

        // Incorrect password fails
        assertFalse(userDAO.verifyPassword("WrongPassword", hash));
        assertFalse(userDAO.verifyPassword("", hash));
        assertFalse(userDAO.verifyPassword(null, hash));
    }

    @Test
    public void testRejectionOfMalformedHashes() {
        UserDAO userDAO = new UserDAO();
        // Starts with $2a$ but corrupted / short
        String malformedHash = "$2a$10$invalidhashstringthatisnotlongenough";
        assertFalse(UserDAO.isBCryptHash(malformedHash));

        // Corrupted 60-char hash starting with $2a$
        String corrupted60 = "$2a$10$......................................................";
        assertFalse(userDAO.verifyPassword("test", corrupted60));
    }

    @Test
    public void testLegacyPlaintextPasswordHandling() {
        UserDAO userDAO = new UserDAO();
        String legacyPlaintextStored = "rohan123";

        // Must recognize that it is NOT a BCrypt hash
        assertFalse(UserDAO.isBCryptHash(legacyPlaintextStored));

        // Must verify correctly for legacy password migration
        assertTrue(userDAO.verifyPassword("rohan123", legacyPlaintextStored));
        assertFalse(userDAO.verifyPassword("wrong", legacyPlaintextStored));
    }

    @Test
    public void testUsernameValidation() {
        // Valid usernames
        assertTrue(InputValidator.isValidUsername("rohan"));
        assertTrue(InputValidator.isValidUsername("rohan_patil"));
        assertTrue(InputValidator.isValidUsername("user-123"));
        assertTrue(InputValidator.isValidUsername("john.doe"));

        // Invalid usernames
        assertFalse(InputValidator.isValidUsername("ab")); // too short (<3)
        assertFalse(InputValidator.isValidUsername("")); // empty
        assertFalse(InputValidator.isValidUsername(null)); // null
        assertFalse(InputValidator.isValidUsername("user with spaces")); // spaces
        assertFalse(InputValidator.isValidUsername("user<script>")); // dangerous chars
        assertFalse(InputValidator.isValidUsername("user' OR '1'='1")); // SQLi chars
        assertFalse(InputValidator.isValidUsername("a".repeat(51))); // too long (>50)
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(InputValidator.isValidPassword("123456"));
        assertTrue(InputValidator.isValidPassword("complexPassword!@#"));

        assertFalse(InputValidator.isValidPassword("12345")); // too short (<6)
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword(null));
        assertFalse(InputValidator.isValidPassword("a".repeat(101))); // too long (>100)
    }

    @Test
    public void testMonthAndYearValidation() {
        // Months
        assertTrue(InputValidator.isValidMonth("JANUARY"));
        assertTrue(InputValidator.isValidMonth("january"));
        assertTrue(InputValidator.isValidMonth("March"));
        assertFalse(InputValidator.isValidMonth("NotAMonth"));
        assertFalse(InputValidator.isValidMonth(""));
        assertFalse(InputValidator.isValidMonth(null));

        // Years
        assertTrue(InputValidator.isValidYear(2025));
        assertTrue(InputValidator.isValidYear(2020));
        assertTrue(InputValidator.isValidYear(2099));
        assertFalse(InputValidator.isValidYear(2019));
        assertFalse(InputValidator.isValidYear(2100));
        assertFalse(InputValidator.isValidYear(-1));
    }

    @Test
    public void testMonetaryAmountValidation() {
        assertTrue(InputValidator.isValidBudgetAmount(new BigDecimal("100.00")));
        assertTrue(InputValidator.isValidBudgetAmount(new BigDecimal("10000000.00")));

        // Budget amount must be strictly positive
        assertFalse(InputValidator.isValidBudgetAmount(BigDecimal.ZERO));
        assertFalse(InputValidator.isValidBudgetAmount(new BigDecimal("-50.00")));
        assertFalse(InputValidator.isValidBudgetAmount(new BigDecimal("10000000.01"))); // exceeds max
        assertFalse(InputValidator.isValidBudgetAmount(null));

        // Spending amount can be zero (e.g. skip day)
        assertTrue(InputValidator.isValidSpendAmount(BigDecimal.ZERO));
        assertTrue(InputValidator.isValidSpendAmount(new BigDecimal("50.00")));
        assertFalse(InputValidator.isValidSpendAmount(new BigDecimal("-1.00")));
    }

    @Test
    public void testNoteSanitization() {
        assertEquals("General Spend", InputValidator.sanitizeNote(null));
        assertEquals("General Spend", InputValidator.sanitizeNote("   "));
        assertEquals("Groceries at Store", InputValidator.sanitizeNote("  Groceries at Store  "));

        // Truncate if over 255
        String longNote = "A".repeat(300);
        assertEquals(255, InputValidator.sanitizeNote(longNote).length());
    }

    @Test
    public void testHtmlEscaping() {
        String xss1 = "<script>alert('xss')</script>";
        assertEquals("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;&#x2F;script&gt;", SecurityUtil.escapeHtml(xss1));

        String xss2 = "\" onfocus=\"alert(1)\"";
        assertEquals("&quot; onfocus=&quot;alert(1)&quot;", SecurityUtil.escapeHtml(xss2));

        String safe = "Patil Rohan";
        assertEquals("Patil Rohan", SecurityUtil.escapeHtml(safe));
        assertEquals("", SecurityUtil.escapeHtml(null));
    }
}
