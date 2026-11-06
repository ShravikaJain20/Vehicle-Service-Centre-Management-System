package model;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int invoiceId;
    private Timestamp paymentDate;
    private double amountPaid;
    private String paymentMethod; // 'Cash', 'Card', 'UPI', 'Bank Transfer'
    private String notes;

    public Payment() {}

    public Payment(int paymentId, int invoiceId, Timestamp paymentDate, double amountPaid, String paymentMethod, String notes) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.paymentDate = paymentDate;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " - INR " + String.format("%.2f", amountPaid) + " via " + paymentMethod;
    }
}
