package io.github.fpedrazav02.panela.runner.input;

import io.github.fpedrazav02.panela.PanelaHome;
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
        System.out.println("DEBUG input.config=" + input.config());
        System.out.println("DEBUG rawPath=" + input.config().get("path") + " type=" + input.config().get("type"));
        PathResolver pathResolver = PathResolver.getInstance();
        PanelaHome home = PanelaHome.getInstance();

        // Get config
        String rawPath = (String) input.config().get("path");
        FileType type = FileType.from(input.config().get("type"));

        // Resolve jobFolder
        Path jobBaseDir = home.getJobBaseDir(jobName);

        Path resolved = pathResolver.resolve(rawPath, jobBaseDir);
        pathResolver.requireReadableFile(resolved);

        // Validate extension
        FileTypeValidator validator = new FileTypeValidator(resolved, type);
        Result<Path> validation = validator.validate();

        if (!validation.isSuccess()) {
            throw new IllegalArgumentException(validation.getError().orElse("Invalid file type"));
        }
        Path validPath = validation.getValue().orElseThrow();

        // Validate rel path does not go outside jobdir
        if (!Path.of(rawPath).isAbsolute()) {
            Path base = jobBaseDir.toAbsolutePath().normalize();
            if (!resolved.startsWith(base)) {
                throw new IllegalArgumentException("Relative path escapes job directory: " + rawPath);
            }
        }

        switch (type) {
            case JSON, TXT -> {
                return Files.readString(validPath, StandardCharsets.UTF_8);
            }
            case CSV -> {
                return new CsvTableDecoder().decode(validPath);
            }
            default -> {throw new  IllegalArgumentException("Unsupported file type");}
        }
    }

    @Override
    public String getType() {
        return "file";
    }
}