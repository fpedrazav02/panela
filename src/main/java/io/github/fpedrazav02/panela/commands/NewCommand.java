package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.JobAlreadyExistsException;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.service.JobCreator;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(name = "new", description = "Create a new Panela job")
public class NewCommand implements Runnable {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String DIM    = "\u001B[2m";

    @CommandLine.Parameters(index = "0", description = "Name of the new job")
    private String jobName;

    @Override
    public void run() {
        try {
            PanelaHome panelaHome = PanelaHome.getInstance();
            JobCreator jobCreator = JobCreator.of(panelaHome);
            jobCreator.createJob(jobName);
            System.out.printf("%nJob '%s' created%n", jobName);
            System.out.printf("  %s→ %s%s%n%n", DIM, panelaHome.getJobBaseDir(jobName), RESET);
        } catch (JobAlreadyExistsException e) {
            System.err.printf("%n%serror:%s %s%n%n", YELLOW + BOLD, RESET, e.getMessage());
        } catch (PanelaException e) {
            System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
        } catch (IOException e) {
            System.err.printf("%n%serror:%s Could not create job '%s': %s%n%n", RED + BOLD, RESET, jobName, e.getMessage());
        }
    }
}
