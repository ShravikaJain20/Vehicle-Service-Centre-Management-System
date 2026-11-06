package ui.theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class ModernTable extends JTable {

    public ModernTable() {
        super();
        initStyle();
    }

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setFont(UITheme.FONT_BODY);
        setRowHeight(36);
        setShowGrid(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionBackground(new Color(219, 234, 254)); // Light Blue
        setSelectionForeground(UITheme.TEXT_PRIMARY);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(UITheme.FONT_BODY_BOLD);
        header.setBackground(UITheme.NAVY_SIDEBAR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(UITheme.NAVY_SIDEBAR);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UITheme.FONT_BODY_BOLD);
                lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                return lbl;
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        if (!isRowSelected(row)) {
            // Alternating Row Striping
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            c.setForeground(UITheme.TEXT_PRIMARY);
        }

        // Padding
        if (c instanceof JComponent) {
            ((JComponent) c).setBorder(new EmptyBorder(0, 12, 0, 12));
        }

        // Status Badge rendering if text looks like a status
        Object val = getValueAt(row, column);
        if (val != null) {
            String str = val.toString();
            if (isStatusValue(str)) {
                return renderStatusBadge(str, isRowSelected(row));
            }
        }

        return c;
    }

    private boolean isStatusValue(String s) {
        return s.equals("Active") || s.equals("Overdue Service") || s.equals("Service Due") || s.equals("Upcoming Service") ||
               s.equals("Paid") || s.equals("Unpaid") || s.equals("Partially Paid") ||
               s.equals("Completed") || s.equals("Pending") || s.equals("In Progress") || s.equals("Confirmed") || s.equals("Cancelled") ||
               s.equals("Available") || s.equals("Busy") || s.equals("On Leave") || s.equals("Resolved");
    }

    private Component renderStatusBadge(String status, boolean isSelected) {
        JLabel badge = new JLabel(status, SwingConstants.CENTER);
        badge.setFont(UITheme.FONT_SMALL);
        badge.setOpaque(true);

        Color bg;
        Color fg;

        switch (status) {
            case "Overdue Service":
            case "Cancelled":
            case "Unpaid":
                bg = new Color(254, 226, 226); // Light Red
                fg = UITheme.DANGER;
                break;
            case "Service Due":
            case "Partially Paid":
            case "Busy":
            case "Pending":
                bg = new Color(254, 243, 199); // Light Amber
                fg = new Color(180, 83, 9);
                break;
            case "Upcoming Service":
            case "In Progress":
            case "Confirmed":
                bg = new Color(224, 242, 254); // Light Cyan
                fg = UITheme.PRIMARY_BLUE;
                break;
            case "Paid":
            case "Completed":
            case "Available":
            case "Resolved":
            default:
                bg = new Color(209, 250, 229); // Light Green
                fg = new Color(4, 120, 87);
                break;
        }

        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        return badge;
    }
}
