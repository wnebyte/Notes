package utilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TextEditorPaneUtilities {

    @NotNull
    public static String indent(int length) {
        String base = "";
        char[] arr = new char[length];
        Arrays.fill(arr, ' ');
        return base + new String(arr);
    }

    @NotNull
    public static String enclose(String text, int indentation) {
        return indent(indentation) + "<" + text + ">\n" +
                indent(indentation + 2) + "\n" +
                indent(indentation) + "</" + text + ">";
    }

    public static int countIndentation(@NotNull String line) {
        int count = 0;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                break;
            }
            count++;
        }

        return count;
    }

    public static boolean hasTxtExtension(@NotNull File file) {
        String[] split = file.getPath().split("[.]");
        return split[split.length - 1].equals("txt");
    }

    public static String getFileName(@NotNull File file) {
        return file.getName().split("[.]")[0];
    }

    @NotNull
    public static String getTitle(File file) {
        return getFileName(file).split("[.]")[0] + " - RandEDT";
    }

    public static String getText(ArrayList<String> lines) {
        return String.join("\n", lines);
    }
}
