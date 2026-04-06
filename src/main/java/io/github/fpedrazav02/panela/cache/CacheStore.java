package io.github.fpedrazav02.panela.cache;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.model.tabular.Table;
import io.github.fpedrazav02.panela.runner.input.decoders.CsvTableDecoder;
import io.github.fpedrazav02.panela.runner.output.writters.CsvTableWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CacheStore {

    private final String jobName;

    public CacheStore(String jobName) {
        this.jobName = jobName;
    }

    public boolean exists(String nodeHash) {
        Path dir = nodeDir(nodeHash);
        return Files.exists(dir.resolve("output.csv")) || Files.exists(dir.resolve("output.txt"));
    }

    public void store(String nodeHash, Object result) throws Exception {
        Path dir = nodeDir(nodeHash);
        Files.createDirectories(dir);
        if (result instanceof Table table) {
            CsvTableWriter.write(table, dir.resolve("output.csv"));
        } else {
            Files.writeString(dir.resolve("output.txt"), result.toString(), StandardCharsets.UTF_8);
        }
    }

    public Object load(String nodeHash) throws Exception {
        Path dir = nodeDir(nodeHash);
        Path csv = dir.resolve("output.csv");
        if (Files.exists(csv)) {
            return new CsvTableDecoder().decode(csv);
        }
        return Files.readString(dir.resolve("output.txt"), StandardCharsets.UTF_8);
    }

    public void cleanJob() throws IOException {
        Path cacheDir = PanelaHome.getInstance().getCacheDir(jobName);
        if (Files.exists(cacheDir)) {
            deleteRecursively(cacheDir);
        }
    }

    public static void cleanAll() throws IOException {
        Path cacheRoot = PanelaHome.getInstance().getBaseDir().resolve("cache");
        if (Files.exists(cacheRoot)) {
            deleteRecursively(cacheRoot);
        }
    }

    private Path nodeDir(String nodeHash) {
        return PanelaHome.getInstance().getCacheDir(jobName).resolve(nodeHash);
    }

    static void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
