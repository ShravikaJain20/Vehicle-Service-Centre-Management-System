package model;

import java.sql.Timestamp;

public class SparePart {
    private int partId;
    private String partName;
    private String partCategory;
    private int quantity;
    private int minThreshold;
    private double unitPrice;
    private String supplierName;
    private Timestamp updatedAt;

    public SparePart() {}

    public SparePart(int partId, String partName, String partCategory, int quantity,
                     int minThreshold, double unitPrice, String supplierName, Timestamp updatedAt) {
        this.partId = partId;
        this.partName = partName;
        this.partCategory = partCategory;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.unitPrice = unitPrice;
        this.supplierName = supplierName;
        this.updatedAt = updatedAt;
    }

    public int getPartId() { return partId; }
    public void setPartId(int partId) { this.partId = partId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getPartCategory() { return partCategory; }
    public void setPartCategory(String partCategory) { this.partCategory = partCategory; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }

    @Override
    public String toString() {
        return partName + " (Stock: " + quantity + " | INR " + String.format("%.2f", unitPrice) + ")";
    }
}
