package ui.theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Modern Automobile Theme Design System.
 * Dark Navy Blue, Electric Blue, and Slate accents.
 */
public class UITheme {

    // Primary Palette
    public static final Color NAVY_DARK = new Color(15, 23, 42);       // #0F172A (Sidebar / Dark headers)
    public static final Color NAVY_SIDEBAR = new Color(30, 41, 59);    // #1E293B
    public static final Color NAVY_HOVER = new Color(51, 65, 85);      // #334155
    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);   // #2563EB (Electric Blue)
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);  // #1D4ED8
    public static final Color ACCENT_CYAN = new Color(14, 165, 233);   // #0EA5E9

    // Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);       // #10B981
    public static final Color WARNING = new Color(245, 158, 11);       // #F59E0B
    public static final Color DANGER = new Color(239, 68, 68);         // #EF4444
    public static final Color INFO = new Color(59, 130, 246);          // #3B82F6

    // Neutral Surfaces & Text
    public static final Color BG_MAIN = new Color(248, 250, 252);      // #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // #E2E8F0
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // #0F172A
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // #64748B
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // #94A3B8

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VAL = new Font("Segoe UI", Font.BOLD, 26);

    // Common Borders
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(16, 18, 16, 18)
    );

    public static final Border BORDER_INPUT = BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(8, 12, 8, 12)
    );

    /**
     * Initializes FlatLaf if available on classpath; otherwise configures modern UI defaults.
     */
    public static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf Light Look & Feel
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            LookAndFeel laf = (LookAndFeel) flatLafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
            return;
        } catch (Throwable ignored) {
            // FlatLaf not present; fall back to Nimbus or System LAF
        }

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Apply smooth font rendering hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
}
