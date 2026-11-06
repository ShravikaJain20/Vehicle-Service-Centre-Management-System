package ui;

import model.*;
import service.BillingService;
import service.BookingService;
import service.CustomerService;
import service.InventoryService;
import service.VehicleService;
import ui.theme.ModernButton;
import ui.theme.ModernTable;
import ui.theme.ModernTextField;
import ui.theme.UITheme;
import util.DateUtil;
import util.PDFGenerator;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class BillingPanel extends JPanel {

    private final BillingService billingService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final InventoryService inventoryService;

    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JLabel lblTotalRevenue;

    public BillingPanel() {
        this.billingService = new BillingService();
        this.customerService = new CustomerService();
        this.vehicleService = new VehicleService();
        this.bookingService = new BookingService();
        this.inventoryService = new InventoryService();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Billing & Invoice Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblTotalRevenue = new JLabel("Loading billing data...");
        lblTotalRevenue.setFont(UITheme.FONT_SMALL);
        lblTotalRevenue.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblTotalRevenue);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        txtSearch = new ModernTextField("Search invoice #, customer, vehicle...");
        txtSearch.setPreferredSize(new Dimension(240, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnNewInvoice = new ModernButton("+ Generate Invoice", ModernButton.ButtonType.PRIMARY);
        btnNewInvoice.setPreferredSize(new Dimension(155, 36));
        controls.add(btnNewInvoice);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"ID", "Invoice #", "Date", "Customer Name", "Vehicle Reg", "Labor (INR)", "Parts (INR)", "GST (18%)", "Grand Total", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.getColumnModel().getColumn(8).setPreferredWidth(115);
        table.getColumnModel().getColumn(9).setPreferredWidth(95);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTION BUTTONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnRecordPayment = new ModernButton("\uD83D\uDCB3 Record Payment", ModernButton.ButtonType.SUCCESS);
        btnRecordPayment.setPreferredSize(new Dimension(150, 36));
        bottomBar.add(btnRecordPayment);

        ModernButton btnExportPdf = new ModernButton("\uD83D\uDCC4 Export as PDF", ModernButton.ButtonType.PRIMARY);
        btnExportPdf.setPreferredSize(new Dimension(140, 36));
        bottomBar.add(btnExportPdf);

        ModernButton btnPrint = new ModernButton("\uD83D\uDDA8\uFE0F Print Invoice", ModernButton.ButtonType.SECONDARY);
        btnPrint.setPreferredSize(new Dimension(125, 36));
        bottomBar.add(btnPrint);

        ModernButton btnViewItems = new ModernButton("View Breakdown", ModernButton.ButtonType.SECONDARY);
        btnViewItems.setPreferredSize(new Dimension(135, 36));
        bottomBar.add(btnViewItems);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnNewInvoice.addActionListener(e -> showGenerateInvoiceDialog());
        btnRecordPayment.addActionListener(e -> showRecordPaymentDialog());
        btnExportPdf.addActionListener(e -> exportSelectedInvoiceToPdf());
        btnPrint.addActionListener(e -> printSelectedInvoice());
        btnViewItems.addActionListener(e -> viewInvoiceItems());
        btnSearch.addActionListener(e -> searchInvoices());
        txtSearch.addActionListener(e -> searchInvoices());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadInvoices();
        });

        loadInvoices();
    }

    public void loadInvoices() {
        SwingWorker<List<Invoice>, Void> worker = new SwingWorker<>() {
            private double totalRevenue = 0.0;

            @Override
            protected List<Invoice> doInBackground() throws Exception {
                totalRevenue = billingService.getTotalRevenue();
                return billingService.getAllInvoices();
            }

            @Override
            protected void done() {
                try {
                    List<Invoice> list = get();
                    populateTable(list);
                    lblTotalRevenue.setText(String.format("Total Invoices: %d | Total Revenue Collected: INR %,.2f", list.size(), totalRevenue));
                } catch (Exception e) {
                    lblTotalRevenue.setText("Error loading invoices: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchInvoices() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadInvoices();
            return;
        }

        SwingWorker<List<Invoice>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Invoice> doInBackground() throws Exception {
                return billingService.searchInvoices(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Invoice> list = get();
                    populateTable(list);
                    lblTotalRevenue.setText("Found " + list.size() + " invoice(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Invoice> list) {
        tableModel.setRowCount(0);
        for (Invoice inv : list) {
            tableModel.addRow(new Object[]{
                inv.getInvoiceId(),
                inv.getInvoiceNumber(),
                DateUtil.formatDateTime(inv.getCreatedAt()),
                inv.getCustomerName(),
                inv.getVehicleRegistration(),
                String.format("%.2f", inv.getServiceCost()),
                String.format("%.2f", inv.getPartsCost()),
                String.format("%.2f", inv.getGstAmount()),
                String.format("INR %.2f", inv.getFinalAmount()),
                inv.getPaymentStatus()
            });
        }
    }

    private void exportSelectedInvoiceToPdf() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to export to PDF.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) tableModel.getValueAt(row, 0);
        String invNumber = (String) tableModel.getValueAt(row, 1);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as PDF");
        fileChooser.setSelectedFile(new File(invNumber + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
            }

            try {
                billingService.exportInvoiceToPdf(fileToSave, invoiceId);
                JOptionPane.showMessageDialog(this,
                    "PDF Invoice generated successfully!\nSaved to: " + fileToSave.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);

                // Attempt to open in default viewer
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "PDF Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printSelectedInvoice() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to print.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) tableModel.getValueAt(row, 0);
        try {
            Invoice inv = billingService.getInvoiceById(invoiceId);
            Customer cust = customerService.getCustomerById(inv.getCustomerId());
            Vehicle veh = vehicleService.getVehicleById(inv.getVehicleId());
            List<InvoiceItem> items = billingService.getItems(invoiceId);

            PDFGenerator.printInvoice(this, inv, cust, veh, items);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Print initialization error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewInvoiceItems() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to view line items.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) tableModel.getValueAt(row, 0);
        String invNumber = (String) tableModel.getValueAt(row, 1);

        try {
            List<InvoiceItem> items = billingService.getItems(invoiceId);
            List<Payment> payments = billingService.getPayments(invoiceId);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invoice Details - " + invNumber, true);
            dialog.setSize(580, 480);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(new EmptyBorder(16, 18, 16, 18));
            panel.setBackground(Color.WHITE);

            // Item table
            String[] itemCols = {"Type", "Description", "Qty", "Unit Price", "Total (INR)"};
            DefaultTableModel itemModel = new DefaultTableModel(itemCols, 0);
            for (InvoiceItem it : items) {
                itemModel.addRow(new Object[]{it.getItemType(), it.getDescription(), it.getQuantity(), String.format("%.2f", it.getUnitPrice()), String.format("%.2f", it.getTotalPrice())});
            }
            ModernTable itTable = new ModernTable(itemModel);
            panel.add(new JScrollPane(itTable), BorderLayout.CENTER);

            // Payments summary
            JPanel payPanel = new JPanel();
            payPanel.setLayout(new BoxLayout(payPanel, BoxLayout.Y_AXIS));
            payPanel.setOpaque(false);
            payPanel.add(new JLabel("Payment History:"));
            if (payments.isEmpty()) {
                payPanel.add(new JLabel(" - No payments recorded yet."));
            } else {
                for (Payment p : payments) {
                    payPanel.add(new JLabel(String.format(" - Paid INR %.2f via %s on %s (%s)", p.getAmountPaid(), p.getPaymentMethod(), DateUtil.formatDateTime(p.getPaymentDate()), p.getNotes() != null ? p.getNotes() : "")));
                }
            }
            panel.add(payPanel, BorderLayout.SOUTH);

            dialog.add(panel);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRecordPaymentDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to record payment.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = (int) tableModel.getValueAt(row, 0);
        String invNumber = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 9);

        if ("Paid".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This invoice is already fully paid.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Invoice inv = billingService.getInvoiceById(invoiceId);
            List<Payment> payments = billingService.getPayments(invoiceId);
            double totalPaid = 0;
            for (Payment p : payments) totalPaid += p.getAmountPaid();
            double balance = Math.max(0, inv.getFinalAmount() - totalPaid);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Payment - " + invNumber, true);
            dialog.setSize(440, 360);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 24, 20, 24));
            panel.setBackground(Color.WHITE);

            JLabel lblInv = new JLabel("Invoice: " + invNumber);
            lblInv.setFont(UITheme.FONT_SUBTITLE);
            panel.add(lblInv);

            JLabel lblBal = new JLabel(String.format("Final Total: INR %.2f | Outstanding Balance: INR %.2f", inv.getFinalAmount(), balance));
            lblBal.setFont(UITheme.FONT_BODY);
            lblBal.setForeground(UITheme.DANGER);
            panel.add(lblBal);
            panel.add(Box.createVerticalStrut(15));

            ModernTextField txtAmount = new ModernTextField("Amount Paid (INR)");
            txtAmount.setText(String.format("%.2f", balance));

            String[] methods = {"Cash", "Card", "UPI", "Bank Transfer"};
            JComboBox<String> cmbMethod = new JComboBox<>(methods);

            ModernTextField txtNotes = new ModernTextField("Reference / Txn ID / Notes");

            addFormField(panel, "AMOUNT RECEIVED (INR) *", txtAmount);
            addFormField(panel, "PAYMENT METHOD *", cmbMethod);
            addFormField(panel, "PAYMENT TRANSACTION NOTES", txtNotes);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setOpaque(false);

            ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
            ModernButton btnSave = new ModernButton("Submit Payment", ModernButton.ButtonType.PRIMARY);

            btnCancel.addActionListener(e -> dialog.dispose());
            btnSave.addActionListener(e -> {
                try {
                    double amt = Double.parseDouble(txtAmount.getText().trim());
                    String method = (String) cmbMethod.getSelectedItem();
                    String notes = txtNotes.getText().trim();

                    Payment p = new Payment(0, invoiceId, null, amt, method, notes);
                    billingService.recordPayment(p);
                    JOptionPane.showMessageDialog(dialog, "Payment recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadInvoices();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric payment amount.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnPanel.add(btnCancel);
            btnPanel.add(btnSave);
            panel.add(Box.createVerticalStrut(10));
            panel.add(btnPanel);

            dialog.add(panel);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGenerateInvoiceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Tax Invoice & Service Bill", true);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        panel.setBackground(Color.WHITE);

        JComboBox<CustomerItem> cmbCustomer = new JComboBox<>();
        JComboBox<VehicleItem> cmbVehicle = new JComboBox<>();

        try {
            List<Customer> customers = customerService.getAllCustomers();
            for (Customer c : customers) {
                cmbCustomer.addItem(new CustomerItem(c.getCustomerId(), c.getFullName() + " (" + c.getPhoneNumber() + ")"));
            }
        } catch (Exception ignored) {}

        Runnable loadVehicles = () -> {
            cmbVehicle.removeAllItems();
            CustomerItem ci = (CustomerItem) cmbCustomer.getSelectedItem();
            if (ci != null) {
                try {
                    List<Vehicle> vehs = vehicleService.getVehiclesByCustomerId(ci.id);
                    for (Vehicle v : vehs) {
                        cmbVehicle.addItem(new VehicleItem(v.getVehicleId(), v.getRegistrationNumber() + " - " + v.getBrand() + " " + v.getModel()));
                    }
                } catch (Exception ignored) {}
            }
        };
        cmbCustomer.addActionListener(e -> loadVehicles.run());
        loadVehicles.run();

        ModernTextField txtLaborCharge = new ModernTextField("Labor / Inspection Charge (e.g. 1500.00)");
        txtLaborCharge.setText("1500.00");

        ModernTextField txtDiscount = new ModernTextField("Discount Amount (e.g. 0.00)");
        txtDiscount.setText("0.00");

        // Spare parts picker for invoice
        JComboBox<PartItem> cmbPart = new JComboBox<>();
        try {
            List<SparePart> parts = inventoryService.getAllParts();
            for (SparePart p : parts) {
                cmbPart.addItem(new PartItem(p.getPartId(), p.getPartName(), p.getUnitPrice(), p.getQuantity()));
            }
        } catch (Exception ignored) {}

        ModernTextField txtPartQty = new ModernTextField("Qty (e.g. 1)");
        txtPartQty.setText("1");
        ModernButton btnAddPart = new ModernButton("+ Add Part to Bill", ModernButton.ButtonType.SECONDARY);

        // Selected parts table
        String[] partCols = {"Part Name", "Qty", "Unit Price", "Total"};
        DefaultTableModel partsTableModel = new DefaultTableModel(partCols, 0);
        ModernTable partsTable = new ModernTable(partsTableModel);
        partsTable.setPreferredScrollableViewportSize(new Dimension(540, 120));

        List<InvoiceItem> items = new ArrayList<>();
        List<SparePart> partsToDeduct = new ArrayList<>();

        btnAddPart.addActionListener(e -> {
            PartItem pi = (PartItem) cmbPart.getSelectedItem();
            if (pi != null) {
                try {
                    int q = Integer.parseInt(txtPartQty.getText().trim());
                    if (q <= 0) throw new IllegalArgumentException("Quantity must be positive.");
                    if (q > pi.stock) {
                        JOptionPane.showMessageDialog(dialog, "Only " + pi.stock + " units available in stock!", "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    double total = q * pi.price;
                    partsTableModel.addRow(new Object[]{pi.name, q, String.format("%.2f", pi.price), String.format("%.2f", total)});

                    InvoiceItem it = new InvoiceItem("PART", pi.name, q, pi.price);
                    items.add(it);

                    SparePart deduct = new SparePart();
                    deduct.setPartId(pi.id);
                    deduct.setQuantity(q);
                    partsToDeduct.add(deduct);

                    txtPartQty.setText("1");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid quantity: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        addFormField(panel, "CUSTOMER *", cmbCustomer);
        addFormField(panel, "VEHICLE *", cmbVehicle);
        addFormField(panel, "LABOR & SERVICE CHARGES (INR) *", txtLaborCharge);
        addFormField(panel, "DISCOUNT APPLIED (INR)", txtDiscount);

        JPanel partAddRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        partAddRow.setOpaque(false);
        cmbPart.setPreferredSize(new Dimension(300, 36));
        txtPartQty.setPreferredSize(new Dimension(80, 36));
        partAddRow.add(cmbPart);
        partAddRow.add(txtPartQty);
        partAddRow.add(btnAddPart);

        JLabel lblAddPart = new JLabel("ATTACH SPARE PARTS TO BILL");
        lblAddPart.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAddPart.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblAddPart);
        panel.add(Box.createVerticalStrut(3));
        panel.add(partAddRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JScrollPane(partsTable));
        panel.add(Box.createVerticalStrut(14));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnGenerate = new ModernButton("Generate Tax Invoice", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnGenerate.addActionListener(e -> {
            try {
                CustomerItem cust = (CustomerItem) cmbCustomer.getSelectedItem();
                VehicleItem veh = (VehicleItem) cmbVehicle.getSelectedItem();
                if (cust == null || veh == null) {
                    throw new IllegalArgumentException("Customer and Vehicle are mandatory.");
                }

                double labor = Double.parseDouble(txtLaborCharge.getText().trim());
                double discount = Double.parseDouble(txtDiscount.getText().trim());

                Invoice inv = new Invoice();
                inv.setCustomerId(cust.id);
                inv.setVehicleId(veh.id);
                inv.setDiscountAmount(discount);
                inv.setGstRate(18.0);
                inv.setPaymentStatus("Unpaid");

                // Add labor item
                if (labor > 0) {
                    items.add(0, new InvoiceItem("SERVICE", "General Servicing & Workshop Labor Charges", 1, labor));
                }

                billingService.generateInvoice(inv, items, partsToDeduct);
                JOptionPane.showMessageDialog(dialog, "Invoice #" + inv.getInvoiceNumber() + " generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadInvoices();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Billing Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnGenerate);
        panel.add(btnPanel);

        JScrollPane mainScroll = new JScrollPane(panel);
        mainScroll.setBorder(null);
        dialog.add(mainScroll);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(3));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private static class CustomerItem {
        final int id;
        final String label;
        CustomerItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class VehicleItem {
        final int id;
        final String label;
        VehicleItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class PartItem {
        final int id;
        final String name;
        final double price;
        final int stock;
        PartItem(int id, String name, double price, int stock) { this.id = id; this.name = name; this.price = price; this.stock = stock; }
        @Override public String toString() { return name + " (INR " + String.format("%.2f", price) + " | Stock: " + stock + ")"; }
    }
}
