package com.github.wnebyte.notes.util;

public interface Revision<T> {

    boolean hasChanged(final T content, final T snapshot);
}
