package ui.theme;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class ModernButton extends JButton {

    public enum ButtonType {
        PRIMARY, SECONDARY, DANGER, SUCCESS, WARNING
    }

    private final ButtonType type;
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 8;

    public ModernButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public ModernButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        initColors();

        setFont(UITheme.FONT_BODY_BOLD);
        setForeground(textColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    private void initColors() {
        switch (type) {
            case PRIMARY:
                normalColor = UITheme.PRIMARY_BLUE;
                hoverColor = UITheme.PRIMARY_HOVER;
                pressedColor = new Color(30, 64, 175);
                textColor = Color.WHITE;
                break;
            case DANGER:
                normalColor = UITheme.DANGER;
                hoverColor = new Color(220, 38, 38);
                pressedColor = new Color(185, 28, 28);
                textColor = Color.WHITE;
                break;
            case SUCCESS:
                normalColor = UITheme.SUCCESS;
                hoverColor = new Color(5, 150, 105);
                pressedColor = new Color(4, 120, 87);
                textColor = Color.WHITE;
                break;
            case WARNING:
                normalColor = UITheme.WARNING;
                hoverColor = new Color(217, 119, 6);
                pressedColor = new Color(180, 83, 9);
                textColor = Color.WHITE;
                break;
            case SECONDARY:
            default:
                normalColor = Color.WHITE;
                hoverColor = new Color(241, 245, 249);
                pressedColor = new Color(226, 232, 240);
                textColor = UITheme.TEXT_PRIMARY;
                break;
        }
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = normalColor;
        if (!isEnabled()) {
            bg = UITheme.BORDER_COLOR;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            super.paintComponent(g);
            g2.dispose();
            return;
        } else if (isPressed) {
            bg = pressedColor;
        } else if (isHovered) {
            bg = hoverColor;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Border for secondary buttons
        if (type == ButtonType.SECONDARY) {
            g2.setColor(UITheme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
