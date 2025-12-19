package com.security.app.ui;

import com.security.app.dao.EventDAO;
import com.security.app.dao.LogDAO;
import com.security.app.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/**
 * Events Management Panel
 * Displays all events and allows status updates.
 */
public class EventsPanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();
    private final LogDAO logDAO = new LogDAO();
    private final JTable eventsTable = new JTable();
    private final User currentUser;

    private final JButton refreshButton = new JButton("🔄 Refresh");
    private final JButton markResolvedButton = new JButton("✅ Mark as RESOLVED");
    private final JButton markInProgressButton = new JButton("🕒 Mark as IN-PROGRESS");
    private final JButton markPendingButton = new JButton("Mark as PENDING");

    public EventsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Event Management Panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(25, 85, 155));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);

        // Table style
        eventsTable.setRowHeight(28);
        eventsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        eventsTable.getTableHeader().setBackground(new Color(30, 144, 255));
        eventsTable.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(eventsTable), BorderLayout.CENTER);

        // Buttons panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        styleButton(refreshButton, new Color(23, 162, 184));
        styleButton(markResolvedButton, new Color(40, 167, 69));
        styleButton(markInProgressButton, new Color(255, 193, 7));
        styleButton(markPendingButton, new Color(55, 93, 117));

        actionsPanel.add(refreshButton);
        actionsPanel.add(markInProgressButton);
        actionsPanel.add(markResolvedButton);
        actionsPanel.add(markPendingButton);
        add(actionsPanel, BorderLayout.SOUTH);

        // Actions
        refreshButton.addActionListener(e -> loadEvents());
        markResolvedButton.addActionListener(this::onMarkResolved);
        markInProgressButton.addActionListener(this::onMarkInProgress);
        markPendingButton.addActionListener(this:: onPending);

        // Initial load
        loadEvents();
    }

    /** Load event data into the table */
    private void loadEvents() {
        try {
            DefaultTableModel model = eventDAO.getEventsTableModel();
            eventsTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load events: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Handle marking event as RESOLVED */
    private void onMarkResolved(ActionEvent e) {
        updateEventStatus("RESOLVED");
    }

    private void  onPending(ActionEvent e) {
        updateEventStatus("PENDING");
    }

    /** Handle marking event as IN-PROGRESS */
    private void onMarkInProgress(ActionEvent e) {
        updateEventStatus("IN-PROGRESS");
    }

    /** Common logic for updating event status */
    private void updateEventStatus(String newStatus) {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an event first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to mark this event as " + newStatus + "?",
                "Confirm Update", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = eventDAO.updateStatus(eventId, newStatus);
        if (success) {
            if (currentUser != null) {
                logDAO.insertLog(currentUser.getUserId(),
                        "EVENT_UPDATE",
                        "Event " + eventId + " updated to " + newStatus,
                        "127.0.0.1");
            }
            JOptionPane.showMessageDialog(this,
                    "✅ Event updated to " + newStatus,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadEvents();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update event. Please check the database or connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Button styling helper */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    // For testing standalone
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Security Assistant | Events Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new EventsPanel(null)); // Replace null with actual User
            frame.setVisible(true);
        });
    }
}
