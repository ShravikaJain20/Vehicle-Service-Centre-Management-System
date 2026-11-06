package dao;

import config.DatabaseConnection;
import model.Mechanic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MechanicDAO {

    public boolean addMechanic(Mechanic mechanic) throws SQLException {
        String sql = "INSERT INTO mechanics (name, phone, specialization, status, active_jobs_count) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mechanic.getName());
            ps.setString(2, mechanic.getPhone());
            ps.setString(3, mechanic.getSpecialization());
            ps.setString(4, mechanic.getStatus() != null ? mechanic.getStatus() : "Available");
            ps.setInt(5, mechanic.getActiveJobsCount());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        mechanic.setMechanicId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateMechanic(Mechanic mechanic) throws SQLException {
        String sql = "UPDATE mechanics SET name = ?, phone = ?, specialization = ?, status = ?, active_jobs_count = ? WHERE mechanic_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mechanic.getName());
            ps.setString(2, mechanic.getPhone());
            ps.setString(3, mechanic.getSpecialization());
            ps.setString(4, mechanic.getStatus());
            ps.setInt(5, mechanic.getActiveJobsCount());
            ps.setInt(6, mechanic.getMechanicId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteMechanic(int mechanicId) throws SQLException {
        String sql = "DELETE FROM mechanics WHERE mechanic_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mechanicId);
            return ps.executeUpdate() > 0;
        }
    }

    public Mechanic findById(int mechanicId) throws SQLException {
        String sql = "SELECT * FROM mechanics WHERE mechanic_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mechanicId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Mechanic> getAllMechanics() throws SQLException {
        List<Mechanic> list = new ArrayList<>();
        String sql = "SELECT * FROM mechanics ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Mechanic> getAvailableMechanics() throws SQLException {
        List<Mechanic> list = new ArrayList<>();
        String sql = "SELECT * FROM mechanics WHERE status = 'Available' ORDER BY active_jobs_count ASC, name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void incrementActiveJobs(int mechanicId) throws SQLException {
        String sql = "UPDATE mechanics SET active_jobs_count = active_jobs_count + 1, " +
                     "status = CASE WHEN active_jobs_count + 1 >= 3 THEN 'Busy' ELSE status END " +
                     "WHERE mechanic_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mechanicId);
            ps.executeUpdate();
        }
    }

    public void decrementActiveJobs(int mechanicId) throws SQLException {
        String sql = "UPDATE mechanics SET active_jobs_count = GREATEST(0, active_jobs_count - 1), " +
                     "status = CASE WHEN active_jobs_count - 1 < 3 AND status = 'Busy' THEN 'Available' ELSE status END " +
                     "WHERE mechanic_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mechanicId);
            ps.executeUpdate();
        }
    }

    private Mechanic mapRow(ResultSet rs) throws SQLException {
        return new Mechanic(
            rs.getInt("mechanic_id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("specialization"),
            rs.getString("status"),
            rs.getInt("active_jobs_count"),
            rs.getTimestamp("created_at")
        );
    }
}
