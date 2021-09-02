package com.github.wnebyte.notes.io;

import org.fxmisc.richtext.model.Paragraph;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Storage {

    public static ArrayList<String> read(final File file) {
        ArrayList<String> lines = new ArrayList<>();
        if (file == null) { return lines; }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return lines;
    }

    public static void write(
            final File file,
            final List<Paragraph<Collection<String>, String, Collection<String>>> paragraphs)
    {
        if ((file == null) || (paragraphs == null)) {
            throw new IllegalArgumentException(
                    "File and Paragraphs must be non null"
            );
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < paragraphs.size(); i++) {
                writer.write(paragraphs.get(i).getText());
                if (i < paragraphs.size() - 1){
                    writer.write(System.lineSeparator());
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
