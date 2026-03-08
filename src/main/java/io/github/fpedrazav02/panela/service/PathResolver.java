package io.github.fpedrazav02.panela.service;

import io.github.fpedrazav02.panela.exceptions.custom.PathResolutionException;

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

    public Path resolve(String rawPath, Path baseDir) throws PathResolutionException {
        if (rawPath == null || rawPath.isBlank()) {
            throw new PathResolutionException("Input path cannot be null or blank");
        }

        Path p = Paths.get(rawPath);

        if (!p.isAbsolute()) {
            if (baseDir == null) {
                throw new PathResolutionException("Base directory cannot be null when path is relative: " + rawPath);
            }
            p = baseDir.resolve(p);
        }

        return p.normalize().toAbsolutePath();
    }

    public Path requireReadableFile(Path p) throws PathResolutionException {
        if (!Files.exists(p)) {
            throw new PathResolutionException("File does not exist: " + p);
        }
        if (!Files.isRegularFile(p)) {
            throw new PathResolutionException("Path is not a regular file: " + p);
        }
        if (!Files.isReadable(p)) {
            throw new PathResolutionException("File is not readable: " + p);
        }
        return p;
    }
}