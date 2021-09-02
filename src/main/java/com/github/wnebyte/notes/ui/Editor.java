package com.github.wnebyte.notes.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
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
        area.addEventHandler(KeyEvent.KEY_PRESSED, autoIndentation());
        area.multiPlainChanges()
                .successionEnds(Duration.ofMillis(50))
                .subscribe(textChangedSubscription());
        area.multiPlainChanges()
                .successionEnds(Duration.ofMillis(55))
                .subscribe(ignore -> area.setStyleSpans(0, styleSpans(area.getText())));
        /*
        IntFunction<Node> numberFactory = LineNumberFactory.get(area);
        IntFunction<Node> graphicFactory = line -> {
            HBox hBox = new HBox(numberFactory.apply(line));
            hBox.setAlignment(Pos.CENTER_LEFT);
            return hBox;
        };
        area.setParagraphGraphicFactory(graphicFactory);
         */
        // library bug fix
        area.getUndoManager().performingActionProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null) {
                    area.moveTo(area.getCurrentParagraph(), area.getCaretColumn());
                }
            }
        });
    }

    private Consumer<List<PlainTextChange>> textChangedSubscription() {
        return plainTextChanges -> {
            for (PlainTextChange plainTextChange : plainTextChanges) {
                String inserted = plainTextChange.getInserted();
                String text = area.getText(area.getCurrentParagraph());

                if (inserted.equals(">") &&
                        Pattern.compile("\\s*<[^</>]*>")
                                .matcher(text).matches()) {
                    String element = text.substring(text.lastIndexOf("<") + 1, text.lastIndexOf(">"));
                    String append = "</".concat(element).concat(">");
                    area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), append);
                    area.moveTo(area.getCurrentParagraph(),
                            area.getText(area.getCurrentParagraph()).length() - append.length());
                }
            }
        };
    }

    private EventHandler<KeyEvent> autoIndentation() {
        return event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Matcher m0 = Pattern.compile("^\\s+")
                        .matcher(getFirstSegment(area.getCurrentParagraph() - 1));
                if (m0.find()) {
                    area.insertText(area.getCaretPosition(), m0.group());
                }
                boolean m1 = Pattern.compile("^\\s*#[^#:]*:")
                        .matcher(getFirstSegment(area.getCurrentParagraph() - 1))
                        .matches();
                if (m1) {
                    area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), "\t");
                }
                boolean m2 = Pattern.compile("^\\s*-\\s(.*|)")
                        .matcher(getFirstSegment(area.getCurrentParagraph() - 1))
                        .matches();
                if (m2) {
                    area.insertText(area.getCurrentParagraph(), area.getCaretColumn(), "- ");
                }
            }
        };
    }

    private StyleSpans<Collection<String>> styleSpans(final String text) {
        String TAG_PATTERN = "(<|</)([^</>]*)(>)";
        Pattern PATTERN = Pattern.compile("(?<ELE>" + TAG_PATTERN + ")");

        Matcher matcher = PATTERN.matcher(text);
        int i = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = matcher.group("ELE") != null ? "element" : "";
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