package com.jsp.Utils;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CSRFTokenUtil {
    private static final String CSRF_SESSION_ATTR = "csrfToken";
    private static final String CSRF_PARAM_NAME = "csrfToken";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a new secure CSRF token, stores it in the session, and returns it.
     */
    public static String generateToken(HttpSession session) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(CSRF_SESSION_ATTR, token);
        return token;
    }

    /**
     * Gets the existing CSRF token from session, or generates one if missing.
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object token = session.getAttribute(CSRF_SESSION_ATTR);
        if (token instanceof String && !((String) token).trim().isEmpty()) {
            return (String) token;
        }
        return generateToken(session);
    }

    /**
     * Validates that the request parameter 'csrfToken' matches the token stored in session.
     */
    public static boolean isValid(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            return false;
        }
        return sessionToken.equals(requestToken.trim());
    }
}
