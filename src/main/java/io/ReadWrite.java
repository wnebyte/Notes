package io;

import org.fxmisc.richtext.model.Paragraph;
import org.jetbrains.annotations.NotNull;
import org.reactfx.collection.LiveList;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class ReadWrite {

    public static ArrayList<String> read(File file) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return lines;
    }

    public static void write(File file,
                             @NotNull LiveList<
                                     Paragraph<Collection<String>, String, Collection<String>>> paragraphs)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
        {
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
