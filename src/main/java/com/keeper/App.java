package com.keeper;

import com.keeper.crypto.Crypto;
import com.keeper.db.Database;
import com.keeper.ui.MainWindow;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Base64;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        showLoginScreen(stage);
    }

    private void showLoginScreen(Stage stage) {
        Stage loginStage = new Stage();
        loginStage.setTitle("Keeper — Unlock");
        loginStage.setResizable(false);

        Label titleLabel = new Label("Keeper");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #a78bfa;");

        Label subtitle = new Label("Enter master password to continue");
        subtitle.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Master password");
        passField.setMaxWidth(280);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        Button unlockBtn = new Button("Unlock");
        unlockBtn.setMaxWidth(280);
        unlockBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 0 8 0; -fx-background-radius: 8;");

        VBox box = new VBox(12, titleLabel, subtitle, passField, errorLabel, unlockBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e2e;");
        box.setPrefWidth(360);

        unlockBtn.setOnAction(e -> tryUnlock(loginStage, stage, passField, errorLabel));
        passField.setOnAction(e -> tryUnlock(loginStage, stage, passField, errorLabel));

        Scene scene = new Scene(box);
        loginStage.setScene(scene);
        loginStage.show();
    }

    private void tryUnlock(Stage loginStage, Stage mainStage, PasswordField passField, Label errorLabel) {
        String password = passField.getText();
        if (password.isBlank()) {
            errorLabel.setText("Password cannot be empty");
            return;
        }

        try {
            String dbPath = System.getProperty("user.home") + "/Keeper/keeper.db";
            boolean isNewDb = !new java.io.File(dbPath).exists();

            byte[] salt;
            Database tempDb = null;

            if (isNewDb) {
                salt = Crypto.generateSalt();
                Crypto crypto = new Crypto(password, salt);
                Database db = new Database(crypto);
                db.setMeta("salt", Base64.getEncoder().encodeToString(salt));
                db.setMeta("password_hash", Crypto.hashPassword(password, salt));
                openMainWindow(loginStage, mainStage, crypto, db);
            } else {
                Crypto dummyCrypto = new Crypto(password, new byte[16]);
                tempDb = new Database(dummyCrypto);
                String saltStr = tempDb.getMeta("salt");
                String storedHash = tempDb.getMeta("password_hash");
                tempDb.close();

                if (saltStr == null || storedHash == null) {
                    errorLabel.setText("Database corrupted");
                    return;
                }

                salt = Base64.getDecoder().decode(saltStr);
                String inputHash = Crypto.hashPassword(password, salt);

                if (!inputHash.equals(storedHash)) {
                    errorLabel.setText("Wrong password");
                    passField.clear();
                    return;
                }

                Crypto crypto = new Crypto(password, salt);
                Database db = new Database(crypto);
                openMainWindow(loginStage, mainStage, crypto, db);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void openMainWindow(Stage loginStage, Stage mainStage, Crypto crypto, Database db) throws Exception {
        MainWindow window = new MainWindow(crypto, db);
        Scene scene = new Scene(window.getRoot(), 960, 580);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        mainStage.setTitle("Keeper");
        mainStage.setScene(scene);
        mainStage.setMinWidth(750);
        mainStage.setMinHeight(480);
        mainStage.show();
        loginStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}