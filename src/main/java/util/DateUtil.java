package util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for formatting and parsing dates across SQL, Java Time, and UI views.
 */
public class DateUtil {

    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    public static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatDate(Date date) {
        if (date == null) return "N/A";
        return date.toLocalDate().format(DISPLAY_DATE_FORMAT);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_DATE_FORMAT);
    }

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return timestamp.toLocalDateTime().format(DISPLAY_DATETIME_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_DATETIME_FORMAT);
    }

    public static Date toSqlDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.valueOf(localDate);
    }

    public static LocalDate toLocalDate(Date sqlDate) {
        if (sqlDate == null) return null;
        return sqlDate.toLocalDate();
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        text = text.trim();
        try {
            return LocalDate.parse(text, DISPLAY_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {}
        try {
            return LocalDate.parse(text, ISO_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {}
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {}
        return null;
    }
}
