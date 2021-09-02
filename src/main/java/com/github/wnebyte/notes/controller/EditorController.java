package com.github.wnebyte.notes.controller;

import com.github.wnebyte.notes.util.Revision;
import com.github.wnebyte.notes.ui.Editor;
import com.github.wnebyte.notes.io.Repository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.github.wnebyte.notes.ui.SaveChangesAlert;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class EditorController {

    private final Repository repository = Repository.getInstance();

    private final Alert alert = new SaveChangesAlert(Alert.AlertType.CONFIRMATION);

    @FXML
    private Editor editor;

    private File dir;

    private File content;

    private final SimpleObjectProperty<File> contentProperty = new SimpleObjectProperty<>(null);

    private String snapshot;

    private Stage stage;

    private final Revision<String> version = new Revision<String>() {
        @Override
        public boolean hasChanged(final String content, final String snapshot) {
            if ((content != null) && (snapshot != null)) {
                return !(content.equals(snapshot));
            }
            return (content != null) && !(content.equals(""));
        }
    };

    public EditorController() {}

    public void initialize() {
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
                                alert.initOwner(stage);
                            }
                        }
                    });
                }
            }
        });
    }

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
        fileChooser.setTitle("Open");
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

    @FXML
    private void closeFile() {
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

    @FXML
    private void saveFile() {
        if (content == null) {
            saveFileAs();
        }
        else {
            repository.write(content, editor.getParagraphs());
        }
    }

    @FXML
    private void saveFileAs() {
        String fileName = (content != null) ? content.getName() : "untitled.txt";
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(dir);
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            contentProperty.setValue(file);
            saveFile();
        }
    }

    // Todo: accelerator events are eaten up by the editor
    @FXML
    private void cut() {
        editor.cut();
    }

    @FXML
    private void copy() {
        editor.copy();
    }

    @FXML
    private void paste() {
        editor.paste();
    }

    @FXML
    private void undo() {
        editor.undo();
    }

    @FXML
    private void delete() {
        editor.delete();
    }

    private ButtonBar.ButtonData promptSaveChanges() {
        alert.setHeaderText("Do you want to save changes to " + stage.getTitle());
        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent()) ? result.get().getButtonData() : ButtonBar.ButtonData.CANCEL_CLOSE;
    }
}