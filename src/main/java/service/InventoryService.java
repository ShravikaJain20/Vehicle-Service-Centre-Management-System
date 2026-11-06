package service;

import dao.InventoryDAO;
import model.SparePart;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public void addPart(SparePart part) throws SQLException, IllegalArgumentException {
        validatePart(part);
        boolean ok = inventoryDAO.addPart(part);
        if (!ok) {
            throw new SQLException("Failed to add spare part.");
        }
    }

    public void updatePart(SparePart part) throws SQLException, IllegalArgumentException {
        if (part.getPartId() <= 0) {
            throw new IllegalArgumentException("Invalid part ID.");
        }
        validatePart(part);
        boolean ok = inventoryDAO.updatePart(part);
        if (!ok) {
            throw new SQLException("Failed to update spare part.");
        }
    }

    public void deletePart(int partId) throws SQLException {
        boolean ok = inventoryDAO.deletePart(partId);
        if (!ok) {
            throw new SQLException("Failed to delete part. It may be referenced in transactions or invoice items.");
        }
    }

    public boolean adjustStock(int partId, int quantityChange, String transactionType, Integer serviceId, String notes) throws SQLException {
        return inventoryDAO.updateStock(partId, quantityChange, transactionType, serviceId, notes);
    }

    public SparePart getPartById(int partId) throws SQLException {
        return inventoryDAO.findById(partId);
    }

    public List<SparePart> getAllParts() throws SQLException {
        return inventoryDAO.getAllParts();
    }

    public List<SparePart> getLowStockParts() throws SQLException {
        return inventoryDAO.getLowStockParts();
    }

    public List<SparePart> searchParts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllParts();
        }
        return inventoryDAO.searchParts(keyword.trim());
    }

    private void validatePart(SparePart p) throws IllegalArgumentException {
        if (!ValidationUtil.isNotEmpty(p.getPartName())) {
            throw new IllegalArgumentException("Part name is required.");
        }
        if (!ValidationUtil.isNotEmpty(p.getPartCategory())) {
            throw new IllegalArgumentException("Part category is required.");
        }
        if (p.getQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (p.getMinThreshold() < 0) {
            throw new IllegalArgumentException("Minimum threshold cannot be negative.");
        }
        if (p.getUnitPrice() < 0.0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
    }
}
