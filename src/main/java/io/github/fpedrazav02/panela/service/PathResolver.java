package io.github.fpedrazav02.panela.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathResolver {

    private static class PathHolder {
        private static final PathResolver uniqueInstance = new PathResolver();
    }

    public static PathResolver getInstance() {
        return PathHolder.uniqueInstance;
    }

    public Path resolve(String rawPath, Path baseDir) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("path cannot be null/blank");
        }

        Path p = Paths.get(rawPath);

        if (!p.isAbsolute()) {
            if (baseDir == null) {
                throw new IllegalArgumentException("baseDir cannot be null when path is relative");
            }
            p = baseDir.resolve(p);
        }

        return p.normalize().toAbsolutePath();
    }

    public Path requireReadableFile(Path p) {
        if (!Files.exists(p)) {
            throw new IllegalArgumentException("File does not exist: " + p);
        }
        if (!Files.isRegularFile(p)) {
            throw new IllegalArgumentException("Path is not a regular file: " + p);
        }
        if (!Files.isReadable(p)) {
            throw new IllegalArgumentException("File is not readable: " + p);
        }
        return p;
    }
}