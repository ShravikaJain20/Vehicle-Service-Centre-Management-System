package ui;

import model.Vehicle;
import service.AIRecommendationService;
import service.VehicleService;
import ui.theme.ModernButton;
import ui.theme.ModernCard;
import ui.theme.ModernTextField;
import ui.theme.UITheme;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AIAssistantPanel extends JPanel {

    private final AIRecommendationService aiService;
    private final VehicleService vehicleService;

    private JComboBox<VehicleComboItem> cmbVehicle;
    private JTextArea txtRecommendations;
    private JTextArea txtPredictive;
    private JTextArea txtExecutiveSummary;

    // Chat components
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private ModernTextField txtChatInput;

    public AIAssistantPanel() {
        this.aiService = AIRecommendationService.getInstance();
        this.vehicleService = new VehicleService();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("AI-Powered Vehicle Maintenance Assistant");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Intelligent Service Recommendations, Predictive Health Audits, and Technical Assistance");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Vehicle Selector on Top Right
        JPanel vehPicker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        vehPicker.setOpaque(false);

        JLabel lblSelect = new JLabel("ACTIVE VEHICLE CONTEXT:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSelect.setForeground(UITheme.TEXT_SECONDARY);
        vehPicker.add(lblSelect);

        cmbVehicle = new JComboBox<>();
        cmbVehicle.setPreferredSize(new Dimension(320, 36));
        vehPicker.add(cmbVehicle);

        ModernButton btnReloadVeh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnReloadVeh.setPreferredSize(new Dimension(85, 36));
        vehPicker.add(btnReloadVeh);

        topPanel.add(vehPicker, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABS FOR 4 AI FEATURES
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        tabbedPane.addTab("\uD83D\uDCA1 Service Recommendations", createRecommendationTab());
        tabbedPane.addTab("\uD83D\uDD2C Predictive Maintenance Audit", createPredictiveTab());
        tabbedPane.addTab("\uD83D\uDCAC Smart Diagnostic Assistant", createChatTab());
        tabbedPane.addTab("\uD83D\uDCCB Vehicle Health Summary", createSummaryTab());

        add(tabbedPane, BorderLayout.CENTER);

        btnReloadVeh.addActionListener(e -> loadVehicles());
        loadVehicles();
    }

    private void loadVehicles() {
        cmbVehicle.removeAllItems();
        try {
            List<Vehicle> list = vehicleService.getAllVehicles();
            for (Vehicle v : list) {
                cmbVehicle.addItem(new VehicleComboItem(v));
            }
        } catch (Exception e) {
            System.err.println("Error loading vehicles for AI assistant: " + e.getMessage());
        }
    }

    private Vehicle getSelectedVehicle() {
        VehicleComboItem item = (VehicleComboItem) cmbVehicle.getSelectedItem();
        return item != null ? item.vehicle : null;
    }

    // -------------------------------------------------------------------------
    // TAB 1: AI Service Recommendations
    // -------------------------------------------------------------------------
    private JPanel createRecommendationTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);

        ModernButton btnGen = new ModernButton("\u26A1 Generate AI Maintenance Recommendation", ModernButton.ButtonType.PRIMARY);
        btnGen.setPreferredSize(new Dimension(330, 38));
        topBar.add(btnGen);

        ModernButton btnCopy = new ModernButton("\uD83D\uDCCB Copy to Clipboard", ModernButton.ButtonType.SECONDARY);
        btnCopy.setPreferredSize(new Dimension(160, 38));
        topBar.add(btnCopy);

        p.add(topBar, BorderLayout.NORTH);

        txtRecommendations = new JTextArea();
        txtRecommendations.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtRecommendations.setLineWrap(true);
        txtRecommendations.setWrapStyleWord(true);
        txtRecommendations.setEditable(false);
        txtRecommendations.setText("Select a vehicle above and click 'Generate AI Maintenance Recommendation' to evaluate telemetry and service history.");

        JScrollPane scroll = new JScrollPane(txtRecommendations);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        p.add(scroll, BorderLayout.CENTER);

        btnGen.addActionListener(e -> {
            Vehicle v = getSelectedVehicle();
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle from the top dropdown.", "Vehicle Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtRecommendations.setText("Analyzing vehicle telemetry, past records, and odometer reading...");
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return aiService.getServiceRecommendation(v);
                }

                @Override
                protected void done() {
                    try {
                        txtRecommendations.setText(get());
                    } catch (Exception ex) {
                        txtRecommendations.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        btnCopy.addActionListener(e -> {
            txtRecommendations.selectAll();
            txtRecommendations.copy();
            JOptionPane.showMessageDialog(this, "Recommendation text copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        return p;
    }

    // -------------------------------------------------------------------------
    // TAB 2: Predictive Maintenance Audit
    // -------------------------------------------------------------------------
    private JPanel createPredictiveTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);

        ModernButton btnAudit = new ModernButton("\uD83D\uDD0D Run Predictive Health & Risk Audit", ModernButton.ButtonType.PRIMARY);
        btnAudit.setPreferredSize(new Dimension(300, 38));
        topBar.add(btnAudit);

        p.add(topBar, BorderLayout.NORTH);

        txtPredictive = new JTextArea();
        txtPredictive.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtPredictive.setLineWrap(true);
        txtPredictive.setWrapStyleWord(true);
        txtPredictive.setEditable(false);
        txtPredictive.setText("Click 'Run Predictive Health & Risk Audit' to perform wear estimation and component failure probability checks.");

        JScrollPane scroll = new JScrollPane(txtPredictive);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        p.add(scroll, BorderLayout.CENTER);

        btnAudit.addActionListener(e -> {
            Vehicle v = getSelectedVehicle();
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle from the top dropdown.", "Vehicle Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtPredictive.setText("Evaluating component lifecycles, friction wear, and fluid breakdown intervals...");
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return aiService.getPredictiveMaintenance(v);
                }

                @Override
                protected void done() {
                    try {
                        txtPredictive.setText(get());
                    } catch (Exception ex) {
                        txtPredictive.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        return p;
    }

    // -------------------------------------------------------------------------
    // TAB 3: Smart Service Assistant (Chat UI)
    // -------------------------------------------------------------------------
    private JPanel createChatTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Quick Prompt Chips
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chipsPanel.setOpaque(false);

        JLabel lblQuick = new JLabel("Quick Diagnostic Queries:");
        lblQuick.setFont(UITheme.FONT_SMALL);
        lblQuick.setForeground(UITheme.TEXT_SECONDARY);
        chipsPanel.add(lblQuick);

        addQuickChip(chipsPanel, "Brake pedal feels spongy");
        addQuickChip(chipsPanel, "Engine vibration at idle");
        addQuickChip(chipsPanel, "AC blowing warm air");
        addQuickChip(chipsPanel, "White smoke from exhaust");
        addQuickChip(chipsPanel, "Vehicle pulling to left");

        p.add(chipsPanel, BorderLayout.NORTH);

        // Chat conversation history
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(Color.WHITE);
        chatMessagesPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        p.add(chatScrollPane, BorderLayout.CENTER);

        // Add welcome message
        addChatMessage("Auto Assistant", "Hello! I am your AI Workshop Diagnostic Assistant. Ask me about mechanical symptoms, torque specifications, troubleshooting steps, or maintenance intervals.", false);

        // Bottom input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);

        txtChatInput = new ModernTextField("Ask automotive assistant a diagnostic or service question...");
        txtChatInput.setPreferredSize(new Dimension(400, 40));
        inputRow.add(txtChatInput, BorderLayout.CENTER);

        ModernButton btnSend = new ModernButton("Ask Assistant", ModernButton.ButtonType.PRIMARY);
        btnSend.setPreferredSize(new Dimension(130, 40));
        inputRow.add(btnSend, BorderLayout.EAST);

        p.add(inputRow, BorderLayout.SOUTH);

        Runnable sendAction = () -> {
            String query = txtChatInput.getText().trim();
            if (query.isEmpty()) return;

            addChatMessage("You", query, true);
            txtChatInput.setText("");
            btnSend.setEnabled(false);

            Vehicle v = getSelectedVehicle();

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return aiService.answerQuery(query, v);
                }

                @Override
                protected void done() {
                    btnSend.setEnabled(true);
                    try {
                        addChatMessage("Auto Assistant", get(), false);
                    } catch (Exception ex) {
                        addChatMessage("Auto Assistant", "Diagnostic error: " + ex.getMessage(), false);
                    }
                }
            };
            worker.execute();
        };

        btnSend.addActionListener(e -> sendAction.run());
        txtChatInput.addActionListener(e -> sendAction.run());

        return p;
    }

    private void addQuickChip(JPanel parent, String prompt) {
        JButton chip = new JButton(prompt);
        chip.setFont(UITheme.FONT_SMALL);
        chip.setBackground(new Color(241, 245, 249));
        chip.setForeground(UITheme.PRIMARY_BLUE);
        chip.setFocusPainted(false);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.addActionListener(e -> {
            txtChatInput.setText(prompt);
            txtChatInput.requestFocusInWindow();
        });
        parent.add(chip);
    }

    private void addChatMessage(String sender, String message, boolean isUser) {
        JPanel msgCard = new JPanel(new BorderLayout(6, 4));
        msgCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        msgCard.setOpaque(false);
        msgCard.setBorder(new EmptyBorder(6, 0, 8, 0));

        JPanel bubble = new JPanel(new BorderLayout(4, 4));
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        if (isUser) {
            bubble.setBackground(new Color(239, 246, 255)); // Light Blue
            bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
        } else {
            bubble.setBackground(new Color(248, 250, 252)); // Light Slate
            bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
        }

        JLabel lblSender = new JLabel(sender);
        lblSender.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSender.setForeground(isUser ? UITheme.PRIMARY_BLUE : UITheme.TEXT_PRIMARY);

        JTextArea txt = new JTextArea(message);
        txt.setFont(UITheme.FONT_BODY);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setEditable(false);
        txt.setOpaque(false);

        bubble.add(lblSender, BorderLayout.NORTH);
        bubble.add(txt, BorderLayout.CENTER);

        if (isUser) {
            msgCard.add(bubble, BorderLayout.EAST);
        } else {
            msgCard.add(bubble, BorderLayout.WEST);
        }

        chatMessagesPanel.add(msgCard);
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    // -------------------------------------------------------------------------
    // TAB 4: Vehicle Health Summary
    // -------------------------------------------------------------------------
    private JPanel createSummaryTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);

        ModernButton btnSum = new ModernButton("\uD83D\uDCCB Generate Vehicle Health Executive Summary", ModernButton.ButtonType.PRIMARY);
        btnSum.setPreferredSize(new Dimension(360, 38));
        topBar.add(btnSum);

        p.add(topBar, BorderLayout.NORTH);

        txtExecutiveSummary = new JTextArea();
        txtExecutiveSummary.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtExecutiveSummary.setLineWrap(true);
        txtExecutiveSummary.setWrapStyleWord(true);
        txtExecutiveSummary.setEditable(false);
        txtExecutiveSummary.setText("Select a vehicle and click 'Generate Vehicle Health Executive Summary' to produce an executive brief of visit history and top 3 priorities.");

        JScrollPane scroll = new JScrollPane(txtExecutiveSummary);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        p.add(scroll, BorderLayout.CENTER);

        btnSum.addActionListener(e -> {
            Vehicle v = getSelectedVehicle();
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle from the top dropdown.", "Vehicle Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtExecutiveSummary.setText("Compiling executive health overview and service milestones...");
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() {
                    return aiService.generateVehicleSummary(v);
                }

                @Override
                protected void done() {
                    try {
                        txtExecutiveSummary.setText(get());
                    } catch (Exception ex) {
                        txtExecutiveSummary.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        return p;
    }

    private static class VehicleComboItem {
        final Vehicle vehicle;

        VehicleComboItem(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public String toString() {
            return vehicle.getRegistrationNumber() + " - " + vehicle.getBrand() + " " + vehicle.getModel() + " (" + String.format("%,d KM", vehicle.getCurrentKm()) + ")";
        }
    }
}
