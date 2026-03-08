package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.InvalidInputException;
import io.github.fpedrazav02.panela.exceptions.custom.PathResolutionException;
import io.github.fpedrazav02.panela.model.Input;
import io.github.fpedrazav02.panela.model.types.FileType;
import io.github.fpedrazav02.panela.runner.input.decoders.CsvTableDecoder;
import io.github.fpedrazav02.panela.service.PathResolver;
import io.github.fpedrazav02.panela.utils.Result;
import io.github.fpedrazav02.panela.validator.impl.FileTypeValidator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInputRunner implements InputRunner {

    @Override
    public Object execute(Input input, String jobName) throws Exception {
        PathResolver pathResolver = PathResolver.getInstance();
        PanelaHome home = PanelaHome.getInstance();

        String rawPath = (String) input.config().get("path");
        if (rawPath == null || rawPath.isBlank()) {
            throw new InvalidInputException("Input '" + input.name() + "': missing config field 'path'");
        }

        FileType type = FileType.from(input.config().get("type"));

        Path jobBaseDir = home.getJobBaseDir(jobName);
        Path resolved = pathResolver.resolve(rawPath, jobBaseDir);
        pathResolver.requireReadableFile(resolved);

        // Validate extension matches declared type
        FileTypeValidator validator = new FileTypeValidator(resolved, type);
        Result<Path> validation = validator.validate();
        if (!validation.isSuccess()) {
            throw new InvalidInputException("Input '" + input.name() + "': "
                    + validation.getError().orElse("file type mismatch"));
        }
        Path validPath = validation.getValue().orElseThrow();

        // Reject relative paths that escape the job directory
        if (!Path.of(rawPath).isAbsolute()) {
            Path base = jobBaseDir.toAbsolutePath().normalize();
            if (!resolved.startsWith(base)) {
                throw new PathResolutionException(
                        "Input '" + input.name() + "': path escapes job directory: " + rawPath);
            }
        }

        return switch (type) {
            case JSON, TXT -> Files.readString(validPath, StandardCharsets.UTF_8);
            case CSV       -> new CsvTableDecoder().decode(validPath);
        };
    }

    @Override
    public String getType() {
        return "file";
    }
}