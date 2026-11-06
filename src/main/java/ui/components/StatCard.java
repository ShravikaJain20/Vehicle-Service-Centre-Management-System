package ui.components;

import ui.theme.ModernCard;
import ui.theme.UITheme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class StatCard extends ModernCard {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String icon, String title, String value, String subtitle, Color accentColor) {
        super(new BorderLayout(12, 8));
        this.accentColor = accentColor;

        // Top Row: Title + Icon Badge
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        topPanel.add(lblIcon, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: Primary Value
        lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_STAT_VAL);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);
        lblValue.setBorder(new EmptyBorder(4, 0, 4, 0));
        add(lblValue, BorderLayout.CENTER);

        // Bottom: Subtitle / trend
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        add(lblSubtitle, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(190, 115));
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint top accent line
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(16, 4, getWidth() - 32, 4, 2, 2);
        g2.dispose();
    }
}
