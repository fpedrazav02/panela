package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.model.Output;
import io.github.fpedrazav02.panela.model.tabular.Table;
import io.github.fpedrazav02.panela.service.PathResolver;
import io.github.fpedrazav02.panela.runner.output.writters.CsvTableWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileOutputRunner implements OutputRunner {

    @Override
    public void execute(Output output, Object inputData, String jobName) throws Exception {
        PanelaHome home = PanelaHome.getInstance();
        PathResolver resolver = PathResolver.getInstance();

        Path buildDir = home.getBuildDir(jobName);

        Files.createDirectories(buildDir);

        Map<String, Object> cfg = output.config();
        String format = (String) cfg.get("format");
        String rawPath = (String) cfg.get("path");

        Path outPath = resolveOutputPath(buildDir, rawPath, output.name(), format);

        if (inputData instanceof Table table) {
            String fmt = (format == null || format.isBlank()) ? "csv" : format;
            if (!fmt.equalsIgnoreCase("csv")) {
                throw new IllegalArgumentException("Table output only supports format=csv for now. Got: " + fmt);
            }
            CsvTableWriter.write(table, outPath);
            return;
        }

        String text = String.valueOf(inputData);
        Files.createDirectories(outPath.getParent());
        Files.writeString(outPath, text, StandardCharsets.UTF_8);
    }

    private Path resolveOutputPath(Path buildDir, String rawPath, String outputName, String format) {
        String ext = inferExt(format, "txt");

        Path rel;
        if (rawPath == null || rawPath.isBlank()) {
            rel = Path.of(outputName + "." + ext);
        } else {
            Path p = Path.of(rawPath);
            if (p.isAbsolute()) {
                throw new IllegalArgumentException("Absolute output paths are not allowed. Use relative to build/: " + rawPath);
            }
            if (!p.getFileName().toString().contains(".")) {
                p = Path.of(rawPath + "." + ext);
            }
            rel = p;
        }

        Path resolved = buildDir.resolve(rel).normalize();
        Path base = buildDir.toAbsolutePath().normalize();
        if (!resolved.toAbsolutePath().startsWith(base)) {
            throw new IllegalArgumentException("Output path escapes build directory: " + rel);
        }
        return resolved;
    }

    private String inferExt(String format, String defaultExt) {
        if (format == null || format.isBlank()) return defaultExt;
        return switch (format.toLowerCase()) {
            case "csv" -> "csv";
            case "txt" -> "txt";
            case "json" -> "json";
            default -> defaultExt;
        };
    }

    @Override
    public String getType() {
        return "file";
    }
}