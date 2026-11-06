package service;

import dao.VehicleDAO;
import model.Vehicle;
import util.ValidationUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VehicleService {
    private final VehicleDAO vehicleDAO;

    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
    }

    public void addVehicle(Vehicle vehicle) throws SQLException, IllegalArgumentException {
        validateVehicle(vehicle);

        Vehicle existing = vehicleDAO.findByRegistration(vehicle.getRegistrationNumber());
        if (existing != null) {
            throw new IllegalArgumentException("A vehicle with registration number '" + vehicle.getRegistrationNumber() + "' is already registered.");
        }

        boolean ok = vehicleDAO.addVehicle(vehicle);
        if (!ok) {
            throw new SQLException("Failed to save vehicle.");
        }

        // Trigger reminder check for this new vehicle
        ReminderService.getInstance().checkVehicle(vehicle);
    }

    public void updateVehicle(Vehicle vehicle) throws SQLException, IllegalArgumentException {
        if (vehicle.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Invalid vehicle ID.");
        }
        validateVehicle(vehicle);

        Vehicle existing = vehicleDAO.findByRegistration(vehicle.getRegistrationNumber());
        if (existing != null && existing.getVehicleId() != vehicle.getVehicleId()) {
            throw new IllegalArgumentException("Registration number '" + vehicle.getRegistrationNumber() + "' is assigned to another vehicle.");
        }

        boolean ok = vehicleDAO.updateVehicle(vehicle);
        if (!ok) {
            throw new SQLException("Failed to update vehicle details.");
        }

        // Re-evaluate reminders
        ReminderService.getInstance().checkVehicle(vehicle);
    }

    public void updateKilometres(int vehicleId, int newKm) throws SQLException {
        if (newKm < 0) {
            throw new IllegalArgumentException("Kilometre reading cannot be negative.");
        }
        Vehicle v = vehicleDAO.findById(vehicleId);
        if (v == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        if (newKm < v.getCurrentKm()) {
            throw new IllegalArgumentException("New kilometre reading (" + newKm + " KM) cannot be less than current odometer reading (" + v.getCurrentKm() + " KM).");
        }

        vehicleDAO.updateKilometres(vehicleId, newKm);
        v.setCurrentKm(newKm);
        ReminderService.getInstance().checkVehicle(v);
    }

    public void updateLastService(int vehicleId, Date date, int serviceKm) throws SQLException {
        vehicleDAO.updateLastService(vehicleId, date, serviceKm);
        Vehicle v = vehicleDAO.findById(vehicleId);
        if (v != null) {
            ReminderService.getInstance().checkVehicle(v);
        }
    }

    public void deleteVehicle(int vehicleId) throws SQLException {
        boolean ok = vehicleDAO.deleteVehicle(vehicleId);
        if (!ok) {
            throw new SQLException("Could not delete vehicle. Ensure there are no foreign key constraints on bookings or invoices.");
        }
    }

    public Vehicle getVehicleById(int vehicleId) throws SQLException {
        return vehicleDAO.findById(vehicleId);
    }

    public Vehicle getVehicleByRegistration(String reg) throws SQLException {
        return vehicleDAO.findByRegistration(reg);
    }

    public List<Vehicle> getVehiclesByCustomerId(int customerId) throws SQLException {
        return vehicleDAO.getByCustomerId(customerId);
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        return vehicleDAO.getAllVehicles();
    }

    public List<Vehicle> searchVehicles(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVehicles();
        }
        return vehicleDAO.searchVehicles(keyword.trim());
    }

    public int getTotalCount() throws SQLException {
        return vehicleDAO.getTotalVehiclesCount();
    }

    private void validateVehicle(Vehicle v) throws IllegalArgumentException {
        if (v.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Please select an owner (customer) for the vehicle.");
        }
        if (!ValidationUtil.isValidRegistrationNumber(v.getRegistrationNumber())) {
            throw new IllegalArgumentException("Please provide a valid registration number (e.g. KA-01-MJ-4589).");
        }
        if (!ValidationUtil.isNotEmpty(v.getBrand())) {
            throw new IllegalArgumentException("Vehicle brand is required.");
        }
        if (!ValidationUtil.isNotEmpty(v.getModel())) {
            throw new IllegalArgumentException("Vehicle model is required.");
        }
        int currentYear = LocalDate.now().getYear();
        if (v.getManufacturingYear() < 1980 || v.getManufacturingYear() > currentYear + 1) {
            throw new IllegalArgumentException("Manufacturing year must be between 1980 and " + (currentYear + 1) + ".");
        }
        if (v.getCurrentKm() < 0) {
            throw new IllegalArgumentException("Current kilometre reading cannot be negative.");
        }
        if (v.getLastServiceKm() < 0) {
            throw new IllegalArgumentException("Last service kilometre reading cannot be negative.");
        }
    }
}
