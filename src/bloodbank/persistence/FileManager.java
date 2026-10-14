package bloodbank.persistence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FileManager {
    private final Path dir;

    public FileManager() {
        this("data");
    }

    public FileManager(String directory) {
        dir = Paths.get(directory);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data folder", e);
        }
    }

    public synchronized List<String> read(String name) {
        Path p = dir.resolve(name);
        if (!Files.exists(p)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not read " + p + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized void write(String name, List<String> lines) {
        Path p = dir.resolve(name);
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                w.write(line);
                w.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save " + p, e);
        }
    }

    public static String enc(String s) {
        return Base64.getEncoder().encodeToString((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
    }

    public static String dec(String s) {
        return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
    }

    public static String[] split(String line) {
        return line.split("\\|", -1);
    }
}