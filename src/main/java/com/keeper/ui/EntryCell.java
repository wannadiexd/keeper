package com.keeper.ui;

import com.keeper.model.Entry;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EntryCell extends ListCell<Entry> {

    private VBox box;
    private Label title;
    private Label username;

    public EntryCell() {
        title = new Label();
        title.getStyleClass().add("cell-title");
        username = new Label();
        username.getStyleClass().add("cell-username");
        box = new VBox(2, title, username);
        box.setPadding(new javafx.geometry.Insets(6, 8, 6, 8));
    }

    @Override
    protected void updateItem(Entry e, boolean empty) {
        super.updateItem(e, empty);
        if (empty || e == null) {
            setGraphic(null);
        } else {
            title.setText(e.getTitle());
            username.setText(e.getUsername());
            setGraphic(box);
        }
    }
}