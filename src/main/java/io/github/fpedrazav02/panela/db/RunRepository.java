package io.github.fpedrazav02.panela.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RunRepository {

    private final Connection conn;

    public RunRepository() {
        this.conn = PanelaDatabase.getInstance().getConnection();
    }

    public long insertRun(String jobName, String jobVersion, long startedAt) throws SQLException {
        String sql = "INSERT INTO runs (job_name, job_version, started_at, status) VALUES (?, ?, ?, 'RUNNING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, jobName);
            ps.setString(2, jobVersion);
            ps.setLong(3, startedAt);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.getLong(1);
            }
        }
    }

    public void updateRunFinished(long runId, long finishedAt, String status) throws SQLException {
        String sql = "UPDATE runs SET finished_at = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, finishedAt);
            ps.setString(2, status);
            ps.setLong(3, runId);
            ps.executeUpdate();
        }
    }

    public void insertNodeRun(long runId, String nodeName, String nodeType, String nodeHash,
                               long durationMs, String shape, String status, boolean cached) throws SQLException {
        String sql = """
                INSERT INTO node_runs
                    (run_id, node_name, node_type, node_hash, duration_ms, shape, status, cached)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, runId);
            ps.setString(2, nodeName);
            ps.setString(3, nodeType);
            ps.setString(4, nodeHash);
            ps.setLong(5, durationMs);
            ps.setString(6, shape);
            ps.setString(7, status);
            ps.setInt(8, cached ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void deleteByJobName(String jobName) throws SQLException {
        String deleteNodes = "DELETE FROM node_runs WHERE run_id IN (SELECT id FROM runs WHERE job_name = ?)";
        String deleteRuns  = "DELETE FROM runs WHERE job_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteNodes)) {
            ps.setString(1, jobName);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(deleteRuns)) {
            ps.setString(1, jobName);
            ps.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM node_runs");
            stmt.executeUpdate("DELETE FROM runs");
        }
    }
}
