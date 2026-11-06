-- =====================================================================
-- VEHICLE SERVICE CENTRE MANAGEMENT SYSTEM
-- Master All-In-One Setup Script (Schema + Sample Data)
-- Open and Execute this file in MySQL Workbench or run via mysql CLI
-- =====================================================================

DROP DATABASE IF EXISTS vehicle_service_management;
CREATE DATABASE vehicle_service_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vehicle_service_management;

-- 1. USERS & AUTHENTICATION TABLE
CREATE TABLE `users` (
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
CREATE TABLE `customers` (
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
CREATE TABLE `vehicles` (
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
CREATE TABLE `mechanics` (
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
CREATE TABLE `service_types` (
    `service_type_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `base_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `estimated_hours` DECIMAL(4,1) NOT NULL DEFAULT 1.0,
    `description` TEXT NULL
) ENGINE=InnoDB;

-- 6. SERVICE BOOKINGS TABLE
CREATE TABLE `service_bookings` (
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
CREATE TABLE `service_records` (
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
CREATE TABLE `spare_parts` (
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
CREATE TABLE `inventory_transactions` (
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
CREATE TABLE `invoices` (
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
CREATE TABLE `invoice_items` (
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
CREATE TABLE `payments` (
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
CREATE TABLE `service_reminders` (
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

-- 14. REMINDER INTERVAL CONFIGURATIONS TABLE
CREATE TABLE `reminder_configurations` (
    `config_id` INT AUTO_INCREMENT PRIMARY KEY,
    `vehicle_type` VARCHAR(50) NOT NULL UNIQUE,
    `interval_days` INT NOT NULL DEFAULT 180,
    `interval_km` INT NOT NULL DEFAULT 5000,
    `advance_notice_days` INT NOT NULL DEFAULT 15,
    `advance_notice_km` INT NOT NULL DEFAULT 500
) ENGINE=InnoDB;

-- =====================================================================
-- POPULATE DEMO DATA
-- =====================================================================

-- 1. USERS (admin / admin123 and staff / staff123)
INSERT INTO `users` (`username`, `password_hash`, `salt`, `full_name`, `email`, `role`, `is_active`) VALUES
('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'autoservice2026', 'System Administrator', 'admin@autoservice.com', 'ADMIN', TRUE),
('staff', 'e5f7396a84d4370335e23635ee4febeeb53dcf5ea1385cc8fb483984d720b6d2', 'autoservice2026', 'Rajesh Sharma', 'rajesh.staff@autoservice.com', 'STAFF', TRUE);

-- 2. REMINDER CONFIGURATIONS
INSERT INTO `reminder_configurations` (`vehicle_type`, `interval_days`, `interval_km`, `advance_notice_days`, `advance_notice_km`) VALUES
('Hatchback', 180, 5000, 15, 500),
('Sedan', 180, 5000, 15, 500),
('SUV', 180, 5000, 15, 500),
('Luxury', 180, 7500, 20, 750),
('Commercial', 90, 4000, 10, 400),
('Two-Wheeler', 120, 3000, 15, 300);

-- 3. CUSTOMERS
INSERT INTO `customers` (`customer_id`, `full_name`, `phone_number`, `email`, `address`) VALUES
(1, 'Vikram Malhotra', '+91 98765 43210', 'vikram.malhotra@gmail.com', '42 Palm Avenue, Indiranagar, Bengaluru'),
(2, 'Ananya Sen', '+91 98111 22334', 'ananya.sen@outlook.com', '15 Silver Birch Lane, Powai, Mumbai'),
(3, 'Rohan Deshmukh', '+91 99222 33445', 'rohan.d@gmail.com', 'B-304 Lakeview Towers, Viman Nagar, Pune'),
(4, 'Priya Nair', '+91 97444 55667', 'priya.nair@yahoo.com', '7 Marine Promenade, Kochi'),
(5, 'Amitav Verma', '+91 98888 77665', 'amitav.verma@techmail.com', '108 Sector 14, Gurugram, Delhi NCR'),
(6, 'Sneha Patel', '+91 94222 88990', 'sneha.patel@gmail.com', '21 Sunrise Duplex, Bodakdev, Ahmedabad');

-- 4. VEHICLES
INSERT INTO `vehicles` (`vehicle_id`, `customer_id`, `registration_number`, `brand`, `model`, `vehicle_type`, `manufacturing_year`, `current_km`, `last_service_date`, `last_service_km`) VALUES
(1, 1, 'KA-01-MJ-4589', 'Honda', 'City ZX CVT', 'Sedan', 2021, 46500, '2026-04-05', 40000),
(2, 2, 'MH-02-DZ-7721', 'Hyundai', 'Creta SX (O)', 'SUV', 2022, 29700, '2026-05-18', 25000),
(3, 3, 'MH-12-PQ-9102', 'Maruti Suzuki', 'Swift ZXi+', 'Hatchback', 2023, 14200, '2026-10-15', 13700),
(4, 4, 'KL-07-BW-3310', 'Tata', 'Nexon EV Empowered', 'SUV', 2023, 35400, '2026-06-10', 30100),
(5, 5, 'HR-26-EG-1144', 'Toyota', 'Innova Crysta 2.4 VX', 'Commercial', 2020, 89300, '2026-08-01', 84000),
(6, 6, 'GJ-01-AB-9988', 'BMW', '330i M Sport', 'Luxury', 2022, 22000, '2026-08-20', 18000),
(7, 1, 'KA-05-ER-6060', 'Royal Enfield', 'Classic 350 Dark', 'Two-Wheeler', 2022, 12800, '2026-07-02', 9600);

-- 5. MECHANICS
INSERT INTO `mechanics` (`mechanic_id`, `name`, `phone`, `specialization`, `status`, `active_jobs_count`) VALUES
(1, 'Suresh Kulkarni', '+91 91234 56789', 'Engine & Transmission Specialist', 'Busy', 2),
(2, 'Mohammad Farooq', '+91 92345 67890', 'Auto Electrical & Diagnostic Expert', 'Available', 0),
(3, 'Dinesh Chauhan', '+91 93456 78901', 'Suspension, Brakes & Steering', 'Busy', 1),
(4, 'Harpreet Singh', '+91 94567 89012', 'HVAC, AC & Cooling Systems', 'Available', 0),
(5, 'Ramesh Babu', '+91 95678 90123', 'Periodic Maintenance & Detailing', 'Available', 0);

-- 6. SERVICE TYPES
INSERT INTO `service_types` (`service_type_id`, `name`, `base_price`, `estimated_hours`, `description`) VALUES
(1, 'General Servicing', 1500.00, 2.5, 'Comprehensive multi-point inspection, fluid top-up, filter cleaning, and basic tuning'),
(2, 'Oil Change & Filter Replacement', 600.00, 1.0, 'Engine oil drain, flush, new premium filter installation, and waste disposal'),
(3, 'Brake Inspection & Pad Replacement', 950.00, 1.5, 'Front/Rear brake pad inspection, caliper servicing, rotor inspection, brake fluid test'),
(4, 'Engine Diagnostic & Tune-up', 2200.00, 3.5, 'ECU OBD-II scanning, spark plug check, throttle body cleaning, and sensor calibration'),
(5, 'Battery Check & Replacement', 400.00, 0.5, 'Load testing, terminal cleaning, voltage check, and battery swap'),
(6, 'Tyre Rotation & Wheel Balancing', 800.00, 1.5, '4-wheel computerised dynamic balancing, tyre rotation, and pressure check'),
(7, 'AC Disinfection & Gas Recharge', 1800.00, 2.0, 'Condenser clean, AC filter replacement, R134a refrigerant top-up, leak test'),
(8, 'Premium Foam Wash & Detailing', 1200.00, 2.0, 'High-pressure underbody wash, snow foam exterior, interior vacuuming, dashboard polish');

-- 7. SPARE PARTS
INSERT INTO `spare_parts` (`part_id`, `part_name`, `part_category`, `quantity`, `min_threshold`, `unit_price`, `supplier_name`) VALUES
(1, 'Castrol EDGE 5W-30 Full Synthetic Oil (4L)', 'Fluids & Lubricants', 24, 8, 2850.00, 'Castrol India Ltd'),
(2, 'Mobil 1 ESP 5W-30 Synthetic Oil (1L)', 'Fluids & Lubricants', 40, 10, 850.00, 'ExxonMobil Dist'),
(3, 'Bosch Heavy Duty Oil Filter (O-451)', 'Filters', 35, 10, 380.00, 'Bosch Automotive Parts'),
(4, 'OEM Engine Air Filter Element', 'Filters', 18, 5, 520.00, 'Denso Auto Spares'),
(5, 'Bosch Carbon Cabin AC Filter', 'Filters', 14, 5, 650.00, 'Bosch Automotive Parts'),
(6, 'Ferodo Ceramic Front Brake Pad Set', 'Brakes', 12, 6, 2100.00, 'Federal-Mogul Auto'),
(7, 'Brembo Vented Brake Disc Rotor', 'Brakes', 2, 4, 3800.00, 'Brembo Direct Import'),
(8, 'NGK Iridium IX Spark Plug (Set of 4)', 'Ignition', 3, 5, 1950.00, 'NGK Spark Plugs'),
(9, 'Exide Mileage 12V 45Ah Car Battery', 'Electrical', 8, 3, 4600.00, 'Exide Industries'),
(10, 'Bosch Clear Advantage Wiper Blades (Pair)', 'Accessories', 15, 6, 750.00, 'Bosch Automotive Parts'),
(11, 'Motul DOT 4 High Performance Brake Fluid (500ml)', 'Fluids & Lubricants', 22, 6, 420.00, 'Motul Tech Spares'),
(12, 'Prestone Extended Life Anti-Freeze Coolant (1L)', 'Fluids & Lubricants', 20, 5, 390.00, 'Prestone Global');

-- 8. SERVICE BOOKINGS
INSERT INTO `service_bookings` (`booking_id`, `customer_id`, `vehicle_id`, `service_type_id`, `mechanic_id`, `booking_date`, `status`, `notes`) VALUES
(1, 1, 1, 1, 1, '2026-11-05', 'In Progress', 'Customer complaints of engine vibration at idle; also request general servicing'),
(2, 2, 2, 3, 3, '2026-11-06', 'Confirmed', 'Squeaking sound from front wheels when braking gently'),
(3, 3, 3, 8, 5, '2026-11-04', 'Completed', 'Periodic wash and interior ozone sanitisation requested'),
(4, 4, 4, 6, 1, '2026-11-07', 'Pending', 'Steering pull towards left at highway speeds (80km/h)'),
(5, 5, 5, 7, 4, '2026-11-05', 'In Progress', 'AC cooling weak in rear vents; check refrigerant level'),
(6, 6, 6, 2, 2, '2026-11-08', 'Pending', 'Scheduled 20k milestone synthetic oil swap');

-- 9. SERVICE RECORDS (HISTORY)
INSERT INTO `service_records` (`record_id`, `booking_id`, `vehicle_id`, `mechanic_id`, `service_date`, `km_reading`, `service_type`, `description`, `parts_replaced`, `total_cost`, `next_recommended_date`, `next_recommended_km`) VALUES
(1, 3, 3, 5, '2026-10-15', 13700, 'General Servicing & Wash', 'Full vehicle inspection completed. Fluids topped up, tyres inspected, brake pads checked (80% life remaining).', 'Castrol EDGE 5W-30 (4L), Bosch Oil Filter', 3850.00, '2027-04-15', 18700),
(2, NULL, 1, 1, '2026-04-05', 40000, 'General Servicing & Brake Service', '40,000 KM major maintenance. Replaced engine oil, spark plugs, front brake pads, and flushed brake lines.', 'Mobil 1 ESP 5W-30, Bosch Oil Filter, Ferodo Brake Pads, NGK Spark Plugs', 7800.00, '2026-10-05', 45000),
(3, NULL, 2, 3, '2026-05-18', 25000, 'Oil Change & Periodic Service', '25,000 KM routine inspection. Replaced oil, air filter, and cabin filter.', 'Synthetic Oil, Air Filter, Cabin Filter', 3200.00, '2026-11-18', 30000),
(4, NULL, 5, 4, '2026-08-01', 84000, 'Commercial Fleet Servicing', 'Complete suspension lubrication, coolant flush, engine oil replacement, and battery health test.', 'Engine Oil 15W-40, Coolant, Oil Filter', 4500.00, '2026-11-01', 88000);

-- 10. INVOICES
INSERT INTO `invoices` (`invoice_id`, `invoice_number`, `booking_id`, `customer_id`, `vehicle_id`, `service_cost`, `parts_cost`, `subtotal`, `gst_rate`, `gst_amount`, `discount_amount`, `final_amount`, `payment_status`, `created_at`) VALUES
(1, 'INV-2026-0001', 3, 3, 3, 1200.00, 2650.00, 3850.00, 18.00, 693.00, 200.00, 4343.00, 'Paid', '2026-10-15 16:30:00'),
(2, 'INV-2026-0002', NULL, 1, 1, 2450.00, 5350.00, 7800.00, 18.00, 1404.00, 0.00, 9204.00, 'Paid', '2026-04-05 18:00:00'),
(3, 'INV-2026-0003', NULL, 5, 5, 1800.00, 2700.00, 4500.00, 18.00, 810.00, 150.00, 5160.00, 'Partially Paid', '2026-08-01 17:15:00');

-- 11. INVOICE ITEMS
INSERT INTO `invoice_items` (`item_id`, `invoice_id`, `item_type`, `description`, `quantity`, `unit_price`, `total_price`) VALUES
(1, 1, 'SERVICE', 'Premium Foam Wash & Detailing Labor', 1, 1200.00, 1200.00),
(2, 1, 'PART', 'Castrol EDGE 5W-30 Full Synthetic Oil (4L)', 1, 2850.00, 2850.00),
(3, 1, 'PART', 'Bosch Heavy Duty Oil Filter', 1, 380.00, 380.00),
(4, 2, 'SERVICE', 'Major Service & Brake Labor', 1, 2450.00, 2450.00),
(5, 2, 'PART', 'Ferodo Ceramic Front Brake Pad Set', 1, 2100.00, 2100.00),
(6, 2, 'PART', 'NGK Iridium Spark Plugs Set', 1, 1950.00, 1950.00);

-- 12. PAYMENTS
INSERT INTO `payments` (`payment_id`, `invoice_id`, `payment_date`, `amount_paid`, `payment_method`, `notes`) VALUES
(1, 1, '2026-10-15 16:45:00', 4343.00, 'UPI', 'Google Pay Txn #UPI998822441'),
(2, 2, '2026-04-05 18:15:00', 9204.00, 'Card', 'HDFC Visa Platinum Credit Card swipe'),
(3, 3, '2026-08-01 17:30:00', 3000.00, 'Cash', 'Advance partial settlement; remaining balance 2160');

-- 13. AUTOMATIC SERVICE REMINDERS
INSERT INTO `service_reminders` (`reminder_id`, `vehicle_id`, `customer_id`, `reminder_category`, `trigger_reason`, `due_date`, `due_km`, `status`, `created_at`) VALUES
(1, 1, 1, 'Overdue Service', 'Odometer (46,500 KM) exceeded due limit (45,000 KM) by 1,500 KM. Days overdue: 30 days.', '2026-10-05', 45000, 'Active', '2026-10-06 09:00:00'),
(2, 4, 4, 'Overdue Service', 'Odometer (35,400 KM) exceeded due limit (35,100 KM) by 300 KM.', '2026-10-10', 35100, 'Active', '2026-10-11 09:00:00'),
(3, 2, 2, 'Upcoming Service', 'Within 300 KM of service threshold (Due: 30,000 KM, Current: 29,700 KM). Due on: 2026-11-18.', '2026-11-18', 30000, 'Active', '2026-11-01 09:00:00'),
(4, 5, 5, 'Service Due', 'Commercial 90-day maintenance interval expired on 2026-10-30 and 4,000 KM interval completed.', '2026-10-30', 88000, 'Active', '2026-10-30 09:00:00');
