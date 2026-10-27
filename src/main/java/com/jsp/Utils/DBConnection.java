package com.jsp.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/budget_planner?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Rohan@123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found on classpath.", e);
        }
    }

    /**
     * Resolves configuration property: checks System property first, then Environment variable, then default.
     */
    private static String getSetting(String propKey, String envKey, String defaultValue) {
        String sysVal = System.getProperty(propKey);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal.trim();
        }
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        String url = getSetting("db.url", "DB_URL", DEFAULT_URL);
        String user = getSetting("db.user", "DB_USER", DEFAULT_USER);
        String pass = getSetting("db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to MySQL database at URL: " + url + " as user: " + user, e);
            throw e;
        }
    }
}
