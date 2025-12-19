package com.security.app.ui;

import com.security.app.dao.EventDAO;
import com.security.app.dao.LogDAO;
import com.security.app.dao.UserDAO;
import com.security.app.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/**
 * Login window. On success, opens dashboard for the user's role.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserDAO userDao = new UserDAO();
    private final LogDAO logDao = new LogDAO();

    public LoginFrame() {
        setTitle("Security Assistant - Login");
        setSize(400, 230);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Username
        c.gridx = 0; c.gridy = 0;
        p.add(new JLabel("Username:"), c);
        c.gridx = 1;
        usernameField = new JTextField(16);
        p.add(usernameField, c);

        // Password
        c.gridx = 0; c.gridy = 1;
        p.add(new JLabel("Password:"), c);
        c.gridx = 1;
        passwordField = new JPasswordField(16);
        p.add(passwordField, c);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        JButton btnLogin = new JButton("Login");
        JButton btnClear = new JButton("Clear");

        btnLogin.setBackground(new Color(4, 127, 169));
        btnLogin.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(2, 153, 99));
        btnClear.setForeground(Color.WHITE);



        btnLogin.setFocusPainted(false);
        btnClear.setFocusPainted(false);

        // Button actions
        btnLogin.addActionListener(this::onLogin);
        btnClear.addActionListener(this::onClear);

        buttonsPanel.add(btnLogin);
        buttonsPanel.add(btnClear);

        // Add buttons under fields
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        p.add(buttonsPanel, c);

        add(p);
        setVisible(true);
    }

    /** Login button logic */
    private void onLogin(ActionEvent ev) {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        try {
            boolean ok = userDao.validateCredentials(u, p);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userDao.getByUsername(u);
            logDao.insertLog(user.getUserId(), "LOGIN",
                    "User logged into system successfully", "127.0.0.1");

            // Open dashboard
            SwingUtilities.invokeLater(() -> {
                DashboardFrame df = new DashboardFrame(user);
                df.setVisible(true);
            });
            this.dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Clear button logic */
    private void onClear(ActionEvent ev) {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
