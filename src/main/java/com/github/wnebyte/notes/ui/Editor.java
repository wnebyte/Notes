package com.github.wnebyte.notes.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor extends BorderPane {

    private final StyleClassedTextArea area = new StyleClassedTextArea();

    private final VirtualizedScrollPane<StyleClassedTextArea> scrollPane
            = new VirtualizedScrollPane<>(area);

    public Editor() {
        initialize();
    }

    private void initialize() {
        setCenter(scrollPane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        area.multiPlainChanges()
                .successionEnds(Duration.ofMillis(50))
                .subscribe(autCompleteElementHandler()
                        .andThen(ignore -> area.setStyleSpans(0, styleSpans(area.getText()))));
        // library bug fix
        area.getUndoManager().performingActionProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null) {
                    area.moveTo(area.getCurrentParagraph(), area.getCaretColumn());
                }
            }
        });
        Nodes.addInputMap(area, InputMap.consume(EventPattern.keyPressed(KeyCode.ENTER), onEnterHandler()));
        Nodes.addInputMap(area, InputMap.consume(EventPattern.keyPressed(KeyCode.BACK_SPACE), onBackspaceHandler()));
        Nodes.addInputMap(area, InputMap.consume(EventPattern.keyPressed(KeyCode.TAB), onTabHandler()));
        Nodes.addInputMap(area, InputMap.consume(EventPattern.keyPressed(KeyCode.UP), onUpHandler()));
        Nodes.addInputMap(area, InputMap.consume(EventPattern.keyPressed(KeyCode.DOWN), onDownHandler()));
        /*
        remove the following input maps to allow the controller to handle those events.
         */
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.N, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.O, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.S, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.Z, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.X, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.C, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.V, KeyCombination.CONTROL_DOWN)));
        Nodes.addInputMap(area, InputMap.ignore(EventPattern.keyPressed(KeyCode.DELETE)));
    }

    /**
     * When consumed inserts four whitespace characters at the current caret position.
     * @return the EventHandler.
     */
    private Consumer<KeyEvent> onTabHandler() {
        return e -> {
            area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), "    ");
        };
    }

    /**
     * When consumed moves the caret one paragraph position up.
     * @return the EventHandler.
     */
    private Consumer<KeyEvent> onUpHandler() {
        return e -> {
            if (1 <= area.getCurrentParagraph()) {
                int currentCol = area.getCaretColumn();
                int maxCol = area.getParagraphLength(area.getCurrentParagraph() - 1);
                area.moveTo(area.getCurrentParagraph() - 1, Math.min(currentCol, maxCol));
            }
        };
    }

    /**
     * When consumed moves the caret one paragraph position down.
     * @return the EventHandler.
     */
    private Consumer<KeyEvent> onDownHandler() {
        return e -> {
            if (area.getCurrentParagraph() + 1 < area.getParagraphs().size()) {
                int currentCol = area.getCaretColumn();
                int maxCol = area.getParagraphLength(area.getCurrentParagraph() + 1);
                area.moveTo(area.getCurrentParagraph() + 1, Math.min(currentCol, maxCol));
            }
        };
    }

    /**
     * When consumed either deletes the selected text if present,
     * or the previous four whitespace characters if present,
     * or deletes the previous character.
     * @return the EventHandler
     */
    private Consumer<KeyEvent> onBackspaceHandler() {
        return e -> {
            IndexRange range = area.selectionProperty().getValue();
            if ((range != null) && (1 <= range.getLength())) {
                area.deleteText(range);
                return;
            }
            String text = area.getText(area.getCurrentParagraph(), 0,
                    area.getCurrentParagraph(), area.getCaretColumn());
            if (Pattern.compile("[ \\n\\x0B\\f\\r]+").matcher(text).matches()) {
                int len = Math.min(4, text.length());
                for (int i = 0; i < len; i++) {
                    area.deletePreviousChar();
                }
            } else {
                area.deletePreviousChar();
            }
        };
    }

    /**
     * When consumed inserts a new line character at the current caret position,
     * and adds the proper level of indentation.
     * @return the EventHandler.
     */
    private Consumer<KeyEvent> onEnterHandler() {
        return e -> {
            area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), System.lineSeparator());
            Matcher m0 = Pattern.compile("^\\s+")
                    .matcher(getFirstSegment(area.getCurrentParagraph() - 1));
            if (m0.find()) {
                area.insertText(area.getCaretPosition(), m0.group());
            }
            boolean m2 = Pattern.compile("^\\s*-\\s(.*|)")
                    .matcher(getFirstSegment(area.getCurrentParagraph() - 1)).matches();
            if (m2) {
                area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), "- ");
            }
        };
    }

    /**
     * When consumed and an opened element is discovered, the element is automatically closed, and the
     * caret is relocated to the position in between the two tags.
     * @return the EventHandler.
     */
    private Consumer<List<PlainTextChange>> autCompleteElementHandler() {
        return plainTextChanges -> {
            for (PlainTextChange plainTextChange : plainTextChanges) {
                String inserted = plainTextChange.getInserted();
                String text = area.getText(area.getCurrentParagraph());

                if (inserted.equals(">") &&
                        Pattern.compile("\\s*<[^</>]*>").matcher(text).matches()) {
                    String element = text.substring(text.lastIndexOf("<") + 1, text.lastIndexOf(">"));
                    String append = "</".concat(element).concat(">");
                    area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), append);
                    area.moveTo(area.getCurrentParagraph(),
                            area.getText(area.getCurrentParagraph()).length() - append.length());
                }
            }
        };
    }

    private StyleSpans<Collection<String>> styleSpans(final String text) {
        String ELEMENT_PATTERN = "(<|</)([^</>]*)(>)";
        String HASH_PATTERN = "#\\s[^#:]*:";

        Pattern PATTERN = Pattern.compile(
                "(?<ELEMENT>" + ELEMENT_PATTERN + ")"
                        + "|(?<HASH>" + HASH_PATTERN + ")"
        );
        Matcher matcher = PATTERN.matcher(text);
        int i = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                    matcher.group("ELEMENT") != null ? "element" :
                            matcher.group("HASH") != null ? "hash" : "";
            spansBuilder.add(Collections.emptyList(), matcher.start() - i);
            spansBuilder.add(Collections.singletonList(styleClass), matcher.end() - matcher.start());
            i = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - i);
        return spansBuilder.create();
    }

    private String getFirstSegment(int paragraph) {
        try {
            return area.getParagraph(paragraph).getSegments().get(0);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String getLastSegment(int paragraph) {
        return area.getParagraph(paragraph).getSegments()
                .get(area.getParagraph(paragraph).getSegments().size() - 1);
    }

    private void deleteCurrentParagraph() {
        area.deleteText(area.getCurrentParagraph(), 0,
                area.getCurrentParagraph(), area.getText(area.getCurrentParagraph()).length());
    }


    public List<Paragraph<Collection<String>, String, Collection<String>>> getParagraphs() {
        return area.getDocument().getParagraphs();
    }

    public void setTextPropertyChangeListener(final ChangeListener<String> changeListener) {
        area.textProperty().addListener(changeListener);
    }

    public String getText() {
        return area.textProperty().getValue();
    }

    public void replace(final List<String> paragraphs) {
        delete();
        if (paragraphs != null) {
            paragraphs.forEach(paragraph -> area.appendText(paragraph + System.lineSeparator()));
        }
    }

    public void paste() {
        area.paste();
    }

    public void cut() {
        area.cut();
    }

    public void copy() {
        area.copy();
    }

    public void undo() {
        area.undo();
    }

    public void delete() {
        area.clear();
    }
}