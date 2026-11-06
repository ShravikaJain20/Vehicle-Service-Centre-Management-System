package ui.theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ModernCard extends JPanel {

    private int cornerRadius = 12;
    private Color cardBackground = Color.WHITE;

    public ModernCard() {
        this(new BorderLayout());
    }

    public ModernCard(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    public void setCardBackground(Color bg) {
        this.cardBackground = bg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(cardBackground);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Subtle border
        g2.setColor(UITheme.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
