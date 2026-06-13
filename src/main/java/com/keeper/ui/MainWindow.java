package com.keeper.ui;

import com.keeper.crypto.Crypto;
import com.keeper.db.Database;
import com.keeper.model.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;

import java.util.List;
import java.util.stream.Collectors;

public class MainWindow {

    private BorderPane root;
    private Database db;
    private Crypto crypto;
    private ObservableList<Entry> entries;
    private TableView<Entry> tableView;
    private VBox detailPane;
    private TreeView<String> groupTree;

    public MainWindow(Crypto crypto, Database db) throws Exception {
        this.crypto = crypto;
        this.db = db;
        entries = FXCollections.observableArrayList(db.getAll());
        build();
    }

    private void build() {
        root = new BorderPane();

        TreeItem<String> rootItem = new TreeItem<>("All Entries");
        rootItem.setExpanded(true);

        String[] groups = {"Website", "Email", "Banking", "Work", "Other"};
        for (String g : groups) {
            rootItem.getChildren().add(new TreeItem<>(g));
        }

        groupTree = new TreeView<>(rootItem);
        groupTree.getStyleClass().add("group-tree");
        groupTree.setPrefWidth(190);
        groupTree.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            String selected = val.getValue();
            try {
                List<Entry> all = db.getAll();
                if (selected.equals("All Entries")) {
                    entries.setAll(all);
                } else {
                    entries.setAll(all.stream()
                        .filter(e -> selected.equals(e.getCategory()))
                        .collect(Collectors.toList()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox leftPanel = new VBox();
        leftPanel.getStyleClass().add("sidebar");

        Label logo = new Label("Keeper");
        logo.getStyleClass().add("logo");

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((obs, old, val) -> filter(val));

        Button addBtn = new Button("+ New Entry");
        addBtn.getStyleClass().add("add-btn");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> showAddDialog());

        VBox.setMargin(logo, new Insets(16, 16, 8, 16));
        VBox.setMargin(search, new Insets(0, 16, 8, 16));
        VBox.setMargin(addBtn, new Insets(0, 16, 12, 16));
        VBox.setMargin(groupTree, new Insets(0, 0, 0, 0));

        leftPanel.getChildren().addAll(logo, search, addBtn, groupTree);

        TableColumn<Entry, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(180);

        TableColumn<Entry, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setPrefWidth(160);

        TableColumn<Entry, String> passCol = new TableColumn<>("Password");
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passCol.setPrefWidth(140);
        passCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "••••••••");
            }
        });

        TableColumn<Entry, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlCol.setPrefWidth(180);

