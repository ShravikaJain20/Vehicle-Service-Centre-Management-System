package dao;

import config.DatabaseConnection;
import model.ServiceBooking;
import model.ServiceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean addBooking(ServiceBooking booking) throws SQLException {
        String sql = "INSERT INTO service_bookings (customer_id, vehicle_id, service_type_id, mechanic_id, booking_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getVehicleId());
            ps.setInt(3, booking.getServiceTypeId());
            if (booking.getMechanicId() != null) {
                ps.setInt(4, booking.getMechanicId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDate(5, booking.getBookingDate());
            ps.setString(6, booking.getStatus() != null ? booking.getStatus() : "Pending");
            ps.setString(7, booking.getNotes());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateBooking(ServiceBooking booking) throws SQLException {
        String sql = "UPDATE service_bookings SET customer_id = ?, vehicle_id = ?, service_type_id = ?, mechanic_id = ?, booking_date = ?, status = ?, notes = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getVehicleId());
            ps.setInt(3, booking.getServiceTypeId());
            if (booking.getMechanicId() != null) {
                ps.setInt(4, booking.getMechanicId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDate(5, booking.getBookingDate());
            ps.setString(6, booking.getStatus());
            ps.setString(7, booking.getNotes());
            ps.setInt(8, booking.getBookingId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int bookingId, String newStatus) throws SQLException {
        String sql = "UPDATE service_bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM service_bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public ServiceBooking findById(int bookingId) throws SQLException {
        String sql = getBaseSelectQuery() + " WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ServiceBooking> getAllBookings() throws SQLException {
        List<ServiceBooking> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " ORDER BY b.booking_date DESC, b.booking_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ServiceBooking> searchBookings(String keyword) throws SQLException {
        List<ServiceBooking> list = new ArrayList<>();
        String sql = getBaseSelectQuery() +
                     " WHERE c.full_name LIKE ? OR v.registration_number LIKE ? OR st.name LIKE ? OR m.name LIKE ? OR b.status LIKE ? " +
                     " ORDER BY b.booking_date DESC";
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

    public List<ServiceBooking> getBookingsByStatus(String status) throws SQLException {
        List<ServiceBooking> list = new ArrayList<>();
        String sql = getBaseSelectQuery() + " WHERE b.status = ? ORDER BY b.booking_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ServiceType> getAllServiceTypes() throws SQLException {
        List<ServiceType> list = new ArrayList<>();
        String sql = "SELECT * FROM service_types ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ServiceType(
                    rs.getInt("service_type_id"),
                    rs.getString("name"),
                    rs.getDouble("base_price"),
                    rs.getDouble("estimated_hours"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    public ServiceType getServiceTypeById(int id) throws SQLException {
        String sql = "SELECT * FROM service_types WHERE service_type_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ServiceType(
                        rs.getInt("service_type_id"),
                        rs.getString("name"),
                        rs.getDouble("base_price"),
                        rs.getDouble("estimated_hours"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public int getTotalBookingsCount() throws SQLException {
        return getCount("SELECT COUNT(*) FROM service_bookings");
    }

    public int getPendingBookingsCount() throws SQLException {
        return getCount("SELECT COUNT(*) FROM service_bookings WHERE status IN ('Pending', 'Confirmed', 'In Progress')");
    }

    public int getCompletedBookingsCount() throws SQLException {
        return getCount("SELECT COUNT(*) FROM service_bookings WHERE status = 'Completed'");
    }

    private int getCount(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private String getBaseSelectQuery() {
        return "SELECT b.*, c.full_name AS customer_name, v.registration_number, st.name AS service_type_name, m.name AS mechanic_name " +
               "FROM service_bookings b " +
               "JOIN customers c ON b.customer_id = c.customer_id " +
               "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
               "JOIN service_types st ON b.service_type_id = st.service_type_id " +
               "LEFT JOIN mechanics m ON b.mechanic_id = m.mechanic_id ";
    }

    private ServiceBooking mapRow(ResultSet rs) throws SQLException {
        Integer mechId = rs.getInt("mechanic_id");
        if (rs.wasNull()) mechId = null;

        ServiceBooking b = new ServiceBooking(
            rs.getInt("booking_id"),
            rs.getInt("customer_id"),
            rs.getInt("vehicle_id"),
            rs.getInt("service_type_id"),
            mechId,
            rs.getDate("booking_date"),
            rs.getString("status"),
            rs.getString("notes"),
            rs.getTimestamp("created_at")
        );
        b.setCustomerName(rs.getString("customer_name"));
        b.setVehicleRegistration(rs.getString("registration_number"));
        b.setServiceTypeName(rs.getString("service_type_name"));
        b.setMechanicName(rs.getString("mechanic_name"));
        return b;
    }
}
