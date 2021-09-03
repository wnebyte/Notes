package com.github.wnebyte.notes.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

public class SaveChangesAlert extends Alert {

    public SaveChangesAlert(final AlertType alertType) {
        super(alertType);
        initialize();
    }

    private void initialize() {
        initStyle(StageStyle.DECORATED);
        setTitle("Notes");
        getButtonTypes().clear();
        getButtonTypes().addAll(
                new ButtonType("Save", ButtonBar.ButtonData.YES),
                new ButtonType("Don't Save", ButtonBar.ButtonData.NO),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}
