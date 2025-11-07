package com.shotdevs.shotdata.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.shotdevs.shotdata.ShotDataPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles saving and loading pending payloads from the local file system.
 */
public class FileStorage {

    private final File pendingFolder;

    public FileStorage(File pendingFolder) {
        this.pendingFolder = pendingFolder;
        if (!pendingFolder.exists()) {
            pendingFolder.mkdirs();
        }
    }

    public void savePendingRequest(String jsonPayload, String type) {
        String fileName = "pending_" + type + "_" + UUID.randomUUID().toString() + ".json";
        Path filePath = new File(pendingFolder, fileName).toPath();
        try {
            Files.writeString(filePath, jsonPayload, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            ShotDataPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to save pending request to file.", e);
        }
    }

    public List<File> getPendingFiles() {
        try (Stream<Path> stream = Files.walk(pendingFolder.toPath())) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            ShotDataPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to read pending files.", e);
            return Collections.emptyList();
        }
    }

    public String readPendingFile(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ShotDataPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to read pending file: " + file.getName(), e);
            return null;
        }
    }

    public void deletePendingFile(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            ShotDataPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to delete pending file: " + file.getName(), e);
        }
    }
}
