package io.github.fpedrazav02.panela.runner.output;

import io.github.fpedrazav02.panela.model.Output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOutputRunner implements OutputRunner {

    private static final String RESET = "\u001B[0m";
    private static final String DIM = "\u001B[2m";

    @Override
    public void execute(Output output, Object inputData) throws IOException {
        String pathStr = (String) output.config().get("path");
        Path path = Path.of(pathStr);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(path, inputData.toString());
        System.out.printf("  %s→ Wrote to:%s %s%n", DIM, RESET, path);
    }

    @Override
    public String getType() {
        return "file";
    }
}