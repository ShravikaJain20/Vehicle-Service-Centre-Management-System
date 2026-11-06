package ui;

import model.User;
import service.AuthService;
import service.DashboardService;
import service.ReminderScheduler;
import service.ReminderService;
import ui.components.ChartPanel;
import ui.components.StatCard;
import ui.theme.ModernButton;
import ui.theme.ModernCard;
import ui.theme.UITheme;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardFrame extends JFrame {

    private final DashboardService dashboardService;
    private final AuthService authService;

    // Layout
    private JPanel sidebarPanel;
    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JLabel lblHeaderTitle;
    private JLabel lblHeaderSubtitle;
    private JLabel lblClock;
    private ModernButton btnReminderAlertPill;

    // Subpanels
    private JPanel overviewPanel;
    private CustomerPanel customerPanel;
    private VehiclePanel vehiclePanel;
    private BookingPanel bookingPanel;
    private MechanicPanel mechanicPanel;
    private InventoryPanel inventoryPanel;
    private BillingPanel billingPanel;
    private ServiceHistoryPanel historyPanel;
    private ReminderPanel reminderPanel;
    private AIAssistantPanel aiAssistantPanel;

    // Stats Cards
    private StatCard cardCust;
    private StatCard cardVeh;
    private StatCard cardBookings;
    private StatCard cardPending;
    private StatCard cardCompleted;
    private StatCard cardRevenue;
    private StatCard cardReminders;

    // Charts
    private ChartPanel revenueChart;
    private ChartPanel serviceDonutChart;

    // Nav Buttons
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentSection = "DASHBOARD";

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("INR #,##0.00");
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | hh:mm:ss a");

    public DashboardFrame() {
        this.dashboardService = new DashboardService();
        this.authService = AuthService.getInstance();

        setTitle("Vehicle Service Centre Management System - Enterprise Workshop Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        // Start background service reminder scheduler
        ReminderScheduler.getInstance().start();

        initLayout();
        startLiveClock();
        loadDashboardData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ReminderScheduler.getInstance().stop();
            }
        });
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // 1. LEFT SIDEBAR (Dark Navy)
        sidebarPanel = createSidebar();
        root.add(sidebarPanel, BorderLayout.WEST);

        // 2. RIGHT CONTAINER (Header + CardLayout Content)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        // Top Header
        JPanel headerBar = createHeaderBar();
        rightContainer.add(headerBar, BorderLayout.NORTH);

        // Main Views Container
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setOpaque(false);

        // Initialize subpanels
        overviewPanel = createOverviewPanel();
        customerPanel = new CustomerPanel();
        vehiclePanel = new VehiclePanel();
        bookingPanel = new BookingPanel();
        mechanicPanel = new MechanicPanel();
        inventoryPanel = new InventoryPanel();
        billingPanel = new BillingPanel();
        historyPanel = new ServiceHistoryPanel();
        reminderPanel = new ReminderPanel();
        aiAssistantPanel = new AIAssistantPanel();

        contentContainer.add(overviewPanel, "DASHBOARD");
        contentContainer.add(customerPanel, "CUSTOMERS");
        contentContainer.add(vehiclePanel, "VEHICLES");
        contentContainer.add(bookingPanel, "BOOKINGS");
        contentContainer.add(mechanicPanel, "MECHANICS");
        contentContainer.add(inventoryPanel, "INVENTORY");
        contentContainer.add(billingPanel, "BILLING");
        contentContainer.add(historyPanel, "HISTORY");
        contentContainer.add(reminderPanel, "REMINDERS");
        contentContainer.add(aiAssistantPanel, "AI_ASSISTANT");

        rightContainer.add(contentContainer, BorderLayout.CENTER);
        root.add(rightContainer, BorderLayout.CENTER);

        add(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UITheme.NAVY_DARK);
        sidebar.setPreferredSize(new Dimension(250, 0));

        // Brand Banner Top
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel lblBrand = new JLabel("AUTO CENTRE PRO");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblTag = new JLabel("Automotive Workshop Hub");
        lblTag.setFont(UITheme.FONT_SMALL);
        lblTag.setForeground(UITheme.ACCENT_CYAN);

        brandPanel.add(lblBrand);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(lblTag);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Links Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(6, 12, 6, 12));

        addNavItem(menuPanel, "\uD83D\uDCCA Dashboard Overview", "DASHBOARD");
        addNavItem(menuPanel, "\uD83D\uDC65 Customer Management", "CUSTOMERS");
        addNavItem(menuPanel, "\uD83D\uDE97 Vehicle Fleet Registry", "VEHICLES");
        addNavItem(menuPanel, "\uD83D\uDCC5 Service Bookings", "BOOKINGS");
        addNavItem(menuPanel, "\uD83D\uDD27 Mechanic & Staff", "MECHANICS");
        addNavItem(menuPanel, "\uD83D\uDCE6 Spare Parts & Stock", "INVENTORY");
        addNavItem(menuPanel, "\uD83E\uDDFE Billing & Invoices", "BILLING");
        addNavItem(menuPanel, "\uD83D\uDCDC Service History Log", "HISTORY");
        addNavItem(menuPanel, "\u26A1 Service Reminders", "REMINDERS");
        addNavItem(menuPanel, "\uD83E\uDD16 AI Maintenance Assist", "AI_ASSISTANT");

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        sidebar.add(menuScroll, BorderLayout.CENTER);

        // User Profile & Logout Bottom
        JPanel footerPanel = new JPanel(new BorderLayout(8, 0));
        footerPanel.setBackground(new Color(11, 17, 32));
        footerPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        User current = authService.getCurrentUser();
        String name = current != null ? current.getFullName() : "Administrator";
        String role = current != null ? current.getRole() : "ADMIN";

        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setOpaque(false);

        JLabel lblName = new JLabel(name);
        lblName.setFont(UITheme.FONT_BODY_BOLD);
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(role.toUpperCase() + " ACCESS");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRole.setForeground(UITheme.ACCENT_CYAN);

        userBox.add(lblName);
        userBox.add(Box.createVerticalStrut(2));
        userBox.add(lblRole);

        footerPanel.add(userBox, BorderLayout.WEST);

        ModernButton btnLogout = new ModernButton("Logout", ModernButton.ButtonType.DANGER);
        btnLogout.setPreferredSize(new Dimension(75, 30));
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.addActionListener(e -> logout());
        footerPanel.add(btnLogout, BorderLayout.EAST);

        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel menu, String text, String cardKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UITheme.NAVY_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addActionListener(e -> switchView(cardKey, text));

        navButtons.add(btn);
        menu.add(btn);
        menu.add(Box.createVerticalStrut(3));
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        lblHeaderTitle = new JLabel("Workshop Operations Dashboard");
        lblHeaderTitle.setFont(UITheme.FONT_TITLE);
        lblHeaderTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblHeaderSubtitle = new JLabel("Real-time operational metrics, service statistics, and revenue insights");
        lblHeaderSubtitle.setFont(UITheme.FONT_SMALL);
        lblHeaderSubtitle.setForeground(UITheme.TEXT_SECONDARY);

        titleBox.add(lblHeaderTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblHeaderSubtitle);
        header.add(titleBox, BorderLayout.WEST);

        // Right side: Clock + Alert Pill + Refresh
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        lblClock = new JLabel("Loading system clock...");
        lblClock.setFont(UITheme.FONT_SMALL);
        lblClock.setForeground(UITheme.TEXT_SECONDARY);
        rightBox.add(lblClock);

        btnReminderAlertPill = new ModernButton("\u26A0\uFE0F 0 Overdue Reminders", ModernButton.ButtonType.DANGER);
        btnReminderAlertPill.setPreferredSize(new Dimension(190, 34));
        btnReminderAlertPill.setFont(UITheme.FONT_SMALL);
        btnReminderAlertPill.addActionListener(e -> switchView("REMINDERS", "\u26A1 Service Reminders"));
        rightBox.add(btnReminderAlertPill);

        ModernButton btnRefreshAll = new ModernButton("\u21BB Refresh All", ModernButton.ButtonType.SECONDARY);
        btnRefreshAll.setPreferredSize(new Dimension(110, 34));
        btnRefreshAll.setFont(UITheme.FONT_SMALL);
        btnRefreshAll.addActionListener(e -> refreshAllData());
        rightBox.add(btnRefreshAll);

        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createOverviewPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. STATS CARDS ROW (Grid of 6 KPIs)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);

        cardCust = new StatCard("\uD83D\uDC65", "Total Customers", "0", "Registered vehicle owners", UITheme.PRIMARY_BLUE);
        cardVeh = new StatCard("\uD83D\uDE97", "Registered Vehicles", "0", "Active vehicle fleet", UITheme.ACCENT_CYAN);
        cardBookings = new StatCard("\uD83D\uDCC5", "Total Bookings", "0", "Lifetime work orders", new Color(139, 92, 246));
        cardPending = new StatCard("\u23F3", "Pending Services", "0", "Currently in workshop", UITheme.WARNING);
        cardCompleted = new StatCard("\u2705", "Completed Services", "0", "Successfully serviced", UITheme.SUCCESS);
        cardRevenue = new StatCard("\uD83D\uDCB0", "Total Revenue", "INR 0", "All settled payments", UITheme.PRIMARY_BLUE);

        statsGrid.add(cardCust);
        statsGrid.add(cardVeh);
        statsGrid.add(cardBookings);
        statsGrid.add(cardPending);
        statsGrid.add(cardCompleted);
        statsGrid.add(cardRevenue);

        p.add(statsGrid, BorderLayout.NORTH);

        // 2. CHARTS SECTION (Monthly Revenue Bar Chart + Service Status Donut)
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 14, 14));
        chartsGrid.setOpaque(false);

        revenueChart = new ChartPanel("Monthly Workshop Revenue (INR)", ChartPanel.ChartType.BAR_REVENUE);
        serviceDonutChart = new ChartPanel("Service Work Orders Distribution", ChartPanel.ChartType.DONUT_SERVICE);

        chartsGrid.add(revenueChart);
        chartsGrid.add(serviceDonutChart);

        p.add(chartsGrid, BorderLayout.CENTER);

        // 3. BOTTOM INNOVATION BANNER
        ModernCard reminderBanner = new ModernCard(new BorderLayout(14, 0));
        reminderBanner.setCardBackground(new Color(239, 246, 255)); // Soft blue
        reminderBanner.setPreferredSize(new Dimension(0, 68));

        JLabel lblInnovTitle = new JLabel("\u26A1 MAIN INNOVATION: AUTOMATIC VEHICLE SERVICE REMINDER SYSTEM ACTIVE");
        lblInnovTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblInnovTitle.setForeground(UITheme.PRIMARY_BLUE);

        JLabel lblInnovDesc = new JLabel("Background engine autonomously evaluates odometer KM readings and service date thresholds every 30 minutes.");
        lblInnovDesc.setFont(UITheme.FONT_SMALL);
        lblInnovDesc.setForeground(UITheme.TEXT_SECONDARY);

        JPanel textB = new JPanel();
        textB.setLayout(new BoxLayout(textB, BoxLayout.Y_AXIS));
        textB.setOpaque(false);
        textB.add(lblInnovTitle);
        textB.add(Box.createVerticalStrut(3));
        textB.add(lblInnovDesc);

        reminderBanner.add(textB, BorderLayout.CENTER);

        ModernButton btnGoReminders = new ModernButton("View Reminder Alerts", ModernButton.ButtonType.PRIMARY);
        btnGoReminders.setPreferredSize(new Dimension(170, 36));
        btnGoReminders.addActionListener(e -> switchView("REMINDERS", "\u26A1 Service Reminders"));
        reminderBanner.add(btnGoReminders, BorderLayout.EAST);

        p.add(reminderBanner, BorderLayout.SOUTH);

        return p;
    }

    private void switchView(String cardKey, String navLabel) {
        currentSection = cardKey;
        cardLayout.show(contentContainer, cardKey);

        // Update nav highlight
        for (JButton b : navButtons) {
            if (b.getText().contains(navLabel.substring(Math.min(2, navLabel.length())).trim())) {
                b.setBackground(UITheme.PRIMARY_BLUE);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(UITheme.NAVY_DARK);
                b.setForeground(new Color(203, 213, 225));
            }
        }

        // Update title
        lblHeaderTitle.setText(cleanNavTitle(navLabel));
        lblHeaderSubtitle.setText("Vehicle Service Centre Operations Portal - " + cardKey);

        // Trigger reload on child panel
        switch (cardKey) {
            case "DASHBOARD": loadDashboardData(); break;
            case "CUSTOMERS": customerPanel.loadCustomers(); break;
            case "VEHICLES": vehiclePanel.loadVehicles(); break;
            case "BOOKINGS": bookingPanel.loadBookings(); break;
            case "MECHANICS": mechanicPanel.loadMechanics(); break;
            case "INVENTORY": inventoryPanel.loadParts(); break;
            case "BILLING": billingPanel.loadInvoices(); break;
            case "HISTORY": historyPanel.loadRecords(); break;
            case "REMINDERS": reminderPanel.loadReminders(); break;
        }
    }

    private String cleanNavTitle(String raw) {
        return raw.replaceAll("[^a-zA-Z &]", "").trim();
    }

    public void loadDashboardData() {
        SwingWorker<DashboardService.DashboardStats, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardService.DashboardStats doInBackground() {
                return dashboardService.getStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardService.DashboardStats s = get();
                    cardCust.setValue(String.valueOf(s.totalCustomers));
                    cardVeh.setValue(String.valueOf(s.totalVehicles));
                    cardBookings.setValue(String.valueOf(s.totalBookings));
                    cardPending.setValue(String.valueOf(s.pendingServices));
                    cardCompleted.setValue(String.valueOf(s.completedServices));
                    cardRevenue.setValue(CURRENCY_FMT.format(s.totalRevenue));

                    // Charts
                    revenueChart.setBarData(s.monthlyRevenue);
                    serviceDonutChart.setDonutData(s.completedServices, s.pendingServices, Math.max(0, s.totalBookings - (s.completedServices + s.pendingServices)));

                    // Header Reminder Alert Pill
                    if (s.overdueReminders > 0) {
                        btnReminderAlertPill.setText(String.format("\u26A0\uFE0F %d OVERDUE REMINDERS", s.overdueReminders));
                        btnReminderAlertPill.setVisible(true);
                    } else if (s.activeReminders > 0) {
                        btnReminderAlertPill.setText(String.format("\u23F0 %d Active Reminders", s.activeReminders));
                        btnReminderAlertPill.setVisible(true);
                    } else {
                        btnReminderAlertPill.setText("\u2705 All Vehicles Serviced");
                    }
                } catch (Exception e) {
                    System.err.println("Error rendering dashboard data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshAllData() {
        loadDashboardData();
        customerPanel.loadCustomers();
        vehiclePanel.loadVehicles();
        bookingPanel.loadBookings();
        mechanicPanel.loadMechanics();
        inventoryPanel.loadParts();
        billingPanel.loadInvoices();
        historyPanel.loadRecords();
        reminderPanel.loadReminders();
        JOptionPane.showMessageDialog(this, "All database records and dashboard KPIs refreshed!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startLiveClock() {
        Timer timer = new Timer(1000, e -> {
            lblClock.setText(LocalDateTime.now().format(CLOCK_FMT));
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            ReminderScheduler.getInstance().stop();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }
}
