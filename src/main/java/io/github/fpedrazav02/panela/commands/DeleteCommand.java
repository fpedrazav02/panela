package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.exceptions.custom.JobNotFoundException;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

@Command(name = "delete", description = "Delete an existing Panela job")
public class DeleteCommand implements Runnable {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String DIM    = "\u001B[2m";

    @CommandLine.Parameters(index = "0", description = "Name of the job to delete")
    private String jobName;

    @CommandLine.Option(names = {"-f", "--force"}, description = "Skip confirmation prompt")
    private boolean force;

    @Override
    public void run() {
        try {
            PanelaHome panelaHome = PanelaHome.getInstance();
            Path jobDir = panelaHome.getJobBaseDir(jobName);

            if (!Files.exists(jobDir)) {
                throw new JobNotFoundException(jobName);
            }

            if (!force) {
                System.out.printf("%nDelete job '%s'?  %s%s%s  [y/N] ",
                        jobName, DIM, jobDir, RESET);
                String answer = new Scanner(System.in).nextLine().trim();
                if (!answer.equalsIgnoreCase("y")) {
                    System.out.printf("%sAborted.%s%n%n", DIM, RESET);
                    return;
                }
            }

            deleteRecursively(jobDir);

            System.out.printf("%nJob '%s' deleted.%n%n", jobName);

        } catch (JobNotFoundException e) {
            System.err.printf("%n%serror:%s %s%n%n", RED + BOLD, RESET, e.getMessage());
        } catch (IOException e) {
            System.err.printf("%n%serror:%s Could not delete job '%s': %s%n%n",
                    RED + BOLD, RESET, jobName, e.getMessage());
        }
    }

    private void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

