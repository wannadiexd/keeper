package com.keeper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.keeper.ui.MainWindow;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MainWindow window = new MainWindow();
        Scene scene = new Scene(window.getRoot(), 860, 520);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setTitle("Keeper");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(420);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}