package io.github.fpedrazav02.panela.validator.impl;

import io.github.fpedrazav02.panela.model.types.FileType;
import io.github.fpedrazav02.panela.utils.Result;
import io.github.fpedrazav02.panela.validator.IValidator;

import java.nio.file.Path;
import java.util.Locale;

public class FileTypeValidator implements IValidator<Path> {

    private final Path path;
    private final FileType expectedType;

    public FileTypeValidator(Path path, FileType expectedType) {
        this.path = path;
        this.expectedType = expectedType;
    }

    @Override
    public Result<Path> validate() {
        if (path == null) {
            return Result.fail("path cannot be null");
        }
        if (expectedType == null) {
            return Result.fail("type cannot be null");
        }

        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);

        String ext = "";
        int idx = name.lastIndexOf('.');
        if (idx >= 0 && idx < name.length() - 1) {
            ext = name.substring(idx + 1);
        }

        if (!ext.equals(expectedType.ext())) {
            return Result.fail(
                    "File extension '" + ext + "' does not match expected type '" + expectedType.ext() + "': " + path
            );
        }

        return Result.ok(path);
    }
}