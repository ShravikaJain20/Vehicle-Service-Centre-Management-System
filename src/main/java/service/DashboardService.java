package service;

import dao.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {
    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;
    private final InvoiceDAO invoiceDAO;
    private final ReminderDAO reminderDAO;
    private final InventoryDAO inventoryDAO;

    public DashboardService() {
        this.customerDAO = new CustomerDAO();
        this.vehicleDAO = new VehicleDAO();
        this.bookingDAO = new BookingDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.reminderDAO = new ReminderDAO();
        this.inventoryDAO = new InventoryDAO();
    }

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        try {
            stats.totalCustomers = customerDAO.getTotalCustomersCount();
            stats.totalVehicles = vehicleDAO.getTotalVehiclesCount();
            stats.totalBookings = bookingDAO.getTotalBookingsCount();
            stats.pendingServices = bookingDAO.getPendingBookingsCount();
            stats.completedServices = bookingDAO.getCompletedBookingsCount();
            stats.totalRevenue = invoiceDAO.getTotalRevenue();
            stats.activeReminders = reminderDAO.getActiveReminderCount();
            stats.overdueReminders = reminderDAO.getOverdueCount();
            stats.lowStockParts = inventoryDAO.getLowStockParts().size();
            stats.monthlyRevenue = invoiceDAO.getMonthlyRevenueData();

            // Service status distribution
            Map<String, Integer> statusMap = new LinkedHashMap<>();
            statusMap.put("Completed", stats.completedServices);
            statusMap.put("Pending/Active", stats.pendingServices);
            stats.serviceStatusDistribution = statusMap;
        } catch (SQLException e) {
            System.err.println("Error fetching dashboard statistics: " + e.getMessage());
        }
        return stats;
    }

    public static class DashboardStats {
        public int totalCustomers = 0;
        public int totalVehicles = 0;
        public int totalBookings = 0;
        public int pendingServices = 0;
        public int completedServices = 0;
        public double totalRevenue = 0.0;
        public int activeReminders = 0;
        public int overdueReminders = 0;
        public int lowStockParts = 0;
        public Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        public Map<String, Integer> serviceStatusDistribution = new LinkedHashMap<>();
    }
}
