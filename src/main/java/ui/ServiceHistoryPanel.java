package ui;

import dao.ServiceRecordDAO;
import model.ServiceRecord;
import ui.theme.ModernButton;
import ui.theme.ModernTable;
import ui.theme.ModernTextField;
import ui.theme.UITheme;
import util.DateUtil;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ServiceHistoryPanel extends JPanel {

    private final ServiceRecordDAO recordDAO;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JLabel lblCount;

    public ServiceHistoryPanel() {
        this.recordDAO = new dao.ServiceRecordDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Vehicle Service History & Logbook");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading service logs...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        txtSearch = new ModernTextField("Search vehicle reg # (e.g. KA-01-MJ-4589)...");
        txtSearch.setPreferredSize(new Dimension(300, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"Record ID", "Service Date", "Vehicle Reg", "Owner", "Service Type", "KM Reading", "Mechanic", "Parts Replaced", "Cost (INR)", "Next Due Date", "Next Due KM"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(95);
        table.getColumnModel().getColumn(2).setPreferredWidth(115);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        table.getColumnModel().getColumn(8).setPreferredWidth(95);
        table.getColumnModel().getColumn(9).setPreferredWidth(100);
        table.getColumnModel().getColumn(10).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM BAR
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnDetails = new ModernButton("View Full Job Description", ModernButton.ButtonType.PRIMARY);
        btnDetails.setPreferredSize(new Dimension(200, 36));
        bottomBar.add(btnDetails);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnSearch.addActionListener(e -> searchRecords());
        txtSearch.addActionListener(e -> searchRecords());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadRecords();
        });
        btnDetails.addActionListener(e -> viewDetails());

        loadRecords();
    }

    public void loadRecords() {
        SwingWorker<List<ServiceRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceRecord> doInBackground() throws Exception {
                return recordDAO.getAllRecords();
            }

            @Override
            protected void done() {
                try {
                    List<ServiceRecord> list = get();
                    populateTable(list);
                    lblCount.setText("Total Historical Service Records: " + list.size());
                } catch (Exception e) {
                    lblCount.setText("Error loading records: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchRecords() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadRecords();
            return;
        }

        SwingWorker<List<ServiceRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceRecord> doInBackground() throws Exception {
                return recordDAO.searchRecords(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<ServiceRecord> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " record(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ServiceHistoryPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<ServiceRecord> list) {
        tableModel.setRowCount(0);
        for (ServiceRecord r : list) {
            tableModel.addRow(new Object[]{
                r.getRecordId(),
                DateUtil.formatDate(r.getServiceDate()),
                r.getVehicleRegistration(),
                r.getCustomerName(),
                r.getServiceType(),
                String.format("%,d KM", r.getKmReading()),
                r.getMechanicName() != null ? r.getMechanicName() : "N/A",
                r.getPartsReplaced() != null ? r.getPartsReplaced() : "None",
                String.format("%.2f", r.getTotalCost()),
                DateUtil.formatDate(r.getNextRecommendedDate()),
                r.getNextRecommendedKm() != null ? String.format("%,d KM", r.getNextRecommendedKm()) : "N/A"
            });
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service log from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int recordId = (int) tableModel.getValueAt(row, 0);
        String reg = (String) tableModel.getValueAt(row, 2);
        String type = (String) tableModel.getValueAt(row, 4);

        try {
            List<ServiceRecord> all = recordDAO.getAllRecords();
            ServiceRecord matched = null;
            for (ServiceRecord r : all) {
                if (r.getRecordId() == recordId) {
                    matched = r;
                    break;
                }
            }

            if (matched != null) {
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Service Log Details - " + reg, true);
                dialog.setSize(520, 420);
                dialog.setLocationRelativeTo(this);

                JPanel p = new JPanel(new BorderLayout(12, 12));
                p.setBorder(new EmptyBorder(18, 20, 18, 20));
                p.setBackground(Color.WHITE);

                JLabel h = new JLabel(reg + " - " + type + " (" + DateUtil.formatDate(matched.getServiceDate()) + ")");
                h.setFont(UITheme.FONT_SUBTITLE);
                h.setForeground(UITheme.PRIMARY_BLUE);
                p.add(h, BorderLayout.NORTH);

                JTextArea txt = new JTextArea();
                txt.setFont(UITheme.FONT_BODY);
                txt.setLineWrap(true);
                txt.setWrapStyleWord(true);
                txt.setEditable(false);

                StringBuilder sb = new StringBuilder();
                sb.append("Odometer Reading : ").append(matched.getKmReading()).append(" KM\n");
                sb.append("Technician       : ").append(matched.getMechanicName() != null ? matched.getMechanicName() : "N/A").append("\n");
                sb.append("Total Invoiced   : INR ").append(String.format("%.2f", matched.getTotalCost())).append("\n");
                sb.append("Parts Replaced   : ").append(matched.getPartsReplaced() != null ? matched.getPartsReplaced() : "None").append("\n");
                sb.append("Next Recommended : ").append(DateUtil.formatDate(matched.getNextRecommendedDate()))
                  .append(" or ").append(matched.getNextRecommendedKm() != null ? matched.getNextRecommendedKm() + " KM" : "N/A").append("\n\n");
                sb.append("Job Work Scope & Mechanics Notes:\n");
                sb.append(matched.getDescription() != null ? matched.getDescription() : "Routine maintenance executed.");

                txt.setText(sb.toString());
                p.add(new JScrollPane(txt), BorderLayout.CENTER);

                dialog.add(p);
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
