package model;

import java.sql.Date;
import java.sql.Timestamp;

public class ServiceBooking {
    private int bookingId;
    private int customerId;
    private int vehicleId;
    private int serviceTypeId;
    private Integer mechanicId;
    private Date bookingDate;
    private String status; // 'Pending', 'Confirmed', 'In Progress', 'Completed', 'Cancelled'
    private String notes;
    private Timestamp createdAt;

    // UI display helpers
    private String customerName;
    private String vehicleRegistration;
    private String serviceTypeName;
    private String mechanicName;

    public ServiceBooking() {}

    public ServiceBooking(int bookingId, int customerId, int vehicleId, int serviceTypeId,
                          Integer mechanicId, Date bookingDate, String status, String notes,
                          Timestamp createdAt) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.serviceTypeId = serviceTypeId;
        this.mechanicId = mechanicId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(int serviceTypeId) { this.serviceTypeId = serviceTypeId; }

    public Integer getMechanicId() { return mechanicId; }
    public void setMechanicId(Integer mechanicId) { this.mechanicId = mechanicId; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    public String getServiceTypeName() { return serviceTypeName; }
    public void setServiceTypeName(String serviceTypeName) { this.serviceTypeName = serviceTypeName; }

    public String getMechanicName() { return mechanicName; }
    public void setMechanicName(String mechanicName) { this.mechanicName = mechanicName; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " - " + vehicleRegistration + " (" + status + ")";
    }
}
