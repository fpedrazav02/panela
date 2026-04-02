package io.github.fpedrazav02.panela.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PanelaLogger {

    private static final Logger log = LoggerFactory.getLogger("panela");

    private PanelaLogger() {}

    public static void jobStart(String jobName, String version, int nodeCount) {
        log.info("JOB_START job={} version={} nodes={}", jobName, version, nodeCount);
    }

    public static void jobSuccess(String jobName, long durationMs) {
        log.info("JOB_SUCCESS job={} duration_ms={}", jobName, durationMs);
    }

    public static void jobFailed(String jobName, long durationMs, Throwable cause) {
        log.error("JOB_FAILED job={} duration_ms={} error_type={} message={}",
                jobName, durationMs,
                cause.getClass().getSimpleName(),
                cause.getMessage());
    }

    public static void dagBuilt(String jobName, int nodeCount) {
        log.debug("DAG_BUILT job={} nodes={}", jobName, nodeCount);
    }

    public static void dagValidated(String jobName) {
        log.debug("DAG_VALID job={}", jobName);
    }

    public static void nodeStart(String jobName, String nodeName, String nodeType) {
        log.debug("NODE_START job={} node={} type={}", jobName, nodeName, nodeType);
    }

    public static void nodeSuccess(String jobName, String nodeName, String nodeType,
                                   long durationMs, String shape) {
        log.info("NODE_OK job={} node={} type={} duration_ms={} shape={}",
                jobName, nodeName, nodeType, durationMs, shape);
    }

    public static void nodeFailed(String jobName, String nodeName, String nodeType,
                                  long durationMs, Throwable cause) {
        log.error("NODE_FAILED job={} node={} type={} duration_ms={} error_type={} message={}",
                jobName, nodeName, nodeType, durationMs,
                cause.getClass().getSimpleName(),
                cause.getMessage());
    }
}
