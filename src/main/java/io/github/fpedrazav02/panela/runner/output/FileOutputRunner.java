package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.InvalidOutputException;
import io.github.fpedrazav02.panela.exceptions.custom.PathResolutionException;
import io.github.fpedrazav02.panela.model.Output;
import io.github.fpedrazav02.panela.model.tabular.Table;
import io.github.fpedrazav02.panela.runner.output.writters.CsvTableWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileOutputRunner implements OutputRunner {

    @Override
    public void execute(Output output, Object inputData, String jobName) throws Exception {
        PanelaHome home = PanelaHome.getInstance();

        Path buildDir = home.getBuildDir(jobName);
        Files.createDirectories(buildDir);

        Map<String, Object> cfg = output.config();
        String format  = (String) cfg.get("format");
        String rawPath = (String) cfg.get("path");

        Path outPath = resolveOutputPath(buildDir, rawPath, output.name(), format);

        if (inputData instanceof Table table) {
            String fmt = (format == null || format.isBlank()) ? "csv" : format;
            if (!fmt.equalsIgnoreCase("csv")) {
                throw new InvalidOutputException(
                        "Output '" + output.name() + "': Table data only supports format=csv, got '" + fmt + "'");
            }
            CsvTableWriter.write(table, outPath);
            return;
        }

        String text = String.valueOf(inputData);
        Files.createDirectories(outPath.getParent());
        Files.writeString(outPath, text, StandardCharsets.UTF_8);
    }

    private Path resolveOutputPath(Path buildDir, String rawPath, String outputName, String format)
            throws PathResolutionException {
        String ext = inferExt(format, "txt");

        Path rel;
        if (rawPath == null || rawPath.isBlank()) {
            rel = Path.of(outputName + "." + ext);
        } else {
            Path p = Path.of(rawPath);
            if (p.isAbsolute()) {
                throw new PathResolutionException(
                        "Absolute output paths are not allowed — use a path relative to build/: " + rawPath);
            }
            if (!p.getFileName().toString().contains(".")) {
                p = Path.of(rawPath + "." + ext);
            }
            rel = p;
        }

        Path resolved = buildDir.resolve(rel).normalize();
        if (!resolved.toAbsolutePath().startsWith(buildDir.toAbsolutePath().normalize())) {
            throw new PathResolutionException("Output path escapes build directory: " + rel);
        }
        return resolved;
    }

    private String inferExt(String format, String defaultExt) {
        if (format == null || format.isBlank()) return defaultExt;
        return switch (format.toLowerCase()) {
            case "csv"  -> "csv";
            case "txt"  -> "txt";
            case "json" -> "json";
            default     -> defaultExt;
        };
    }

    @Override
    public String getType() {
        return "file";
    }
}