package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command(name = "list", description = "List all available jobs")
public class ListCommand implements Runnable {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String CYAN  = "\u001B[36m";
    private static final String RED   = "\u001B[31m";

    @Override
    public void run() {
        PanelaHome home = PanelaHome.getInstance();
        Path jobsDir = home.getJobDir();

        try (var stream = Files.list(jobsDir)) {
            List<Path> jobs = stream
                    .filter(Files::isDirectory)
                    .sorted()
                    .toList();

            if (jobs.isEmpty()) {
                System.out.printf("%nNo jobs found. Create one with: panela new <name>%n%n");
                return;
            }

            System.out.printf("%n%sJobs  %s%s(%d)%s%n%n", BOLD, RESET, DIM, jobs.size(), RESET);
            for (Path job : jobs) {
                String name = job.getFileName().toString();
                boolean hasJobFile = Files.exists(job.resolve("job.lua"));
                String status = hasJobFile ? "" : "  " + DIM + "(no job.lua)" + RESET;
                System.out.printf("  %s%s%s%s%n", CYAN, name, RESET, status);
            }
            System.out.println();

        } catch (IOException e) {
            System.err.printf("%n%serror:%s Could not read jobs directory: %s%n%n",
                    RED + BOLD, RESET, e.getMessage());
        }
    }
}



