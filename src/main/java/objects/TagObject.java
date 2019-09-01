package objects;

public class TagObject {
    private final int indent;
    private final String textProperty;
    private final String endTagProperty;

    public TagObject(String text, int indent, String endTagProperty
                     ) {
        this.indent = indent;
        textProperty = text;
        this.endTagProperty = endTagProperty;
    }

    public String getTextProperty() {
        return textProperty;
    }

    public int getIndent() {
        return indent;
    }

    public String getEndTagProperty() {
        return endTagProperty;
    }
}