        TableColumn<Entry, String> categoryCol = new TableColumn<>("Group");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);

        tableView = new TableView<>(entries);
        tableView.getStyleClass().add("entry-table");
        tableView.getColumns().addAll(titleCol, userCol, passCol, urlCol, categoryCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, val) -> showDetail(val)
        );

        tableView.setRowFactory(tv -> {
            TableRow<Entry> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            return row;
        });

        detailPane = new VBox(10);
        detailPane.setPrefWidth(240);
        detailPane.setPadding(new Insets(16));
        detailPane.getStyleClass().add("detail-pane");

        root.setLeft(leftPanel);
        root.setCenter(tableView);
        root.setRight(detailPane);
    }

    private void filter(String query) {
        try {
            List<Entry> all = db.getAll();
            if (query == null || query.isBlank()) {
                entries.setAll(all);
            } else {
                String q = query.toLowerCase();
                entries.setAll(all.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(q) ||
                                 e.getUsername().toLowerCase().contains(q) ||
                                 (e.getUrl() != null && e.getUrl().toLowerCase().contains(q)))
                    .collect(Collectors.toList()));
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
        HBox userRow = fieldRow(e.getUsername(), false);

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("field-label");
        HBox passRow = passwordRow(e.getPassword());

        Label urlLabel = new Label("URL");
        urlLabel.getStyleClass().add("field-label");
        HBox urlRow = fieldRow(e.getUrl(), false);

        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("field-label");
        Label notesValue = new Label(e.getNotes() != null ? e.getNotes() : "");
        notesValue.getStyleClass().add("field-value");
        notesValue.setWrapText(true);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setOnAction(ev -> showEditDialog(e));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(ev -> deleteEntry(e));

        HBox btns = new HBox(8, editBtn, deleteBtn);

        detailPane.getChildren().addAll(
            title,
            userLabel, userRow,
            passLabel, passRow,
            urlLabel, urlRow,
            notesLabel, notesValue,
            new Separator(),
            btns
        );
    }

    private HBox fieldRow(String value, boolean hidden) {
        Label val = new Label(hidden ? "••••••••" : (value != null ? value : ""));
        val.getStyleClass().add("field-value");
        HBox.setHgrow(val, Priority.ALWAYS);

        Button copyBtn = new Button("⎘");
        copyBtn.getStyleClass().add("copy-btn");
        copyBtn.setTooltip(new Tooltip("Copy"));
        copyBtn.setOnAction(e -> copyToClipboard(value != null ? value : ""));

        HBox row = new HBox(6, val, copyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox passwordRow(String password) {
        Label val = new Label("••••••••");
        val.getStyleClass().add("field-value");
        HBox.setHgrow(val, Priority.ALWAYS);

        final boolean[] visible = {false};

        Button showBtn = new Button("👁");
        showBtn.getStyleClass().add("copy-btn");
        showBtn.setTooltip(new Tooltip("Show/Hide"));
        showBtn.setOnAction(e -> {
            visible[0] = !visible[0];
            val.setText(visible[0] ? password : "••••••••");
        });

        Button copyBtn = new Button("⎘");
        copyBtn.getStyleClass().add("copy-btn");
        copyBtn.setTooltip(new Tooltip("Copy"));
        copyBtn.setOnAction(e -> copyToClipboard(password));

        HBox row = new HBox(6, val, showBtn, copyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void copyToClipboard(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
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
                showDetail(updated);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void deleteEntry(Entry e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete \"" + e.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
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
        dialog.setTitle(existing == null ? "New Entry" : "Edit Entry");
        dialog.setHeaderText(null);

        TextField titleF  = new TextField(existing != null ? existing.getTitle()    : "");
        TextField userF   = new TextField(existing != null ? existing.getUsername() : "");
        TextField urlF    = new TextField(existing != null ? existing.getUrl()      : "");
        TextArea  notesF  = new TextArea(existing  != null ? existing.getNotes()    : "");
        notesF.setPrefRowCount(3);

        PasswordField passF   = new PasswordField();
        TextField     passVis = new TextField();
        if (existing != null) {
            passF.setText(existing.getPassword());
            passVis.setText(existing.getPassword());
        }
        passVis.setVisible(false);
        passVis.setManaged(false);
        passF.textProperty().bindBidirectional(passVis.textProperty());

        FontIcon eyeIcon = new FontIcon(MaterialDesignE.EYE);
        eyeIcon.setIconSize(16);
        eyeIcon.setIconColor(javafx.scene.paint.Color.web("#a78bfa"));
        Button togglePass = new Button();
        togglePass.setGraphic(eyeIcon);
        togglePass.getStyleClass().add("copy-btn");
        togglePass.setOnAction(e -> {
            boolean show = !passVis.isVisible();
            passVis.setVisible(show);
            passVis.setManaged(show);
            passF.setVisible(!show);
            passF.setManaged(!show);
        });

        FontIcon refreshIcon = new FontIcon(MaterialDesignR.REFRESH);
        refreshIcon.setIconSize(16);
        refreshIcon.setIconColor(javafx.scene.paint.Color.web("#a78bfa"));
        Button genBtn = new Button();
        genBtn.setGraphic(refreshIcon);
        genBtn.getStyleClass().add("generate-btn");
        genBtn.setOnAction(e -> {
            String generated = generatePassword(16);
            passF.setText(generated);
            passVis.setText(generated);
        });

        HBox passRow = new HBox(6, passF, passVis, togglePass, genBtn);
        HBox.setHgrow(passF, Priority.ALWAYS);
        HBox.setHgrow(passVis, Priority.ALWAYS);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Website", "Email", "Banking", "Work", "Other");
        categoryBox.setValue(existing != null && existing.getCategory() != null
            ? existing.getCategory() : "Website");

        titleF.setPromptText("Title");
        userF.setPromptText("Username");
        passF.setPromptText("Password");
        passVis.setPromptText("Password");
        urlF.setPromptText("URL");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        ColumnConstraints col1 = new ColumnConstraints(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(new Label("Title"),    0, 0); grid.add(titleF,      1, 0);
        grid.add(new Label("Username"), 0, 1); grid.add(userF,       1, 1);
        grid.add(new Label("Password"), 0, 2); grid.add(passRow,     1, 2);
        grid.add(new Label("URL"),      0, 3); grid.add(urlF,        1, 3);
        grid.add(new Label("Group"),    0, 4); grid.add(categoryBox, 1, 4);
        grid.add(new Label("Notes"),    0, 5); grid.add(notesF,      1, 5);
        GridPane.setHgrow(titleF, Priority.ALWAYS);
        GridPane.setHgrow(userF,  Priority.ALWAYS);
        GridPane.setHgrow(urlF,   Priority.ALWAYS);
        GridPane.setHgrow(notesF, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(460);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Entry e = existing != null ? existing : new Entry();
                e.setTitle(titleF.getText());
                e.setUsername(userF.getText());
                e.setPassword(passF.getText());
                e.setUrl(urlF.getText());
                e.setNotes(notesF.getText());
                e.setCategory(categoryBox.getValue());
                return e;
            }
            return null;
        });

        return dialog;
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom rng = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public BorderPane getRoot() { return root; }
}