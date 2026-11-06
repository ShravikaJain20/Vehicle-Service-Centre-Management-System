-- =====================================================================
-- VEHICLE SERVICE CENTRE MANAGEMENT SYSTEM
-- Database Schema Script
-- Database: vehicle_service_management
-- Author: Google Deepmind Antigravity Pair Programmer
-- Compatible with: MySQL 8.0+ / MySQL Workbench / MariaDB
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `vehicle_service_management`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `vehicle_service_management`;

-- 1. USERS & AUTHENTICATION TABLE
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `salt` VARCHAR(64) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `role` ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB;

-- 2. CUSTOMERS TABLE
CREATE TABLE IF NOT EXISTS `customers` (
    `customer_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `phone_number` VARCHAR(20) NOT NULL UNIQUE,
    `email` VARCHAR(100) NULL,
    `address` TEXT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_cust_phone` (`phone_number`),
    INDEX `idx_cust_name` (`full_name`)
) ENGINE=InnoDB;

-- 3. VEHICLES TABLE
CREATE TABLE IF NOT EXISTS `vehicles` (
    `vehicle_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT NOT NULL,
    `registration_number` VARCHAR(20) NOT NULL UNIQUE,
    `brand` VARCHAR(50) NOT NULL,
    `model` VARCHAR(50) NOT NULL,
    `vehicle_type` ENUM('Hatchback', 'Sedan', 'SUV', 'Luxury', 'Commercial', 'Two-Wheeler') NOT NULL DEFAULT 'Sedan',
    `manufacturing_year` INT NOT NULL,
    `current_km` INT NOT NULL DEFAULT 0,
    `last_service_date` DATE NULL,
    `last_service_km` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_vehicles_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_veh_reg` (`registration_number`),
    INDEX `idx_veh_cust` (`customer_id`)
) ENGINE=InnoDB;

