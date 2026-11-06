package model;

import java.sql.Timestamp;

public class InventoryTransaction {
    private int transactionId;
    private int partId;
    private String transactionType; // 'IN', 'OUT'
    private int quantity;
    private Integer relatedServiceId;
    private Timestamp transactionDate;
    private String notes;

    public InventoryTransaction() {}

    public InventoryTransaction(int transactionId, int partId, String transactionType, int quantity,
                                Integer relatedServiceId, Timestamp transactionDate, String notes) {
        this.transactionId = transactionId;
        this.partId = partId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.relatedServiceId = relatedServiceId;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getPartId() { return partId; }
    public void setPartId(int partId) { this.partId = partId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Integer getRelatedServiceId() { return relatedServiceId; }
    public void setRelatedServiceId(Integer relatedServiceId) { this.relatedServiceId = relatedServiceId; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
