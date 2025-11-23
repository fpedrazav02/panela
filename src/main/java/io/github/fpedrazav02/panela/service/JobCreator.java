package io.github.fpedrazav02.panela.service;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.InvalidJobName;
import io.github.fpedrazav02.panela.utils.Result;
import io.github.fpedrazav02.panela.validator.impl.JobNameValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JobCreator {
    private final PanelaHome panelaHome;

    private JobCreator(PanelaHome panelaHome) {
        this.panelaHome = panelaHome;
    }

    public static JobCreator of(PanelaHome panelaHome) {
        return new JobCreator(panelaHome);
    }

    public void createJob(String jobName) throws IOException, InvalidJobName {
        Result<String> validation = new JobNameValidator(jobName).validate();

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

        //Copy SDK Lua files to Job
        //this.copyResourcesToFolder();

        //Copy runtime files to Job


        // TODO: DB or file registration

    }

    private void copyResourcesToFolder(String resourcePath, Path destinationFolder) throws IOException {

    }


}