package com.jsp.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.jsp.Models.User;
import com.jsp.Utils.DBConnection;
import com.jsp.Utils.InputValidator;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Checks if a string has the format of a standard BCrypt hash.
     */
    public static boolean isBCryptHash(String password) {
        if (password == null || password.length() != 60) {
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    /**
     * Find user by username.
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT userId, username, password FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding user by username: " + username, e);
        }
        return null;
    }

    /**
     * Check if a username already exists.
     */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Verify password supporting both BCrypt hashes and legacy plaintext verification.
     * Note: Does NOT fall back to plaintext if a BCrypt hash is malformed.
     */
    public boolean verifyPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        if (isBCryptHash(storedPassword)) {
            try {
                return BCrypt.checkpw(plainPassword, storedPassword);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error validating BCrypt hash.", e);
                return false;
            }
        }
        // Legacy plaintext check (for pre-existing un-migrated accounts)
        return storedPassword.equals(plainPassword);
    }

    /**
     * Verifies credentials and immediately upgrades legacy plaintext passwords to BCrypt.
     */
    public boolean verifyAndUpgradePassword(User user, String plainPassword) {
        if (user == null || plainPassword == null) {
            return false;
        }
        String storedPassword = user.getPassword();
        boolean valid = verifyPassword(plainPassword, storedPassword);
        if (valid) {
            // If stored password was legacy plaintext, automatically upgrade to BCrypt
            if (!isBCryptHash(storedPassword)) {
                try {
                    String newHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
                    if (updatePassword(user.getUserId(), newHash)) {
                        user.setPassword(newHash);
                        LOGGER.info("Successfully migrated legacy plaintext password to BCrypt for user: " + user.getUsername());
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to upgrade legacy password for user: " + user.getUsername(), e);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Updates user's password with a new BCrypt hash.
     */
    public boolean updatePassword(int userId, String newHashedPassword) {
        if (userId <= 0 || newHashedPassword == null || !isBCryptHash(newHashedPassword)) {
            return false;
        }
        String sql = "UPDATE users SET password = ? WHERE userId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update password for userId: " + userId, e);
            return false;
        }
    }

    /**
     * Save new user to database (registration) with BCrypt hashed password.
     */
    public boolean saveUser(User user) {
        if (user == null || !InputValidator.isValidUsername(user.getUsername()) || !InputValidator.isValidPassword(user.getPassword())) {
            return false;
        }

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername().trim());
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10));
            stmt.setString(2, hashedPassword);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save user: " + user.getUsername(), e);
            return false;
        }
    }
}
