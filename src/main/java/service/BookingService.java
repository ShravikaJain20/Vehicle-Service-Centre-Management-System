package service;

import dao.BookingDAO;
import dao.MechanicDAO;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import model.ServiceBooking;
import model.ServiceRecord;
import model.ServiceType;
import model.Vehicle;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO;
    private final MechanicDAO mechanicDAO;
    private final VehicleDAO vehicleDAO;
    private final ServiceRecordDAO serviceRecordDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.mechanicDAO = new MechanicDAO();
        this.vehicleDAO = new VehicleDAO();
        this.serviceRecordDAO = new ServiceRecordDAO();
    }

    public void createBooking(ServiceBooking booking) throws SQLException, IllegalArgumentException {
        validateBooking(booking);

        boolean ok = bookingDAO.addBooking(booking);
        if (!ok) {
            throw new SQLException("Failed to create booking.");
        }

        if (booking.getMechanicId() != null && booking.getMechanicId() > 0) {
            mechanicDAO.incrementActiveJobs(booking.getMechanicId());
        }
    }

    public void updateBooking(ServiceBooking booking) throws SQLException, IllegalArgumentException {
        if (booking.getBookingId() <= 0) {
            throw new IllegalArgumentException("Invalid booking ID.");
        }
        validateBooking(booking);

        ServiceBooking old = bookingDAO.findById(booking.getBookingId());
        boolean ok = bookingDAO.updateBooking(booking);
        if (!ok) {
            throw new SQLException("Failed to update booking.");
        }

        // Adjust mechanic load if mechanic changed
        if (old != null && old.getMechanicId() != null && !old.getMechanicId().equals(booking.getMechanicId())) {
            mechanicDAO.decrementActiveJobs(old.getMechanicId());
            if (booking.getMechanicId() != null && booking.getMechanicId() > 0) {
                mechanicDAO.incrementActiveJobs(booking.getMechanicId());
            }
        }
    }

    public void updateBookingStatus(int bookingId, String newStatus) throws SQLException {
        ServiceBooking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found.");
        }

        String oldStatus = booking.getStatus();
        boolean ok = bookingDAO.updateStatus(bookingId, newStatus);
        if (!ok) {
            throw new SQLException("Failed to update booking status.");
        }

        // When booking is completed
        if ("Completed".equalsIgnoreCase(newStatus) && !"Completed".equalsIgnoreCase(oldStatus)) {
            if (booking.getMechanicId() != null && booking.getMechanicId() > 0) {
                mechanicDAO.decrementActiveJobs(booking.getMechanicId());
            }

            // Update vehicle last service record and date
            Vehicle v = vehicleDAO.findById(booking.getVehicleId());
            if (v != null) {
                vehicleDAO.updateLastService(v.getVehicleId(), Date.valueOf(LocalDate.now()), v.getCurrentKm());

                // Create a service record in history
                ServiceRecord record = new ServiceRecord();
                record.setBookingId(bookingId);
                record.setVehicleId(v.getVehicleId());
                record.setMechanicId(booking.getMechanicId());
                record.setServiceDate(Date.valueOf(LocalDate.now()));
                record.setKmReading(v.getCurrentKm());
                record.setServiceType(booking.getServiceTypeName() != null ? booking.getServiceTypeName() : "General Servicing");
                record.setDescription("Completed service under booking #" + bookingId + ". " + (booking.getNotes() != null ? booking.getNotes() : ""));
                record.setPartsReplaced("Standard consumables");
                record.setTotalCost(1500.00);
                record.setNextRecommendedDate(Date.valueOf(LocalDate.now().plusMonths(6)));
                record.setNextRecommendedKm(v.getCurrentKm() + 5000);
                serviceRecordDAO.addRecord(record);

                // Auto-resolve any active reminders for this vehicle!
                ReminderService.getInstance().resolveRemindersForVehicle(v.getVehicleId());
            }
        } else if ("Cancelled".equalsIgnoreCase(newStatus) && !"Cancelled".equalsIgnoreCase(oldStatus)) {
            if (booking.getMechanicId() != null && booking.getMechanicId() > 0) {
                mechanicDAO.decrementActiveJobs(booking.getMechanicId());
            }
        }
    }

    public void deleteBooking(int bookingId) throws SQLException {
        ServiceBooking b = bookingDAO.findById(bookingId);
        if (b != null && b.getMechanicId() != null && !"Completed".equalsIgnoreCase(b.getStatus()) && !"Cancelled".equalsIgnoreCase(b.getStatus())) {
            mechanicDAO.decrementActiveJobs(b.getMechanicId());
        }
        boolean ok = bookingDAO.deleteBooking(bookingId);
        if (!ok) {
            throw new SQLException("Failed to delete booking.");
        }
    }

    public ServiceBooking getBookingById(int bookingId) throws SQLException {
        return bookingDAO.findById(bookingId);
    }

    public List<ServiceBooking> getAllBookings() throws SQLException {
        return bookingDAO.getAllBookings();
    }

    public List<ServiceBooking> searchBookings(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBookings();
        }
        return bookingDAO.searchBookings(keyword.trim());
    }

    public List<ServiceBooking> getBookingsByStatus(String status) throws SQLException {
        return bookingDAO.getBookingsByStatus(status);
    }

    public List<ServiceType> getAllServiceTypes() throws SQLException {
        return bookingDAO.getAllServiceTypes();
    }

    public int getTotalCount() throws SQLException {
        return bookingDAO.getTotalBookingsCount();
    }

    public int getPendingCount() throws SQLException {
        return bookingDAO.getPendingBookingsCount();
    }

    public int getCompletedCount() throws SQLException {
        return bookingDAO.getCompletedBookingsCount();
    }

    private void validateBooking(ServiceBooking b) throws IllegalArgumentException {
        if (b.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Customer is required for booking.");
        }
        if (b.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Vehicle is required for booking.");
        }
        if (b.getServiceTypeId() <= 0) {
            throw new IllegalArgumentException("Please select a service type.");
        }
        if (b.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date is required.");
        }
    }
}
