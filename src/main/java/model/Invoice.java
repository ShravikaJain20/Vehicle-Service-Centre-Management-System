package model;

import java.sql.Timestamp;

public class Invoice {
    private int invoiceId;
    private String invoiceNumber;
    private Integer bookingId;
    private int customerId;
    private int vehicleId;
    private double serviceCost;
    private double partsCost;
    private double subtotal;
    private double gstRate;
    private double gstAmount;
    private double discountAmount;
    private double finalAmount;
    private String paymentStatus; // 'Paid', 'Unpaid', 'Partially Paid'
    private Timestamp createdAt;

    // UI helpers
    private String customerName;
    private String customerPhone;
    private String vehicleRegistration;

    public Invoice() {
        this.gstRate = 18.0;
    }

    public Invoice(int invoiceId, String invoiceNumber, Integer bookingId, int customerId, int vehicleId,
                   double serviceCost, double partsCost, double subtotal, double gstRate,
                   double gstAmount, double discountAmount, double finalAmount,
                   String paymentStatus, Timestamp createdAt) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.serviceCost = serviceCost;
        this.partsCost = partsCost;
        this.subtotal = subtotal;
        this.gstRate = gstRate;
        this.gstAmount = gstAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public double getServiceCost() { return serviceCost; }
    public void setServiceCost(double serviceCost) { this.serviceCost = serviceCost; }

    public double getPartsCost() { return partsCost; }
    public void setPartsCost(double partsCost) { this.partsCost = partsCost; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getGstRate() { return gstRate; }
    public void setGstRate(double gstRate) { this.gstRate = gstRate; }

    public double getGstAmount() { return gstAmount; }
    public void setGstAmount(double gstAmount) { this.gstAmount = gstAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    @Override
    public String toString() {
        return invoiceNumber + " - INR " + String.format("%.2f", finalAmount) + " (" + paymentStatus + ")";
    }
}