-- 4. MECHANICS TABLE
CREATE TABLE IF NOT EXISTS `mechanics` (
    `mechanic_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `specialization` VARCHAR(100) NOT NULL,
    `status` ENUM('Available', 'Busy', 'On Leave') NOT NULL DEFAULT 'Available',
    `active_jobs_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_mech_status` (`status`)
) ENGINE=InnoDB;

-- 5. SERVICE TYPES TABLE
CREATE TABLE IF NOT EXISTS `service_types` (
    `service_type_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `base_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `estimated_hours` DECIMAL(4,1) NOT NULL DEFAULT 1.0,
    `description` TEXT NULL
) ENGINE=InnoDB;

-- 6. SERVICE BOOKINGS TABLE
CREATE TABLE IF NOT EXISTS `service_bookings` (
    `booking_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT NOT NULL,
    `vehicle_id` INT NOT NULL,
    `service_type_id` INT NOT NULL,
    `mechanic_id` INT NULL,
    `booking_date` DATE NOT NULL,
    `status` ENUM('Pending', 'Confirmed', 'In Progress', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Pending',
    `notes` TEXT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bookings_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_vehicle` FOREIGN KEY (`vehicle_id`) 
        REFERENCES `vehicles` (`vehicle_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_service_type` FOREIGN KEY (`service_type_id`) 
        REFERENCES `service_types` (`service_type_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_mechanic` FOREIGN KEY (`mechanic_id`) 
        REFERENCES `mechanics` (`mechanic_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_booking_status` (`status`),
    INDEX `idx_booking_date` (`booking_date`)
) ENGINE=InnoDB;

-- 7. SERVICE RECORDS (HISTORY) TABLE
CREATE TABLE IF NOT EXISTS `service_records` (
    `record_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NULL,
    `vehicle_id` INT NOT NULL,
    `mechanic_id` INT NULL,
    `service_date` DATE NOT NULL,
    `km_reading` INT NOT NULL,
    `service_type` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `parts_replaced` TEXT NULL,
    `total_cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `next_recommended_date` DATE NULL,
    `next_recommended_km` INT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_records_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `service_bookings` (`booking_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_records_vehicle` FOREIGN KEY (`vehicle_id`) 
        REFERENCES `vehicles` (`vehicle_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_records_mechanic` FOREIGN KEY (`mechanic_id`) 
        REFERENCES `mechanics` (`mechanic_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_records_veh` (`vehicle_id`),
    INDEX `idx_records_date` (`service_date`)
) ENGINE=InnoDB;

-- 8. SPARE PARTS & INVENTORY TABLE
CREATE TABLE IF NOT EXISTS `spare_parts` (
    `part_id` INT AUTO_INCREMENT PRIMARY KEY,
    `part_name` VARCHAR(100) NOT NULL,
    `part_category` VARCHAR(50) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    `min_threshold` INT NOT NULL DEFAULT 5,
    `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `supplier_name` VARCHAR(100) NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_parts_name` (`part_name`),
    INDEX `idx_parts_category` (`part_category`)
) ENGINE=InnoDB;

-- 9. INVENTORY TRANSACTIONS TABLE
CREATE TABLE IF NOT EXISTS `inventory_transactions` (
    `transaction_id` INT AUTO_INCREMENT PRIMARY KEY,
    `part_id` INT NOT NULL,
    `transaction_type` ENUM('IN', 'OUT') NOT NULL,
    `quantity` INT NOT NULL,
    `related_service_id` INT NULL,
    `transaction_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `notes` VARCHAR(255) NULL,
    CONSTRAINT `fk_inventory_part` FOREIGN KEY (`part_id`) 
        REFERENCES `spare_parts` (`part_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_trans_part` (`part_id`)
) ENGINE=InnoDB;

-- 10. INVOICES TABLE
CREATE TABLE IF NOT EXISTS `invoices` (
    `invoice_id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_number` VARCHAR(30) NOT NULL UNIQUE,
    `booking_id` INT NULL,
    `customer_id` INT NOT NULL,
    `vehicle_id` INT NOT NULL,
    `service_cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `parts_cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `gst_rate` DECIMAL(5,2) NOT NULL DEFAULT 18.00,
    `gst_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `final_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `payment_status` ENUM('Paid', 'Unpaid', 'Partially Paid') NOT NULL DEFAULT 'Unpaid',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_invoices_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `service_bookings` (`booking_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_invoices_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_invoices_vehicle` FOREIGN KEY (`vehicle_id`) 
        REFERENCES `vehicles` (`vehicle_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_invoice_number` (`invoice_number`),
    INDEX `idx_invoice_status` (`payment_status`)
) ENGINE=InnoDB;

-- 11. INVOICE ITEMS TABLE
CREATE TABLE IF NOT EXISTS `invoice_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` INT NOT NULL,
    `item_type` ENUM('SERVICE', 'PART') NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    CONSTRAINT `fk_invoice_items_inv` FOREIGN KEY (`invoice_id`) 
        REFERENCES `invoices` (`invoice_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_items_invoice` (`invoice_id`)
) ENGINE=InnoDB;

-- 12. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` INT NOT NULL,
    `payment_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `amount_paid` DECIMAL(10,2) NOT NULL,
    `payment_method` ENUM('Cash', 'Card', 'UPI', 'Bank Transfer') NOT NULL DEFAULT 'Cash',
    `notes` VARCHAR(255) NULL,
    CONSTRAINT `fk_payments_invoice` FOREIGN KEY (`invoice_id`) 
        REFERENCES `invoices` (`invoice_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_payments_invoice` (`invoice_id`)
) ENGINE=InnoDB;

-- 13. AUTOMATIC SERVICE REMINDERS TABLE
CREATE TABLE IF NOT EXISTS `service_reminders` (
    `reminder_id` INT AUTO_INCREMENT PRIMARY KEY,
    `vehicle_id` INT NOT NULL,
    `customer_id` INT NOT NULL,
    `reminder_category` ENUM('Upcoming Service', 'Service Due', 'Overdue Service') NOT NULL,
    `trigger_reason` VARCHAR(255) NOT NULL,
    `due_date` DATE NOT NULL,
    `due_km` INT NOT NULL,
    `status` ENUM('Active', 'Resolved', 'Dismissed') NOT NULL DEFAULT 'Active',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `resolved_at` DATETIME NULL,
    CONSTRAINT `fk_reminders_vehicle` FOREIGN KEY (`vehicle_id`) 
        REFERENCES `vehicles` (`vehicle_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reminders_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_reminder_status` (`status`),
    INDEX `idx_reminder_category` (`reminder_category`)
) ENGINE=InnoDB;

-- 14. REMINDER INTERVAL CONFIGURATION TABLE (Optional config per vehicle type)
CREATE TABLE IF NOT EXISTS `reminder_configurations` (
    `config_id` INT AUTO_INCREMENT PRIMARY KEY,
    `vehicle_type` VARCHAR(50) NOT NULL UNIQUE,
    `interval_days` INT NOT NULL DEFAULT 180,
    `interval_km` INT NOT NULL DEFAULT 5000,
    `advance_notice_days` INT NOT NULL DEFAULT 15,
    `advance_notice_km` INT NOT NULL DEFAULT 500
) ENGINE=InnoDB;
