package com.github.wnebyte.notes.util;

/**
 * This functional interface declares a method for determining whether two Objects are equal.
 * @param <T> the Type of the two Objects.
 */
public interface Checksum<T> {

    boolean hasChanged(final T content, final T snapshot);
}
