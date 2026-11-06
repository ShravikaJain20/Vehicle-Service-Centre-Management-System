package model;

public class ServiceType {
    private int serviceTypeId;
    private String name;
    private double basePrice;
    private double estimatedHours;
    private String description;

    public ServiceType() {}

    public ServiceType(int serviceTypeId, String name, double basePrice, double estimatedHours, String description) {
        this.serviceTypeId = serviceTypeId;
        this.name = name;
        this.basePrice = basePrice;
        this.estimatedHours = estimatedHours;
        this.description = description;
    }

    public int getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(int serviceTypeId) { this.serviceTypeId = serviceTypeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name + " (INR " + String.format("%.2f", basePrice) + ")";
    }
}
