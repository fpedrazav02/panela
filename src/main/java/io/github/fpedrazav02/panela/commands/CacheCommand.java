package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.cache.CacheStore;
import io.github.fpedrazav02.panela.db.RunRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "cache", description = "Manage the Panela cache",
        subcommands = { CacheCommand.Clean.class })
public class CacheCommand implements Runnable {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";

    @Override
    public void run() {
        System.out.println();
        System.out.println(CYAN + BOLD + "panela cache" + RESET);
        System.out.println("  clean [<job>]   Remove cached outputs and run history");
        System.out.println();
    }

    @Command(name = "clean", description = "Remove cached outputs and run history")
    static class Clean implements Runnable {

        private static final String RESET  = "\u001B[0m";
        private static final String BOLD   = "\u001B[1m";
        private static final String GREEN  = "\u001B[32m";
        private static final String RED    = "\u001B[31m";

        @Parameters(arity = "0..1", description = "Job name (omit to clean all)")
        private String jobName;

        @Override
        public void run() {
            try {
                RunRepository repo = new RunRepository();
                if (jobName != null) {
                    new CacheStore(jobName).cleanJob();
                    repo.deleteByJobName(jobName);
                    System.out.printf("%n%s✔%s Cache cleared for job '%s'.%n%n", GREEN + BOLD, RESET, jobName);
                } else {
                    CacheStore.cleanAll();
                    repo.deleteAll();
                    System.out.printf("%n%s✔%s Cache cleared.%n%n", GREEN + BOLD, RESET);
                }
            } catch (Exception e) {
                System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
            }
        }
    }
}
