package com.security.app.ui;

import com.security.app.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Live Camera Viewer Panel with simulated feeds and recording capabilities
 */
public class LiveCameraViewerPanel extends JPanel {
    private JPanel cameraGridPanel;
    private JPanel controlPanel;
    private List<CameraFeed> cameraFeeds = new ArrayList<>();
    private CameraFeed selectedCamera = null;
    private JPanel fullScreenPanel = null;
    private javax.swing.Timer simulationTimer;

    public LiveCameraViewerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        // Title
        JLabel titleLabel = new JLabel("📹 Live Camera Feeds");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 85, 155));
        add(titleLabel, BorderLayout.NORTH);

        // Camera grid
        cameraGridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        cameraGridPanel.setBackground(new Color(240, 242, 245));
        JScrollPane scrollPane = new JScrollPane(cameraGridPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Control panel
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Load cameras and start simulation
        loadCameras();
        startSimulation();
    }

    /**
     * Create control panel with buttons
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(240, 242, 245));

        JButton refreshBtn = createButton("🔄 Refresh", new Color(23, 162, 184));
        refreshBtn.addActionListener(e -> refreshCameras());

        JButton recordBtn = createButton("⏺️ Record Selected", new Color(220, 53, 69));
        recordBtn.addActionListener(e -> recordSelectedCamera());

        JButton snapshotBtn = createButton("📸 Snapshot", new Color(40, 167, 69));
        snapshotBtn.addActionListener(e -> takeSnapshot());

        JButton fullscreenBtn = createButton("⛶ Fullscreen", new Color(108, 117, 125));
        fullscreenBtn.addActionListener(e -> toggleFullscreen());

        JButton viewRecordingsBtn = createButton("📂 View Recordings", new Color(255, 193, 7));
        viewRecordingsBtn.addActionListener(e -> viewRecordings());

        panel.add(refreshBtn);
        panel.add(recordBtn);
        panel.add(snapshotBtn);
        panel.add(fullscreenBtn);
        panel.add(viewRecordingsBtn);

        return panel;
    }

    /**
     * Create styled button
     */
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Load cameras from database
     */
    private void loadCameras() {
        cameraGridPanel.removeAll();
        cameraFeeds.clear();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT CameraID, CameraName, Location, Status, IPAddress FROM cameras ORDER BY CameraID";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int cameraId = rs.getInt("CameraID");
                    String name = rs.getString("CameraName");
                    String location = rs.getString("Location");
                    String status = rs.getString("Status");
                    String ip = rs.getString("IPAddress");

                    CameraFeed feed = new CameraFeed(cameraId, name, location, status, ip);
                    cameraFeeds.add(feed);
                    cameraGridPanel.add(feed.getPanel());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load cameras: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        cameraGridPanel.revalidate();
        cameraGridPanel.repaint();
    }

    /**
     * Start camera simulation
     */
    private void startSimulation() {
        simulationTimer = new javax.swing.Timer(100, e -> {
            for (CameraFeed feed : cameraFeeds) {
                if (feed.isActive()) {
                    feed.updateFrame();
                }
            }
        });
        simulationTimer.start();
    }

    /**
     * Refresh camera list
     */
    private void refreshCameras() {
        loadCameras();
        JOptionPane.showMessageDialog(this,
                "Camera feeds refreshed!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Record selected camera
     */
    private void recordSelectedCamera() {
        if (selectedCamera == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a camera first by clicking on it.",
                    "No Camera Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedCamera.isRecording()) {
            selectedCamera.stopRecording();
            JOptionPane.showMessageDialog(this,
                    "Recording stopped for " + selectedCamera.getName(),
                    "Recording Stopped", JOptionPane.INFORMATION_MESSAGE);
        } else {
            selectedCamera.startRecording();
            JOptionPane.showMessageDialog(this,
                    "Recording started for " + selectedCamera.getName() +
                            "\nRecording will be saved to: recordings/" + selectedCamera.getName() + ".avi",
                    "Recording Started", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Take snapshot
     */
    private void takeSnapshot() {
        if (selectedCamera == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a camera first.",
                    "No Camera Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filename = selectedCamera.takeSnapshot();
        JOptionPane.showMessageDialog(this,
                "Snapshot saved to: " + filename,
                "Snapshot Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Toggle fullscreen mode
     */
    private void toggleFullscreen() {
        if (selectedCamera == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a camera first.",
                    "No Camera Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fullScreenPanel != null) {
            // Exit fullscreen
            Window window = SwingUtilities.getWindowAncestor(fullScreenPanel);
            if (window != null) {
                window.dispose();
            }
            fullScreenPanel = null;
        } else {
            // Enter fullscreen
            JFrame fullscreenFrame = new JFrame("Fullscreen - " + selectedCamera.getName());
            fullscreenFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fullscreenFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            fullscreenFrame.setUndecorated(true);

            fullScreenPanel = new JPanel(new BorderLayout());
            fullScreenPanel.setBackground(Color.BLACK);

            // Large camera view
            JLabel largeView = new JLabel();
            largeView.setHorizontalAlignment(SwingConstants.CENTER);
            fullScreenPanel.add(largeView, BorderLayout.CENTER);

            // Exit button
            JButton exitBtn = new JButton("Exit Fullscreen (ESC)");
            exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            exitBtn.addActionListener(e -> fullscreenFrame.dispose());
            fullScreenPanel.add(exitBtn, BorderLayout.SOUTH);

            // ESC key to exit
            fullScreenPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
            fullScreenPanel.getActionMap().put("exit", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fullscreenFrame.dispose();
                }
            });

            // Update large view with camera frames
            javax.swing.Timer updateTimer = new javax.swing.Timer(100, e -> {
                BufferedImage frame = selectedCamera.getCurrentFrame();
                if (frame != null) {
                    int maxWidth = fullscreenFrame.getWidth() - 100;
                    int maxHeight = fullscreenFrame.getHeight() - 100;
                    Image scaled = frame.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
                    largeView.setIcon(new ImageIcon(scaled));
                }
            });
            updateTimer.start();

            fullscreenFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    updateTimer.stop();
                    fullScreenPanel = null;
                }
            });

            fullscreenFrame.add(fullScreenPanel);
            fullscreenFrame.setVisible(true);
        }
    }

    /**
     * View recordings
     */
    private void viewRecordings() {
        File recordingsDir = new File("recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        File[] files = recordingsDir.listFiles((dir, name) ->
                name.endsWith(".avi") || name.endsWith(".mp4") || name.endsWith(".png"));

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No recordings found in the recordings folder.",
                    "No Recordings", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFrame recordingsFrame = new JFrame("Recordings Library");
        recordingsFrame.setSize(600, 400);
        recordingsFrame.setLocationRelativeTo(this);

        String[] columnNames = {"Filename", "Size (KB)", "Date Modified"};
        Object[][] data = new Object[files.length][3];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < files.length; i++) {
            data[i][0] = files[i].getName();
            data[i][1] = files[i].length() / 1024;
            data[i][2] = sdf.format(new java.util.Date(files[i].lastModified()));
        }

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton openFolderBtn = new JButton("Open Folder");
        openFolderBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(recordingsDir);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(recordingsFrame,
                        "Failed to open folder: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(openFolderBtn);

        recordingsFrame.add(scrollPane, BorderLayout.CENTER);
        recordingsFrame.add(buttonPanel, BorderLayout.SOUTH);
        recordingsFrame.setVisible(true);
    }

    /**
     * Cleanup when panel is closed
     */
    public void cleanup() {
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        for (CameraFeed feed : cameraFeeds) {
            feed.stopRecording();
        }
    }

    /**
     * Inner class representing a single camera feed
     */
    private class CameraFeed {
        private int cameraId;
        private String name;
        private String location;
        private String status;
        private String ipAddress;
        private JPanel panel;
        private JLabel videoLabel;
        private JLabel statusLabel;
        private BufferedImage currentFrame;
        private boolean recording = false;
        private long frameCount = 0;
        private Random random = new Random();

        public CameraFeed(int cameraId, String name, String location, String status, String ipAddress) {
            this.cameraId = cameraId;
            this.name = name;
            this.location = location;
            this.status = status;
            this.ipAddress = ipAddress;
            createPanel();
            generateInitialFrame();
        }

        private void createPanel() {
            panel = new JPanel(new BorderLayout(5, 5));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            // Camera info header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(Color.WHITE);

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            headerPanel.add(nameLabel, BorderLayout.WEST);

            statusLabel = new JLabel(isActive() ? "🟢 LIVE" : "🔴 OFFLINE");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            statusLabel.setForeground(isActive() ? new Color(40, 167, 69) : new Color(220, 53, 69));
            headerPanel.add(statusLabel, BorderLayout.EAST);

            panel.add(headerPanel, BorderLayout.NORTH);

            // Video display
            videoLabel = new JLabel();
            videoLabel.setPreferredSize(new Dimension(300, 200));
            videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            videoLabel.setBackground(Color.BLACK);
            videoLabel.setOpaque(true);
            videoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panel.add(videoLabel, BorderLayout.CENTER);

            // Location info
            JLabel locationLabel = new JLabel("📍 " + location);
            locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            locationLabel.setForeground(Color.GRAY);
            panel.add(locationLabel, BorderLayout.SOUTH);

            // Click to select
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectCamera();
                }
            });
        }

        private void selectCamera() {
            if (selectedCamera != null) {
                selectedCamera.panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
            selectedCamera = this;
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 3),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        private void generateInitialFrame() {
            currentFrame = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = currentFrame.createGraphics();

            // Background
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, 320, 240);

            // Camera info
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString(name, 10, 20);
            g.drawString(location, 10, 35);
            g.drawString(ipAddress, 10, 50);

            // Timestamp
            g.drawString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()), 10, 230);

            g.dispose();
            updateVideoLabel();
        }

        public void updateFrame() {
            if (!isActive()) return;

            frameCount++;
            Graphics2D g = currentFrame.createGraphics();

            // Simulate camera movement/changes
            // Add random "motion" pixels
            for (int i = 0; i < 50; i++) {
                int x = random.nextInt(320);
                int y = random.nextInt(240);
                g.setColor(new Color(random.nextInt(100) + 100, random.nextInt(100) + 100, random.nextInt(100) + 100));
                g.fillRect(x, y, 2, 2);
            }

            // Update timestamp
            g.setColor(Color.BLACK);
            g.fillRect(0, 215, 320, 25);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()), 10, 230);

            // Recording indicator
            if (recording) {
                g.setColor(Color.RED);
                g.fillOval(295, 10, 15, 15);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("REC", 265, 20);
            }

            g.dispose();
            updateVideoLabel();
        }

        private void updateVideoLabel() {
            if (currentFrame != null) {
                videoLabel.setIcon(new ImageIcon(currentFrame));
            }
        }

        public void startRecording() {
            recording = true;
            frameCount = 0;
            statusLabel.setText("🔴 RECORDING");
            // In a real implementation, you would start writing frames to a video file
        }

        public void stopRecording() {
            if (recording) {
                recording = false;
                statusLabel.setText(isActive() ? "🟢 LIVE" : "🔴 OFFLINE");

                // Simulate saving recording
                File recordingsDir = new File("recordings");
                if (!recordingsDir.exists()) {
                    recordingsDir.mkdirs();
                }

                String filename = "recordings/" + name.replaceAll(" ", "_") + "_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".avi";

                // In real implementation, save the video file
                System.out.println("Recording saved: " + filename + " (" + frameCount + " frames)");
            }
        }

        public String takeSnapshot() {
            File snapshotsDir = new File("recordings");
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }

            String filename = "recordings/" + name.replaceAll(" ", "_") + "_snapshot_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".png";

            try {
                ImageIO.write(currentFrame, "png", new File(filename));
                return filename;
            } catch (IOException ex) {
                ex.printStackTrace();
                return "Failed to save snapshot";
            }
        }

        public boolean isActive() {
            return "ACTIVE".equalsIgnoreCase(status);
        }

        public boolean isRecording() {
            return recording;
        }

        public JPanel getPanel() {
            return panel;
        }

        public String getName() {
            return name;
        }

        public BufferedImage getCurrentFrame() {
            return currentFrame;
        }
    }
}