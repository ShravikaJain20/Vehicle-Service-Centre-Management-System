package util;

import java.util.regex.Pattern;

/**
 * Validates user inputs across all management modules.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Matches standard phone numbers with optional country code and spaces/hyphens
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+\\d{1,3}[- ]?)?\\d{10}$"
    );

    // Registration number: flexible for various Indian and global formats (e.g., KA-01-MJ-4589 or DL1CAB1234)
    private static final Pattern REGISTRATION_PATTERN = Pattern.compile(
        "^[A-Z0-9\\- ]{4,15}$", Pattern.CASE_INSENSITIVE
    );

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Optional field in DB
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleaned.length() >= 7 && cleaned.length() <= 15 && cleaned.matches("^\\+?\\d+$");
    }

    public static boolean isValidRegistrationNumber(String reg) {
        if (reg == null || reg.trim().isEmpty()) return false;
        return REGISTRATION_PATTERN.matcher(reg.trim()).matches();
    }

    public static boolean isPositiveInteger(String str) {
        if (str == null) return false;
        try {
            int val = Integer.parseInt(str.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String str) {
        if (str == null) return false;
        try {
            int val = Integer.parseInt(str.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveDecimal(String str) {
        if (str == null) return false;
        try {
            double val = Double.parseDouble(str.trim());
            return val > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeDecimal(String str) {
        if (str == null) return false;
        try {
            double val = Double.parseDouble(str.trim());
            return val >= 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
