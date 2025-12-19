package com.security.app;

import com.security.app.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Load MySQL driver (optional for newer JDBC, but safe)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginFrame lf = new LoginFrame();
            lf.setVisible(true);
        });
    }
}
