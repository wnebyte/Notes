package com.github.wnebyte.notes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Notes extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/Editor.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 775, 550);
        stage.setScene(scene);
        stage.setTitle("#otes");
        stage.show();
    }
}
