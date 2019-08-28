package objects;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.IntFunction;

import static javafx.scene.input.KeyCode.*;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static utilities.TextEditorPaneUtilities.getSpaceChar;
import static utilities.TextEditorPaneUtilities.getTagString;
import static utilities.TextEditorPaneUtilities.countIndentation;
import static utilities.TextEditorPaneUtilities.hasTxtExtension;
import static utilities.TextEditorPaneUtilities.getTitle;
import static io.ReadWrite.read;
import static io.ReadWrite.write;

public class TextEditorPane extends BorderPane {
    private final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private int cpg;
    private ArrayList<TagObject> tagObjects = new ArrayList<>();
    private File txtFile;

    private Stage stage;

    /** if the application is started with the pathway of a .txt file as it's parameters.
     */
    public void setFile(File txtFile) {
        this.txtFile = txtFile;
        parse(read(txtFile));
    }

    public TextEditorPane() {
        // add listener on textArea's windowProperty
        // for the purpose of saving the stage's reference for future usage.
        textArea.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        stage = (Stage) newWindow;
                    }
                });
            }
        });

        // init general settings
        VirtualizedScrollPane<StyleClassedTextArea> vScrollPane =
                new VirtualizedScrollPane<>(textArea);

        setCenter(vScrollPane);
        vScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        vScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        textArea.setEditable(true);
        textArea.setWrapText(false);

        this.getStyleClass().add("root");
        textArea.getStyleClass().add("area");

        // init paragraph-box's graphic factory
        IntFunction<Node> numberFactory = LineNumberFactory.get(textArea);
        IntFunction<Node> arrowFactory = new ArrowFactory(textArea.currentParagraphProperty());
        IntFunction<Node> graphicFactory = line -> {
            HBox hBox = new HBox(
            //  numberFactory.apply(line),
              arrowFactory.apply(line)
            );
            hBox.setAlignment(Pos.CENTER_LEFT);
            return hBox;
        };
        textArea.setParagraphGraphicFactory(graphicFactory);

        // add listener on currentParagraphProperty
        // for easier access to reference / value
        textArea.currentParagraphProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                cpg = newValue;
            }
        });

        // override textArea's KeyPressed.ENTER for custom implementation
        Nodes.addInputMap(textArea, InputMap.consume(keyPressed(ENTER), e -> {

            int indent = 0;
            boolean outerbreak = false;
            for (int paragraph = cpg; paragraph >= 0; paragraph--) {
                String text = textArea.getText(paragraph);
                for (int j = tagObjects.size() - 1; j >= 0; j--) {
                    if (text.contains(tagObjects.get(j).getTextProperty())) {
                        indent = tagObjects.get(j).getIndent() + 2;
                        outerbreak = true;
                        break;
                    }
                }
                if (outerbreak) {
                    break;
                }
            }
            textArea.insertText(
                    cpg, textArea.getText(cpg).length(),
                    "\n" + getSpaceChar(indent));
            textArea.setStyle(cpg, textArea.getInitialTextStyle());
        }));

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressedOnTextArea);
    }

    private void keyPressedOnTextArea(@NotNull KeyEvent event) {
        KeyCode c = event.getCode();

        if (c == F1 || c == F2 || c == F3 || c == F4) {
            String text = textArea.getText(cpg).stripLeading();
            int indent = 0;
            String style = "";
            switch (event.getCode()) {
                case F1:
                    indent = 0;
                    style = "header-1";
                    break;
                case F2:
                    indent = 2;
                    style = "header-2";
                    break;
                case F3:
                    indent = 4;
                    style = "header-3";
                    break;
                case F4:
                    indent = 6;
                    style = "header-4";
                    break;
            }

            textArea.deleteText(cpg, 0, cpg, textArea.getText(cpg).length());
            textArea.insertText(cpg, 0, getTagString(text, indent));
            textArea.moveTo(cpg - 1, textArea.getText(cpg - 1).length());
            textArea.setStyle(cpg - 1, Arrays.asList("scale-size-up", style));
            textArea.setStyle(cpg + 1, Arrays.asList("scale-size-up", style));
            tagObjects.add(new TagObject("<" + text + ">", indent));
        }

        else if (c == BACK_SPACE) {
            if (textArea.getText(cpg).strip().equals("")) {
                textArea.setStyle(cpg, textArea.getInitialTextStyle());
            }
        }
    }

    public void newEvent() {
        Optional<ButtonType> input = confirm();

        if (input.isPresent()) {
            if (input.get().getButtonData() == ButtonBar.ButtonData.YES) {
                saveEvent();
                reset();
                stage.setTitle("untitled - RandEDT");
            }
            else if (input.get().getButtonData() == ButtonBar.ButtonData.NO) {
                reset();
                stage.setTitle("untitled - RandEDT");
            }
        }
    }

    public void openEvent() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.setInitialDirectory(new File("C:/users/wne-/dev"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            if (hasTxtExtension(file)) {
                reset();

                txtFile = file;
                stage.setTitle(getTitle(txtFile));
                parse(read(txtFile));
            }
        }
    }

    public void saveAsEvent() {
        String initFileName;
        File initDir = new File("C:/Users/wne-/dev");

        if (txtFile != null) {
            initFileName = txtFile.getName();
        } else {
            initFileName = "untitled.txt";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(initFileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        if (initDir.exists() && initDir.isDirectory()) {
            fileChooser.setInitialDirectory(initDir);
        }
        File file = fileChooser.showSaveDialog(stage);

        if (file != null ) {
            if (hasTxtExtension(file)) {
                txtFile = file;
                write(txtFile, textArea.getParagraphs());
                stage.setTitle(getTitle(txtFile));
            }
        }
    }

    public void saveEvent() {
        if (txtFile != null) {
            if (hasTxtExtension(txtFile)) {
                write(txtFile, textArea.getParagraphs());
            }
        } else {
            saveAsEvent();
        }
    }

    private Optional<ButtonType> confirm() {
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

        return alert.showAndWait();
    }

    private void parse(@NotNull ArrayList<String> text) {
        for (int i = 0; i < text.size(); i++) {
            textArea.appendText(text.get(i));
            String stripedText = text.get(i).strip();

            if (stripedText.length() >= 2 && stripedText.charAt(0) == '<' &&
                    stripedText.charAt(stripedText.length() - 1) == '>') {

                switch (countIndentation(text.get(i))) {
                    case 0:
                        textArea.setStyle(cpg, Collections.singletonList("header-1"));
                        break;
                    case 2:
                        textArea.setStyle(cpg, Collections.singletonList("header-2"));
                        break;
                    case 4:
                        textArea.setStyle(cpg, Collections.singletonList("header-3"));
                        break;
                    case 6:
                        textArea.setStyle(cpg, Collections.singletonList("header-4"));
                        break;
                }

                if (stripedText.charAt(1) != '/') {
                    tagObjects.add(new TagObject(stripedText, countIndentation(text.get(i))));
                }
            }
            else {
                textArea.setStyle(cpg, textArea.getInitialTextStyle());
            }

            if (i < text.size() - 1) {
                textArea.appendText("\n");
            }
        }
    }

    private void reset() {
        textArea.clear();
        textArea.clearStyle(cpg);
        txtFile = null;
    }

    public void requestFocus() {
        super.requestFocus();
        textArea.requestFocus();
    }
}