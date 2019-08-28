package utilities;

import objects.TagObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TextEditorPaneUtilities {

    public static String getSpaceChar(int length) {
        String base = "";
        char[] arr = new char[length];
        Arrays.fill(arr, ' ');
        return base + new String(arr);
    }

    public static String getTagString(String text, int indentation) {
        return getSpaceChar(indentation) + "<" + text + ">\n" +
                getSpaceChar(indentation + 2) + "\n" +
                getSpaceChar(indentation) + "</" + text + ">";
    }

    public static String getIdentifier(ArrayList<TagObject> objs, int indent) {
        int count = 0;
        for (TagObject obj : objs) {
            if (obj.getIndent() == indent) {
                count++;
            }
        }

        int order;
        switch (indent) {
            case 0:
                order = 1;
                break;
            case 2:
                order = 2;
                break;
            case 4:
                order = 3;
                break;
            case 6:
                order = 4;
                break;
            default:
                order = 0;
        }

        return String.format("%d.%d ", order, count);
    }

    public static int countIndentation(String line) {
        int count = 0;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                break;
            }
            count++;
        }

        return count;
    }

    public static boolean hasTxtExtension(File file) {
        String[] split = file.getPath().split("[.]");
        return split[split.length - 1].equals("txt");
    }

    public static String getFileName(File file) {
        return file.getName().split("[.]")[0];
    }

    public static String getTitle(File file) {
        return getFileName(file).split("[.]")[0] + " - RandEDT";
    }
}
