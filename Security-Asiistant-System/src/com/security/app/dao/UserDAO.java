package com.security.app.dao;

import com.security.app.DBConnection;
import com.security.app.models.User;
import com.security.app.util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Basic user DAO: fetch user by username, authenticate, create user (simple).
 */
public class UserDAO {

    public User getByUsername(String username) throws SQLException {
        String sql = "SELECT UserID, Username, PasswordHash, Email, FullName, Role FROM users WHERE Username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Email"),
                        rs.getString("FullName"),
                        rs.getString("Role")
                );
            }
            return null;
        }
    }

    /**
     * Validate user credentials. This supports plain text passwords (as in your DB)
     * and can be extended to support hashed passwords easily.
     */
    public boolean validateCredentials(String username, String password) throws SQLException {
        User u = getByUsername(username);
        if (u == null) return false;

        String stored = u.getPasswordHash();
        // Support both plain-text and hashed workflow:
        if (stored == null) return false;
        // 1) If stored equals raw password (your current DB sample), accept
        if (stored.equals(password)) return true;

        // 2) If stored is SHA-256 hex, verify:
        String sha = HashUtil.sha256Hex(password);
        if (sha.equalsIgnoreCase(stored)) return true;

        // Add any other checks (bcrypt) if you migrate later.
        return false;
    }

    public boolean createUser(String username, String passwordPlain, String email, String fullName, String role) throws SQLException {
        String sql = "INSERT INTO users (Username, PasswordHash, Email, FullName, Role, CreatedAt) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordPlain); // consider hashing before storing in production
            ps.setString(3, email);
            ps.setString(4, fullName);
            ps.setString(5, role);
            int rows = ps.executeUpdate();
            return rows == 1;
        }
    }
}
