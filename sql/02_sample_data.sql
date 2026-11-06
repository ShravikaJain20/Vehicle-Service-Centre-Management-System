-- =====================================================================
-- VEHICLE SERVICE CENTRE MANAGEMENT SYSTEM
-- Sample Data Script
-- Populates demo users, customers, vehicles, mechanics, parts,
-- bookings, service records, invoices, and service reminders.
-- =====================================================================

USE `vehicle_service_management`;

-- 1. USERS (Passwords hashed with SHA-256 + salt: 'admin123' and 'staff123')
-- Salt: 'autoservice2026'
-- SHA-256('admin123' + 'autoservice2026') = 
-- 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918 (admin / admin123)
-- SHA-256('staff123' + 'autoservice2026') = 
-- e5f7396a84d4370335e23635ee4febeeb53dcf5ea1385cc8fb483984d720b6d2 (staff / staff123)

INSERT INTO `users` (`username`, `password_hash`, `salt`, `full_name`, `email`, `role`, `is_active`) VALUES
('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'autoservice2026', 'Administrator', 'admin@autoservice.com', 'ADMIN', TRUE),
('staff', 'e5f7396a84d4370335e23635ee4febeeb53dcf5ea1385cc8fb483984d720b6d2', 'autoservice2026', 'Rajesh Sharma', 'rajesh.staff@autoservice.com', 'STAFF', TRUE)
ON DUPLICATE KEY UPDATE `full_name`=VALUES(`full_name`);

-- 2. REMINDER CONFIGURATIONS
INSERT INTO `reminder_configurations` (`vehicle_type`, `interval_days`, `interval_km`, `advance_notice_days`, `advance_notice_km`) VALUES
('Hatchback', 180, 5000, 15, 500),
('Sedan', 180, 5000, 15, 500),
('SUV', 180, 5000, 15, 500),
('Luxury', 180, 7500, 20, 750),
('Commercial', 90, 4000, 10, 400),
('Two-Wheeler', 120, 3000, 15, 300)
ON DUPLICATE KEY UPDATE `interval_days`=VALUES(`interval_days`);

-- 3. CUSTOMERS
INSERT INTO `customers` (`customer_id`, `full_name`, `phone_number`, `email`, `address`) VALUES
(1, 'Vikram Malhotra', '+91 98765 43210', 'vikram.malhotra@gmail.com', '42 Palm Avenue, Indiranagar, Bengaluru'),
(2, 'Ananya Sen', '+91 98111 22334', 'ananya.sen@outlook.com', '15 Silver Birch Lane, Powai, Mumbai'),
(3, 'Rohan Deshmukh', '+91 99222 33445', 'rohan.d@gmail.com', 'B-304 Lakeview Towers, Viman Nagar, Pune'),
(4, 'Priya Nair', '+91 97444 55667', 'priya.nair@yahoo.com', '7 Marine Promenade, Kochi'),
(5, 'Amitav Verma', '+91 98888 77665', 'amitav.verma@techmail.com', '108 Sector 14, Gurugram, Delhi NCR'),
(6, 'Sneha Patel', '+91 94222 88990', 'sneha.patel@gmail.com', '21 Sunrise Duplex, Bodakdev, Ahmedabad')
ON DUPLICATE KEY UPDATE `full_name`=VALUES(`full_name`);

-- 4. VEHICLES
-- Note: Last service dates and KMs set to illustrate:
-- Vehicle 1: Overdue on KM and Date (last serviced 210 days ago, +6,500 km)
-- Vehicle 2: Upcoming service due soon (170 days ago, +4,700 km)
-- Vehicle 3: Recently serviced (20 days ago, +500 km)
-- Vehicle 4: Overdue on KM (> 5,200 km since last service)
-- Vehicle 5: Service Due right now (180 days reached)
-- Vehicle 6: Luxury SUV, good condition
INSERT INTO `vehicles` (`vehicle_id`, `customer_id`, `registration_number`, `brand`, `model`, `vehicle_type`, `manufacturing_year`, `current_km`, `last_service_date`, `last_service_km`) VALUES
(1, 1, 'KA-01-MJ-4589', 'Honda', 'City ZX CVT', 'Sedan', 2021, 46500, '2026-04-05', 40000),
(2, 2, 'MH-02-DZ-7721', 'Hyundai', 'Creta SX (O)', 'SUV', 2022, 29700, '2026-05-18', 25000),
(3, 3, 'MH-12-PQ-9102', 'Maruti Suzuki', 'Swift ZXi+', 'Hatchback', 2023, 14200, '2026-10-15', 13700),
(4, 4, 'KL-07-BW-3310', 'Tata', 'Nexon EV Empowered', 'SUV', 2023, 35400, '2026-06-10', 30100),
(5, 5, 'HR-26-EG-1144', 'Toyota', 'Innova Crysta 2.4 VX', 'Commercial', 2020, 89300, '2026-08-01', 84000),
(6, 6, 'GJ-01-AB-9988', 'BMW', '330i M Sport', 'Luxury', 2022, 22000, '2026-08-20', 18000),
(7, 1, 'KA-05-ER-6060', 'Royal Enfield', 'Classic 350 Dark', 'Two-Wheeler', 2022, 12800, '2026-07-02', 9600)
ON DUPLICATE KEY UPDATE `brand`=VALUES(`brand`);

-- 5. MECHANICS
INSERT INTO `mechanics` (`mechanic_id`, `name`, `phone`, `specialization`, `status`, `active_jobs_count`) VALUES
(1, 'Suresh Kulkarni', '+91 91234 56789', 'Engine & Transmission Specialist', 'Busy', 2),
(2, 'Mohammad Farooq', '+91 92345 67890', 'Auto Electrical & Diagnostic Expert', 'Available', 0),
(3, 'Dinesh Chauhan', '+91 93456 78901', 'Suspension, Brakes & Steering', 'Busy', 1),
(4, 'Harpreet Singh', '+91 94567 89012', 'HVAC, AC & Cooling Systems', 'Available', 0),
(5, 'Ramesh Babu', '+91 95678 90123', 'Periodic Maintenance & Detailing', 'Available', 0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 6. SERVICE TYPES
INSERT INTO `service_types` (`service_type_id`, `name`, `base_price`, `estimated_hours`, `description`) VALUES
(1, 'General Servicing', 1500.00, 2.5, 'Comprehensive multi-point inspection, fluid top-up, filter cleaning, and basic tuning'),
(2, 'Oil Change & Filter Replacement', 600.00, 1.0, 'Engine oil drain, flush, new premium filter installation, and waste disposal'),
(3, 'Brake Inspection & Pad Replacement', 950.00, 1.5, 'Front/Rear brake pad inspection, caliper servicing, rotor inspection, brake fluid test'),
(4, 'Engine Diagnostic & Tune-up', 2200.00, 3.5, 'ECU OBD-II scanning, spark plug check, throttle body cleaning, and sensor calibration'),
(5, 'Battery Check & Replacement', 400.00, 0.5, 'Load testing, terminal cleaning, voltage check, and battery swap'),
(6, 'Tyre Rotation & Wheel Balancing', 800.00, 1.5, '4-wheel computerised dynamic balancing, tyre rotation, and pressure check'),
(7, 'AC Disinfection & Gas Recharge', 1800.00, 2.0, 'Condenser clean, AC filter replacement, R134a/R1234yf refrigerant top-up, leak test'),
(8, 'Premium Foam Wash & Detailing', 1200.00, 2.0, 'High-pressure underbody wash, snow foam exterior, interior vacuuming, dashboard polish')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 7. SPARE PARTS & INVENTORY
-- Note: 'Brake Rotor' and 'Spark Plug' have low stock to test low-stock alerts
INSERT INTO `spare_parts` (`part_id`, `part_name`, `part_category`, `quantity`, `min_threshold`, `unit_price`, `supplier_name`) VALUES
(1, 'Castrol EDGE 5W-30 Full Synthetic Oil (4L)', 'Fluids & Lubricants', 24, 8, 2850.00, 'Castrol India Ltd'),
(2, 'Mobil 1 ESP 5W-30 Synthetic Oil (1L)', 'Fluids & Lubricants', 40, 10, 850.00, 'ExxonMobil Dist'),
(3, 'Bosch Heavy Duty Oil Filter (O-451)', 'Filters', 35, 10, 380.00, 'Bosch Automotive Parts'),
(4, 'OEM Engine Air Filter Element', 'Filters', 18, 5, 520.00, 'Denso Auto Spares'),
(5, 'Bosch Carbon Cabin AC Filter', 'Filters', 14, 5, 650.00, 'Bosch Automotive Parts'),
(6, 'Ferodo Ceramic Front Brake Pad Set', 'Brakes', 12, 6, 2100.00, 'Federal-Mogul Auto'),
(7, 'Brembo Vented Brake Disc Rotor', 'Brakes', 2, 4, 3800.00, 'Brembo Direct Import'), -- LOW STOCK ALERT!
(8, 'NGK Iridium IX Spark Plug (Set of 4)', 'Ignition', 3, 5, 1950.00, 'NGK Spark Plugs'), -- LOW STOCK ALERT!
(9, 'Exide Mileage 12V 45Ah Car Battery', 'Electrical', 8, 3, 4600.00, 'Exide Industries'),
(10, 'Bosch Clear Advantage Wiper Blades (Pair)', 'Accessories', 15, 6, 750.00, 'Bosch Automotive Parts'),
(11, 'Motul DOT 4 High Performance Brake Fluid (500ml)', 'Fluids & Lubricants', 22, 6, 420.00, 'Motul Tech Spares'),
(12, 'Prestone Extended Life Anti-Freeze Coolant (1L)', 'Fluids & Lubricants', 20, 5, 390.00, 'Prestone Global')
ON DUPLICATE KEY UPDATE `part_name`=VALUES(`part_name`);

-- 8. SERVICE BOOKINGS
INSERT INTO `service_bookings` (`booking_id`, `customer_id`, `vehicle_id`, `service_type_id`, `mechanic_id`, `booking_date`, `status`, `notes`) VALUES
(1, 1, 1, 1, 1, '2026-11-05', 'In Progress', 'Customer complaints of engine vibration at idle; also request general servicing'),
(2, 2, 2, 3, 3, '2026-11-06', 'Confirmed', 'Squeaking sound from front wheels when braking gently'),
(3, 3, 3, 8, 5, '2026-11-04', 'Completed', 'Periodic wash and interior ozone sanitisation requested'),
(4, 4, 4, 6, 1, '2026-11-07', 'Pending', 'Steering pull towards left at highway speeds (80km/h)'),
(5, 5, 5, 7, 4, '2026-11-05', 'In Progress', 'AC cooling weak in rear vents; check refrigerant level'),
(6, 6, 6, 2, 2, '2026-11-08', 'Pending', 'Scheduled 20k milestone synthetic oil swap')
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

-- 9. SERVICE RECORDS (HISTORY)
INSERT INTO `service_records` (`record_id`, `booking_id`, `vehicle_id`, `mechanic_id`, `service_date`, `km_reading`, `service_type`, `description`, `parts_replaced`, `total_cost`, `next_recommended_date`, `next_recommended_km`) VALUES
(1, 3, 3, 5, '2026-10-15', 13700, 'General Servicing & Wash', 'Full vehicle inspection completed. Fluids topped up, tyres inspected, brake pads checked (80% life remaining).', 'Castrol EDGE 5W-30 (4L), Bosch Oil Filter', 3850.00, '2027-04-15', 18700),
(2, NULL, 1, 1, '2026-04-05', 40000, 'General Servicing & Brake Service', '40,000 KM major maintenance. Replaced engine oil, spark plugs, front brake pads, and flushed brake lines.', 'Mobil 1 ESP 5W-30, Bosch Oil Filter, Ferodo Brake Pads, NGK Spark Plugs', 7800.00, '2026-10-05', 45000),
(3, NULL, 2, 3, '2026-05-18', 25000, 'Oil Change & Periodic Service', '25,000 KM routine inspection. Replaced oil, air filter, and cabin filter.', 'Synthetic Oil, Air Filter, Cabin Filter', 3200.00, '2026-11-18', 30000),
(4, NULL, 5, 4, '2026-08-01', 84000, 'Commercial Fleet Servicing', 'Complete suspension lubrication, coolant flush, engine oil replacement, and battery health test.', 'Engine Oil 15W-40, Coolant, Oil Filter', 4500.00, '2026-11-01', 88000)
ON DUPLICATE KEY UPDATE `service_type`=VALUES(`service_type`);

-- 10. INVOICES
INSERT INTO `invoices` (`invoice_id`, `invoice_number`, `booking_id`, `customer_id`, `vehicle_id`, `service_cost`, `parts_cost`, `subtotal`, `gst_rate`, `gst_amount`, `discount_amount`, `final_amount`, `payment_status`, `created_at`) VALUES
(1, 'INV-2026-0001', 3, 3, 3, 1200.00, 2650.00, 3850.00, 18.00, 693.00, 200.00, 4343.00, 'Paid', '2026-10-15 16:30:00'),
(2, 'INV-2026-0002', NULL, 1, 1, 2450.00, 5350.00, 7800.00, 18.00, 1404.00, 0.00, 9204.00, 'Paid', '2026-04-05 18:00:00'),
(3, 'INV-2026-0003', NULL, 5, 5, 1800.00, 2700.00, 4500.00, 18.00, 810.00, 150.00, 5160.00, 'Partially Paid', '2026-08-01 17:15:00')
ON DUPLICATE KEY UPDATE `final_amount`=VALUES(`final_amount`);

-- 11. INVOICE ITEMS
INSERT INTO `invoice_items` (`item_id`, `invoice_id`, `item_type`, `description`, `quantity`, `unit_price`, `total_price`) VALUES
(1, 1, 'SERVICE', 'Premium Foam Wash & Detailing Labor', 1, 1200.00, 1200.00),
(2, 1, 'PART', 'Castrol EDGE 5W-30 Full Synthetic Oil (4L)', 1, 2850.00, 2850.00),
(3, 1, 'PART', 'Bosch Heavy Duty Oil Filter', 1, 380.00, 380.00),
(4, 2, 'SERVICE', 'Major Service & Brake Labor', 1, 2450.00, 2450.00),
(5, 2, 'PART', 'Ferodo Ceramic Front Brake Pad Set', 1, 2100.00, 2100.00),
(6, 2, 'PART', 'NGK Iridium Spark Plugs Set', 1, 1950.00, 1950.00)
ON DUPLICATE KEY UPDATE `description`=VALUES(`description`);

-- 12. PAYMENTS
INSERT INTO `payments` (`payment_id`, `invoice_id`, `payment_date`, `amount_paid`, `payment_method`, `notes`) VALUES
(1, 1, '2026-10-15 16:45:00', 4343.00, 'UPI', 'Google Pay Txn #UPI998822441'),
(2, 2, '2026-04-05 18:15:00', 9204.00, 'Card', 'HDFC Visa Platinum Credit Card swipe'),
(3, 3, '2026-08-01 17:30:00', 3000.00, 'Cash', 'Advance partial settlement; remaining balance 2160')
ON DUPLICATE KEY UPDATE `amount_paid`=VALUES(`amount_paid`);

-- 13. AUTOMATIC SERVICE REMINDERS
-- Vehicle 1: Overdue Service (Last service 2026-04-05 @ 40,000 KM, Current 46,500 KM -> Exceeded by 1,500 KM and 30 days)
-- Vehicle 4: Overdue Service (Last service 2026-06-10 @ 30,100 KM, Current 35,400 KM -> Exceeded 5,000 KM threshold)
-- Vehicle 2: Upcoming Service (Due 2026-11-18 or 30,000 KM, Current 29,700 KM -> Within 300 KM)
-- Vehicle 5: Service Due (Commercial interval 90 days / 4,000 KM, reached threshold)
INSERT INTO `service_reminders` (`reminder_id`, `vehicle_id`, `customer_id`, `reminder_category`, `trigger_reason`, `due_date`, `due_km`, `status`, `created_at`) VALUES
(1, 1, 1, 'Overdue Service', 'Odometer (46,500 KM) exceeded due limit (45,000 KM) by 1,500 KM. Days overdue: 30 days.', '2026-10-05', 45000, 'Active', '2026-10-06 09:00:00'),
(2, 4, 4, 'Overdue Service', 'Odometer (35,400 KM) exceeded due limit (35,100 KM) by 300 KM.', '2026-10-10', 35100, 'Active', '2026-10-11 09:00:00'),
(3, 2, 2, 'Upcoming Service', 'Within 300 KM of service threshold (Due: 30,000 KM, Current: 29,700 KM). Due on: 2026-11-18.', '2026-11-18', 30000, 'Active', '2026-11-01 09:00:00'),
(4, 5, 5, 'Service Due', 'Commercial 90-day maintenance interval expired on 2026-10-30 and 4,000 KM interval completed.', '2026-10-30', 88000, 'Active', '2026-10-30 09:00:00')
ON DUPLICATE KEY UPDATE `trigger_reason`=VALUES(`trigger_reason`);
