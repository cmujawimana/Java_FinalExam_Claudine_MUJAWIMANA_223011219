package com.security.app.ui;

import com.security.app.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Read-only audit logs panel.
 */
public class LogsPanel extends JPanel {
    private JTable table;
    JLabel titleLabel = new JLabel("Audit Logs Panel");

    public LogsPanel() {
        setLayout(new BorderLayout());
        refreshLogs();
        JButton refresh = new JButton("Refresh");
        refresh.setForeground(Color.WHITE);
        refresh.setBackground(new Color(23, 162, 184));
        refresh.addActionListener(e -> refreshLogs());
        add(refresh, BorderLayout.SOUTH);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(25, 85, 155));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
    }

    private void refreshLogs() {
        String sql = "SELECT LogID, UserID, Action, Description, IPAddress, LogDate FROM logs ORDER BY LogDate DESC LIMIT 500";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            String[] cols = {"LogID", "UserID", "Action", "Description", "IPAddress", "LogDate"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            while (rs.next()) {
                Object[] row = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
            if (table == null) {
                table = new JTable(model);
                table.getTableHeader().setForeground(Color.WHITE);
                table.getTableHeader().setBackground(new Color(23, 162, 184));
                table.setRowHeight(30);
                add(new JScrollPane(table), BorderLayout.CENTER);
            } else {
                table.setModel(model);
            }
            revalidate();
            repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not load logs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
