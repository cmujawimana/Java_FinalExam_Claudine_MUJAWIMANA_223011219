package com.security.app.ui;

import com.security.app.dao.LogDAO;
import com.security.app.dao.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.security.app.DBConnection;

/**
 * Simple admin user management panel (list, create)
 */
public class UsersPanel extends JPanel {
    private JTable table;
    private UserDAO userDAO = new UserDAO();
    private LogDAO logDAO = new LogDAO();

    public UsersPanel() {
        setLayout(new BorderLayout());
        refreshUsers();

        JLabel titleLabel = new JLabel("Users Management Page");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(25, 85, 155));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);

        JPanel bottom = new JPanel();
        JButton add = new JButton("Add user");
        add.addActionListener(e -> onAddUser());
        JButton delete = new JButton("Delete selected user");
        delete.addActionListener(e -> onDeleteUser());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshUsers());
        bottom.add(add);
        bottom.add(delete);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);

        add.setBackground(new Color(40, 167, 69));
        add.setForeground(Color.white);
        delete.setBackground(new Color(55, 93, 117));
        delete.setForeground(Color.white);
        refresh.setBackground(new Color(255, 193, 7));
        refresh.setForeground(Color.white);
    }

    private void refreshUsers() {
        String sql = "SELECT UserID, Username, Email, FullName, Role, CreatedAt FROM users ORDER BY UserID";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            String[] cols = {"UserID", "Username", "Email", "FullName", "Role", "CreatedAt"};
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
            JOptionPane.showMessageDialog(this, "Failed to load users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddUser() {
        JTextField username = new JTextField();
        JTextField fullname = new JTextField();
        JTextField email = new JTextField();
        JTextField role = new JTextField();
        JPasswordField pwd = new JPasswordField();

        Object[] fields = {
                "Username", username,
                "Full name", fullname,
                "Email", email,
                "Role (ADMIN/OPERATOR/VIEWER)", role,
                "Password (plain for now)", pwd
        };
        int ok = JOptionPane.showConfirmDialog(this, fields, "Create user", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                boolean created = userDAO.createUser(username.getText().trim(),
                        new String(pwd.getPassword()),
                        email.getText().trim(),
                        fullname.getText().trim(),
                        role.getText().trim().toUpperCase());
                if (created) {
                    JOptionPane.showMessageDialog(this, "User created");
                    refreshUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not create user", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteUser() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first");
            return;
        }
        int userId = (int) table.getValueAt(r, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user id " + userId + " ? This is irreversible.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE UserID = ?")) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    JOptionPane.showMessageDialog(this, "User deleted");
                    refreshUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete user", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void main(String[] args){
        new UsersPanel();
    }
}
