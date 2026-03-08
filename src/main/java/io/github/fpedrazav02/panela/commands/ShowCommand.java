package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.dag.DAGPrinter;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.exceptions.custom.JobNotFoundException;
import io.github.fpedrazav02.panela.exceptions.custom.JobParseException;
import io.github.fpedrazav02.panela.model.Job;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "show", description = "Show job configuration")
public class ShowCommand implements Runnable {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String RED   = "\u001B[31m";
    private static final String DIM   = "\u001B[2m";

    @CommandLine.Parameters(index = "0", description = "Name of the panela job")
    private String jobName;

    @Override
    public void run() {
        try {
            PanelaHome panelaHome = PanelaHome.getInstance();
            Path jobPath = panelaHome.getJobDir().resolve(jobName).resolve("job.lua");

            if (!jobPath.toFile().exists()) {
                throw new JobNotFoundException(jobName);
            }

            Job job;
            try {
                job = LuaJobParser.getInstance().parse(jobPath);
            } catch (Exception e) {
                throw new JobParseException(jobName, e);
            }

            JobDAG dag;
            try {
                dag = new JobDAG(job);
            } catch (Exception e) {
                throw new JobParseException(jobName, e);
            }
            new DAGPrinter(dag).print();

        } catch (PanelaException e) {
            System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
        } catch (Exception e) {
            System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
        }
    }
}