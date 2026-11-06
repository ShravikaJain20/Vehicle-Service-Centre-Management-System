package dao;

import config.DatabaseConnection;
import model.InventoryTransaction;
import model.SparePart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public boolean addPart(SparePart part) throws SQLException {
        String sql = "INSERT INTO spare_parts (part_name, part_category, quantity, min_threshold, unit_price, supplier_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, part.getPartName());
            ps.setString(2, part.getPartCategory());
            ps.setInt(3, part.getQuantity());
            ps.setInt(4, part.getMinThreshold());
            ps.setDouble(5, part.getUnitPrice());
            ps.setString(6, part.getSupplierName());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        part.setPartId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updatePart(SparePart part) throws SQLException {
        String sql = "UPDATE spare_parts SET part_name = ?, part_category = ?, quantity = ?, min_threshold = ?, unit_price = ?, supplier_name = ? WHERE part_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, part.getPartName());
            ps.setString(2, part.getPartCategory());
            ps.setInt(3, part.getQuantity());
            ps.setInt(4, part.getMinThreshold());
            ps.setDouble(5, part.getUnitPrice());
            ps.setString(6, part.getSupplierName());
            ps.setInt(7, part.getPartId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePart(int partId) throws SQLException {
        String sql = "DELETE FROM spare_parts WHERE part_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partId);
            return ps.executeUpdate() > 0;
        }
    }

    public SparePart findById(int partId) throws SQLException {
        String sql = "SELECT * FROM spare_parts WHERE part_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<SparePart> getAllParts() throws SQLException {
        List<SparePart> list = new ArrayList<>();
        String sql = "SELECT * FROM spare_parts ORDER BY part_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<SparePart> getLowStockParts() throws SQLException {
        List<SparePart> list = new ArrayList<>();
        String sql = "SELECT * FROM spare_parts WHERE quantity <= min_threshold ORDER BY quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<SparePart> searchParts(String keyword) throws SQLException {
        List<SparePart> list = new ArrayList<>();
        String sql = "SELECT * FROM spare_parts WHERE part_name LIKE ? OR part_category LIKE ? OR supplier_name LIKE ? ORDER BY part_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pat = "%" + keyword + "%";
            ps.setString(1, pat);
            ps.setString(2, pat);
            ps.setString(3, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Atomically adjusts stock quantity and logs an inventory transaction.
     */
    public boolean updateStock(int partId, int quantityChange, String transactionType, Integer relatedServiceId, String notes) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            String updateSql = "UPDATE spare_parts SET quantity = quantity + ? WHERE part_id = ? AND (quantity + ? >= 0)";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, quantityChange);
                ps.setInt(2, partId);
                ps.setInt(3, quantityChange);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false; // Insufficient stock or part not found
                }
            }

            String transSql = "INSERT INTO inventory_transactions (part_id, transaction_type, quantity, related_service_id, notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(transSql)) {
                ps.setInt(1, partId);
                ps.setString(2, transactionType);
                ps.setInt(3, Math.abs(quantityChange));
                if (relatedServiceId != null) {
                    ps.setInt(4, relatedServiceId);
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setString(5, notes);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private SparePart mapRow(ResultSet rs) throws SQLException {
        return new SparePart(
            rs.getInt("part_id"),
            rs.getString("part_name"),
            rs.getString("part_category"),
            rs.getInt("quantity"),
            rs.getInt("min_threshold"),
            rs.getDouble("unit_price"),
            rs.getString("supplier_name"),
            rs.getTimestamp("updated_at")
        );
    }
}
