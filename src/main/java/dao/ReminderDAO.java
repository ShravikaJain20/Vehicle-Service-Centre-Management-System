package dao;

import config.DatabaseConnection;
import model.ServiceReminder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {

    public boolean addReminder(ServiceReminder reminder) throws SQLException {
        String sql = "INSERT INTO service_reminders (vehicle_id, customer_id, reminder_category, trigger_reason, due_date, due_km, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reminder.getVehicleId());
            ps.setInt(2, reminder.getCustomerId());
            ps.setString(3, reminder.getReminderCategory());
            ps.setString(4, reminder.getTriggerReason());
            ps.setDate(5, reminder.getDueDate());
            ps.setInt(6, reminder.getDueKm());
            ps.setString(7, reminder.getStatus() != null ? reminder.getStatus() : "Active");
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        reminder.setReminderId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<ServiceReminder> getActiveReminders() throws SQLException {
        List<ServiceReminder> list = new ArrayList<>();
        String sql = getBaseSelect() + " WHERE r.status = 'Active' ORDER BY CASE r.reminder_category " +
                     "WHEN 'Overdue Service' THEN 1 WHEN 'Service Due' THEN 2 WHEN 'Upcoming Service' THEN 3 ELSE 4 END, r.due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ServiceReminder> getAllReminders() throws SQLException {
        List<ServiceReminder> list = new ArrayList<>();
        String sql = getBaseSelect() + " ORDER BY r.status ASC, r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean resolveReminder(int reminderId) throws SQLException {
        String sql = "UPDATE service_reminders SET status = 'Resolved', resolved_at = CURRENT_TIMESTAMP WHERE reminder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean dismissReminder(int reminderId) throws SQLException {
        String sql = "UPDATE service_reminders SET status = 'Dismissed', resolved_at = CURRENT_TIMESTAMP WHERE reminder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasActiveReminder(int vehicleId, String category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_reminders WHERE vehicle_id = ? AND reminder_category = ? AND status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveReminderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_reminders WHERE status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getOverdueCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_reminders WHERE status = 'Active' AND reminder_category = 'Overdue Service'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int[] getIntervalConfig(String vehicleType) {
        String sql = "SELECT interval_days, interval_km, advance_notice_days, advance_notice_km FROM reminder_configurations WHERE vehicle_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicleType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[] {
                        rs.getInt("interval_days"),
                        rs.getInt("interval_km"),
                        rs.getInt("advance_notice_days"),
                        rs.getInt("advance_notice_km")
                    };
                }
            }
        } catch (SQLException ignored) {}
        // Default fallback: 180 days, 5000 km, 15 days advance, 500 km advance
        return new int[] { 180, 5000, 15, 500 };
    }

    private String getBaseSelect() {
        return "SELECT r.*, v.registration_number, CONCAT(v.brand, ' ', v.model) AS vehicle_name, v.current_km, " +
               "c.full_name AS customer_name, c.phone_number AS customer_phone, c.email AS customer_email " +
               "FROM service_reminders r " +
               "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
               "JOIN customers c ON r.customer_id = c.customer_id ";
    }

    private ServiceReminder mapRow(ResultSet rs) throws SQLException {
        ServiceReminder rem = new ServiceReminder(
            rs.getInt("reminder_id"),
            rs.getInt("vehicle_id"),
            rs.getInt("customer_id"),
            rs.getString("reminder_category"),
            rs.getString("trigger_reason"),
            rs.getDate("due_date"),
            rs.getInt("due_km"),
            rs.getString("status"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("resolved_at")
        );
        rem.setVehicleRegistration(rs.getString("registration_number"));
        rem.setVehicleBrandModel(rs.getString("vehicle_name"));
        rem.setVehicleCurrentKm(rs.getInt("current_km"));
        rem.setCustomerName(rs.getString("customer_name"));
        rem.setCustomerPhone(rs.getString("customer_phone"));
        rem.setCustomerEmail(rs.getString("customer_email"));
        return rem;
    }
}
