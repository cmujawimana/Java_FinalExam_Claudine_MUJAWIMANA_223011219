package com.security.app.ui;

import com.security.app.dao.LogDAO;
import com.security.app.dao.UserDAO;
import com.security.app.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main dashboard with role-based tabs including new Dashboard Analytics and Live Camera Viewer
 */
public class DashboardFrame extends JFrame {
    private User currentUser;
    private UserDAO userDAO = new UserDAO();
    private LogDAO logDAO = new LogDAO();
    private DashboardAnalyticsPanel dashboardPanel;
    private LiveCameraViewerPanel cameraViewerPanel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("Security Assistant - Dashboard (" + user.getUsername() + " : " + user.getRole() + ")");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // 🆕 Dashboard Analytics Tab (Available to all roles)
        dashboardPanel = new DashboardAnalyticsPanel(currentUser);
        tabs.addTab("📊 Dashboard", dashboardPanel);

        // Events tab: available to all roles
        EventsPanel eventsPanel = new EventsPanel(currentUser);
        tabs.addTab("📋 Events", eventsPanel);

        // 🆕 Live Camera Viewer Tab (Available to all roles)
        cameraViewerPanel = new LiveCameraViewerPanel();
        tabs.addTab("📹 Live Cameras", cameraViewerPanel);

        // Users tab: ADMIN only
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            UsersPanel usersPanel = new UsersPanel();
            tabs.addTab("👥 Users", usersPanel);
        }

        // Logs / Audit view: ADMIN / VIEWER (auditor)
        if ("ADMIN".equalsIgnoreCase(user.getRole()) || "VIEWER".equalsIgnoreCase(user.getRole())) {
            LogsPanel logsPanel = new LogsPanel();
            tabs.addTab("📜 Audit Logs", logsPanel);
        }

        // Add SecurityDataManager as new navigation tab (ADMIN only)
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            try {
                com.security.app.dao.SecurityDataManager dataManagerPanel = new com.security.app.dao.SecurityDataManager();
                tabs.addTab("🚨 Alerts", dataManagerPanel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load SecurityDataManager: " + e.getMessage());
            }
        }

        // Camera Management Tab: ADMIN only
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            try {
                CameraManagerPanel cameraPanel = new CameraManagerPanel();
                tabs.addTab("📸 Camera Config", cameraPanel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load CameraManagerPanel: " + e.getMessage());
            }
        }

        add(tabs, BorderLayout.CENTER);

        // Bottom status bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(248, 249, 250));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JLabel userLabel = new JLabel("Signed in as: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(new Color(100, 100, 100));

        JButton logout = new JButton("Logout");
        logout.setBackground(new Color(220, 53, 69));
        logout.setForeground(Color.WHITE);
        logout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        logout.addActionListener(this::onLogout);

        bottom.add(userLabel);
        bottom.add(Box.createHorizontalStrut(15));
        bottom.add(logout);
        add(bottom, BorderLayout.SOUTH);

        // Cleanup on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void onLogout(ActionEvent ev) {
        logDAO.insertLog(currentUser.getUserId(), "LOGOUT", "User logged out of system", "127.0.0.1");
        int logout = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (logout == JOptionPane.YES_OPTION) {
            cleanup();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame lf = new LoginFrame();
                lf.setVisible(true);
            });
        }
    }

    /**
     * Cleanup resources before closing
     */
    private void cleanup() {
        if (dashboardPanel != null) {
            dashboardPanel.cleanup();
        }
        if (cameraViewerPanel != null) {
            cameraViewerPanel.cleanup();
        }
    }
}