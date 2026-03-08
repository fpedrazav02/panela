package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class JobParseException extends PanelaException {
    public JobParseException(String jobName, String message) {
        super("Failed to parse job '" + jobName + "': " + message);
    }

    public JobParseException(String jobName, Throwable cause) {
        super("Failed to parse job '" + jobName + "': " + cause.getMessage(), cause);
    }
}

