package com.github.wnebyte.notes.util;

import java.util.ArrayList;
import java.util.Collection;

public final class CollectionUtils {

    public static <T> int intersections(final Collection<T> c1, final Collection<T> c2) {
        return intersection(c1, c2).size();
    }

    public static <T> Collection<T> intersection(final Collection<T> c1, final Collection<T> c2) {
        Collection<T> collection = new ArrayList<>();
        if ((c1 == null) || (c2 == null) || (c1.isEmpty()) || (c2.isEmpty())) { return collection; }

        for (T t : c1) {
            if ((t != null) && (c2.contains(t))) {
                collection.add(t);
            }
        }
        return collection;
    }
}
