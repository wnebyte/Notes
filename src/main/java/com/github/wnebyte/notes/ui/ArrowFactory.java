package com.github.wnebyte.notes.ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.reactfx.value.Val;
import java.util.function.IntFunction;

public class ArrowFactory implements IntFunction<Node> {

    private final ObservableValue<Integer> shownLine;

    ArrowFactory(final ObservableValue<Integer> shownLine) {
        this.shownLine = shownLine;
    }

    @Override
    public Node apply(final int lineNumber) {
        Polygon triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0);
        triangle.setFill(Color.GREEN);
        Val<Boolean> visible = Val.map(
                shownLine, sl -> sl == lineNumber);
        triangle.visibleProperty().bind(visible.conditionOnShowing(triangle));
        return triangle;
    }
}
