package com.security.app.dao;

import com.security.app.DBConnection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Minimal event DAO with methods to populate a table model.
 */
public class EventDAO extends JFrame implements ActionListener {
    public DefaultTableModel getEventsTableModel() throws SQLException {
        String sql = "SELECT EventID, ReferenceID, CameraID, Description, EventType, EventDate, Status, Severity, Remarks FROM events ORDER BY EventDate DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String[] cols = {"EventID", "ReferenceID", "CameraID", "Description", "EventType", "EventDate", "Status", "Severity", "Remarks"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            while (rs.next()) {
                Object[] row = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
            return model;
        }
    }

    public EventDAO(){
        setTitle("Events");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }


    public boolean updateStatus(int eventId, String newStatus) {
        String sql = "UPDATE events SET Status = ? WHERE EventID = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, eventId);
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

    }
    public static void main(String[] args) {
        EventDAO dao = new EventDAO();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
