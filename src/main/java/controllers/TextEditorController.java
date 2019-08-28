package controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import objects.TextEditorPane;

import java.io.*;

import static utilities.TextEditorPaneUtilities.hasTxtExtension;
import static utilities.TextEditorPaneUtilities.getFileName;

public class TextEditorController {

    @FXML
    private TextEditorPane root;

    private String stageTitle;

    // Stage properties
    private final int STAGE_WIDTH = 650;
    private final int STAGE_HEIGHT = 475;

    public void initialize() {
        root.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        Stage stage = (Stage) newWindow;

                        if (stageTitle != null) {
                            stage.setTitle(stageTitle + " - RandEDT");
                        } else {
                            stage.setTitle("untitled - RandEDT");
                        }
                        stage.setWidth(STAGE_WIDTH);
                        stage.setHeight(STAGE_HEIGHT);
                        stage.initStyle(StageStyle.DECORATED);
                        root.getStylesheets().addAll(
                                getClass().getResource("/css/style.css").toExternalForm(),
                                getClass().getResource("/css/textEditor.css").toExternalForm()
                        );

                        /*
                        stage.getIcons().add(new Image(
                                getClass().getResource("/images/dice2.png").toExternalForm()
                        ));
                         */
                    }
                });
            }
        });

    }

    public void initController(String parameters) {
        if (parameters == null)
            return;

        File paramFile = new File(parameters);
        if (!hasTxtExtension(paramFile) || !paramFile.canExecute())
            return;

        stageTitle = getFileName(paramFile);

        root.setFile(paramFile);
    }

    @FXML
    private void newAction() {
        root.newEvent();
    }

    @FXML
    private void openEvent() {
        root.openEvent();
    }

    @FXML
    private void saveAction() {
        root.saveEvent();
    }

    @FXML
    private void saveAsAction() {
        root.saveAsEvent();
    }

    @FXML
    private void exitAction() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void deleteAction() {
        System.out.println("delete!");
    }

    @FXML
    private void cutAction() {
        System.out.println("cut!");
    }
}