package ui;

import model.Customer;
import model.Mechanic;
import model.ServiceBooking;
import model.ServiceType;
import model.Vehicle;
import service.BookingService;
import service.CustomerService;
import service.MechanicService;
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

public class BookingPanel extends JPanel {

    private final BookingService bookingService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final MechanicService mechanicService;

    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernTextField txtSearch;
    private final JComboBox<String> cmbFilterStatus;
    private final JLabel lblCount;

    public BookingPanel() {
        this.bookingService = new BookingService();
        this.customerService = new CustomerService();
        this.vehicleService = new VehicleService();
        this.mechanicService = new MechanicService();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER & TOOLBAR
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Service Bookings & Work Orders");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading service bookings...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        String[] statuses = {"All Statuses", "Pending", "Confirmed", "In Progress", "Completed", "Cancelled"};
        cmbFilterStatus = new JComboBox<>(statuses);
        cmbFilterStatus.setPreferredSize(new Dimension(130, 36));
        controls.add(cmbFilterStatus);

        txtSearch = new ModernTextField("Search vehicle, customer, mechanic...");
        txtSearch.setPreferredSize(new Dimension(240, 36));
        controls.add(txtSearch);

        ModernButton btnSearch = new ModernButton("Search", ModernButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(85, 36));
        controls.add(btnSearch);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnAdd = new ModernButton("+ Book Service", ModernButton.ButtonType.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(140, 36));
        controls.add(btnAdd);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"ID", "Booking Date", "Vehicle Reg", "Customer Name", "Service Type", "Assigned Mechanic", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(220);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTION BUTTONS
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnComplete = new ModernButton("\u2705 Complete Service", ModernButton.ButtonType.SUCCESS);
        btnComplete.setPreferredSize(new Dimension(160, 36));
        bottomBar.add(btnComplete);

        ModernButton btnChangeStatus = new ModernButton("Update Status", ModernButton.ButtonType.SECONDARY);
        btnChangeStatus.setPreferredSize(new Dimension(125, 36));
        bottomBar.add(btnChangeStatus);

        ModernButton btnCancel = new ModernButton("Cancel Booking", ModernButton.ButtonType.WARNING);
        btnCancel.setPreferredSize(new Dimension(135, 36));
        bottomBar.add(btnCancel);

        ModernButton btnDelete = new ModernButton("Delete", ModernButton.ButtonType.DANGER);
        btnDelete.setPreferredSize(new Dimension(95, 36));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAdd.addActionListener(e -> showBookingDialog());
        btnChangeStatus.addActionListener(e -> updateStatusDialog());
        btnComplete.addActionListener(e -> markServiceComplete());
        btnCancel.addActionListener(e -> cancelBooking());
        btnDelete.addActionListener(e -> deleteBooking());
        btnSearch.addActionListener(e -> searchBookings());
        txtSearch.addActionListener(e -> searchBookings());
        cmbFilterStatus.addActionListener(e -> filterByStatus());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterStatus.setSelectedIndex(0);
            loadBookings();
        });

        loadBookings();
    }

    public void loadBookings() {
        SwingWorker<List<ServiceBooking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceBooking> doInBackground() throws Exception {
                return bookingService.getAllBookings();
            }

            @Override
            protected void done() {
                try {
                    List<ServiceBooking> list = get();
                    populateTable(list);
                    lblCount.setText("Total Service Bookings: " + list.size());
                } catch (Exception e) {
                    lblCount.setText("Error loading bookings: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void filterByStatus() {
        String selected = (String) cmbFilterStatus.getSelectedItem();
        if (selected == null || "All Statuses".equalsIgnoreCase(selected)) {
            loadBookings();
            return;
        }

        SwingWorker<List<ServiceBooking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceBooking> doInBackground() throws Exception {
                return bookingService.getBookingsByStatus(selected);
            }

            @Override
            protected void done() {
                try {
                    List<ServiceBooking> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " bookings in status '" + selected + "'");
                } catch (Exception e) {
                    lblCount.setText("Filter error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void searchBookings() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadBookings();
            return;
        }

        SwingWorker<List<ServiceBooking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceBooking> doInBackground() throws Exception {
                return bookingService.searchBookings(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<ServiceBooking> list = get();
                    populateTable(list);
                    lblCount.setText("Found " + list.size() + " booking(s) matching '" + keyword + "'");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BookingPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<ServiceBooking> list) {
        tableModel.setRowCount(0);
        for (ServiceBooking b : list) {
            tableModel.addRow(new Object[]{
                b.getBookingId(),
                DateUtil.formatDate(b.getBookingDate()),
                b.getVehicleRegistration(),
                b.getCustomerName(),
                b.getServiceTypeName(),
                b.getMechanicName() != null ? b.getMechanicName() : "Unassigned",
                b.getStatus(),
                b.getNotes() != null ? b.getNotes() : ""
            });
        }
    }

    private void markServiceComplete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to mark as completed.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);
        if ("Completed".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This booking is already marked as Completed.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark Booking #" + bookingId + " as COMPLETED?\n\nThis will:\n" +
            "- Free the assigned mechanic's active job slot\n" +
            "- Automatically record a Service History entry\n" +
            "- Update the vehicle's last service date & KM\n" +
            "- Auto-resolve active service reminders for this vehicle!",
            "Complete Service Booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.updateBookingStatus(bookingId, "Completed");
                JOptionPane.showMessageDialog(this, "Service marked as Completed! Service history and reminders updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error completing service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStatusDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to change status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        String[] options = {"Pending", "Confirmed", "In Progress", "Completed", "Cancelled"};
        String chosen = (String) JOptionPane.showInputDialog(this,
            "Select new status for Booking #" + bookingId + ":", "Update Booking Status",
            JOptionPane.QUESTION_MESSAGE, null, options, currentStatus);

        if (chosen != null && !chosen.equalsIgnoreCase(currentStatus)) {
            try {
                bookingService.updateBookingStatus(bookingId, chosen);
                JOptionPane.showMessageDialog(this, "Status updated to " + chosen + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking #" + bookingId + "?", "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.updateBookingStatus(bookingId, "Cancelled");
                JOptionPane.showMessageDialog(this, "Booking marked as Cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Permanently delete Booking #" + bookingId + "?", "Confirm Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingService.deleteBooking(bookingId);
                JOptionPane.showMessageDialog(this, "Booking deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showBookingDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Service Booking", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        JComboBox<ComboItem> cmbCustomer = new JComboBox<>();
        JComboBox<ComboItem> cmbVehicle = new JComboBox<>();
        JComboBox<ComboItem> cmbServiceType = new JComboBox<>();
        JComboBox<ComboItem> cmbMechanic = new JComboBox<>();

        // Populate Customers
        try {
            List<Customer> customers = customerService.getAllCustomers();
            for (Customer c : customers) {
                cmbCustomer.addItem(new ComboItem(c.getCustomerId(), c.getFullName() + " (" + c.getPhoneNumber() + ")"));
            }
        } catch (Exception ignored) {}

        // Populate Vehicles linked to selected Customer
        Runnable refreshVehicles = () -> {
            cmbVehicle.removeAllItems();
            ComboItem selectedCust = (ComboItem) cmbCustomer.getSelectedItem();
            if (selectedCust != null) {
                try {
                    List<Vehicle> vehs = vehicleService.getVehiclesByCustomerId(selectedCust.id);
                    for (Vehicle v : vehs) {
                        cmbVehicle.addItem(new ComboItem(v.getVehicleId(), v.getRegistrationNumber() + " - " + v.getBrand() + " " + v.getModel()));
                    }
                } catch (Exception ignored) {}
            }
        };

        cmbCustomer.addActionListener(e -> refreshVehicles.run());
        refreshVehicles.run();

        // Populate Service Types
        try {
            List<ServiceType> stList = bookingService.getAllServiceTypes();
            for (ServiceType st : stList) {
                cmbServiceType.addItem(new ComboItem(st.getServiceTypeId(), st.getName() + " (INR " + String.format("%.0f", st.getBasePrice()) + ")"));
            }
        } catch (Exception ignored) {}

        // Populate Mechanics
        try {
            cmbMechanic.addItem(new ComboItem(-1, "Auto-assign / Later"));
            List<Mechanic> mechanics = mechanicService.getAllMechanics();
            for (Mechanic m : mechanics) {
                cmbMechanic.addItem(new ComboItem(m.getMechanicId(), m.getName() + " (" + m.getSpecialization() + " - " + m.getStatus() + ")"));
            }
        } catch (Exception ignored) {}

        ModernTextField txtDate = new ModernTextField("YYYY-MM-DD");
        txtDate.setText(LocalDate.now().toString());

        JTextArea txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(UITheme.FONT_BODY);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            new EmptyBorder(6, 8, 6, 8)
        ));

        addFormField(panel, "SELECT CUSTOMER *", cmbCustomer);
        addFormField(panel, "SELECT VEHICLE *", cmbVehicle);
        addFormField(panel, "SERVICE TYPE *", cmbServiceType);
        addFormField(panel, "ASSIGN MECHANIC", cmbMechanic);
        addFormField(panel, "SCHEDULED DATE *", txtDate);

        JLabel lblNotes = new JLabel("CUSTOMER NOTES & COMPLAINTS");
        lblNotes.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNotes.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(lblNotes);
        panel.add(Box.createVerticalStrut(3));
        panel.add(new JScrollPane(txtNotes));
        panel.add(Box.createVerticalStrut(16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton("Confirm Booking", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                ComboItem custItem = (ComboItem) cmbCustomer.getSelectedItem();
                ComboItem vehItem = (ComboItem) cmbVehicle.getSelectedItem();
                ComboItem stItem = (ComboItem) cmbServiceType.getSelectedItem();
                ComboItem mechItem = (ComboItem) cmbMechanic.getSelectedItem();

                if (custItem == null || vehItem == null || stItem == null) {
                    throw new IllegalArgumentException("Customer, Vehicle, and Service Type are mandatory.");
                }

                LocalDate date = DateUtil.parseDate(txtDate.getText().trim());
                if (date == null) {
                    throw new IllegalArgumentException("Please enter a valid date (YYYY-MM-DD).");
                }

                Integer mechId = (mechItem != null && mechItem.id > 0) ? mechItem.id : null;

                ServiceBooking b = new ServiceBooking();
                b.setCustomerId(custItem.id);
                b.setVehicleId(vehItem.id);
                b.setServiceTypeId(stItem.id);
                b.setMechanicId(mechId);
                b.setBookingDate(Date.valueOf(date));
                b.setStatus("Confirmed");
                b.setNotes(txtNotes.getText().trim());

                bookingService.createBooking(b);
                JOptionPane.showMessageDialog(dialog, "Service Booking confirmed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadBookings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
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
        parent.add(Box.createVerticalStrut(3));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private static class ComboItem {
        final int id;
        final String label;

        ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
