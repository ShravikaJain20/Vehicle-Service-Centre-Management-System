package ui;

import model.Customer;
import service.CustomerService;
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

public class CustomerPanel extends JPanel {

    private final CustomerService customerService;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JLabel lblCount;

    public CustomerPanel() {
        this.customerService = new CustomerService();
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER & TOOLBAR
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Customer Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading registered customers...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        // Action controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        txtSearch = new ModernTextField("Search by name, phone, or ID...");
        txtSearch.setPreferredSize(new Dimension(240, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnAdd = new ModernButton("+ Add Customer", ModernButton.ButtonType.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(140, 36));
        controls.add(btnAdd);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"Customer ID", "Full Name", "Phone Number", "Email Address", "Residential / Office Address", "Registered On"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTION BUTTONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnEdit = new ModernButton("Edit Selected", ModernButton.ButtonType.SECONDARY);
        btnEdit.setPreferredSize(new Dimension(120, 36));
        bottomBar.add(btnEdit);

        ModernButton btnDelete = new ModernButton("Delete Customer", ModernButton.ButtonType.DANGER);
        btnDelete.setPreferredSize(new Dimension(130, 36));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAdd.addActionListener(e -> showCustomerDialog(null));
        btnEdit.addActionListener(e -> editSelectedCustomer());
        btnDelete.addActionListener(e -> deleteSelectedCustomer());
        btnSearch.addActionListener(e -> searchCustomers());
        txtSearch.addActionListener(e -> searchCustomers());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadCustomers();
        });

        loadCustomers();
    }

    public void loadCustomers() {
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                return customerService.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    List<Customer> list = get();
                    populateTable(list);
                    lblCount.setText("Total Registered Customers: " + list.size());
                } catch (Exception e) {
                    lblCount.setText("Error loading customers: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchCustomers() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomers();
            return;
        }

        SwingWorker<List<Customer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                return customerService.searchCustomers(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Customer> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " customer(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Customer> list) {
        tableModel.setRowCount(0);
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getPhoneNumber(),
                c.getEmail() != null ? c.getEmail() : "N/A",
                c.getAddress() != null ? c.getAddress() : "N/A",
                DateUtil.formatDateTime(c.getCreatedAt())
            });
        }
    }

    private void editSelectedCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                showCustomerDialog(customer);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not fetch customer details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete customer '" + name + "' (ID: " + customerId + ")?\nAll registered vehicles and booking history will also be affected.",
            "Confirm Customer Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                customerService.deleteCustomer(customerId);
                JOptionPane.showMessageDialog(this, "Customer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCustomerDialog(Customer existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Customer Details" : "Register New Customer", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        ModernTextField txtName = new ModernTextField("Full Name (e.g. Ramesh Kumar)");
        ModernTextField txtPhone = new ModernTextField("Phone Number (e.g. +91 9876543210)");
        ModernTextField txtEmail = new ModernTextField("Email Address (e.g. ramesh@example.com)");
        JTextArea txtAddress = new JTextArea(3, 20);
        txtAddress.setFont(UITheme.FONT_BODY);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            new EmptyBorder(6, 8, 6, 8)
        ));

        if (isEdit) {
            txtName.setText(existing.getFullName());
            txtPhone.setText(existing.getPhoneNumber());
            txtEmail.setText(existing.getEmail() != null ? existing.getEmail() : "");
            txtAddress.setText(existing.getAddress() != null ? existing.getAddress() : "");
        }

        addFormField(panel, "FULL NAME *", txtName);
        addFormField(panel, "PHONE NUMBER *", txtPhone);
        addFormField(panel, "EMAIL ADDRESS", txtEmail);

        JLabel lblAddr = new JLabel("RESIDENTIAL / OFFICE ADDRESS");
        lblAddr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAddr.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblAddr);
        panel.add(Box.createVerticalStrut(4));
        panel.add(new JScrollPane(txtAddress));
        panel.add(Box.createVerticalStrut(20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton(isEdit ? "Update Customer" : "Save Customer", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                if (isEdit) {
                    existing.setFullName(txtName.getText().trim());
                    existing.setPhoneNumber(txtPhone.getText().trim());
                    existing.setEmail(txtEmail.getText().trim());
                    existing.setAddress(txtAddress.getText().trim());
                    customerService.updateCustomer(existing);
                    JOptionPane.showMessageDialog(dialog, "Customer details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Customer c = new Customer(txtName.getText().trim(), txtPhone.getText().trim(), txtEmail.getText().trim(), txtAddress.getText().trim());
                    customerService.addCustomer(c);
                    JOptionPane.showMessageDialog(dialog, "New customer registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadCustomers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        panel.add(btnPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
    }
}
