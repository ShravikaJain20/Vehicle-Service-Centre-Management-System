package service;

import dao.MechanicDAO;
import model.Mechanic;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class MechanicService {
    private final MechanicDAO mechanicDAO;

    public MechanicService() {
        this.mechanicDAO = new MechanicDAO();
    }

    public void addMechanic(Mechanic mechanic) throws SQLException, IllegalArgumentException {
        validateMechanic(mechanic);
        boolean ok = mechanicDAO.addMechanic(mechanic);
        if (!ok) {
            throw new SQLException("Failed to add mechanic.");
        }
    }

    public void updateMechanic(Mechanic mechanic) throws SQLException, IllegalArgumentException {
        if (mechanic.getMechanicId() <= 0) {
            throw new IllegalArgumentException("Invalid mechanic ID.");
        }
        validateMechanic(mechanic);
        boolean ok = mechanicDAO.updateMechanic(mechanic);
        if (!ok) {
            throw new SQLException("Failed to update mechanic.");
        }
    }

    public void deleteMechanic(int mechanicId) throws SQLException {
        boolean ok = mechanicDAO.deleteMechanic(mechanicId);
        if (!ok) {
            throw new SQLException("Could not delete mechanic. They may be assigned to existing bookings.");
        }
    }

    public Mechanic getMechanicById(int mechanicId) throws SQLException {
        return mechanicDAO.findById(mechanicId);
    }

    public List<Mechanic> getAllMechanics() throws SQLException {
        return mechanicDAO.getAllMechanics();
    }

    public List<Mechanic> getAvailableMechanics() throws SQLException {
        return mechanicDAO.getAvailableMechanics();
    }

    private void validateMechanic(Mechanic m) throws IllegalArgumentException {
        if (!ValidationUtil.isNotEmpty(m.getName())) {
            throw new IllegalArgumentException("Mechanic name is required.");
        }
        if (!ValidationUtil.isValidPhone(m.getPhone())) {
            throw new IllegalArgumentException("Please provide a valid phone number.");
        }
        if (!ValidationUtil.isNotEmpty(m.getSpecialization())) {
            throw new IllegalArgumentException("Mechanic specialization is required.");
        }
    }
}
