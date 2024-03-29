package com.github.wnebyte.notes.io;

import org.fxmisc.richtext.model.Paragraph;
import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * This class is a repository class used to read and write files to the filesystem.
 */
public class Repository {

    private static Repository instance = null;

    public Repository() {}

    public List<String> read(final File file) {
        return IO.read(file);
    }

    public void write(
            final File file,
            final List<Paragraph<Collection<String>, String, Collection<String>>> paragraphs
    ) {
        IO.write(file, paragraphs);
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }
}
