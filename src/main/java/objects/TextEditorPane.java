package objects;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jetbrains.annotations.NotNull;
import org.reactfx.collection.LiveList;

import java.time.Duration;
import java.util.*;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.scene.input.KeyCode.*;
import static utilities.TextEditorPaneUtilities.*;

public class TextEditorPane extends BorderPane {
    private final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private int currPg;

    private ArrayList<TagObject> tagObjects = new ArrayList<>();
    private final ArrayList<String> TAGS = new ArrayList<>();

    public TextEditorPane() {
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

        // add listener on currentParagraphProperty
        // for easier access to reference / value
        textArea.currentParagraphProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                currPg = newValue;
            }
        });


        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile("^\\s+");
        textArea.addEventHandler(KeyEvent.KEY_PRESSED, KE ->
        {
            if (KE.getCode() == ENTER && !KE.isControlDown() && !KE.isAltDown() && !KE.isShiftDown()) {
                int caretPosition = textArea.getCaretPosition();
                int currentParagraph = textArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(
                        textArea.getParagraph(currentParagraph - 1).getSegments().get(0));
                if (m0.find()) Platform.runLater(() -> textArea.insertText(caretPosition, m0.group()));
            }
        });

        // add highlighting functionality
        textArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(ignore -> textArea.setStyleSpans(0, computeHighlighting(textArea.getText())));

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode c = event.getCode();

            // add insert 'tag' functionality
            if (event.isShiftDown() && c == ENTER) {
                if (!textArea.getText(currPg).stripLeading().startsWith("$")) {
                    int indent = getNextIndent();
                    String text = textArea.getText(currPg).strip();

                    textArea.deleteText(currPg, 0, currPg, textArea.getText(currPg).length());
                    textArea.insertText(currPg, 0, enclose(text, indent));
                    textArea.moveTo(currPg - 1, textArea.getText(currPg - 1).length());
                    tagObjects.add(new TagObject("<" + text + ">", indent, "</" + text + ">"));
                    TAGS.add(text);
                }

                // add insert 'list' functionality
                else {
                    final String KEYWORD = "^[$]list[:]\\s\\d$";

                    if (Pattern.matches(KEYWORD, textArea.getText(currPg).strip())) {
                        int items = Integer.parseInt(
                                textArea.getText(currPg).strip().split("\\s")[1]
                        );
                        int indent = getNextIndent();

                        textArea.deleteText(currPg, 0, currPg, textArea.getText(currPg).length());
                        int par = currPg;
                        int start = par;
                        for (int i = 1; i <= items; i++) {
                            textArea.insertText(par, 0,
                                    indent(indent) + i + ". ");
                            if (i < items) {
                                textArea.insertText(par, textArea.getText(par).length(), "\n");
                            }
                            par++;
                        }
                        textArea.moveTo(start, textArea.getText(start).length());
                    }
                }
            }
        });
    }

    private void parse(@NotNull ArrayList<String> text) {
        for (int i = 0; i < text.size(); i++) {
            textArea.appendText(text.get(i));
            String stripedText = text.get(i).strip();

            if (stripedText.length() >= 3 && stripedText.charAt(0) == '<' &&
                    stripedText.charAt(1) != '/' &&
                    stripedText.charAt(stripedText.length() - 1) == '>') {

                String str = stripedText.replace("<", "").replace(">", "");

                tagObjects.add(new TagObject(
                        "<" + str + ">",
                        countIndentation(text.get(i)),
                        "</" + str + ">"
                ));
                TAGS.add(str);
            }

            if (i < text.size() - 1) {
                textArea.appendText("\n");
            }
        }
    }


    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        String TAG_PATTERN = "(<|</)(" + String.join("|", TAGS) + ")>";

        final String[] NUM_KEYWORDS = new String[]{
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        };

        final String[] V_KEYWORDS = new String[]{"v", "V"};

        final String[] X_KEYWORDS = new String[]{"x", "X"};

        final String NUM_PATTERN = "(" + String.join("|", NUM_KEYWORDS) + ")";

        final String V_PATTERN = "\\[\\s(" + String.join("|", V_KEYWORDS) + ")\\s]";

        final String X_PATTERN = "\\[\\s(" + String.join("|", X_KEYWORDS) + ")\\s]";

        Pattern PATTERN = Pattern.compile(
                "(?<TAG>" + TAG_PATTERN + ")"
                        + "|(?<NUM>" + NUM_PATTERN + ")"
                        + "|(?<V>" + V_PATTERN + ")"
                        + "|(?<X>" + X_PATTERN + ")"
        );

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                    matcher.group("TAG") != null ? "tag" :
                            matcher.group("NUM") != null ? "num" :
                                    matcher.group("V") != null ? "v" :
                                            matcher.group("X") != null ? "x" : "";

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singletonList(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private int getNextIndent() {
        int indent = 0;
        boolean breakOuter = false;

        for (int i = tagObjects.size() - 1; i >= 0; i--) {

            for (int j = currPg; j >= 0; j--) {
                String currPgText = textArea.getText(j);

                if (currPgText.contains(tagObjects.get(i).getEndTagProperty())) {
                    // tag is closed
                    break;
                } else if (currPgText.contains(tagObjects.get(i).getTextProperty())) {
                    // the nearest open tag has been located
                    indent = tagObjects.get(i).getIndent() + 2;
                    breakOuter = true;
                    break;
                }
            }
            if (breakOuter) {
                break;
            }
        }

        return indent;
    }

    public void displayText(ArrayList<String> lines) {
        resetEditor();
        parse(lines);
    }

    public void resetEditor() {
        textArea.clear();
        textArea.clearStyle(currPg);
    }

    public LiveList<Paragraph<Collection<String>, String, Collection<String>>> getParagraphs() {
        return textArea.getParagraphs();
    }


    public boolean equals(String text) {
        return textArea.getText().equals(text);
    }


    @Override
    public void requestFocus() {
        super.requestFocus();
        textArea.requestFocus();
    }
}