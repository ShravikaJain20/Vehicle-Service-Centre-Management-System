package service;

import dao.CustomerDAO;
import model.Customer;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public void addCustomer(Customer customer) throws SQLException, IllegalArgumentException {
        validateCustomer(customer);

        Customer existing = customerDAO.findByPhone(customer.getPhoneNumber());
        if (existing != null) {
            throw new IllegalArgumentException("A customer with phone number " + customer.getPhoneNumber() + " already exists.");
        }

        boolean ok = customerDAO.addCustomer(customer);
        if (!ok) {
            throw new SQLException("Failed to save customer to database.");
        }
    }

    public void updateCustomer(Customer customer) throws SQLException, IllegalArgumentException {
        if (customer.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customer ID.");
        }
        validateCustomer(customer);

        Customer existing = customerDAO.findByPhone(customer.getPhoneNumber());
        if (existing != null && existing.getCustomerId() != customer.getCustomerId()) {
            throw new IllegalArgumentException("Phone number " + customer.getPhoneNumber() + " is already assigned to another customer.");
        }

        boolean ok = customerDAO.updateCustomer(customer);
        if (!ok) {
            throw new SQLException("Failed to update customer details.");
        }
    }

    public void deleteCustomer(int customerId) throws SQLException {
        boolean ok = customerDAO.deleteCustomer(customerId);
        if (!ok) {
            throw new SQLException("Could not delete customer. Check if there are active bookings or invoices linked.");
        }
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.searchCustomers(keyword.trim());
    }

    public int getTotalCount() throws SQLException {
        return customerDAO.getTotalCustomersCount();
    }

    private void validateCustomer(Customer c) throws IllegalArgumentException {
        if (!ValidationUtil.isNotEmpty(c.getFullName())) {
            throw new IllegalArgumentException("Customer full name is required.");
        }
        if (!ValidationUtil.isValidPhone(c.getPhoneNumber())) {
            throw new IllegalArgumentException("Please provide a valid phone number (at least 7 to 15 digits).");
        }
        if (ValidationUtil.isNotEmpty(c.getEmail()) && !ValidationUtil.isValidEmail(c.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }
}
