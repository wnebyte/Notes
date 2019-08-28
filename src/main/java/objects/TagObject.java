package objects;

public class TagObject {
    private final int indent;
    private final String textProperty;

    public TagObject(String text, int indent
                     ) {
        this.indent = indent;
        textProperty = text;
    }

    public String getTextProperty() {
        return textProperty;
    }

    public int getIndent() {
        return indent;
    }
}
