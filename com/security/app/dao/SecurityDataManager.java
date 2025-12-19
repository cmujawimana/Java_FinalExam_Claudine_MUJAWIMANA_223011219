package com.security.app.dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * GUI version of Security Data Manager showing alerts table (as JPanel).
 */
public class SecurityDataManager extends JPanel implements ActionListener {

    private static final String URL = "jdbc:mysql://localhost:3306/securityassistantsystem?serverTimezone=UTC";
    private static final String USER = "root"; // change if needed
    private static final String PASSWORD = ""; // change if needed

    private Connection conn;
    private JTable alertsTable;
    private DefaultTableModel tableModel;

    private JButton btnRefresh, btnMarkProgress, btnMarkResolved, btnMarkPending;

    public SecurityDataManager() {
        setLayout(new BorderLayout());
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }

        // ======== Top Title ========
        JLabel title = new JLabel("Alert Management Panel", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setOpaque(true);
        title.setBackground(new Color(25, 60, 130));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(1000, 40));
        add(title, BorderLayout.NORTH);

        // ======== Table ========
        String[] columns = {
                "AlertID", "ReferenceID", "EventID", "Description",
                "AlertDate", "Status", "Priority", "Remarks"
        };
        tableModel = new DefaultTableModel(columns, 0);
        alertsTable = new JTable(tableModel);
        alertsTable.setRowHeight(25);
        alertsTable.setSelectionBackground(new Color(220, 230, 255));
        alertsTable.setSelectionForeground(Color.BLACK);
        JScrollPane scroll = new JScrollPane(alertsTable);
        add(scroll, BorderLayout.CENTER);

        // ======== Buttons Panel ========
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnRefresh = createButton("🔁 Refresh", new Color(100, 160, 255));
        btnMarkProgress = createButton("🟨 Mark as IN-PROGRESS", new Color(255, 180, 0));
        btnMarkResolved = createButton("🟩 Mark as RESOLVED", new Color(0, 170, 70));
        btnMarkPending = createButton("🟦 Mark as PENDING", new Color(50, 70, 130));

        buttonsPanel.add(btnRefresh);
        buttonsPanel.add(btnMarkProgress);
        buttonsPanel.add(btnMarkResolved);
        buttonsPanel.add(btnMarkPending);

        add(buttonsPanel, BorderLayout.SOUTH);

        // ======== Event Listeners ========
        btnRefresh.addActionListener(this);
        btnMarkProgress.addActionListener(this);
        btnMarkResolved.addActionListener(this);
        btnMarkPending.addActionListener(this);

        // ======== Load Data Initially ========
        loadAlerts();
    }

    private JButton createButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return b;
    }

    // ======== Load Alerts into Table ========
    private void loadAlerts() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM alerts ORDER BY AlertID ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("AlertID"),
                        rs.getString("ReferenceID"),
                        rs.getInt("EventID"),
                        rs.getString("Description"),
                        rs.getTimestamp("AlertDate"),
                        rs.getString("Status"),
                        rs.getString("Priority"),
                        rs.getString("Remarks")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading alerts: " + e.getMessage());
        }
    }

    // ======== Update Alert Status ========
    private void updateAlertStatus(String newStatus) {
        int row = alertsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an alert first.");
            return;
        }

        int alertId = (int) tableModel.getValueAt(row, 0);

        // ✅ Normalize status (avoid invalid DB insert)
        newStatus = newStatus.trim().toUpperCase();

        // ✅ Optional: restrict to allowed statuses
        if (!newStatus.equals("PENDING") &&
                !newStatus.equals("IN_PROGRESS") &&
                !newStatus.equals("RESOLVED")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid status value. Allowed: PENDING, IN_PROGRESS, RESOLVED.");
            return;
        }

        String sql = "UPDATE alerts SET Status = ? WHERE AlertID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, alertId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Alert #" + alertId + " updated to " + newStatus);
                loadAlerts(); // refresh table
            } else {
                JOptionPane.showMessageDialog(this,
                        "No record updated. Please try again.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnRefresh) {
            loadAlerts();
        } else if (src == btnMarkProgress) {
            updateAlertStatus("IN PROGRESS");
        } else if (src == btnMarkResolved) {
            updateAlertStatus("RESOLVED");
        } else if (src == btnMarkPending) {
            updateAlertStatus("PENDING");
        }
    }
}
