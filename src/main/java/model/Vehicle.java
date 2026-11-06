package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Vehicle {
    private int vehicleId;
    private int customerId;
    private String registrationNumber;
    private String brand;
    private String model;
    private String vehicleType;
    private int manufacturingYear;
    private int currentKm;
    private Date lastServiceDate;
    private int lastServiceKm;
    private Timestamp createdAt;

    // Associated UI helper fields
    private String customerName;
    private String customerPhone;

    public Vehicle() {}

    public Vehicle(int vehicleId, int customerId, String registrationNumber, String brand, String model,
                   String vehicleType, int manufacturingYear, int currentKm, Date lastServiceDate,
                   int lastServiceKm, Timestamp createdAt) {
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.manufacturingYear = manufacturingYear;
        this.currentKm = currentKm;
        this.lastServiceDate = lastServiceDate;
        this.lastServiceKm = lastServiceKm;
        this.createdAt = createdAt;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getManufacturingYear() { return manufacturingYear; }
    public void setManufacturingYear(int manufacturingYear) { this.manufacturingYear = manufacturingYear; }

    public int getCurrentKm() { return currentKm; }
    public void setCurrentKm(int currentKm) { this.currentKm = currentKm; }

    public Date getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(Date lastServiceDate) { this.lastServiceDate = lastServiceDate; }

    public int getLastServiceKm() { return lastServiceKm; }
    public void setLastServiceKm(int lastServiceKm) { this.lastServiceKm = lastServiceKm; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDisplayName() {
        return registrationNumber + " - " + brand + " " + model;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
