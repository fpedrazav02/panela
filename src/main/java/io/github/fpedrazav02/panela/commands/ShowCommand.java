package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.model.Job;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "show", description = "Show job configuration")
public class ShowCommand implements Runnable {

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

            System.out.println("=== Job: " + job.name() + " ===");
            System.out.println("Version: " + job.version());
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error parsing job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}