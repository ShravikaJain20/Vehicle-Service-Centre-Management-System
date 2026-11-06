package ui;

import service.AuthService;
import ui.theme.ModernButton;
import ui.theme.ModernTextField;
import ui.theme.UITheme;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class LoginFrame extends JFrame {

    private ModernTextField txtUsername;
    private JPasswordField txtPassword;
    private ModernButton btnLogin;
    private JLabel lblStatus;

    public LoginFrame() {
        setTitle("Vehicle Service Centre Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));

        // LEFT BRANDING BANNER (Dark Navy)
        JPanel leftBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle gradient
                GradientPaint gp = new GradientPaint(0, 0, UITheme.NAVY_DARK, getWidth(), getHeight(), new Color(20, 30, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Automotive Accent lines
                g2.setColor(new Color(37, 99, 235, 40));
                g2.fillOval(-50, getHeight() - 200, 300, 300);
                g2.setColor(new Color(14, 165, 233, 30));
                g2.fillOval(getWidth() - 150, -50, 250, 250);
                g2.dispose();
            }
        };
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel lblLogo = new JLabel("AUTO CENTRE PRO");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblTagline = new JLabel("Intelligent Vehicle Service & Workshop Platform");
        lblTagline.setFont(UITheme.FONT_BODY);
        lblTagline.setForeground(UITheme.ACCENT_CYAN);

        leftBanner.add(lblLogo);
        leftBanner.add(Box.createVerticalStrut(6));
        leftBanner.add(lblTagline);
        leftBanner.add(Box.createVerticalStrut(40));

        // Features list
        addFeatureItem(leftBanner, "\u26A1 Automatic Service Reminders", "Dual-threshold (Date & KM) proactive notifications.");
        addFeatureItem(leftBanner, "\uD83E\uDD16 AI Maintenance Assistant", "Intelligent predictive repair and diagnostic queries.");
        addFeatureItem(leftBanner, "\uD83D\uDCCA Operational Real-Time Dashboard", "Comprehensive KPI metrics, inventory, and revenue charts.");
        addFeatureItem(leftBanner, "\uD83E\uDDFE GST Billing & PDF Generation", "Automated invoice calculation and direct print/export.");

        leftBanner.add(Box.createVerticalGlue());
        JLabel lblVer = new JLabel("v1.0.0 Enterprise Edition | Built for Automobile Workshops");
        lblVer.setFont(UITheme.FONT_SMALL);
        lblVer.setForeground(new Color(148, 163, 184));
        leftBanner.add(lblVer);

        // RIGHT LOGIN FORM (Clean White)
        JPanel rightForm = new JPanel();
        rightForm.setBackground(Color.WHITE);
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBorder(new EmptyBorder(60, 50, 40, 50));

        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblInstruction = new JLabel("Sign in to your automobile service management portal");
        lblInstruction.setFont(UITheme.FONT_BODY);
        lblInstruction.setForeground(UITheme.TEXT_SECONDARY);

        rightForm.add(lblWelcome);
        rightForm.add(Box.createVerticalStrut(6));
        rightForm.add(lblInstruction);
        rightForm.add(Box.createVerticalStrut(35));

        // Username Field
        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(UITheme.TEXT_SECONDARY);
        rightForm.add(lblUser);
        rightForm.add(Box.createVerticalStrut(6));

        txtUsername = new ModernTextField("Enter username (e.g. admin)");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsername.setText("admin"); // Demo default
        rightForm.add(txtUsername);
        rightForm.add(Box.createVerticalStrut(18));

        // Password Field
        JLabel lblPass = new JLabel("PASSWORD");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(UITheme.TEXT_SECONDARY);
        rightForm.add(lblPass);
        rightForm.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField("admin123"); // Demo default
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        rightForm.add(txtPassword);
        rightForm.add(Box.createVerticalStrut(12));

        // Status Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.DANGER);
        rightForm.add(lblStatus);
        rightForm.add(Box.createVerticalStrut(14));

        // Login Button
        btnLogin = new ModernButton("Sign In to Workshop", ModernButton.ButtonType.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setPreferredSize(new Dimension(360, 42));
        rightForm.add(btnLogin);
        rightForm.add(Box.createVerticalStrut(25));

        // Demo Credentials Hint Box
        JPanel hintBox = new JPanel(new GridLayout(2, 1, 4, 4));
        hintBox.setBackground(new Color(241, 245, 249));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        hintBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel h1 = new JLabel("Demo Admin: admin / admin123 (Full Access)");
        h1.setFont(UITheme.FONT_SMALL);
        h1.setForeground(UITheme.TEXT_PRIMARY);

        JLabel h2 = new JLabel("Demo Staff: staff / staff123 (Service Desk)");
        h2.setFont(UITheme.FONT_SMALL);
        h2.setForeground(UITheme.TEXT_SECONDARY);

        hintBox.add(h1);
        hintBox.add(h2);
        rightForm.add(hintBox);

        mainContainer.add(leftBanner);
        mainContainer.add(rightForm);

        add(mainContainer);

        // Actions
        btnLogin.addActionListener(e -> performLogin());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private void addFeatureItem(JPanel parent, String title, String desc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_BODY_BOLD);
        t.setForeground(Color.WHITE);

        JLabel d = new JLabel(desc);
        d.setFont(UITheme.FONT_SMALL);
        d.setForeground(new Color(203, 213, 225));

        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(d);
        p.add(Box.createVerticalStrut(18));

        parent.add(p);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        lblStatus.setForeground(UITheme.PRIMARY_BLUE);
        lblStatus.setText("Authenticating credentials...");
        btnLogin.setEnabled(false);

        // SwingWorker prevents UI freezing
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMsg = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return AuthService.getInstance().login(username, password);
                } catch (Exception e) {
                    errorMsg = "Database error: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        lblStatus.setForeground(UITheme.SUCCESS);
                        lblStatus.setText("Login successful! Loading dashboard...");

                        SwingUtilities.invokeLater(() -> {
                            new DashboardFrame().setVisible(true);
                            dispose();
                        });
                    } else {
                        lblStatus.setForeground(UITheme.DANGER);
                        if (errorMsg != null) {
                            lblStatus.setText("Database connection error. Check MySQL service.");
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                errorMsg + "\n\nPlease ensure MySQL is running on port 3306 and database 'vehicle_service_management' is created.\nCheck sql/setup_database.sql",
                                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            lblStatus.setText("Invalid username or password. Check demo credentials.");
                        }
                    }
                } catch (Exception e) {
                    lblStatus.setForeground(UITheme.DANGER);
                    lblStatus.setText("Error: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }
}
