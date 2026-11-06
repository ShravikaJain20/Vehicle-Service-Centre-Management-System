package ui;

import model.Customer;
import model.Vehicle;
import service.CustomerService;
import service.VehicleService;
import ui.theme.ModernButton;
import ui.theme.ModernTable;
import ui.theme.ModernTextField;
import ui.theme.UITheme;
import util.DateUtil;

import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class VehiclePanel extends JPanel {

    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JLabel lblCount;

    public VehiclePanel() {
        this.vehicleService = new VehicleService();
        this.customerService = new CustomerService();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER & CONTROLS
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Vehicle Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading vehicle registry...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        txtSearch = new ModernTextField("Search by registration, brand, model, or customer...");
        txtSearch.setPreferredSize(new Dimension(300, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnAdd = new ModernButton("+ Add Vehicle", ModernButton.ButtonType.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(135, 36));
        controls.add(btnAdd);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"ID", "Reg Number", "Brand & Model", "Type", "Year", "Current KM", "Last Service Date", "Last Serv KM", "Owner Name", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(115);
        table.getColumnModel().getColumn(7).setPreferredWidth(95);
        table.getColumnModel().getColumn(8).setPreferredWidth(140);
        table.getColumnModel().getColumn(9).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM BAR WITH QUICK ACTIONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnUpdateKm = new ModernButton("\uD83D\uDD04 Update Odometer (KM)", ModernButton.ButtonType.SECONDARY);
        btnUpdateKm.setPreferredSize(new Dimension(185, 36));
        bottomBar.add(btnUpdateKm);

        ModernButton btnEdit = new ModernButton("Edit Selected", ModernButton.ButtonType.SECONDARY);
        btnEdit.setPreferredSize(new Dimension(115, 36));
        bottomBar.add(btnEdit);

        ModernButton btnDelete = new ModernButton("Delete Vehicle", ModernButton.ButtonType.DANGER);
        btnDelete.setPreferredSize(new Dimension(125, 36));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAdd.addActionListener(e -> showVehicleDialog(null));
        btnEdit.addActionListener(e -> editSelectedVehicle());
        btnDelete.addActionListener(e -> deleteSelectedVehicle());
        btnUpdateKm.addActionListener(e -> promptUpdateKm());
        btnSearch.addActionListener(e -> searchVehicles());
        txtSearch.addActionListener(e -> searchVehicles());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadVehicles();
        });

        loadVehicles();
    }

    public void loadVehicles() {
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                return vehicleService.getAllVehicles();
            }

            @Override
            protected void done() {
                try {
                    List<Vehicle> list = get();
                    populateTable(list);
                    lblCount.setText("Total Registered Vehicles: " + list.size());
                } catch (Exception e) {
                    lblCount.setText("Error loading vehicles: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchVehicles() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadVehicles();
            return;
        }

        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                return vehicleService.searchVehicles(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Vehicle> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " vehicle(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VehiclePanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Vehicle> list) {
        tableModel.setRowCount(0);
        for (Vehicle v : list) {
            tableModel.addRow(new Object[]{
                v.getVehicleId(),
                v.getRegistrationNumber(),
                v.getBrand() + " " + v.getModel(),
                v.getVehicleType(),
                v.getManufacturingYear(),
                String.format("%,d KM", v.getCurrentKm()),
                DateUtil.formatDate(v.getLastServiceDate()),
                String.format("%,d KM", v.getLastServiceKm()),
                v.getCustomerName() != null ? v.getCustomerName() : "N/A",
                v.getCustomerPhone() != null ? v.getCustomerPhone() : "N/A"
            });
        }
    }

    private void promptUpdateKm() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to update its kilometre reading.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = (int) tableModel.getValueAt(row, 0);
        String reg = (String) tableModel.getValueAt(row, 1);

        try {
            Vehicle v = vehicleService.getVehicleById(vehicleId);
            if (v == null) return;

            String input = JOptionPane.showInputDialog(this,
                "Vehicle: " + reg + " (" + v.getBrand() + " " + v.getModel() + ")\n" +
                "Current Odometer: " + v.getCurrentKm() + " KM\n\n" +
                "Enter new updated kilometre reading (KM):",
                v.getCurrentKm());

            if (input != null && !input.trim().isEmpty()) {
                int newKm = Integer.parseInt(input.trim());
                vehicleService.updateKilometres(vehicleId, newKm);
                JOptionPane.showMessageDialog(this,
                    "Odometer successfully updated to " + String.format("%,d KM", newKm) + "!\nAutomatic service reminder engine has re-evaluated this vehicle.",
                    "Kilometres Updated", JOptionPane.INFORMATION_MESSAGE);
                loadVehicles();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format for kilometres.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedVehicle() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = (int) tableModel.getValueAt(row, 0);
        try {
            Vehicle v = vehicleService.getVehicleById(vehicleId);
            if (v != null) {
                showVehicleDialog(v);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not fetch vehicle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedVehicle() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = (int) tableModel.getValueAt(row, 0);
        String reg = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete vehicle '" + reg + "' (ID: " + vehicleId + ")?\nAll associated service records and reminders will also be deleted.",
            "Confirm Vehicle Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                vehicleService.deleteVehicle(vehicleId);
                JOptionPane.showMessageDialog(this, "Vehicle deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVehicles();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting vehicle: " + e.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showVehicleDialog(Vehicle existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Vehicle Details" : "Register New Vehicle", true);
        dialog.setSize(520, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        // Customer selection dropdown
        JComboBox<CustomerItem> cmbCustomer = new JComboBox<>();
        try {
            List<Customer> customers = customerService.getAllCustomers();
            for (Customer c : customers) {
                cmbCustomer.addItem(new CustomerItem(c.getCustomerId(), c.getFullName() + " (" + c.getPhoneNumber() + ")"));
            }
        } catch (Exception ignored) {}

        ModernTextField txtReg = new ModernTextField("Registration No. (e.g. KA-01-MJ-4589)");
        ModernTextField txtBrand = new ModernTextField("Brand (e.g. Honda, Hyundai, Toyota)");
        ModernTextField txtModel = new ModernTextField("Model (e.g. City ZX, Creta SX, Fortuner)");

        String[] types = {"Sedan", "Hatchback", "SUV", "Luxury", "Commercial", "Two-Wheeler"};
        JComboBox<String> cmbType = new JComboBox<>(types);

        ModernTextField txtYear = new ModernTextField("Manufacturing Year (e.g. 2022)");
        txtYear.setText(String.valueOf(LocalDate.now().getYear()));

        ModernTextField txtCurrentKm = new ModernTextField("Current Odometer KM (e.g. 25000)");
        txtCurrentKm.setText("0");

        ModernTextField txtLastServiceDate = new ModernTextField("Last Service Date (YYYY-MM-DD or DD-MMM-YYYY)");
        ModernTextField txtLastServiceKm = new ModernTextField("Last Service KM Reading");
        txtLastServiceKm.setText("0");

        if (isEdit) {
            // Select existing customer
            for (int i = 0; i < cmbCustomer.getItemCount(); i++) {
                if (cmbCustomer.getItemAt(i).id == existing.getCustomerId()) {
                    cmbCustomer.setSelectedIndex(i);
                    break;
                }
            }
            txtReg.setText(existing.getRegistrationNumber());
            txtBrand.setText(existing.getBrand());
            txtModel.setText(existing.getModel());
            cmbType.setSelectedItem(existing.getVehicleType());
            txtYear.setText(String.valueOf(existing.getManufacturingYear()));
            txtCurrentKm.setText(String.valueOf(existing.getCurrentKm()));
            if (existing.getLastServiceDate() != null) {
                txtLastServiceDate.setText(existing.getLastServiceDate().toString());
            }
            txtLastServiceKm.setText(String.valueOf(existing.getLastServiceKm()));
        }

        addFormField(panel, "OWNER (CUSTOMER) *", cmbCustomer);
        addFormField(panel, "REGISTRATION NUMBER *", txtReg);
        addFormField(panel, "BRAND / MANUFACTURER *", txtBrand);
        addFormField(panel, "MODEL / VARIANT *", txtModel);
        addFormField(panel, "VEHICLE TYPE", cmbType);
        addFormField(panel, "MANUFACTURING YEAR *", txtYear);
        addFormField(panel, "CURRENT KILOMETRE READING (KM) *", txtCurrentKm);
        addFormField(panel, "LAST SERVICE DATE (Optional)", txtLastServiceDate);
        addFormField(panel, "LAST SERVICE KILOMETRE READING", txtLastServiceKm);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton(isEdit ? "Update Vehicle" : "Register Vehicle", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                CustomerItem selectedCust = (CustomerItem) cmbCustomer.getSelectedItem();
                if (selectedCust == null) {
                    throw new IllegalArgumentException("Please select a customer.");
                }

                String reg = txtReg.getText().trim();
                String brand = txtBrand.getText().trim();
                String model = txtModel.getText().trim();
                String type = (String) cmbType.getSelectedItem();
                int year = Integer.parseInt(txtYear.getText().trim());
                int currentKm = Integer.parseInt(txtCurrentKm.getText().trim());
                int lastKm = Integer.parseInt(txtLastServiceKm.getText().trim());

                LocalDate parsedDate = DateUtil.parseDate(txtLastServiceDate.getText().trim());
                Date sqlDate = parsedDate != null ? Date.valueOf(parsedDate) : null;

                if (isEdit) {
                    existing.setCustomerId(selectedCust.id);
                    existing.setRegistrationNumber(reg);
                    existing.setBrand(brand);
                    existing.setModel(model);
                    existing.setVehicleType(type);
                    existing.setManufacturingYear(year);
                    existing.setCurrentKm(currentKm);
                    existing.setLastServiceDate(sqlDate);
                    existing.setLastServiceKm(lastKm);

                    vehicleService.updateVehicle(existing);
                    JOptionPane.showMessageDialog(dialog, "Vehicle updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Vehicle v = new Vehicle(0, selectedCust.id, reg, brand, model, type, year, currentKm, sqlDate, lastKm, null);
                    vehicleService.addVehicle(v);
                    JOptionPane.showMessageDialog(dialog, "Vehicle registered successfully! Service reminder evaluation scheduled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }

                dialog.dispose();
                loadVehicles();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Year and Kilometres must be valid whole numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        dialog.add(scroll);
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

        CustomerItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
