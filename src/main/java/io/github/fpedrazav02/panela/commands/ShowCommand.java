package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.model.*;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;

@Command(name = "show", description = "Shows Job metadata and DAG")
public class ShowCommand implements Runnable {

    private final PanelaHome panelaHome = PanelaHome.getInstance();
    private final LuaJobParser luaJobParser = LuaJobParser.getInstance();

    @CommandLine.Parameters(index = "0", description = "Name of the panela job to show")
    private String jobName;

    @Override
    public void run() {
        Path jobPath = panelaHome.getJobPath(jobName);

        try {
            Job job = luaJobParser.parse(jobPath);
            displayJob(job);
        } catch (Exception e) {
            System.err.println("Error parsing job: " + e.getMessage());
        }
    }

    private void displayJob(Job job) {
        System.out.println("📦 Job: " + job.name() + " v" + job.version());
        System.out.println();

        System.out.println("Inputs (" + job.inputs().size() + "):");
        for (Input input : job.inputs()) {
            System.out.println("  • " + input.name() + " [" + input.type() + "] = " + input.data());
        }
        System.out.println();

        System.out.println("Transforms (" + job.transforms().size() + "):");
        for (Transform transform : job.transforms()) {
            System.out.println("  • " + transform.name() + " ← " + transform.function() + "(" + transform.from() + ")");
            transform.params().forEach((k, v) ->
                    System.out.println("      " + k + " = " + v)
            );
        }
        System.out.println();

        System.out.println("Outputs (" + job.outputs().size() + "):");
        for (Output output : job.outputs()) {
            System.out.println("  • " + output.name() + " → " + output.destination() + " ← " + output.from());
        }
    }
}