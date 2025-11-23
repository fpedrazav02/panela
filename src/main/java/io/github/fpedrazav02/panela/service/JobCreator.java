package io.github.fpedrazav02.panela.service;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.InvalidJobName;
import io.github.fpedrazav02.panela.utils.Result;
import io.github.fpedrazav02.panela.validator.impl.JobNameValidator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JobCreator {
    private final PanelaHome panelaHome;

    private JobCreator(PanelaHome panelaHome) {
        this.panelaHome = panelaHome;
    }

    public static JobCreator of(PanelaHome panelaHome) {
        return new JobCreator(panelaHome);
    }

    public void createJob(String jobName) throws IOException, InvalidJobName {
        // Check validations
        Result<String> validation = new JobNameValidator(jobName).validate();
        // TODO: Include exists validation ¿?! DBs + Providers + Local

        if (!validation.isSuccess()) {
            throw new InvalidJobName(validation.getError().orElse("Invalid Job Name"));
        }

        String validName = validation.getValue().orElseThrow();

        // Resolve job directory, sdk, runtime, .meta
        Path jobDir = panelaHome.getJobDir().resolve(validName);
        Path metaDir = jobDir.resolve(".meta");
        Path luaSdkDir = metaDir.resolve("lua-sdk");
        Path runtimeDir = metaDir.resolve("runtime");

        //Create Jobs directories
        Files.createDirectories(luaSdkDir);
        Files.createDirectories(runtimeDir);

        // Copy SDK Lua files
        this.copyResourcesToFolder("templates.job/meta/lua-sdk/job.d.lua", luaSdkDir.resolve("job.d.lua"));
        //Copy runtime files to Job
        this.copyResourcesToFolder("templates.job/meta/runtime/job.lua", runtimeDir.resolve("job.lua"));

        // TODO: Pass this to a file
        String jobContent = """
                local job = require("job")
                
                return job.define {
                  name = "%s",
                  version = "0.1.0",
                  inputs = {},
                  transforms = {},
                  outputs = {}
                }
                """.formatted(jobName);
        Files.writeString(jobDir.resolve("job.lua"), jobContent);

        // TODO: DB or file registration
    }

    private void copyResourcesToFolder(String resourcePath, Path destinationFolder) throws IOException {
        // Since it is a JAR compilation an inputStream is needed + try-with-resources cleans the stream
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Files.createDirectories(destinationFolder);
            Files.copy(inputStream, destinationFolder.resolve(resourcePath), StandardCopyOption.REPLACE_EXISTING);
        }

    }
}