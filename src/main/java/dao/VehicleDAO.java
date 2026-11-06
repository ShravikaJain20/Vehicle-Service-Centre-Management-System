package dao;

import config.DatabaseConnection;
import model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public boolean addVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (customer_id, registration_number, brand, model, vehicle_type, manufacturing_year, current_km, last_service_date, last_service_km) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehicle.getCustomerId());
            ps.setString(2, vehicle.getRegistrationNumber());
            ps.setString(3, vehicle.getBrand());
            ps.setString(4, vehicle.getModel());
            ps.setString(5, vehicle.getVehicleType());
            ps.setInt(6, vehicle.getManufacturingYear());
            ps.setInt(7, vehicle.getCurrentKm());
            ps.setDate(8, vehicle.getLastServiceDate());
            ps.setInt(9, vehicle.getLastServiceKm());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        vehicle.setVehicleId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateVehicle(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET customer_id = ?, registration_number = ?, brand = ?, model = ?, vehicle_type = ?, manufacturing_year = ?, current_km = ?, last_service_date = ?, last_service_km = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicle.getCustomerId());
            ps.setString(2, vehicle.getRegistrationNumber());
            ps.setString(3, vehicle.getBrand());
            ps.setString(4, vehicle.getModel());
            ps.setString(5, vehicle.getVehicleType());
            ps.setInt(6, vehicle.getManufacturingYear());
            ps.setInt(7, vehicle.getCurrentKm());
            ps.setDate(8, vehicle.getLastServiceDate());
            ps.setInt(9, vehicle.getLastServiceKm());
            ps.setInt(10, vehicle.getVehicleId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    public Vehicle findById(int vehicleId) throws SQLException {
        String sql = "SELECT v.*, c.full_name as customer_name, c.phone_number as customer_phone " +
                     "FROM vehicles v " +
                     "LEFT JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE v.vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithCustomer(rs);
                }
            }
        }
        return null;
    }

    public Vehicle findByRegistration(String reg) throws SQLException {
        String sql = "SELECT v.*, c.full_name as customer_name, c.phone_number as customer_phone " +
                     "FROM vehicles v " +
                     "LEFT JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE v.registration_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reg.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithCustomer(rs);
                }
            }
        }
        return null;
    }

    public List<Vehicle> getByCustomerId(int customerId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, c.full_name as customer_name, c.phone_number as customer_phone " +
                     "FROM vehicles v " +
                     "LEFT JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE v.customer_id = ? ORDER BY v.registration_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithCustomer(rs));
                }
            }
        }
        return list;
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, c.full_name as customer_name, c.phone_number as customer_phone " +
                     "FROM vehicles v " +
                     "LEFT JOIN customers c ON v.customer_id = c.customer_id " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithCustomer(rs));
            }
        }
        return list;
    }

    public List<Vehicle> searchVehicles(String keyword) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, c.full_name as customer_name, c.phone_number as customer_phone " +
                     "FROM vehicles v " +
                     "LEFT JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE v.registration_number LIKE ? OR v.brand LIKE ? OR v.model LIKE ? OR v.vehicle_type LIKE ? OR c.full_name LIKE ? " +
                     "ORDER BY v.registration_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithCustomer(rs));
                }
            }
        }
        return list;
    }

    public boolean updateKilometres(int vehicleId, int newKm) throws SQLException {
        String sql = "UPDATE vehicles SET current_km = ? WHERE vehicle_id = ? AND current_km <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newKm);
            ps.setInt(2, vehicleId);
            ps.setInt(3, newKm);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateLastService(int vehicleId, Date serviceDate, int serviceKm) throws SQLException {
        String sql = "UPDATE vehicles SET last_service_date = ?, last_service_km = ?, current_km = GREATEST(current_km, ?) WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, serviceDate);
            ps.setInt(2, serviceKm);
            ps.setInt(3, serviceKm);
            ps.setInt(4, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getTotalVehiclesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Vehicle mapRowWithCustomer(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle(
            rs.getInt("vehicle_id"),
            rs.getInt("customer_id"),
            rs.getString("registration_number"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getString("vehicle_type"),
            rs.getInt("manufacturing_year"),
            rs.getInt("current_km"),
            rs.getDate("last_service_date"),
            rs.getInt("last_service_km"),
            rs.getTimestamp("created_at")
        );
        try {
            v.setCustomerName(rs.getString("customer_name"));
            v.setCustomerPhone(rs.getString("customer_phone"));
        } catch (SQLException ignored) {}
        return v;
    }
}
