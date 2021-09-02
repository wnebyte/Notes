package com.github.wnebyte.notes.util;

import java.util.Arrays;

public final class StringUtils {

    public static String stripLeading(final String s) {
        if (s == null) { return ""; }
        char[] arr = s.toCharArray();
        int i = 0;

        while ((i < arr.length) && (arr[i] == ' ')) {
            i++;
        }
        return s.substring(i);
    }

    public static String stripTrailing(final String s) {
        if (s == null) { return ""; }
        char[] arr = s.toCharArray();
        int i = arr.length - 1;

        while ((0 <= i) && (arr[i] == ' ')) {
            i--;
        }
        return s.substring(0, i + 1);
    }

    public static String strip(final String s) {
        String tmp = stripLeading(s);
        return stripTrailing(tmp);
    }

    public static String whitespace(final int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, ' ');
        return new String(arr);
    }
}
