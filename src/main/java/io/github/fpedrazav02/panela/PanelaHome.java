package io.github.fpedrazav02.panela;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PanelaHome
 */
public class PanelaHome {

    // TODO: Posible refactor for Thread safety
    private static PanelaHome uniqueInstance = null;

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
    }

    public static PanelaHome getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new PanelaHome();
        }
        return uniqueInstance;
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
}
