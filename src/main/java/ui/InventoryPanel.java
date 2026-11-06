package ui;

import model.SparePart;
import service.InventoryService;
import ui.theme.ModernButton;
import ui.theme.ModernTable;
import ui.theme.ModernTextField;
import ui.theme.UITheme;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JLabel lblCount;
    private final JCheckBox chkLowStockOnly;

    public InventoryPanel() {
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

        JLabel lblTitle = new JLabel("Spare Parts & Inventory Control");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading spare parts stock...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        chkLowStockOnly = new JCheckBox("Low Stock Only \u26A0\uFE0F");
        chkLowStockOnly.setFont(UITheme.FONT_BODY_BOLD);
        chkLowStockOnly.setForeground(UITheme.DANGER);
        chkLowStockOnly.setOpaque(false);
        controls.add(chkLowStockOnly);

        txtSearch = new ModernTextField("Search part, category, supplier...");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnAdd = new ModernButton("+ Add Spare Part", ModernButton.ButtonType.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(145, 36));
        controls.add(btnAdd);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"Part ID", "Spare Part Description", "Category", "In Stock", "Min Threshold", "Unit Price", "Supplier Name", "Stock Alert"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTIONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnAdjustStock = new ModernButton("\u267E Adjust Stock (Restock/Issue)", ModernButton.ButtonType.SECONDARY);
        btnAdjustStock.setPreferredSize(new Dimension(220, 36));
        bottomBar.add(btnAdjustStock);

        ModernButton btnEdit = new ModernButton("Edit Selected", ModernButton.ButtonType.SECONDARY);
        btnEdit.setPreferredSize(new Dimension(115, 36));
        bottomBar.add(btnEdit);

        ModernButton btnDelete = new ModernButton("Delete Part", ModernButton.ButtonType.DANGER);
        btnDelete.setPreferredSize(new Dimension(115, 36));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAdd.addActionListener(e -> showPartDialog(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnAdjustStock.addActionListener(e -> showAdjustStockDialog());
        btnSearch.addActionListener(e -> searchParts());
        txtSearch.addActionListener(e -> searchParts());
        chkLowStockOnly.addActionListener(e -> {
            if (chkLowStockOnly.isSelected()) {
                loadLowStock();
            } else {
                loadParts();
            }
        });
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            chkLowStockOnly.setSelected(false);
            loadParts();
        });

        loadParts();
    }

    public void loadParts() {
        SwingWorker<List<SparePart>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SparePart> doInBackground() throws Exception {
                return inventoryService.getAllParts();
            }

            @Override
            protected void done() {
                try {
                    List<SparePart> list = get();
                    populateTable(list);
                    int lowStock = 0;
                    for (SparePart p : list) {
                        if (p.isLowStock()) lowStock++;
                    }
                    lblCount.setText(String.format("Total SKUs: %d | Low Stock Alerts: %d", list.size(), lowStock));
                } catch (Exception e) {
                    lblCount.setText("Error loading parts: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadLowStock() {
        SwingWorker<List<SparePart>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SparePart> doInBackground() throws Exception {
                return inventoryService.getLowStockParts();
            }

            @Override
            protected void done() {
                try {
                    List<SparePart> list = get();
                    populateTable(list);
                    lblCount.setText("Displaying " + list.size() + " LOW STOCK item(s) requiring reorder.");
                } catch (Exception e) {
                    lblCount.setText("Error loading low stock: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchParts() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadParts();
            return;
        }

        SwingWorker<List<SparePart>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SparePart> doInBackground() throws Exception {
                return inventoryService.searchParts(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<SparePart> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " part(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InventoryPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<SparePart> list) {
        tableModel.setRowCount(0);
        for (SparePart p : list) {
            String status = p.isLowStock() ? "Overdue Service" : "Available"; // Uses red / green badge colors from ModernTable
            String statusText = p.isLowStock() ? "LOW STOCK \u26A0\uFE0F" : "Adequate";
            tableModel.addRow(new Object[]{
                p.getPartId(),
                p.getPartName(),
                p.getPartCategory(),
                p.getQuantity(),
                p.getMinThreshold(),
                String.format("INR %.2f", p.getUnitPrice()),
                p.getSupplierName() != null ? p.getSupplierName() : "N/A",
                statusText
            });
        }
    }

    private void showAdjustStockDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a spare part to adjust stock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int partId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int currentQty = (int) tableModel.getValueAt(row, 3);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Adjust Inventory Stock", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        JLabel lblPart = new JLabel(name);
        lblPart.setFont(UITheme.FONT_SUBTITLE);
        lblPart.setForeground(UITheme.PRIMARY_BLUE);
        panel.add(lblPart);

        JLabel lblCur = new JLabel("Current Available Stock: " + currentQty + " Units");
        lblCur.setFont(UITheme.FONT_BODY);
        lblCur.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblCur);
        panel.add(Box.createVerticalStrut(15));

        JComboBox<String> cmbType = new JComboBox<>(new String[]{"IN - Restock / Received Shipment", "OUT - Workshop Consumption / Manual Issue"});
        ModernTextField txtQty = new ModernTextField("Quantity (e.g. 10)");
        ModernTextField txtReason = new ModernTextField("Reason / PO Number / Notes");

        addFormField(panel, "TRANSACTION ACTION *", cmbType);
        addFormField(panel, "UNITS QUANTITY *", txtQty);
        addFormField(panel, "TRANSACTION NOTES", txtReason);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnConfirm = new ModernButton("Apply Adjustment", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(txtQty.getText().trim());
                if (qty <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");

                boolean isOut = cmbType.getSelectedIndex() == 1;
                int delta = isOut ? -qty : qty;
                String transType = isOut ? "OUT" : "IN";

                boolean ok = inventoryService.adjustStock(partId, delta, transType, null, txtReason.getText().trim());
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Stock adjusted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadParts();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cannot deduct more items than available in stock.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a part to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            SparePart p = inventoryService.getPartById(id);
            if (p != null) {
                showPartDialog(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a part to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + name + "'?", "Confirm Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                inventoryService.deletePart(id);
                JOptionPane.showMessageDialog(this, "Spare part deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadParts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Cannot delete part: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPartDialog(SparePart existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Spare Part Details" : "Add New Spare Part", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        ModernTextField txtName = new ModernTextField("Part Name (e.g. Bosch Oil Filter)");
        String[] categories = {"Fluids & Lubricants", "Filters", "Brakes", "Ignition", "Electrical", "Accessories", "Suspension & Steering", "Engine Components"};
        JComboBox<String> cmbCat = new JComboBox<>(categories);

        ModernTextField txtQty = new ModernTextField("Quantity in Stock (e.g. 20)");
        ModernTextField txtMin = new ModernTextField("Minimum Alert Threshold (e.g. 5)");
        ModernTextField txtPrice = new ModernTextField("Unit Price (INR) (e.g. 850.00)");
        ModernTextField txtSupplier = new ModernTextField("Supplier / Vendor Name");

        if (isEdit) {
            txtName.setText(existing.getPartName());
            cmbCat.setSelectedItem(existing.getPartCategory());
            txtQty.setText(String.valueOf(existing.getQuantity()));
            txtMin.setText(String.valueOf(existing.getMinThreshold()));
            txtPrice.setText(String.format("%.2f", existing.getUnitPrice()));
            txtSupplier.setText(existing.getSupplierName() != null ? existing.getSupplierName() : "");
        }

        addFormField(panel, "PART NAME *", txtName);
        addFormField(panel, "CATEGORY *", cmbCat);
        addFormField(panel, "QUANTITY IN STOCK *", txtQty);
        addFormField(panel, "MINIMUM ALERT THRESHOLD *", txtMin);
        addFormField(panel, "UNIT PRICE (INR) *", txtPrice);
        addFormField(panel, "SUPPLIER / DISTRIBUTOR", txtSupplier);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton(isEdit ? "Update Part" : "Save Part", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String cat = (String) cmbCat.getSelectedItem();
                int qty = Integer.parseInt(txtQty.getText().trim());
                int min = Integer.parseInt(txtMin.getText().trim());
                double price = Double.parseDouble(txtPrice.getText().trim());
                String supplier = txtSupplier.getText().trim();

                if (isEdit) {
                    existing.setPartName(name);
                    existing.setPartCategory(cat);
                    existing.setQuantity(qty);
                    existing.setMinThreshold(min);
                    existing.setUnitPrice(price);
                    existing.setSupplierName(supplier);
                    inventoryService.updatePart(existing);
                    JOptionPane.showMessageDialog(dialog, "Part updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    SparePart p = new SparePart(0, name, cat, qty, min, price, supplier, null);
                    inventoryService.addPart(p);
                    JOptionPane.showMessageDialog(dialog, "Spare part added to inventory!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }

                dialog.dispose();
                loadParts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for Quantity, Threshold, and Price.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
}
