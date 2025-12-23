package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.runner.SimpleRunner;
import io.github.fpedrazav02.panela.model.Job;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "run", description = "Run a panela job")
public class RunCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Name of the panela job")
    private String jobName;

    @Override
    public void run() {
        try {
            PanelaHome panelaHome = PanelaHome.getInstance();
            Path jobPath = panelaHome.getJobDir().resolve(jobName).resolve("job.lua");

            if (!jobPath.toFile().exists()) {
                System.err.printf("Job '%s' not found\n", jobName);
                return;
            }

            Job job = LuaJobParser.getInstance().parse(jobPath);
            JobDAG dag = new JobDAG(job);

            SimpleRunner runner = new SimpleRunner(dag);
            runner.run();

        } catch (Exception e) {
            System.err.println("Error running job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}