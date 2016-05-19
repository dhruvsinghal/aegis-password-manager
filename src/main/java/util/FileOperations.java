package main.java.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class for reading and writing to files. Note that this uses the ISO_8859_1 charset.
 */
public class FileOperations {
    public static void writeToFile(String path, byte[] content) throws IOException {
        File file = new File(path);

        // Create directories if necessary
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        FileOutputStream fs = new FileOutputStream(file);

        try {
            fs.write(content);
        } finally {
            fs.close();
        }
    }

    public static void writeToFile(String path, String content) throws IOException {
        writeToFile(path, content.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static byte[] readFileBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static String readFileString(String path) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        return new String(b, StandardCharsets.ISO_8859_1);
    }
}
