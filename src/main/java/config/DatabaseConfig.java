package config;

import java.io.InputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * Loads and manages configuration properties for database, reminders, and AI.
 * Prioritizes environment variables over properties file values.
 */
public class DatabaseConfig {
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        if (loaded) return;
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load db.properties from classpath: " + e.getMessage());
        }
        loaded = true;
    }

    public static String getDbUrl() {
        String env = System.getenv("DB_URL");
        if (env != null && !env.isBlank()) return env;
        return properties.getProperty("db.url", "jdbc:mysql://localhost:3306/vehicle_service_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUser() {
        String env = System.getenv("DB_USER");
        if (env != null && !env.isBlank()) return env;
        return properties.getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        String env = System.getenv("DB_PASSWORD");
        if (env != null) return env;
        return properties.getProperty("db.password", "root");
    }

    public static String getDriver() {
        return properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static int getReminderCheckIntervalMinutes() {
        try {
            return Integer.parseInt(properties.getProperty("reminder.check.interval.minutes", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public static int getDefaultIntervalDays() {
        try {
            return Integer.parseInt(properties.getProperty("reminder.default.interval.days", "180"));
        } catch (NumberFormatException e) {
            return 180;
        }
    }

    public static int getDefaultIntervalKm() {
        try {
            return Integer.parseInt(properties.getProperty("reminder.default.interval.km", "5000"));
        } catch (NumberFormatException e) {
            return 5000;
        }
    }

    public static int getAdvanceNoticeDays() {
        try {
            return Integer.parseInt(properties.getProperty("reminder.advance.notice.days", "15"));
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    public static int getAdvanceNoticeKm() {
        try {
            return Integer.parseInt(properties.getProperty("reminder.advance.notice.km", "500"));
        } catch (NumberFormatException e) {
            return 500;
        }
    }

    public static String getAiApiKey() {
        String env = System.getenv("GEMINI_API_KEY");
        if (env != null && !env.isBlank()) return env;
        env = System.getenv("AI_API_KEY");
        if (env != null && !env.isBlank()) return env;
        return properties.getProperty("ai.api.key", "");
    }

    public static String getAiProvider() {
        return properties.getProperty("ai.api.provider", "gemini");
    }

    public static String getAiModel() {
        return properties.getProperty("ai.api.model", "gemini-1.5-flash");
    }
    public static Connection getConnection() throws SQLException {
    try {
        Class.forName(getDriver());
    } catch (ClassNotFoundException e) {
        throw new SQLException("MySQL Driver not found!", e);
    }

    return DriverManager.getConnection(
        getDbUrl(),
        getDbUser(),
        getDbPassword()
    );
}
}
