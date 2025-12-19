package com.security.app.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Panel to manage Cameras in the Security Assistant System.
 * Works with table: cameras
 */
public class CameraManagerPanel extends JPanel implements ActionListener {

    private static final String URL = "jdbc:mysql://localhost:3306/securityassistantsystem?serverTimezone=UTC";
    private static final String USER = "root";  // change if needed
    private static final String PASSWORD = "";  // change if needed

    private Connection conn;
    private JTable table;
    private DefaultTableModel model;

    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;
    private JTextField txtName, txtLocation, txtIP, txtAccessID;
    private JComboBox<String> cbStatus;

    public CameraManagerPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            return;
        }

        // ===== Top title =====
        JLabel title = new JLabel("📸 Camera Management", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setOpaque(true);
        title.setBackground(new Color(30, 80, 160));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(1000, 40));
        add(title, BorderLayout.NORTH);

        // ===== Table setup =====
        model = new DefaultTableModel(
                new Object[]{"CameraID", "CameraName", "Location", "Status", "IPAddress", "AccessControlID", "CreatedAt"}, 0
        );
        table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(Color.BLACK);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Form fields =====
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Camera Information"));
        txtName = new JTextField();
        txtLocation = new JTextField();
        txtIP = new JTextField();
        txtAccessID = new JTextField();
        cbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "MAINTENANCE"});

        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(txtLocation);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(cbStatus);
        formPanel.add(new JLabel("IP Address:"));
        formPanel.add(txtIP);
        formPanel.add(new JLabel("Access Control ID:"));
        formPanel.add(txtAccessID);

        add(formPanel, BorderLayout.SOUTH);

        // ===== Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnAdd = createButton("➕ Add", new Color(0, 150, 0));
        btnUpdate = createButton("✏️ Update", new Color(255, 170, 0));
        btnDelete = createButton("🗑 Delete", new Color(200, 50, 50));
        btnRefresh = createButton("🔁 Refresh", new Color(70, 130, 180));

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.NORTH);

        // ===== Events =====
        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnRefresh.addActionListener(this);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtName.setText(model.getValueAt(row, 1).toString());
                    txtLocation.setText(model.getValueAt(row, 2).toString());
                    cbStatus.setSelectedItem(model.getValueAt(row, 3).toString());
                    txtIP.setText(model.getValueAt(row, 4).toString());
                    txtAccessID.setText(model.getValueAt(row, 5).toString());
                }
            }
        });

        // ===== Load data initially =====
        loadCameras();
    }

    private JButton createButton(String text, Color c) {
        JButton b = new JButton(text);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return b;
    }

    private void loadCameras() {
        model.setRowCount(0);
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM cameras ORDER BY CameraID ASC")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("CameraID"),
                        rs.getString("CameraName"),
                        rs.getString("Location"),
                        rs.getString("Status"),
                        rs.getString("IPAddress"),
                        rs.getInt("AccessControlID"),
                        rs.getTimestamp("CreatedAt")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading cameras: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnAdd) {
            addCamera();
        } else if (src == btnUpdate) {
            updateCamera();
        } else if (src == btnDelete) {
            deleteCamera();
        } else if (src == btnRefresh) {
            loadCameras();
        }
    }

    private void addCamera() {
        String sql = "INSERT INTO cameras (CameraName, Location, Status, IPAddress, AccessControlID) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtName.getText());
            ps.setString(2, txtLocation.getText());
            ps.setString(3, cbStatus.getSelectedItem().toString());
            ps.setString(4, txtIP.getText());
            ps.setInt(5, Integer.parseInt(txtAccessID.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Camera added successfully!");
            loadCameras();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding camera: " + ex.getMessage());
        }
    }

    private void updateCamera() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a camera first!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        String sql = "UPDATE cameras SET CameraName=?, Location=?, Status=?, IPAddress=?, AccessControlID=? WHERE CameraID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtName.getText());
            ps.setString(2, txtLocation.getText());
            ps.setString(3, cbStatus.getSelectedItem().toString());
            ps.setString(4, txtIP.getText());
            ps.setInt(5, Integer.parseInt(txtAccessID.getText()));
            ps.setInt(6, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Camera updated successfully!");
            loadCameras();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating camera: " + ex.getMessage());
        }
    }

    private void deleteCamera() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a camera to delete!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this camera?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cameras WHERE CameraID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "🗑 Camera deleted!");
                loadCameras();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting camera: " + ex.getMessage());
            }
        }
    }
}
