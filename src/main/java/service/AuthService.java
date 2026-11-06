package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authenticates username and password against MySQL database.
     * Supports both salted hash and fallback for bootstrap verification.
     */
    public boolean login(String username, String password) throws SQLException {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !user.isActive()) {
            return false;
        }

        // Verify SHA-256 + Salt
        boolean verified = PasswordUtil.verifyPassword(password.trim(), user.getSalt(), user.getPasswordHash());

        // Bootstrap convenience: if stored password equals raw password (unhashed dev seed)
        if (!verified && password.trim().equals(user.getPasswordHash())) {
            verified = true;
        }

        if (verified) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout() {
        this.currentUser = null;
    }
}
