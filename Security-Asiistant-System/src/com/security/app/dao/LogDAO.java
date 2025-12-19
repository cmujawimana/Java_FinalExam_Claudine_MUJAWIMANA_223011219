package com.security.app.dao;

import com.security.app.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Writes audit records into logs table.
 */
public class LogDAO {
    public void insertLog(int userId, String action, String description, String ipAddress) {
        String sql = "INSERT INTO logs (UserID, Action, Description, IPAddress, LogDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.setString(4, ipAddress);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Do not throw—logging should not break UI. Consider central logging for production.
        }
    }
}
