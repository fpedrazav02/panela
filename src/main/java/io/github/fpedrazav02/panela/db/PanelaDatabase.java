package io.github.fpedrazav02.panela.db;

import io.github.fpedrazav02.panela.PanelaHome;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PanelaDatabase {

    private static final String CREATE_RUNS = """
            CREATE TABLE IF NOT EXISTS runs (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                job_name    TEXT    NOT NULL,
                job_version TEXT    NOT NULL,
                started_at  INTEGER NOT NULL,
                finished_at INTEGER,
                status      TEXT    NOT NULL
            )
            """;

    private static final String CREATE_NODE_RUNS = """
            CREATE TABLE IF NOT EXISTS node_runs (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                run_id      INTEGER NOT NULL REFERENCES runs(id),
                node_name   TEXT    NOT NULL,
                node_type   TEXT    NOT NULL,
                node_hash   TEXT    NOT NULL,
                duration_ms INTEGER,
                shape       TEXT,
                status      TEXT    NOT NULL,
                cached      INTEGER NOT NULL DEFAULT 0
            )
            """;

    private final Connection connection;

    private PanelaDatabase() {
        try {
            String url = "jdbc:sqlite:" + PanelaHome.getInstance().getDbPath().toAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(CREATE_RUNS);
                stmt.executeUpdate(CREATE_NODE_RUNS);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize Panela database", e);
        }
    }

    private static class Holder {
        private static final PanelaDatabase INSTANCE = new PanelaDatabase();
    }

    public static PanelaDatabase getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() {
        return connection;
    }
}
