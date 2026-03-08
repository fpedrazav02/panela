package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class JobNotFoundException extends PanelaException {
    public JobNotFoundException(String jobName) {
        super("Job '" + jobName + "' does not exist.");
    }
}

