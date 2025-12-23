package io.github.fpedrazav02.panela;

import io.github.fpedrazav02.panela.commands.NewCommand;
import io.github.fpedrazav02.panela.commands.ShowCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * App
 */
@Command(name = "panela",
        description = "Panela - A lightweight and extendable ETL runner",
        version = "0.0.1",
        subcommands = {NewCommand.class, ShowCommand.class}
)
public class Panela implements Runnable {

    // ANSI VARS
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show help message")
    boolean helpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info")
    boolean versionRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Panela()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        String border = "───────────────────────────────────────────────";
        System.out.println();
        System.out.println(CYAN + "🎋  " + BOLD + "Panela v1.0.0" + RESET);
        System.out.println(CYAN + border + RESET);
        System.out.println("A lightweight and extendable ETL runner\n");
        System.out.println(BOLD + "Usage" + RESET);
        System.out.println("  panela <command> [options]\n");
        System.out.println(BOLD + "Commands:" + RESET);
        System.out.println("  help      Show this help message\n");
        System.out.println(CYAN + "Docs → https://github.com/fpedrazav02/panela" + RESET);
        System.out.println(CYAN + border + RESET);
        System.out.println();
    }
}
