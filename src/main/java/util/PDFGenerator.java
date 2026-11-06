package util;

import model.Customer;
import model.Invoice;
import model.InvoiceItem;
import model.Vehicle;

import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Generates formatted PDF service invoices and provides direct printing capabilities.
 * Pure Java PDF generation with zero external dependencies, plus standard AWT Printable integration.
 */
public class PDFGenerator {

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("INR #,##0.00");

    /**
     * Exports a complete invoice to a valid standard PDF 1.4 file.
     */
    public static void exportInvoiceToPdf(File targetFile, Invoice invoice, Customer customer, Vehicle vehicle, List<InvoiceItem> items) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            writePdfDocument(fos, invoice, customer, vehicle, items);
        }
    }

    /**
     * Opens standard system print dialog to print or save via "Microsoft Print to PDF".
     */
    public static void printInvoice(Component parent, Invoice invoice, Customer customer, Vehicle vehicle, List<InvoiceItem> items) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Invoice_" + invoice.getInvoiceNumber());

        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        // A4 Paper in 1/72 inch points
        double width = 595.27559;
        double height = 841.88976;
        paper.setSize(width, height);
        paper.setImageableArea(36, 36, width - 72, height - 72);
        pf.setPaper(paper);

        job.setPrintable(new InvoicePrintable(invoice, customer, vehicle, items), pf);

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parent, "Invoice sent to printer successfully!", "Print Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(parent, "Printing failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Internal standard PDF 1.4 builder that outputs valid PDF byte streams.
     */
    private static void writePdfDocument(OutputStream out, Invoice invoice, Customer customer, Vehicle vehicle, List<InvoiceItem> items) throws IOException {
        List<Long> offsets = new ArrayList<>();
        long byteCount = 0;

        // PDF Header
        byte[] header = "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(header);
        byteCount += header.length;

        // Build content stream
        StringBuilder cs = new StringBuilder();
        cs.append("q\n");

        // Header Background Banner (Dark Navy)
        cs.append("0.058 0.090 0.168 rg\n"); // #0F172B
        cs.append("36 730 523 75 re f\n");

        // Header Text (White)
        cs.append("1 1 1 rg\n");
        cs.append("BT /F2 18 Tf 50 772 Td (AUTO SERVICE CENTRE MANAGEMENT SYSTEM) Tj ET\n");
        cs.append("BT /F1 10 Tf 50 754 Td (Premium Automobile Maintenance, Diagnostic & Care Hub) Tj ET\n");
        cs.append("BT /F1 9 Tf 50 740 Td (Tel: +91 80 4455 6677 | Email: service@autocentre.com | GSTIN: 29AABCS1429B1Z) Tj ET\n");

        // Invoice Title & Status Badge
        cs.append("0 0 0 rg\n");
        cs.append("BT /F2 16 Tf 36 700 Td (TAX INVOICE) Tj ET\n");
        cs.append("BT /F2 11 Tf 400 700 Td (Invoice #: ").append(sanitize(invoice.getInvoiceNumber())).append(") Tj ET\n");
        cs.append("BT /F1 10 Tf 400 686 Td (Date: ").append(sanitize(DateUtil.formatDateTime(invoice.getCreatedAt()))).append(") Tj ET\n");
        cs.append("BT /F2 10 Tf 400 672 Td (Status: ").append(sanitize(invoice.getPaymentStatus())).append(") Tj ET\n");

        // Customer & Vehicle Details Box
        cs.append("0.94 0.96 0.98 rg\n"); // Light slate background
        cs.append("36 600 523 60 re f\n");
        cs.append("0.8 0.85 0.90 RG 1 w\n");
        cs.append("36 600 523 60 re s\n");

        cs.append("0.1 0.1 0.1 rg\n");
        cs.append("BT /F2 10 Tf 46 645 Td (CUSTOMER DETAILS) Tj ET\n");
        cs.append("BT /F1 9 Tf 46 630 Td (Name: ").append(sanitize(customer != null ? customer.getFullName() : "N/A")).append(") Tj ET\n");
        cs.append("BT /F1 9 Tf 46 615 Td (Phone: ").append(sanitize(customer != null ? customer.getPhoneNumber() : "N/A")).append(") Tj ET\n");

        cs.append("BT /F2 10 Tf 300 645 Td (VEHICLE DETAILS) Tj ET\n");
        String vehStr = vehicle != null ? (vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getRegistrationNumber() + ")") : "N/A";
        cs.append("BT /F1 9 Tf 300 630 Td (Vehicle: ").append(sanitize(vehStr)).append(") Tj ET\n");
        String kmStr = vehicle != null ? ("Odometer: " + vehicle.getCurrentKm() + " KM") : "";
        cs.append("BT /F1 9 Tf 300 615 Td (").append(sanitize(kmStr)).append(") Tj ET\n");

        // Table Header
        int tableTop = 570;
        cs.append("0.14 0.38 0.92 rg\n"); // Electric Blue
        cs.append("36 ").append(tableTop).append(" 523 20 re f\n");

        cs.append("1 1 1 rg\n");
        cs.append("BT /F2 9 Tf 45 ").append(tableTop + 6).append(" Td (#) Tj ET\n");
        cs.append("BT /F2 9 Tf 70 ").append(tableTop + 6).append(" Td (Description) Tj ET\n");
        cs.append("BT /F2 9 Tf 320 ").append(tableTop + 6).append(" Td (Type) Tj ET\n");
        cs.append("BT /F2 9 Tf 380 ").append(tableTop + 6).append(" Td (Qty) Tj ET\n");
        cs.append("BT /F2 9 Tf 430 ").append(tableTop + 6).append(" Td (Unit Price) Tj ET\n");
        cs.append("BT /F2 9 Tf 505 ").append(tableTop + 6).append(" Td (Total) Tj ET\n");

        // Table Rows
        int y = tableTop - 18;
        int idx = 1;
        if (items != null) {
            for (InvoiceItem item : items) {
                if (idx % 2 == 0) {
                    cs.append("0.97 0.98 0.99 rg 36 ").append(y - 3).append(" 523 16 re f\n");
                }
                cs.append("0.2 0.2 0.2 rg\n");
                cs.append("BT /F1 9 Tf 45 ").append(y).append(" Td (").append(idx++).append(") Tj ET\n");
                cs.append("BT /F1 9 Tf 70 ").append(y).append(" Td (").append(sanitize(item.getDescription())).append(") Tj ET\n");
                cs.append("BT /F1 9 Tf 320 ").append(y).append(" Td (").append(sanitize(item.getItemType())).append(") Tj ET\n");
                cs.append("BT /F1 9 Tf 385 ").append(y).append(" Td (").append(item.getQuantity()).append(") Tj ET\n");
                cs.append("BT /F1 9 Tf 430 ").append(y).append(" Td (").append(sanitize(String.format("%.2f", item.getUnitPrice()))).append(") Tj ET\n");
                cs.append("BT /F1 9 Tf 505 ").append(y).append(" Td (").append(sanitize(String.format("%.2f", item.getTotalPrice()))).append(") Tj ET\n");
                y -= 18;
                if (y < 200) break; // Keep within single page
            }
        }

        // Summary Section
        int sumY = Math.min(y - 10, 320);
        cs.append("0.8 0.8 0.8 RG 1 w\n");
        cs.append("300 ").append(sumY + 15).append(" m 559 ").append(sumY + 15).append(" l S\n");

        cs.append("0.2 0.2 0.2 rg\n");
        cs.append("BT /F1 9 Tf 320 ").append(sumY).append(" Td (Service Labor Charges:) Tj ET\n");
        cs.append("BT /F1 9 Tf 490 ").append(sumY).append(" Td (").append(sanitize(String.format("INR %.2f", invoice.getServiceCost()))).append(") Tj ET\n");
        sumY -= 15;

        cs.append("BT /F1 9 Tf 320 ").append(sumY).append(" Td (Spare Parts Total:) Tj ET\n");
        cs.append("BT /F1 9 Tf 490 ").append(sumY).append(" Td (").append(sanitize(String.format("INR %.2f", invoice.getPartsCost()))).append(") Tj ET\n");
        sumY -= 15;

        cs.append("BT /F1 9 Tf 320 ").append(sumY).append(" Td (Subtotal:) Tj ET\n");
        cs.append("BT /F1 9 Tf 490 ").append(sumY).append(" Td (").append(sanitize(String.format("INR %.2f", invoice.getSubtotal()))).append(") Tj ET\n");
        sumY -= 15;

        cs.append("BT /F1 9 Tf 320 ").append(sumY).append(" Td (GST (").append(String.format("%.0f", invoice.getGstRate())).append("%):) Tj ET\n");
        cs.append("BT /F1 9 Tf 490 ").append(sumY).append(" Td (").append(sanitize(String.format("INR %.2f", invoice.getGstAmount()))).append(") Tj ET\n");
        sumY -= 15;

        if (invoice.getDiscountAmount() > 0) {
            cs.append("BT /F1 9 Tf 320 ").append(sumY).append(" Td (Discount Applied:) Tj ET\n");
            cs.append("BT /F1 9 Tf 490 ").append(sumY).append(" Td (- ").append(sanitize(String.format("INR %.2f", invoice.getDiscountAmount()))).append(") Tj ET\n");
            sumY -= 15;
        }

        // Final Grand Total Highlight Box
        cs.append("0.058 0.090 0.168 rg\n");
        cs.append("300 ").append(sumY - 8).append(" 259 26 re f\n");
        cs.append("1 1 1 rg\n");
        cs.append("BT /F2 11 Tf 315 ").append(sumY).append(" Td (GRAND TOTAL:) Tj ET\n");
        cs.append("BT /F2 11 Tf 460 ").append(sumY).append(" Td (").append(sanitize(CURRENCY_FMT.format(invoice.getFinalAmount()))).append(") Tj ET\n");

        // Footer / Terms
        cs.append("0.4 0.4 0.4 rg\n");
        cs.append("BT /F2 9 Tf 36 90 Td (Terms & Conditions:) Tj ET\n");
        cs.append("BT /F1 8 Tf 36 78 Td (1. All services and genuine spare parts carry standard 90 days / 3000 KM warranty.) Tj ET\n");
        cs.append("BT /F1 8 Tf 36 68 Td (2. Payment due upon presentation of invoice. Thank you for your business!) Tj ET\n");

        cs.append("0.8 0.8 0.8 RG 1 w\n");
        cs.append("400 80 m 540 80 l S\n");
        cs.append("BT /F2 8 Tf 420 68 Td (Authorized Signatory) Tj ET\n");

        cs.append("Q\n");

        byte[] contentBytes = cs.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Object 1: Catalog
        offsets.add(byteCount);
        byte[] obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj1);
        byteCount += obj1.length;

        // Object 2: Pages
        offsets.add(byteCount);
        byte[] obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj2);
        byteCount += obj2.length;

        // Object 3: Page
        offsets.add(byteCount);
        byte[] obj3 = ("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.28 841.89] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj3);
        byteCount += obj3.length;

        // Object 4: Contents
        offsets.add(byteCount);
        byte[] obj4Header = ("4 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj4Header);
        byteCount += obj4Header.length;
        out.write(contentBytes);
        byteCount += contentBytes.length;
        byte[] obj4Footer = "\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj4Footer);
        byteCount += obj4Footer.length;

        // Object 5: Standard Font Helvetica
        offsets.add(byteCount);
        byte[] obj5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj5);
        byteCount += obj5.length;

        // Object 6: Standard Font Helvetica-Bold
        offsets.add(byteCount);
        byte[] obj6 = "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
        out.write(obj6);
        byteCount += obj6.length;

        // Cross-Reference Table
        long startXref = byteCount;
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 7\n0000000000 65535 f \n");
        for (long off : offsets) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n").append(startXref).append("\n%%EOF\n");
        byte[] xrefBytes = xref.toString().getBytes(StandardCharsets.ISO_8859_1);
        out.write(xrefBytes);
    }

    private static String sanitize(String text) {
        if (text == null) return "";
        return text.replace("(", "[").replace(")", "]").replace("\\", "/");
    }

    /**
     * Printable implementation for direct AWT printing.
     */
    private static class InvoicePrintable implements Printable {
        private final Invoice invoice;
        private final Customer customer;
        private final Vehicle vehicle;
        private final List<InvoiceItem> items;

        public InvoicePrintable(Invoice invoice, Customer customer, Vehicle vehicle, List<InvoiceItem> items) {
            this.invoice = invoice;
            this.customer = customer;
            this.vehicle = vehicle;
            this.items = items;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) return NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Banner
            g2.setColor(new Color(15, 23, 42));
            g2.fillRect(0, 0, (int) pageFormat.getImageableWidth(), 65);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("AUTO SERVICE CENTRE MANAGEMENT SYSTEM", 15, 28);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Premium Automobile Care & Service Management | Tel: +91 80 4455 6677", 15, 45);

            // Invoice Title & Info
            int y = 90;
            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("TAX INVOICE", 15, y);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Invoice #: " + invoice.getInvoiceNumber(), 340, y);
            y += 15;
            g2.drawString("Date: " + DateUtil.formatDateTime(invoice.getCreatedAt()), 340, y);
            y += 15;
            g2.drawString("Status: " + invoice.getPaymentStatus(), 340, y);

            // Customer & Vehicle Box
            y += 15;
            g2.setColor(new Color(241, 245, 249));
            g2.fillRect(15, y, (int) pageFormat.getImageableWidth() - 30, 50);
            g2.setColor(new Color(203, 213, 225));
            g2.drawRect(15, y, (int) pageFormat.getImageableWidth() - 30, 50);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("CUSTOMER:", 25, y + 18);
            g2.drawString("VEHICLE:", 270, y + 18);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.drawString(customer != null ? customer.getFullName() + " (" + customer.getPhoneNumber() + ")" : "N/A", 25, y + 34);
            g2.drawString(vehicle != null ? vehicle.getBrand() + " " + vehicle.getModel() + " [" + vehicle.getRegistrationNumber() + "]" : "N/A", 270, y + 34);

            // Table Header
            y += 70;
            g2.setColor(new Color(37, 99, 235));
            g2.fillRect(15, y, (int) pageFormat.getImageableWidth() - 30, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("#", 20, y + 14);
            g2.drawString("Item Description", 45, y + 14);
            g2.drawString("Type", 260, y + 14);
            g2.drawString("Qty", 320, y + 14);
            g2.drawString("Unit Price", 360, y + 14);
            g2.drawString("Total", 430, y + 14);

            // Table Rows
            y += 20;
            int idx = 1;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            if (items != null) {
                for (InvoiceItem item : items) {
                    if (idx % 2 == 0) {
                        g2.setColor(new Color(248, 250, 252));
                        g2.fillRect(15, y, (int) pageFormat.getImageableWidth() - 30, 18);
                    }
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(String.valueOf(idx++), 20, y + 13);
                    g2.drawString(item.getDescription(), 45, y + 13);
                    g2.drawString(item.getItemType(), 260, y + 13);
                    g2.drawString(String.valueOf(item.getQuantity()), 325, y + 13);
                    g2.drawString(String.format("%.2f", item.getUnitPrice()), 365, y + 13);
                    g2.drawString(String.format("%.2f", item.getTotalPrice()), 435, y + 13);
                    y += 18;
                }
            }

            // Summary Totals
            y += 20;
            g2.setColor(Color.BLACK);
            g2.drawString("Subtotal: INR " + String.format("%.2f", invoice.getSubtotal()), 330, y);
            y += 15;
            g2.drawString("GST (" + String.format("%.0f", invoice.getGstRate()) + "%): INR " + String.format("%.2f", invoice.getGstAmount()), 330, y);
            y += 15;
            if (invoice.getDiscountAmount() > 0) {
                g2.drawString("Discount: -INR " + String.format("%.2f", invoice.getDiscountAmount()), 330, y);
                y += 15;
            }

            g2.setColor(new Color(15, 23, 42));
            g2.fillRect(320, y, 170, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("TOTAL: INR " + String.format("%.2f", invoice.getFinalAmount()), 330, y + 16);

            return PAGE_EXISTS;
        }
    }
}
