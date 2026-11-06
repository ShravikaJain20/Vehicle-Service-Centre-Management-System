import config.DatabaseConfig;
import config.DatabaseConnection;
import ui.LoginFrame;
import ui.theme.UITheme;

import javax.swing.*;

/**
 * Vehicle Service Centre Management System
 * Main Application Entry Point.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Setup Modern UI Theme and High-DPI font rendering
        UITheme.setupLookAndFeel();

        // 2. Initial Database Diagnostic Check
        boolean dbOk = DatabaseConnection.testConnection();
        if (!dbOk) {
            System.err.println("====================================================================");
            System.err.println("[WARNING] MySQL Database Connection Could Not Be Established!");
            System.err.println("Target URL: " + DatabaseConfig.getDbUrl());
            System.err.println("Target User: " + DatabaseConfig.getDbUser());
            System.err.println("Please ensure:");
            System.err.println("1. MySQL Server is running on port 3306.");
            System.err.println("2. You executed sql/setup_database.sql in MySQL Workbench.");
            System.err.println("3. Credentials in src/main/resources/db.properties match your MySQL password.");
            System.err.println("====================================================================");
        } else {
            System.out.println("[OK] Connected to MySQL database 'vehicle_service_management' successfully.");
        }

        // 3. Launch Desktop GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            if (!dbOk) {
                JOptionPane.showMessageDialog(loginFrame,
                    "<html><b>MySQL Database Connection Notice</b><br><br>" +
                    "The application could not reach MySQL at: <code>" + DatabaseConfig.getDbUrl() + "</code><br><br>" +
                    "<b>Setup Steps:</b><br>" +
                    "1. Start MySQL Server (via MySQL Workbench / XAMPP / Windows Services).<br>" +
                    "2. Open and execute the script: <code>sql/setup_database.sql</code><br>" +
                    "3. If your MySQL root password differs, update <code>src/main/resources/db.properties</code>.<br><br>" +
                    "<i>Default demo login: admin / admin123</i></html>",
                    "Database Connectivity Notice",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
