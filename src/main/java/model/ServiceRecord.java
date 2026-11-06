package model;

import java.sql.Date;
import java.sql.Timestamp;

public class ServiceRecord {
    private int recordId;
    private Integer bookingId;
    private int vehicleId;
    private Integer mechanicId;
    private Date serviceDate;
    private int kmReading;
    private String serviceType;
    private String description;
    private String partsReplaced;
    private double totalCost;
    private Date nextRecommendedDate;
    private Integer nextRecommendedKm;
    private Timestamp createdAt;

    // UI helpers
    private String vehicleRegistration;
    private String mechanicName;
    private String customerName;

    public ServiceRecord() {}

    public ServiceRecord(int recordId, Integer bookingId, int vehicleId, Integer mechanicId,
                         Date serviceDate, int kmReading, String serviceType, String description,
                         String partsReplaced, double totalCost, Date nextRecommendedDate,
                         Integer nextRecommendedKm, Timestamp createdAt) {
        this.recordId = recordId;
        this.bookingId = bookingId;
        this.vehicleId = vehicleId;
        this.mechanicId = mechanicId;
        this.serviceDate = serviceDate;
        this.kmReading = kmReading;
        this.serviceType = serviceType;
        this.description = description;
        this.partsReplaced = partsReplaced;
        this.totalCost = totalCost;
        this.nextRecommendedDate = nextRecommendedDate;
        this.nextRecommendedKm = nextRecommendedKm;
        this.createdAt = createdAt;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public Integer getMechanicId() { return mechanicId; }
    public void setMechanicId(Integer mechanicId) { this.mechanicId = mechanicId; }

    public Date getServiceDate() { return serviceDate; }
    public void setServiceDate(Date serviceDate) { this.serviceDate = serviceDate; }

    public int getKmReading() { return kmReading; }
    public void setKmReading(int kmReading) { this.kmReading = kmReading; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPartsReplaced() { return partsReplaced; }
    public void setPartsReplaced(String partsReplaced) { this.partsReplaced = partsReplaced; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public Date getNextRecommendedDate() { return nextRecommendedDate; }
    public void setNextRecommendedDate(Date nextRecommendedDate) { this.nextRecommendedDate = nextRecommendedDate; }

    public Integer getNextRecommendedKm() { return nextRecommendedKm; }
    public void setNextRecommendedKm(Integer nextRecommendedKm) { this.nextRecommendedKm = nextRecommendedKm; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    public String getMechanicName() { return mechanicName; }
    public void setMechanicName(String mechanicName) { this.mechanicName = mechanicName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @Override
    public String toString() {
        return "Record #" + recordId + " - " + vehicleRegistration + " (" + serviceDate + ")";
    }
}
