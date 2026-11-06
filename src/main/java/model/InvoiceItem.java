package model;

public class InvoiceItem {
    private int itemId;
    private int invoiceId;
    private String itemType; // 'SERVICE', 'PART'
    private String description;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public InvoiceItem() {}

    public InvoiceItem(int itemId, int invoiceId, String itemType, String description, int quantity, double unitPrice, double totalPrice) {
        this.itemId = itemId;
        this.invoiceId = invoiceId;
        this.itemType = itemType;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public InvoiceItem(String itemType, String description, int quantity, double unitPrice) {
        this.itemType = itemType;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return description + " (" + quantity + " x " + unitPrice + " = " + totalPrice + ")";
    }
}
