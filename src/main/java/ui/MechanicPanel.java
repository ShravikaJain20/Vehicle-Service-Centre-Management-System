package ui;

import model.Mechanic;
import service.MechanicService;
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

public class MechanicPanel extends JPanel {

    private final MechanicService mechanicService;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final JLabel lblCount;

    public MechanicPanel() {
        this.mechanicService = new MechanicService();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // HEADER
        JPanel topPanel = new JPanel(new BorderLayout(16, 12));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Mechanic & Technician Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblCount = new JLabel("Loading workshop technicians...");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblCount);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(85, 36));
        controls.add(btnRefresh);

        ModernButton btnAdd = new ModernButton("+ Add Mechanic", ModernButton.ButtonType.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(140, 36));
        controls.add(btnAdd);

        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"ID", "Mechanic Name", "Phone Number", "Technical Specialization", "Status", "Active Jobs", "Enrolled Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM BAR
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        ModernButton btnToggleStatus = new ModernButton("Toggle Status", ModernButton.ButtonType.SECONDARY);
        btnToggleStatus.setPreferredSize(new Dimension(125, 36));
        bottomBar.add(btnToggleStatus);

        ModernButton btnEdit = new ModernButton("Edit Selected", ModernButton.ButtonType.SECONDARY);
        btnEdit.setPreferredSize(new Dimension(115, 36));
        bottomBar.add(btnEdit);

        ModernButton btnDelete = new ModernButton("Delete Mechanic", ModernButton.ButtonType.DANGER);
        btnDelete.setPreferredSize(new Dimension(135, 36));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);

        // LISTENERS
        btnAdd.addActionListener(e -> showMechanicDialog(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnToggleStatus.addActionListener(e -> toggleStatus());
        btnRefresh.addActionListener(e -> loadMechanics());

        loadMechanics();
    }

    public void loadMechanics() {
        SwingWorker<List<Mechanic>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Mechanic> doInBackground() throws Exception {
                return mechanicService.getAllMechanics();
            }

            @Override
            protected void done() {
                try {
                    List<Mechanic> list = get();
                    tableModel.setRowCount(0);
                    int available = 0;
                    for (Mechanic m : list) {
                        if ("Available".equalsIgnoreCase(m.getStatus())) available++;
                        tableModel.addRow(new Object[]{
                            m.getMechanicId(),
                            m.getName(),
                            m.getPhone(),
                            m.getSpecialization(),
                            m.getStatus(),
                            m.getActiveJobsCount() + " Active",
                            DateUtil.formatDateTime(m.getCreatedAt())
                        });
                    }
                    lblCount.setText("Total Workshop Staff: " + list.size() + " (" + available + " Available for dispatch)");
                } catch (Exception e) {
                    lblCount.setText("Error loading mechanics: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a mechanic to toggle status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String current = (String) tableModel.getValueAt(row, 4);

        String[] statuses = {"Available", "Busy", "On Leave"};
        String chosen = (String) JOptionPane.showInputDialog(this, "Select status for technician:", "Change Mechanic Status",
            JOptionPane.QUESTION_MESSAGE, null, statuses, current);

        if (chosen != null && !chosen.equalsIgnoreCase(current)) {
            try {
                Mechanic m = mechanicService.getMechanicById(id);
                if (m != null) {
                    m.setStatus(chosen);
                    mechanicService.updateMechanic(m);
                    loadMechanics();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error changing status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a mechanic to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Mechanic m = mechanicService.getMechanicById(id);
            if (m != null) {
                showMechanicDialog(m);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a mechanic to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete technician '" + name + "'?", "Confirm Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mechanicService.deleteMechanic(id);
                JOptionPane.showMessageDialog(this, "Technician record removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMechanics();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Cannot delete mechanic: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showMechanicDialog(Mechanic existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Technician Details" : "Register New Technician", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        ModernTextField txtName = new ModernTextField("Full Name (e.g. Suresh Kulkarni)");
        ModernTextField txtPhone = new ModernTextField("Phone Number (e.g. +91 9123456789)");
        ModernTextField txtSpec = new ModernTextField("Specialization (e.g. Engine, Brakes, HVAC)");

        String[] statuses = {"Available", "Busy", "On Leave"};
        JComboBox<String> cmbStatus = new JComboBox<>(statuses);

        if (isEdit) {
            txtName.setText(existing.getName());
            txtPhone.setText(existing.getPhone());
            txtSpec.setText(existing.getSpecialization());
            cmbStatus.setSelectedItem(existing.getStatus());
        }

        addFormField(panel, "TECHNICIAN NAME *", txtName);
        addFormField(panel, "CONTACT NUMBER *", txtPhone);
        addFormField(panel, "TECHNICAL SPECIALIZATION *", txtSpec);
        addFormField(panel, "INITIAL AVAILABILITY STATUS", cmbStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);
        ModernButton btnSave = new ModernButton(isEdit ? "Update Mechanic" : "Save Mechanic", ModernButton.ButtonType.PRIMARY);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String phone = txtPhone.getText().trim();
                String spec = txtSpec.getText().trim();
                String status = (String) cmbStatus.getSelectedItem();

                if (isEdit) {
                    existing.setName(name);
                    existing.setPhone(phone);
                    existing.setSpecialization(spec);
                    existing.setStatus(status);
                    mechanicService.updateMechanic(existing);
                    JOptionPane.showMessageDialog(dialog, "Mechanic details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Mechanic m = new Mechanic(0, name, phone, spec, status, 0, null);
                    mechanicService.addMechanic(m);
                    JOptionPane.showMessageDialog(dialog, "Mechanic registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }

                dialog.dispose();
                loadMechanics();
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
        parent.add(Box.createVerticalStrut(12));
    }
}
