package dao;

import config.DatabaseConnection;
import model.ServiceRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {

    public boolean addRecord(ServiceRecord record) throws SQLException {
        String sql = "INSERT INTO service_records (booking_id, vehicle_id, mechanic_id, service_date, km_reading, service_type, description, parts_replaced, total_cost, next_recommended_date, next_recommended_km) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (record.getBookingId() != null) {
                ps.setInt(1, record.getBookingId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, record.getVehicleId());
            if (record.getMechanicId() != null) {
                ps.setInt(3, record.getMechanicId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDate(4, record.getServiceDate());
            ps.setInt(5, record.getKmReading());
            ps.setString(6, record.getServiceType());
            ps.setString(7, record.getDescription());
            ps.setString(8, record.getPartsReplaced());
            ps.setDouble(9, record.getTotalCost());
            ps.setDate(10, record.getNextRecommendedDate());
            if (record.getNextRecommendedKm() != null) {
                ps.setInt(11, record.getNextRecommendedKm());
            } else {
                ps.setNull(11, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        record.setRecordId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<ServiceRecord> getRecordsByVehicleId(int vehicleId) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = getBaseSelect() + " WHERE r.vehicle_id = ? ORDER BY r.service_date DESC, r.record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ServiceRecord> getRecordsByRegistration(String reg) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = getBaseSelect() + " WHERE v.registration_number = ? ORDER BY r.service_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reg.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ServiceRecord> getAllRecords() throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = getBaseSelect() + " ORDER BY r.service_date DESC, r.record_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ServiceRecord> searchRecords(String keyword) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = getBaseSelect() +
                     " WHERE v.registration_number LIKE ? OR r.service_type LIKE ? OR c.full_name LIKE ? OR m.name LIKE ? OR r.description LIKE ? " +
                     " ORDER BY r.service_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pat = "%" + keyword + "%";
            ps.setString(1, pat);
            ps.setString(2, pat);
            ps.setString(3, pat);
            ps.setString(4, pat);
            ps.setString(5, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private String getBaseSelect() {
        return "SELECT r.*, v.registration_number, c.full_name AS customer_name, m.name AS mechanic_name " +
               "FROM service_records r " +
               "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
               "JOIN customers c ON v.customer_id = c.customer_id " +
               "LEFT JOIN mechanics m ON r.mechanic_id = m.mechanic_id ";
    }

    private ServiceRecord mapRow(ResultSet rs) throws SQLException {
        Integer bookingId = rs.getInt("booking_id");
        if (rs.wasNull()) bookingId = null;

        Integer mechanicId = rs.getInt("mechanic_id");
        if (rs.wasNull()) mechanicId = null;

        Integer nextKm = rs.getInt("next_recommended_km");
        if (rs.wasNull()) nextKm = null;

        ServiceRecord r = new ServiceRecord(
            rs.getInt("record_id"),
            bookingId,
            rs.getInt("vehicle_id"),
            mechanicId,
            rs.getDate("service_date"),
            rs.getInt("km_reading"),
            rs.getString("service_type"),
            rs.getString("description"),
            rs.getString("parts_replaced"),
            rs.getDouble("total_cost"),
            rs.getDate("next_recommended_date"),
            nextKm,
            rs.getTimestamp("created_at")
        );
        r.setVehicleRegistration(rs.getString("registration_number"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setMechanicName(rs.getString("mechanic_name"));
        return r;
    }
}
