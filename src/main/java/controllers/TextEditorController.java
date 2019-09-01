package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import objects.TextEditorPane;

import java.io.*;
import java.util.Optional;

import static io.ReadWrite.read;
import static io.ReadWrite.write;
import static utilities.TextEditorPaneUtilities.*;

public class TextEditorController {

    @FXML
    private TextEditorPane root;

    @FXML
    private MenuItem exitMenuItem;

    private File savedFile;

    private final String startTitle
            = "untitled - RandEDT";

    private Stage stage;

    // Stage properties
    private final int STAGE_WIDTH = 650;
    private final int STAGE_HEIGHT = 475;

    public void initialize() {
        root.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        stage = (Stage) newWindow;

                        exitMenuItem.setOnAction(e -> exitAction(new WindowEvent(
                                stage, WindowEvent.WINDOW_CLOSE_REQUEST
                        )));

                        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::exitAction);

                        if (savedFile != null) {
                            stage.setTitle(getTitle(savedFile));
                        } else {
                            stage.setTitle(startTitle);
                        }
                        stage.setWidth(STAGE_WIDTH);
                        stage.setHeight(STAGE_HEIGHT);
                        stage.initStyle(StageStyle.DECORATED);
                        root.getStylesheets().addAll(
                                getClass().getResource("/css/style.css").toExternalForm(),
                                getClass().getResource("/css/textEditor.css").toExternalForm()
                        );
                        stage.getIcons().add(new Image(
                                getClass().getResource("/images/dice2.png").toExternalForm()
                        ));

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

        savedFile = paramFile;

        root.displayText(read(paramFile));

    }

    @FXML
    private void newAction() {
        ButtonBar.ButtonData userInput = confirm();

        if (userInput == ButtonBar.ButtonData.YES) {
            saveAction();
            root.resetEditor();
            savedFile = null;
            stage.setTitle(startTitle);
        }

        else if (userInput == ButtonBar.ButtonData.NO) {
            root.resetEditor();
            savedFile = null;
            stage.setTitle(startTitle);
        }

        else if (userInput == ButtonBar.ButtonData.CANCEL_CLOSE) {
            // do nothing, return
        }

    }

    @FXML
    private void openEvent() {
        ButtonBar.ButtonData userInput = confirm();

        if (userInput == ButtonBar.ButtonData.YES) {
            saveAction();
        }

        else if (userInput == ButtonBar.ButtonData.CANCEL_CLOSE) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.setInitialDirectory(new File("C:/users/wne-/dev"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            if (hasTxtExtension(file)) {
                savedFile = file;

                root.displayText(read(savedFile));
                stage.setTitle(getTitle(savedFile));
            }
        }
    }

    @FXML
    private void saveAsAction() {
        String initFileName;

        if (savedFile != null) {
            initFileName = savedFile.getName();
        } else {
            initFileName = "untitled.txt";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(initFileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        fileChooser.setInitialDirectory(new File("C:/Users/wne-/dev"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            if (hasTxtExtension(file)) {
                savedFile = file;

                write(savedFile, root.getParagraphs());
                stage.setTitle(getTitle(savedFile));
            }
        }
    }

    @FXML
    private void saveAction() {
        if (savedFile != null) {
            if (hasTxtExtension(savedFile)) {
                write(savedFile, root.getParagraphs());
            }
        } else {
            saveAsAction();
        }
    }

    @FXML
    private void exitAction(WindowEvent event) {
        ButtonBar.ButtonData userInput = confirm();

        if (userInput == ButtonBar.ButtonData.YES) {
            saveAction();
        }

        else if (userInput == ButtonBar.ButtonData.CANCEL_CLOSE) {
            if (event != null)
                event.consume();
            return;
        }

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


    @FXML
    public void aboutAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(stage);
        alert.setTitle("About");

        alert.setHeaderText("RandEDT");
        alert.getDialogPane().setGraphic(null);
        alert.setContentText("version 1.0.1\n" +
                "2019-09-01");

        alert.showAndWait();
    }

    private ButtonBar.ButtonData confirm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.DECORATED);
        alert.initOwner(stage);
        alert.setTitle("RandEDT");

        alert.setHeaderText(null);
        alert.getDialogPane().setGraphic(null);
        alert.setHeaderText("Do you want to save changes to " + stage.getTitle().split(" -")[0] + "?");

        alert.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        Button okButton     = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        Button closeButton  = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);

        okButton.setText("Save");
        closeButton.setText("Don't Save");
        cancelButton.setText("Cancel");

        okButton.getStyleClass().add("ok-button");
        closeButton.getStyleClass().add("close-button");
        cancelButton.getStyleClass().add("cancel-button");

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/alert.css").toExternalForm()
        );

        Optional<ButtonType> input = alert.showAndWait();

        if (input.isPresent()) {
            return input.get().getButtonData();
        } else {
            return ButtonBar.ButtonData.CANCEL_CLOSE;
        }
    }
}