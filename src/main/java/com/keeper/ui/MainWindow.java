package com.keeper.ui;

import com.keeper.crypto.Crypto;
import com.keeper.db.Database;
import com.keeper.model.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainWindow {

    private BorderPane root;
    private Database db;
    private Crypto crypto;
    private ObservableList<Entry> entries;
    private ListView<Entry> listView;
    private VBox detailPane;

    public MainWindow() throws Exception {
        crypto = new Crypto("masterpassword"); // временно, потом заменим на мастер-пароль
        db = new Database();
        entries = FXCollections.observableArrayList(db.getAll());
        build();
    }

    private void build() {
        root = new BorderPane();

        // sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setPadding(new Insets(16));
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("Keeper");
        logo.getStyleClass().add("logo");

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((obs, old, val) -> filter(val));

        Button addBtn = new Button("+ New entry");
        addBtn.getStyleClass().add("add-btn");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> showAddDialog());

        sidebar.getChildren().addAll(logo, search, addBtn);

        // list
        listView = new ListView<>(entries);
        listView.getStyleClass().add("entry-list");
        listView.setCellFactory(lv -> new EntryCell());
        listView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, val) -> showDetail(val)
        );

        // detail
        detailPane = new VBox(12);
        detailPane.setPrefWidth(260);
        detailPane.setPadding(new Insets(16));
        detailPane.getStyleClass().add("detail-pane");

        root.setLeft(sidebar);
        root.setCenter(listView);
        root.setRight(detailPane);
    }

    private void filter(String query) {
        try {
            ObservableList<Entry> all = FXCollections.observableArrayList(db.getAll());
            if (query == null || query.isBlank()) {
                entries.setAll(all);
            } else {
                String q = query.toLowerCase();
                entries.setAll(all.filtered(e ->
                    e.getTitle().toLowerCase().contains(q) ||
                    e.getUsername().toLowerCase().contains(q)
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDetail(Entry e) {
        detailPane.getChildren().clear();
        if (e == null) return;

        Label title = new Label(e.getTitle());
        title.getStyleClass().add("detail-title");

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("field-label");
        Label userValue = new Label(e.getUsername());
        userValue.getStyleClass().add("field-value");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("field-label");
        Label passValue = new Label("••••••••");
        passValue.getStyleClass().add("field-value");

        Label urlLabel = new Label("Website");
        urlLabel.getStyleClass().add("field-label");
        Label urlValue = new Label(e.getUrl());
        urlValue.getStyleClass().add("field-value");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setOnAction(ev -> showEditDialog(e));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(ev -> deleteEntry(e));

        HBox btns = new HBox(8, editBtn, deleteBtn);

        detailPane.getChildren().addAll(
            title, userLabel, userValue,
            passLabel, passValue,
            urlLabel, urlValue,
            btns
        );
    }

    private void showAddDialog() {
        Dialog<Entry> dialog = buildDialog(null);
        dialog.showAndWait().ifPresent(e -> {
            try {
                db.add(e);
                entries.setAll(db.getAll());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void showEditDialog(Entry e) {
        Dialog<Entry> dialog = buildDialog(e);
        dialog.showAndWait().ifPresent(updated -> {
            try {
                db.update(updated);
                entries.setAll(db.getAll());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void deleteEntry(Entry e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + e.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    db.delete(e.getId());
                    entries.setAll(db.getAll());
                    detailPane.getChildren().clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private Dialog<Entry> buildDialog(Entry existing) {
        Dialog<Entry> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New entry" : "Edit entry");

        TextField titleF = new TextField(existing != null ? existing.getTitle() : "");
        titleF.setPromptText("Title");
        TextField userF = new TextField(existing != null ? existing.getUsername() : "");
        userF.setPromptText("Username");
        PasswordField passF = new PasswordField();
        passF.setPromptText("Password");
        TextField urlF = new TextField(existing != null ? existing.getUrl() : "");
        urlF.setPromptText("Website");

        VBox content = new VBox(8, titleF, userF, passF, urlF);
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Entry e = existing != null ? existing : new Entry();
                e.setTitle(titleF.getText());
                e.setUsername(userF.getText());
                e.setPassword(passF.getText());
                e.setUrl(urlF.getText());
                e.setCategory("Website");
                return e;
            }
            return null;
        });

        return dialog;
    }

    public BorderPane getRoot() { return root; }
}