package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.service.JobCreator;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

/**
 * NewCommand
 */
@Command(name = "new", description = "Create a new Panela job or transformation")
public class NewCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Name of the panela job")
    private String jobName;

    @Override
    public void run() {
        try {
            System.out.printf("Creating new Job %s\n", jobName);
            PanelaHome panelaHome = PanelaHome.getInstance();
            JobCreator jobCreator = JobCreator.of(panelaHome);
            jobCreator.createJob(jobName);
        } catch (IOException e) {
            System.err.println("Error creating panela Job: " + e.getMessage());
        } catch (PanelaException e) {
            System.err.println("Error running new command: " + e.getMessage());
        }
    }
}
