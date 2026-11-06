package ui.components;

import ui.theme.ModernButton;
import ui.theme.ModernTextField;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class SearchBar extends JPanel {

    private final ModernTextField txtSearch;
    private final ModernButton btnSearch;
    private final ModernButton btnReset;
    private Consumer<String> onSearchCallback;

    public SearchBar(String placeholder) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        txtSearch = new ModernTextField(placeholder);
        txtSearch.setPreferredSize(new Dimension(280, 36));

        btnSearch = new ModernButton("Search", ModernButton.ButtonType.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(90, 36));

        btnReset = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);
        btnReset.setPreferredSize(new Dimension(80, 36));

        add(txtSearch);
        add(btnSearch);
        add(btnReset);

        btnSearch.addActionListener(e -> triggerSearch());
        txtSearch.addActionListener(e -> triggerSearch());

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            if (onSearchCallback != null) {
                onSearchCallback.accept("");
            }
        });
    }

    public void setOnSearch(Consumer<String> callback) {
        this.onSearchCallback = callback;
    }

    private void triggerSearch() {
        if (onSearchCallback != null) {
            onSearchCallback.accept(txtSearch.getText().trim());
        }
    }

    public String getText() {
        return txtSearch.getText().trim();
    }
}
