package io.github.fpedrazav02.panela;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/** App */
@Command(
    name = "panela",
    mixinStandardHelpOptions = true,
    description = "Panela - A lightweight and extendable ETL runner")
public class Panela implements Runnable {
  public static void main(String[] args) {
    int exitCode = new CommandLine(new Panela()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    System.out.println("Panela CLI - use 'panela init <name>' to start a new job.");
  }
}
