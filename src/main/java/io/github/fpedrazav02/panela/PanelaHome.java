package io.github.fpedrazav02.panela;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PanelaHome
 */
public class PanelaHome {
    private static final String ENV_VARIABLE = "PANELA_PATH";
    private final Path baseDir;
    private final Path jobDir;
    private final Path logDir;

    private PanelaHome() {
        String pathEnvVariable = System.getenv(PanelaHome.ENV_VARIABLE);
        this.baseDir = pathEnvVariable != null ? Paths.get(pathEnvVariable)
                : Paths.get(System.getProperty("user.home"), ".panela");

        this.jobDir = this.baseDir.resolve("jobs");
        this.logDir = this.baseDir.resolve("logs");

        //Try creating resources
        try {
            Files.createDirectories(baseDir);
            Files.createDirectories(jobDir);
            Files.createDirectories(logDir);
        } catch (IOException e) {
            throw new IllegalStateException("PanelaHome could not initialize correctly directories under: " + baseDir, e);
        }
    }

    private static class PanelaHomeHolder {
        private static final PanelaHome uniqueInstance = new PanelaHome();
    }

    public static PanelaHome getInstance() {
        return PanelaHomeHolder.uniqueInstance;
    }

    public Path getJobDir() {
        return jobDir;
    }

    public Path getLogDir() {
        return logDir;
    }

    public Path getBaseDir() {
        return baseDir;
    }

    /*
     Based on a JobName (string) return a Path object for the lua interpreter to run
     */
    public Path getJobPath(String job) {
        String JOB_BASE_NAME = "job.lua";
        return this.jobDir.resolve(job).resolve(JOB_BASE_NAME);
    }
}
