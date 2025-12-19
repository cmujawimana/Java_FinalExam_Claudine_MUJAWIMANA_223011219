package com.security.app.ui;

import com.security.app.DBConnection;
import com.security.app.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Dashboard Analytics Panel WITHOUT JFreeChart dependency
 * Shows statistics and data tables instead of charts
 */
public class DashboardAnalyticsPanel extends JPanel {
    private User currentUser;
    private JLabel pendingEventsLabel, activeAlertsLabel, onlineCamerasLabel, avgResponseTimeLabel;
    private JPanel dataPanel;
    private javax.swing.Timer refreshTimer;

    public DashboardAnalyticsPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        // Title
        JLabel titleLabel = new JLabel("📊 Security Dashboard - Real-Time Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 85, 155));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 242, 245));

        // Statistics tiles at top
        JPanel statsPanel = createStatisticsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Data tables in center (instead of charts)
        dataPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dataPanel.setBackground(new Color(240, 242, 245));
        mainPanel.add(dataPanel, BorderLayout.CENTER);

        // Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(240, 242, 245));
        JButton refreshButton = new JButton(" Refresh Dashboard");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setBackground(new Color(23, 162, 184));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        refreshButton.addActionListener(e -> refreshDashboard());
        bottomPanel.add(refreshButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Initial load
        refreshDashboard();

        // Auto-refresh every 30 seconds
        refreshTimer = new Timer(30000, e -> refreshDashboard());
        refreshTimer.start();
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Pending Events
        JPanel pendingTile = createStatTile("📋 PENDING EVENTS", "0", new Color(255, 193, 7));
        pendingEventsLabel = (JLabel) ((JPanel) pendingTile.getComponent(1)).getComponent(0);
        panel.add(pendingTile);

        // Active Alerts
        JPanel alertsTile = createStatTile("🚨 ACTIVE ALERTS", "0", new Color(220, 53, 69));
        activeAlertsLabel = (JLabel) ((JPanel) alertsTile.getComponent(1)).getComponent(0);
        panel.add(alertsTile);

        // Online Cameras
        JPanel camerasTile = createStatTile("📹 ONLINE CAMERAS", "0/0", new Color(40, 167, 69));
        onlineCamerasLabel = (JLabel) ((JPanel) camerasTile.getComponent(1)).getComponent(0);
        panel.add(camerasTile);

        // Avg Response Time
        JPanel responseTile = createStatTile("⏱️ AVG RESPONSE", "0.0h", new Color(23, 162, 184));
        avgResponseTimeLabel = (JLabel) ((JPanel) responseTile.getComponent(1)).getComponent(0);
        panel.add(responseTile);

        return panel;
    }

    private JPanel createStatTile(String title, String value, Color color) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(Color.WHITE);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        valuePanel.setBackground(Color.WHITE);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valuePanel.add(valueLabel);

        tile.add(titleLabel);
        tile.add(valuePanel);

        return tile;
    }

    private void refreshDashboard() {
        SwingUtilities.invokeLater(() -> {
            updateStatistics();
            updateDataTables();
        });
    }

    private void updateStatistics() {
        try (Connection conn = DBConnection.getConnection()) {
            // Pending Events
            String pendingSQL = "SELECT COUNT(*) FROM events WHERE Status = 'PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(pendingSQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pendingEventsLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Active Alerts
            String alertsSQL = "SELECT COUNT(*) FROM alerts WHERE Status IN ('PENDING', 'IN-PROGRESS')";
            try (PreparedStatement ps = conn.prepareStatement(alertsSQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    activeAlertsLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // Online Cameras
            String camerasSQL = "SELECT " +
                    "SUM(CASE WHEN Status = 'ACTIVE' THEN 1 ELSE 0 END) as online, " +
                    "COUNT(*) as total FROM cameras";
            try (PreparedStatement ps = conn.prepareStatement(camerasSQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int online = rs.getInt("online");
                    int total = rs.getInt("total");
                    onlineCamerasLabel.setText(online + "/" + total);
                }
            }

            // Average Response Time
            String responseSQL = "SELECT AVG(TIMESTAMPDIFF(HOUR, EventDate, NOW())) as avg_time " +
                    "FROM events WHERE Status = 'RESOLVED' AND EventDate > DATE_SUB(NOW(), INTERVAL 30 DAY)";
            try (PreparedStatement ps = conn.prepareStatement(responseSQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avgTime = rs.getDouble("avg_time");
                    avgResponseTimeLabel.setText(String.format("%.1fh", avgTime));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateDataTables() {
        dataPanel.removeAll();

        // 1. Events Trend Table
        dataPanel.add(createEventTrendTable());

        // 2. Event Type Distribution Table
        dataPanel.add(createEventTypeTable());

        // 3. Severity Distribution Table
        dataPanel.add(createSeverityTable());

        // 4. Recent Events Table
        dataPanel.add(createRecentEventsTable());

        dataPanel.revalidate();
        dataPanel.repaint();
    }

    private JPanel createEventTrendTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("📈 Events Trend (Last 7 Days)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Date", "Count"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DATE(EventDate) as date, COUNT(*) as count " +
                    "FROM events WHERE EventDate > DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(EventDate) ORDER BY date";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
                while (rs.next()) {
                    Date date = rs.getDate("date");
                    int count = rs.getInt("count");
                    model.addRow(new Object[]{sdf.format(date), count});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEventTypeTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("📊 Event Type Distribution");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Type", "Count", "%"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT EventType, COUNT(*) as count, " +
                    "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM events), 1) as percentage " +
                    "FROM events GROUP BY EventType";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("EventType");
                    int count = rs.getInt("count");
                    double pct = rs.getDouble("percentage");
                    model.addRow(new Object[]{type, count, pct + "%"});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSeverityTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("⚠️ Severity Distribution");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Severity", "Count"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT Severity, COUNT(*) as count FROM events GROUP BY Severity ORDER BY " +
                    "FIELD(Severity, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW')";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String severity = rs.getString("Severity");
                    int count = rs.getInt("count");
                    model.addRow(new Object[]{severity, count});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRecentEventsTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("🕒 Recent Events (Last 10)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Time", "Type", "Severity"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT EventDate, EventType, Severity FROM events " +
                    "ORDER BY EventDate DESC LIMIT 10";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("EventDate");
                    String type = rs.getString("EventType");
                    String severity = rs.getString("Severity");
                    model.addRow(new Object[]{sdf.format(time), type, severity});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}