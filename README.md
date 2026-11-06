# VEHICLE SERVICE CENTRE MANAGEMENT SYSTEM

[![Java](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/)
[![GUI](https://img.shields.io/badge/GUI-Java%20AWT%20%2B%20Swing-navy.svg)]()
[![Database](https://img.shields.io/badge/Database-MySQL%208.0%2B-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/Architecture-Clean%20Layered%20Enterprise-success.svg)]()

> A professional, modern, automotive-themed desktop management system designed to automate daily automobile workshop and dealership operations. Features real-time statistics, customer & fleet management, booking workflows, inventory tracking, GST-compliant billing with PDF invoice generation, an **Intelligent Automatic Vehicle Service Reminder System**, and an **AI-Powered Vehicle Maintenance & Diagnostic Assistant**.

---

## 1. Key Features & Modules

### 🚀 Main Innovation: Automatic Vehicle Service Reminder System
- **Dual-Threshold Detection**: Evaluates both **Elapsed Days** and **Odometer Kilometres (KM)** since last service.
- **Configurable Intervals**: Supports customized service intervals by vehicle type (e.g., 180 Days / 5,000 KM for Passenger Cars; 90 Days / 4,000 KM for Commercial; 120 Days / 3,000 KM for Two-Wheelers).
- **Proactive Tri-State Alert Categories**:
  - `Overdue Service`: Highlighted in high-visibility red whenever either the date threshold OR kilometre threshold is breached.
  - `Service Due`: Triggered when vehicle is within 3 days or 50 KM of service target.
  - `Upcoming Service`: Dispatched within advance notice window (15 days or 500 KM).
- **Background Autonomous Engine**: Powered by Java `ScheduledExecutorService` running quietly on background daemon threads every 30 minutes without freezing the user interface.
- **Auto-Resolution & Anti-Duplication**: Prevents duplicate active reminders for the same service cycle; automatically marks active reminders as resolved upon service completion.
- **One-Click Outreach**: Generates pre-formatted reminder notifications ready for SMS/WhatsApp/Email delivery.

### 🤖 AI-Powered Vehicle Maintenance Assistant
- **AI Service Recommendations**: Formulates tailored maintenance schedules based on vehicle model, odometer reading, and historical repair records.
- **Predictive Maintenance Health Audit**: Estimates friction wear (brake pads, rotors), fluid breakdown (engine oil, transmission oil, coolant), and battery degradation with failure risk ratings (`HIGH`, `MODERATE`, `LOW`).
- **Smart Diagnostic Assistant**: Conversational chat interface for technicians and service advisors to diagnose symptoms (e.g. spongy brakes, cold-start vibration, warm AC air, exhaust smoke).
- **Executive Health Summary**: Generates a concise diagnostic summary of recent visits and top 3 immediate repair priorities.
- **Dual-Engine Architecture**: Seamlessly calls Gemini API when an API key is present, and falls back to a built-in **Rule-Based Automotive Expert System** when offline or unconfigured.

### 📊 Real-Time Operations Dashboard
- Real-time KPI Stat Cards: Total Customers, Registered Fleet, Total Bookings, Pending Services, Completed Services, Total Revenue Collected.
- **Java2D Graphical Charts**:
  - Monthly Workshop Revenue Bar Chart with gradients, value callouts, and gridlines.
  - Service Work Order Distribution Donut Chart (Completed, In Progress, Cancelled).
- Live Overdue Reminders Pill in header with one-click navigation.

### 👥 Customer & Vehicle Fleet Management
- Comprehensive CRUD operations with real-time search and validation.
- Quick Odometer Update tool that immediately re-evaluates service reminder criteria.
- Full vehicle ownership association and contact information.

### 📅 Service Bookings & Mechanic Dispatch
- Dynamic status tracking: `Pending`, `Confirmed`, `In Progress`, `Completed`, `Cancelled`.
- Dynamic mechanic workload allocation: automatically increments/decrements active job counts and prevents overloading.
- Single-click **"Complete Service"** action that automatically writes a permanent `ServiceRecord` into logbook, updates vehicle odometer & last service date, and resolves outstanding reminder alerts.

### 📦 Spare Parts & Inventory Control
- Live stock level monitoring with **Low-Stock Alert Badges** (`Quantity <= Minimum Threshold`).
- Stock adjustment dialog supporting `IN - Restock / Shipment` and `OUT - Workshop Issue / Consumption` with inventory audit trail.

### 💳 GST Billing & Zero-Dependency PDF Invoices
- Automatic calculation of labor charges, spare parts cost, subtotal, 18% GST, and discounts.
- Unique sequential invoice number generation (`INV-YYYY-XXXX`).
- Multi-channel payment recording: `Cash`, `Card`, `UPI`, `Bank Transfer`.
- **Zero-Dependency PDF Export**: Writes vector-sharp, standards-compliant PDF 1.4 invoices without requiring heavy external libraries.
- Standard **AWT Print Integration**: Direct print dialog preview supporting physical printers and *Microsoft Print to PDF*.

### 📜 Vehicle Service History Logbook
- Searchable maintenance archive by registration number.
- Records service types, mechanic notes, parts replaced, total costs, and next recommended service milestones.

---

## 2. Technology Stack & Design

| Layer | Technology |
|---|---|
| **Language** | Java (JDK 11 or higher) |
| **GUI Framework** | Java AWT + Java Swing |
| **UI Theme** | Custom Modern Automotive Dark Navy (`#0F172A`) & Electric Blue (`#2563EB`) Palette with FlatLaf integration |
| **Database Connectivity** | JDBC (`java.sql.*`) with PreparedStatement protection |
| **Database** | MySQL 8.0+ / MariaDB (`vehicle_service_management`) |
| **Database Tool** | MySQL Workbench |
| **AI Integration** | Java 11 `java.net.http.HttpClient` with Gemini REST API + Automotive Expert Rule Fallback |
| **Build Tools** | Apache Maven / Standalone Batch Scripts |

---

## 3. Database Setup (MySQL Workbench)

### Step 1: Open MySQL Workbench
1. Launch **MySQL Workbench** and connect to your local MySQL instance (default: `localhost:3306`).

### Step 2: Execute Master Setup Script
1. In MySQL Workbench, click **File > Open SQL Script...** (or press `Ctrl + Shift + O`).
2. Navigate to:
   ```
   vehicle-service-management/sql/setup_database.sql
   ```
3. Click the **Execute (Lightning icon)** button to run the script.
4. This will:
   - Create the database `vehicle_service_management`.
   - Create all 14 tables with primary keys, foreign keys, and indexes.
   - Populate realistic demo data (users, customers, vehicles with various odometer readings, mechanics, spare parts, bookings, invoices, and service reminders).

### Step 3: Verify Tables
In the Workbench Navigator schemas pane, right-click schemas and click **Refresh All**. You will see `vehicle_service_management` with all 14 tables:
`users`, `customers`, `vehicles`, `mechanics`, `service_types`, `service_bookings`, `service_records`, `spare_parts`, `inventory_transactions`, `invoices`, `invoice_items`, `payments`, `service_reminders`, `reminder_configurations`.

---

## 4. Configuration Settings

Open `src/main/resources/db.properties` and verify your credentials:

```properties
# MySQL Connection Settings
db.url=jdbc:mysql://localhost:3306/vehicle_service_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=root

# Service Reminder Engine
reminder.check.interval.minutes=30
reminder.default.interval.days=180
reminder.default.interval.km=5000
reminder.advance.notice.days=15
reminder.advance.notice.km=500

# Optional AI API Key (Or set GEMINI_API_KEY environment variable)
# If left empty, the intelligent rule-based expert engine runs automatically!
ai.api.key=
ai.api.model=gemini-1.5-flash
```

> **Note on Passwords**: If your MySQL `root` user has a different password (e.g. empty or `admin`), update the `db.password` property above or set the Windows environment variable `DB_PASSWORD`.

---

## 5. How to Run the Application

### Option A: Quick One-Click Launch (Windows)
Double-click `compile_and_run.bat` located in the project root:
```bat
compile_and_run.bat
```
This script automatically detects your JDK, compiles all Java source files, and starts the desktop GUI.

---

### Option B: Visual Studio Code
1. Open Visual Studio Code.
2. Select **File > Open Folder...** and select:
   ```
   C:\Users\SHRAVIKA JAIN\.gemini\antigravity\scratch\vehicle-service-management
   ```
3. Ensure the **Extension Pack for Java** is installed in VS Code.
4. Navigate to `src/main/java/Main.java`.
5. Click the **Run** button (or press `F5` / right-click > **Run Java**).

---

### Option C: IntelliJ IDEA / Eclipse
1. Open IntelliJ IDEA or Eclipse.
2. Click **Open** and select the `pom.xml` file or the `vehicle-service-management` project directory.
3. Choose **Open as Project**.
4. Locate `src/main/java/Main.java`.
5. Right-click `Main.java` and choose **Run 'Main.main()'**.

---

### Option D: Maven Terminal
From Command Prompt or PowerShell in the project directory:
```bash
# Compile and run
mvn compile exec:java -Dexec.mainClass="Main"

# Or package as standalone JAR
mvn clean package
java -jar target/vehicle-service-management-1.0.0.jar
```

---

## 6. Default Demo Credentials

The database script pre-configures two security roles:

| Role | Username | Password | Privileges |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full Access: CRUD, Dispatch, Invoicing, Inventory, Reminders, AI |
| **Service Staff** | `staff` | `staff123` | Operations: Bookings, Vehicle Check-in, Invoices, Customer Service |

*(Passwords are securely salted and hashed using SHA-256 via `PasswordUtil.java`)*

---

## 7. Project Architecture & Directory Structure

```
vehicle-service-management/
├── pom.xml                               # Maven project definition & dependencies
├── compile_and_run.bat                   # Automated Windows build & run utility
├── build.bat                             # JAR packaging script
├── run.bat                               # Application execution script
├── README.md                             # Comprehensive project documentation
├── sql/
│   ├── 01_schema.sql                     # Table schemas, foreign keys, constraints
│   ├── 02_sample_data.sql                # Rich automotive demo data
│   └── setup_database.sql                # Single master all-in-one setup script
└── src/
    └── main/
        ├── resources/
        │   └── db.properties             # Database credentials & reminder configuration
        └── java/
            ├── Main.java                 # Entry point with diagnostic checks & UI launcher
            ├── config/
            │   ├── DatabaseConfig.java   # Config property loader
            │   └── DatabaseConnection.java # Reconnecting thread-safe JDBC connection
            ├── model/
            │   ├── User.java             # System user & authentication model
            │   ├── Customer.java         # Vehicle owner model
            │   ├── Vehicle.java          # Vehicle fleet model
            │   ├── Mechanic.java         # Technician & workload model
            │   ├── ServiceType.java      # Service catalog model
            │   ├── ServiceBooking.java   # Appointment & work order model
            │   ├── ServiceRecord.java    # Historical service record model
            │   ├── SparePart.java        # Inventory SKU & stock model
            │   ├── InventoryTransaction.java # Stock movement log model
            │   ├── Invoice.java          # Billing invoice model
            │   ├── InvoiceItem.java      # Line item model
            │   ├── Payment.java          # Payment transaction model
            │   └── ServiceReminder.java  # Proactive service reminder model
            ├── dao/
            │   ├── UserDAO.java
            │   ├── CustomerDAO.java
            │   ├── VehicleDAO.java
            │   ├── MechanicDAO.java
            │   ├── BookingDAO.java
            │   ├── ServiceRecordDAO.java
            │   ├── InventoryDAO.java
            │   ├── InvoiceDAO.java
            │   └── ReminderDAO.java
            ├── service/
            │   ├── AuthService.java      # Salted password authentication
            │   ├── CustomerService.java   # Customer business rules
            │   ├── VehicleService.java    # Vehicle management & KM trigger
            │   ├── MechanicService.java   # Workload balancing
            │   ├── BookingService.java    # Workflow & auto-service record creation
            │   ├── InventoryService.java  # Stock adjustments & low-stock alerts
            │   ├── BillingService.java    # GST calculation & PDF generation
            │   ├── ReminderService.java   # Dual-threshold reminder engine
            │   ├── ReminderScheduler.java # ScheduledExecutorService background daemon
            │   ├── AIRecommendationService.java # Gemini API & rule expert system
            │   └── DashboardService.java  # KPI aggregation & chart data
            ├── ui/
            │   ├── theme/
            │   │   ├── UITheme.java       # Color palette, fonts, Look & Feel
            │   │   ├── ModernButton.java  # Rounded interactive buttons
            │   │   ├── ModernTextField.java # Placeholders & focus glows
            │   │   ├── ModernCard.java    # Rounded card container
            │   │   └── ModernTable.java   # Striped table with status badges
            │   ├── components/
            │   │   ├── StatCard.java      # Dashboard KPI cards
            │   │   ├── ChartPanel.java    # Java2D Bar & Donut charts
            │   │   └── SearchBar.java     # Search & filter bar
            │   ├── LoginFrame.java        # Split automotive branding login
            │   ├── DashboardFrame.java    # Main application shell & sidebar
            │   ├── CustomerPanel.java     # Customer management tab
            │   ├── VehiclePanel.java      # Vehicle fleet tab
            │   ├── BookingPanel.java      # Service appointments tab
            │   ├── MechanicPanel.java     # Technician workload tab
            │   ├── InventoryPanel.java    # Spare parts inventory tab
            │   ├── BillingPanel.java      # Invoicing & PDF generation tab
            │   ├── ReminderPanel.java     # Main innovation reminder tab
            │   ├── ServiceHistoryPanel.java # Maintenance logbook tab
            │   └── AIAssistantPanel.java  # AI assistant & predictive tab
            └── util/
                ├── PasswordUtil.java      # SHA-256 + salt generator
                ├── ValidationUtil.java    # Regex & input validation
                ├── DateUtil.java          # Date conversion & formatters
                └── PDFGenerator.java      # Pure Java PDF 1.4 invoice writer
```

---

## 8. Verification & Demonstration Checklist

- [x] **Secure Authentication**: Salted SHA-256 authentication with role-based segregation (`ADMIN` and `STAFF`).
- [x] **Database Connectivity**: PreparedStatement SQL operations with relational integrity and foreign key constraints across 14 tables.
- [x] **Automatic Reminders Engine**: Verified against sample dates and KM readings; flags overdue vehicles (e.g. Honda City KA-01-MJ-4589) and upcoming services (Hyundai Creta MH-02-DZ-7721).
- [x] **Odometer Quick Update**: Changing a vehicle's odometer instantly triggers re-evaluation in the reminder engine.
- [x] **Service Booking Completion**: Marking a booking as completed auto-generates a service history entry, updates vehicle odometer & last service date, and clears active reminders.
- [x] **Spare Parts Inventory**: Displays low-stock badges; inventory transactions track stock movements.
- [x] **GST Billing & Invoice Export**: Real-time tax computation (18% GST), payment recording, and valid vector PDF output.
- [x] **AI Diagnostic Assistant**: Technical advice on mechanical queries with rule-based fallback when offline.
- [x] **SwingWorker Threading**: Long-running database operations and AI calls execute in background threads to guarantee a smooth, responsive UI.

---

*Vehicle Service Centre Management System — Developed for academic excellence and commercial automotive workshops.*
