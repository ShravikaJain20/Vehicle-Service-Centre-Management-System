package ui;

import model.ServiceReminder;
import service.ReminderScheduler;
import service.ReminderService;
import ui.theme.ModernButton;
import ui.theme.ModernTable;
import ui.theme.UITheme;
import util.DateUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ReminderPanel extends JPanel {

    private final ReminderService reminderService;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> cmbCategoryFilter;
    private final JCheckBox chkActiveOnly;
    private final JLabel lblStatsSummary;

    private List<ServiceReminder> cachedList = new ArrayList<>();

    public ReminderPanel() {
        this.reminderService = ReminderService.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Automatic Vehicle Service Reminder System");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblStatsSummary = new JLabel("Dual-Threshold Engine: Evaluating Service Dates & Kilometre Readings...");
        lblStatsSummary.setFont(UITheme.FONT_SMALL);
        lblStatsSummary.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblStatsSummary);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        chkActiveOnly = new JCheckBox("Active Only", true);
        chkActiveOnly.setFont(UITheme.FONT_BODY_BOLD);
        chkActiveOnly.setOpaque(false);
        controls.add(chkActiveOnly);

        String[] categories = {"All Categories", "Overdue Service", "Service Due", "Upcoming Service"};
        cmbCategoryFilter = new JComboBox<>(categories);
        cmbCategoryFilter.setPreferredSize(new Dimension(160, 36));
        controls.add(cmbCategoryFilter);

        ModernButton btnAutoCheck = new ModernButton("\u26A1 Run Auto-Check Now", ModernButton.ButtonType.PRIMARY);
        btnAutoCheck.setPreferredSize(new Dimension(195, 36));
        controls.add(btnAutoCheck);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"ID", "Alert Category", "Vehicle Reg", "Vehicle Model", "Current Odometer", "Due Date", "Due KM", "Customer Name", "Contact Phone", "Trigger Reason", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);
        table.getColumnModel().getColumn(9).setPreferredWidth(260);
        table.getColumnModel().getColumn(10).setPreferredWidth(85);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTIONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnContact = new ModernButton("\uD83D\uDCE9 Contact Customer (Call/SMS/Email)", ModernButton.ButtonType.SECONDARY);
        btnContact.setPreferredSize(new Dimension(270, 36));
        bottomBar.add(btnContact);

        ModernButton btnResolve = new ModernButton("\u2705 Mark as Resolved (Serviced)", ModernButton.ButtonType.SUCCESS);
        btnResolve.setPreferredSize(new Dimension(210, 36));
        bottomBar.add(btnResolve);

        ModernButton btnDismiss = new ModernButton("Dismiss Alert", ModernButton.ButtonType.SECONDARY);
        btnDismiss.setPreferredSize(new Dimension(115, 36));
        bottomBar.add(btnDismiss);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAutoCheck.addActionListener(e -> runEngineEvaluation());
        btnResolve.addActionListener(e -> markSelectedAsResolved());
        btnDismiss.addActionListener(e -> dismissSelected());
        btnContact.addActionListener(e -> showCustomerContactDialog());
        cmbCategoryFilter.addActionListener(e -> applyFilter());
        chkActiveOnly.addActionListener(e -> applyFilter());
        btnRefresh.addActionListener(e -> loadReminders());

        loadReminders();
    }

    public void loadReminders() {
        SwingWorker<List<ServiceReminder>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceReminder> doInBackground() throws Exception {
                return reminderService.getAllReminders();
            }

            @Override
            protected void done() {
                try {
                    cachedList = get();
                    applyFilter();
                } catch (Exception e) {
                    lblStatsSummary.setText("Error loading reminders: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void runEngineEvaluation() {
        lblStatsSummary.setText("Running automatic reminder audit on all vehicles...");
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return reminderService.checkAllVehicles();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    JOptionPane.showMessageDialog(ReminderPanel.this,
                        "Automatic Reminder Engine Completed!\nGenerated " + count + " new service reminder alerts based on current odometer readings and service dates.",
                        "Engine Run Complete", JOptionPane.INFORMATION_MESSAGE);
                    loadReminders();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReminderPanel.this, "Engine error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String cat = (String) cmbCategoryFilter.getSelectedItem();
        boolean activeOnly = chkActiveOnly.isSelected();

        tableModel.setRowCount(0);
        int overdue = 0;
        int due = 0;
        int upcoming = 0;

        for (ServiceReminder r : cachedList) {
            if ("Active".equalsIgnoreCase(r.getStatus())) {
                if ("Overdue Service".equalsIgnoreCase(r.getReminderCategory())) overdue++;
                else if ("Service Due".equalsIgnoreCase(r.getReminderCategory())) due++;
                else if ("Upcoming Service".equalsIgnoreCase(r.getReminderCategory())) upcoming++;
            }

            if (activeOnly && !"Active".equalsIgnoreCase(r.getStatus())) {
                continue;
            }

            if (cat != null && !"All Categories".equalsIgnoreCase(cat)) {
                if (!cat.equalsIgnoreCase(r.getReminderCategory())) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                r.getReminderId(),
                r.getReminderCategory(),
                r.getVehicleRegistration(),
                r.getVehicleBrandModel(),
                String.format("%,d KM", r.getVehicleCurrentKm()),
                DateUtil.formatDate(r.getDueDate()),
                String.format("%,d KM", r.getDueKm()),
                r.getCustomerName(),
                r.getCustomerPhone(),
                r.getTriggerReason(),
                r.getStatus()
            });
        }

        lblStatsSummary.setText(String.format("Active Alerts: %d OVERDUE \u26A0\uFE0F | %d SERVICE DUE | %d UPCOMING | Total Tracked: %d",
            overdue, due, upcoming, cachedList.size()));
    }

    private void markSelectedAsResolved() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder alert to resolve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reminderId = (int) tableModel.getValueAt(row, 0);
        String reg = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark service reminder for vehicle " + reg + " as RESOLVED?\nThis indicates the customer brought in the vehicle or servicing was performed.",
            "Confirm Resolution", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reminderService.resolveReminder(reminderId);
                JOptionPane.showMessageDialog(this, "Reminder marked as Resolved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadReminders();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void dismissSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to dismiss.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reminderId = (int) tableModel.getValueAt(row, 0);
        try {
            reminderService.dismissReminder(reminderId);
            JOptionPane.showMessageDialog(this, "Reminder dismissed.", "Information", JOptionPane.INFORMATION_MESSAGE);
            loadReminders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomerContactDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to view customer contact details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cat = (String) tableModel.getValueAt(row, 1);
        String reg = (String) tableModel.getValueAt(row, 2);
        String model = (String) tableModel.getValueAt(row, 3);
        String custName = (String) tableModel.getValueAt(row, 7);
        String phone = (String) tableModel.getValueAt(row, 8);
        String reason = (String) tableModel.getValueAt(row, 9);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Customer Service Outreach - " + reg, true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        JLabel lblCust = new JLabel("Customer: " + custName + " | Contact: " + phone);
        lblCust.setFont(UITheme.FONT_SUBTITLE);
        panel.add(lblCust);

        JLabel lblVeh = new JLabel("Vehicle: " + reg + " (" + model + ") | Alert: " + cat);
        lblVeh.setFont(UITheme.FONT_BODY_BOLD);
        lblVeh.setForeground(UITheme.PRIMARY_BLUE);
        panel.add(lblVeh);
        panel.add(Box.createVerticalStrut(15));

        JLabel lblTemplate = new JLabel("PRE-COMPOSED SMS / WHATSAPP REMINDER MESSAGE:");
        lblTemplate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTemplate.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblTemplate);
        panel.add(Box.createVerticalStrut(4));

        String message = String.format(
            "Dear %s,\n\n" +
            "This is a reminder from Auto Service Centre regarding your %s (%s).\n" +
            "Status: %s.\n" +
            "Diagnosis: %s.\n\n" +
            "To prevent mechanical wear and maintain peak performance, please schedule your maintenance visit.\n" +
            "Call us at +91 80 4455 6677 or reply to book a priority service slot.",
            custName, model, reg, cat.toUpperCase(), reason
        );

        JTextArea txtMsg = new JTextArea(message, 8, 30);
        txtMsg.setFont(UITheme.FONT_BODY);
        txtMsg.setLineWrap(true);
        txtMsg.setWrapStyleWord(true);
        txtMsg.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));
        panel.add(new JScrollPane(txtMsg));
        panel.add(Box.createVerticalStrut(16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnClose = new ModernButton("Close", ModernButton.ButtonType.SECONDARY);
        ModernButton btnCopy = new ModernButton("\uD83D\uDCCB Copy Notification Text", ModernButton.ButtonType.PRIMARY);

        btnCopy.addActionListener(e -> {
            txtMsg.selectAll();
            txtMsg.copy();
            JOptionPane.showMessageDialog(dialog, "Message copied to clipboard! Ready to paste into SMS/WhatsApp.", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });
        btnClose.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnCopy);
        btnPanel.add(btnClose);
        panel.add(btnPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
