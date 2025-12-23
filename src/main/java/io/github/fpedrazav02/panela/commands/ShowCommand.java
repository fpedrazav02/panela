package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.parser.LuaJobParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;


/**
 * ShowCommand
 */
@Command(name = "show", description = "Shows Job metadata and DAG")
public class ShowCommand implements Runnable {

    private final PanelaHome panelaHome = PanelaHome.getInstance();
    private final LuaJobParser luaJobParser = LuaJobParser.getInstance();


    @CommandLine.Parameters(index = "0", description = "Name of the panela job to show")
    private String jobName;

    @Override
    public void run() {
        Path jobPath = panelaHome.getJobPath(jobName);
        System.out.println("Getting job in " + jobPath.toString());

        try {
            luaJobParser.parse(jobPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
