package service;

import dao.CustomerDAO;
import dao.InventoryDAO;
import dao.InvoiceDAO;
import dao.VehicleDAO;
import model.*;
import util.PDFGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BillingService {
    private final InvoiceDAO invoiceDAO;
    private final InventoryDAO inventoryDAO;
    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;

    public BillingService() {
        this.invoiceDAO = new InvoiceDAO();
        this.inventoryDAO = new InventoryDAO();
        this.customerDAO = new CustomerDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    /**
     * Calculates all totals, creates invoice, items, and adjusts inventory.
     */
    public void generateInvoice(Invoice invoice, List<InvoiceItem> items, List<SparePart> partsToDeduct) throws SQLException, IllegalArgumentException {
        if (invoice.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Customer is required for billing.");
        }
        if (invoice.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Vehicle is required for billing.");
        }

        double serviceTotal = 0.0;
        double partsTotal = 0.0;

        if (items != null) {
            for (InvoiceItem item : items) {
                if ("SERVICE".equalsIgnoreCase(item.getItemType())) {
                    serviceTotal += item.getTotalPrice();
                } else {
                    partsTotal += item.getTotalPrice();
                }
            }
        }

        invoice.setServiceCost(serviceTotal);
        invoice.setPartsCost(partsTotal);
        double subtotal = serviceTotal + partsTotal;
        invoice.setSubtotal(subtotal);

        double gstRate = invoice.getGstRate() > 0 ? invoice.getGstRate() : 18.0;
        double gstAmount = (subtotal * gstRate) / 100.0;
        invoice.setGstAmount(gstAmount);

        double discount = Math.max(0.0, invoice.getDiscountAmount());
        double finalAmount = Math.max(0.0, (subtotal + gstAmount) - discount);
        invoice.setFinalAmount(finalAmount);

        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(invoiceDAO.getNextInvoiceNumber());
        }

        boolean ok = invoiceDAO.createInvoice(invoice, items);
        if (!ok) {
            throw new SQLException("Failed to create invoice in database.");
        }

        // Deduct parts inventory if specified
        if (partsToDeduct != null) {
            for (SparePart p : partsToDeduct) {
                inventoryDAO.updateStock(p.getPartId(), -p.getQuantity(), "OUT", invoice.getBookingId(), "Invoiced in " + invoice.getInvoiceNumber());
            }
        }
    }

    public void recordPayment(Payment payment) throws SQLException {
        if (payment.getInvoiceId() <= 0) {
            throw new IllegalArgumentException("Invalid invoice for payment.");
        }
        if (payment.getAmountPaid() <= 0.0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        boolean ok = invoiceDAO.recordPayment(payment);
        if (!ok) {
            throw new SQLException("Failed to record payment.");
        }
    }

    public void exportInvoiceToPdf(File targetFile, int invoiceId) throws SQLException, IOException {
        Invoice invoice = invoiceDAO.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice #" + invoiceId + " not found.");
        }
        Customer customer = customerDAO.findById(invoice.getCustomerId());
        Vehicle vehicle = vehicleDAO.findById(invoice.getVehicleId());
        List<InvoiceItem> items = invoiceDAO.getItemsByInvoiceId(invoiceId);

        PDFGenerator.exportInvoiceToPdf(targetFile, invoice, customer, vehicle, items);
    }

    public Invoice getInvoiceById(int invoiceId) throws SQLException {
        return invoiceDAO.findById(invoiceId);
    }

    public Invoice getInvoiceByNumber(String number) throws SQLException {
        return invoiceDAO.findByInvoiceNumber(number);
    }

    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceDAO.getAllInvoices();
    }

    public List<Invoice> searchInvoices(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllInvoices();
        }
        return invoiceDAO.searchInvoices(keyword.trim());
    }

    public List<InvoiceItem> getItems(int invoiceId) throws SQLException {
        return invoiceDAO.getItemsByInvoiceId(invoiceId);
    }

    public List<Payment> getPayments(int invoiceId) throws SQLException {
        return invoiceDAO.getPaymentsByInvoiceId(invoiceId);
    }

    public double getTotalRevenue() throws SQLException {
        return invoiceDAO.getTotalRevenue();
    }

    public Map<String, Double> getMonthlyRevenueData() throws SQLException {
        return invoiceDAO.getMonthlyRevenueData();
    }

    public String generateNextInvoiceNumber() {
        return invoiceDAO.getNextInvoiceNumber();
    }
}
