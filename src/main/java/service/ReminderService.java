package service;

import config.DatabaseConnection;
import dao.ReminderDAO;
import dao.VehicleDAO;
import model.ServiceReminder;
import model.Vehicle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Core Intelligent Automatic Vehicle Service Reminder Engine.
 * Evaluates dual thresholds (Date and Kilometre) to detect Upcoming, Due, and Overdue services.
 */
public class ReminderService {
    private static ReminderService instance;
    private final ReminderDAO reminderDAO;
    private final VehicleDAO vehicleDAO;

    private ReminderService() {
        this.reminderDAO = new ReminderDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    public static synchronized ReminderService getInstance() {
        if (instance == null) {
            instance = new ReminderService();
        }
        return instance;
    }

    /**
     * Checks all registered vehicles in the database and generates reminders.
     * Called automatically by background scheduler and manual refresh.
     */
    public int checkAllVehicles() {
        int generatedCount = 0;
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            for (Vehicle vehicle : vehicles) {
                if (checkVehicle(vehicle)) {
                    generatedCount++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error evaluating automatic service reminders: " + e.getMessage());
        }
        return generatedCount;
    }

    /**
     * Evaluates a single vehicle's service status against configurable intervals.
     * Returns true if a new reminder was created.
     */
    public boolean checkVehicle(Vehicle vehicle) {
        if (vehicle == null) return false;

        // Obtain interval settings based on vehicle type
        int[] config = reminderDAO.getIntervalConfig(vehicle.getVehicleType());
        int intervalDays = config[0];
        int intervalKm = config[1];
        int advanceNoticeDays = config[2];
        int advanceNoticeKm = config[3];

        LocalDate today = LocalDate.now();

        // Reference point: last service date/km or vehicle creation defaults
        LocalDate lastServiceDate = vehicle.getLastServiceDate() != null 
            ? vehicle.getLastServiceDate().toLocalDate() 
            : (vehicle.getCreatedAt() != null ? vehicle.getCreatedAt().toLocalDateTime().toLocalDate() : today.minusMonths(6));
        
        int lastServiceKm = vehicle.getLastServiceKm();
        int currentKm = vehicle.getCurrentKm();

        // Calculate Next Due targets
        LocalDate nextDueDate = lastServiceDate.plusDays(intervalDays);
        int nextDueKm = lastServiceKm + intervalKm;

        long daysUntilDue = ChronoUnit.DAYS.between(today, nextDueDate);
        int kmUntilDue = nextDueKm - currentKm;

        String category = null;
        StringBuilder reason = new StringBuilder();

        // 1. OVERDUE CONDITION: Either Date or Kilometre threshold breached past due
        if (daysUntilDue < 0 || kmUntilDue <= 0) {
            category = "Overdue Service";
            if (daysUntilDue < 0 && kmUntilDue <= 0) {
                reason.append(String.format("Overdue by %d days and %d KM (Target: %s / %d KM; Current: %d KM)",
                    Math.abs(daysUntilDue), Math.abs(kmUntilDue), nextDueDate, nextDueKm, currentKm));
            } else if (daysUntilDue < 0) {
                reason.append(String.format("Overdue by %d days (Due date was %s; Target: %d KM)",
                    Math.abs(daysUntilDue), nextDueDate, nextDueKm));
            } else {
                reason.append(String.format("Odometer exceeded service limit by %d KM (Target: %d KM; Current: %d KM)",
                    Math.abs(kmUntilDue), nextDueKm, currentKm));
            }
        }
        // 2. SERVICE DUE TODAY / RIGHT NOW (within 3 days or 50 KM)
        else if (daysUntilDue <= 3 || kmUntilDue <= 50) {
            category = "Service Due";
            reason.append(String.format("Service is due now (%d days remaining, %d KM until target %d KM)",
                daysUntilDue, kmUntilDue, nextDueKm));
        }
        // 3. UPCOMING SERVICE (within advance notice window)
        else if (daysUntilDue <= advanceNoticeDays || kmUntilDue <= advanceNoticeKm) {
            category = "Upcoming Service";
            reason.append(String.format("Service due in %d days (%s) or in %d KM (Target: %d KM)",
                daysUntilDue, nextDueDate, kmUntilDue, nextDueKm));
        }

        // If a reminder category applies, save if not already active
        if (category != null) {
            try {
                if (!reminderDAO.hasActiveReminder(vehicle.getVehicleId(), category)) {
                    ServiceReminder rem = new ServiceReminder();
                    rem.setVehicleId(vehicle.getVehicleId());
                    rem.setCustomerId(vehicle.getCustomerId());
                    rem.setReminderCategory(category);
                    rem.setTriggerReason(reason.toString());
                    rem.setDueDate(Date.valueOf(nextDueDate));
                    rem.setDueKm(nextDueKm);
                    rem.setStatus("Active");
                    return reminderDAO.addReminder(rem);
                }
            } catch (SQLException e) {
                System.err.println("Error saving service reminder for vehicle " + vehicle.getRegistrationNumber() + ": " + e.getMessage());
            }
        }
        return false;
    }

    public List<ServiceReminder> getActiveReminders() throws SQLException {
        return reminderDAO.getActiveReminders();
    }

    public List<ServiceReminder> getAllReminders() throws SQLException {
        return reminderDAO.getAllReminders();
    }

    public boolean resolveReminder(int reminderId) throws SQLException {
        return reminderDAO.resolveReminder(reminderId);
    }

    public boolean dismissReminder(int reminderId) throws SQLException {
        return reminderDAO.dismissReminder(reminderId);
    }

    public void resolveRemindersForVehicle(int vehicleId) {
        String sql = "UPDATE service_reminders SET status = 'Resolved', resolved_at = CURRENT_TIMESTAMP WHERE vehicle_id = ? AND status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resolving reminders for vehicle: " + e.getMessage());
        }
    }

    public int getActiveCount() throws SQLException {
        return reminderDAO.getActiveReminderCount();
    }

    public int getOverdueCount() throws SQLException {
        return reminderDAO.getOverdueCount();
    }
}
