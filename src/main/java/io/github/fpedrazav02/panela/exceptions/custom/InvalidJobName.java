package io.github.fpedrazav02.panela.exceptions.custom;

import io.github.fpedrazav02.panela.exceptions.PanelaException;

public class InvalidJobName extends PanelaException {
    public InvalidJobName(String jobName) {
        super("Invalid Job Name: " + jobName);
    }
}
