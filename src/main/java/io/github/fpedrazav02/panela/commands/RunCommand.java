package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.runner.CachingRunner;
import io.github.fpedrazav02.panela.model.Job;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "run", description = "Run a panela job")
public class RunCommand implements Runnable {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String RED   = "\u001B[31m";
    private static final String DIM   = "\u001B[2m";

    @CommandLine.Parameters(index = "0", description = "Name of the panela job")
    private String jobName;

    @Override
    public void run() {
        PanelaHome panelaHome = PanelaHome.getInstance();
        Path jobPath = panelaHome.getJobDir().resolve(jobName).resolve("job.lua");

        if (!jobPath.toFile().exists()) {
            System.err.printf("%nJob '%s' not found%n", jobName);
            System.err.printf("%sExpected: %s%s%n%n", DIM, jobPath, RESET);
            return;
        }

        try {
            Job job = LuaJobParser.getInstance().parse(jobPath);
            JobDAG dag = new JobDAG(job);
            new CachingRunner(dag, jobName).run();
        } catch (PanelaException e) {
            System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
        } catch (Exception e) {
            System.err.printf("%n%serror:%s %s%n", RED + BOLD, RESET, e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.printf("  %s  caused by: %s%s%n", DIM, cause.getMessage(), RESET);
                cause = cause.getCause();
            }
            System.err.println();
        }
    }
}
