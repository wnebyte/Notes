package com.github.wnebyte.notes.controller;

import com.github.wnebyte.notes.util.Checksum;
import com.github.wnebyte.notes.ui.Editor;
import com.github.wnebyte.notes.io.Repository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.github.wnebyte.notes.ui.SaveChangesAlert;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class EditorController {

    @FXML
    private Editor editor;

    private File dir;

    private String snapshot;

    private Stage stage;

    private File content;

    private final SimpleObjectProperty<File> contentProperty = new SimpleObjectProperty<>(null);

    private final Repository repository = Repository.getInstance();

    private final Alert alert = new SaveChangesAlert(Alert.AlertType.CONFIRMATION);

    /*
    used to determine whether unsaved changes have been made to the currently loaded file.
     */
    private final Checksum<String> version = new Checksum<String>() {
        @Override
        public boolean hasChanged(final String content, final String snapshot) {
            if ((content != null) && (snapshot != null)) {
                return !(content.equals(snapshot));
            }
            return (content != null) && !(content.equals(""));
        }
    };

    /**
     * Constructs a new instance.
     */
    public EditorController() { }

    /**
     * Initializes this Controller.
     */
    public void initialize() {
        /*
        initialize the initialDirectory to be used by the fileChooser.
         */
        String username = System.getProperty("user.name");
        File dir = new File("C:"
                .concat(File.separator)
                .concat("Users")
                .concat(File.separator)
                .concat(username)
                .concat(File.separator)
                .concat("Desktop")
        );
        if ((dir.exists()) && (dir.isDirectory())) {
            this.dir = dir;
        } else {
            File[] roots = File.listRoots();
            if (1 <= roots.length) {
                this.dir = roots[0];
            }
        }
        /*
        add a changeListener to the contentProperty to update the content and stage title
        whenever a new file has been loaded.
         */
        contentProperty.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                if (newValue != null) {
                    content = newValue;
                    stage.setTitle(content.getName());
                    snapshot = editor.getText();
                }
            }
        });
        /*
        add a changeListener to the scene and stage properties to assign the stage to an instance variable.
         */
        editor.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(
                    ObservableValue<? extends Scene> observableScene,
                    Scene oldScene,
                    Scene newScene
            ) {
                if ((oldScene == null) && (newScene != null)) {
                    newScene.windowProperty().addListener(new ChangeListener<Window>() {
                        @Override
                        public void changed(
                                ObservableValue<? extends Window> observableWindow,
                                Window oldWindow,
                                Window newWindow
                        ) {
                            if ((oldWindow == null) && (newWindow != null)) {
                                stage = (Stage) newWindow;
                                stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, EditorController.this::close);
                                alert.initOwner(stage);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Lets the user specify a file to be loaded from the filesystem.
     */
    @FXML
    private void openFile() {
        if (version.hasChanged(editor.getText(), snapshot)) {
            switch (promptSaveChanges()) {
                case YES:
                    saveFile();
                    break;
                case CANCEL_CLOSE:
                    return;
            }
        }
        FileChooser fileChooser = new FileChooser();
        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if ((content != null) && (content.equals(file))) {
                return;
            }
            List<String> contents = repository.read(file);
            editor.replace(contents);
            contentProperty.setValue(file);
        }
    }

    /**
     * Lets the user save the loaded file to the filesystem at a location of their choosing.
     */
    @FXML
    private void saveFileAs() {
        String fileName = (content != null) ? content.getName() : "untitled.txt";
        FileChooser fileChooser = new FileChooser();
        if (dir != null) {
            fileChooser.setInitialDirectory(dir);
        }
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            contentProperty.setValue(file);
            saveFile();
        }
    }

    /**
     * Lets the user save the loaded file to the filesystem.
     */
    @FXML
    private void saveFile() {
        if (content == null) {
            saveFileAs();
        }
        else {
            repository.write(content, editor.getParagraphs());
            snapshot = editor.getText();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void close() {
        if (version.hasChanged(editor.getText(), snapshot)) {
            switch (promptSaveChanges()) {
                case YES:
                    saveFile();
                    break;
                case CANCEL_CLOSE:
                    return;
            }
        }
        stage.close();
        System.exit(1);
    }

    private void close(final WindowEvent event) {
        if (version.hasChanged(editor.getText(), snapshot)) {
            switch (promptSaveChanges()) {
                case YES:
                    saveFile();
                    break;
                case CANCEL_CLOSE:
                    if (event != null) {
                        event.consume();
                    }
                    return;
            }
        }
        stage.close();
        System.exit(1);
    }

    /**
     * Transfers the currently selected text to the clipboard, removing the current selection.
     */
    @FXML
    private void cut() {
        editor.cut();
    }

    /**
     * Transfers the currently selected text to the clipboard, leaving the current selection.
     */
    @FXML
    private void copy() {
        editor.copy();
    }

    /**
     * Inserts the content from the clipboard into the editor.
     */
    @FXML
    private void paste() {
        editor.paste();
    }

    /**
     * Undoes the previous modification to the editor.
     */
    @FXML
    private void undo() {
        editor.undo();
    }

    /**
     * Clears the editor.
     */
    @FXML
    private void delete() {
        editor.delete();
    }

    /**
     * Prompts the user whether they'd like to save changes made to the currently loaded file.
     * @return the user initiated result
     */
    private ButtonBar.ButtonData promptSaveChanges() {
        alert.setHeaderText("Do you want to save changes to " + stage.getTitle());
        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent()) ? result.get().getButtonData() : ButtonBar.ButtonData.CANCEL_CLOSE;
    }
}