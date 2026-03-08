package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class JobAlreadyExistsException extends PanelaException {
    public JobAlreadyExistsException(String jobName) {
        super("Job '" + jobName + "' already exists. Use a different name or delete the existing job first.");
    }
}

