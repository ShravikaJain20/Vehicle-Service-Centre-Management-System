package model;

import java.sql.Date;
import java.sql.Timestamp;

public class ServiceReminder {
    private int reminderId;
    private int vehicleId;
    private int customerId;
    private String reminderCategory; // 'Upcoming Service', 'Service Due', 'Overdue Service'
    private String triggerReason;
    private Date dueDate;
    private int dueKm;
    private String status; // 'Active', 'Resolved', 'Dismissed'
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    // UI helpers
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String vehicleRegistration;
    private String vehicleBrandModel;
    private int vehicleCurrentKm;

    public ServiceReminder() {}

    public ServiceReminder(int reminderId, int vehicleId, int customerId, String reminderCategory,
                           String triggerReason, Date dueDate, int dueKm, String status,
                           Timestamp createdAt, Timestamp resolvedAt) {
        this.reminderId = reminderId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.reminderCategory = reminderCategory;
        this.triggerReason = triggerReason;
        this.dueDate = dueDate;
        this.dueKm = dueKm;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public int getReminderId() { return reminderId; }
    public void setReminderId(int reminderId) { this.reminderId = reminderId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getReminderCategory() { return reminderCategory; }
    public void setReminderCategory(String reminderCategory) { this.reminderCategory = reminderCategory; }

    public String getTriggerReason() { return triggerReason; }
    public void setTriggerReason(String triggerReason) { this.triggerReason = triggerReason; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public int getDueKm() { return dueKm; }
    public void setDueKm(int dueKm) { this.dueKm = dueKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    public String getVehicleBrandModel() { return vehicleBrandModel; }
    public void setVehicleBrandModel(String vehicleBrandModel) { this.vehicleBrandModel = vehicleBrandModel; }

    public int getVehicleCurrentKm() { return vehicleCurrentKm; }
    public void setVehicleCurrentKm(int vehicleCurrentKm) { this.vehicleCurrentKm = vehicleCurrentKm; }

    @Override
    public String toString() {
        return "[" + reminderCategory + "] " + vehicleRegistration + " - " + triggerReason;
    }
}
