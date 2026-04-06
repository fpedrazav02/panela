package io.github.fpedrazav02.panela.runner;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.cache.CacheStore;
import io.github.fpedrazav02.panela.cache.NodeHasher;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.db.RunRepository;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.logging.PanelaLogger;
import io.github.fpedrazav02.panela.model.Input;
import io.github.fpedrazav02.panela.model.Output;
import io.github.fpedrazav02.panela.model.Transform;
import io.github.fpedrazav02.panela.model.tabular.Table;
import io.github.fpedrazav02.panela.runner.input.*;
import io.github.fpedrazav02.panela.runner.output.FileOutputRunner;
import io.github.fpedrazav02.panela.runner.output.JavaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.LuaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.OutputRunner;
import io.github.fpedrazav02.panela.runner.transform.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachingRunner implements DagRunner {

    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String GREEN   = "\u001B[32m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";
    private static final String DIM     = "\u001B[2m";

    private final JobDAG dag;
    private final String jobName;
    private final Map<String, Object> results = new HashMap<>();

    private final Map<String, InputRunner>     inputRunners     = new HashMap<>();
    private final Map<String, TransformRunner> transformRunners = new HashMap<>();
    private final Map<String, OutputRunner>    outputRunners    = new HashMap<>();

    public CachingRunner(JobDAG dag, String jobName) {
        this.dag = dag;
        this.jobName = jobName;
        registerRunners();
    }

    private void registerRunners() {
        registerInputRunner(new ValueInputRunner());
        registerInputRunner(new FileInputRunner());
        registerInputRunner(new LuaInputRunner());
        registerInputRunner(new JavaInputRunner());
        registerTransformRunner(new EchoTransformRunner());
        registerTransformRunner(new TableTransformRunner());
        registerTransformRunner(new LuaTransformRunner());
        registerTransformRunner(new JavaTransformRunner());
        registerOutputRunner(new FileOutputRunner());
        registerOutputRunner(new LuaOutputRunner());
        registerOutputRunner(new JavaOutputRunner());
    }

    public void registerInputRunner(InputRunner r)      { inputRunners.put(r.getType(), r); }
    public void registerTransformRunner(TransformRunner r) { transformRunners.put(r.getType(), r); }
    public void registerOutputRunner(OutputRunner r)    { outputRunners.put(r.getType(), r); }

    @Override
    public void run() throws Exception {
        List<String> executionOrder = dag.getExecutionOrder();
        int total = executionOrder.size();

        System.out.println();
        System.out.printf("%s▶ Running job %s'%s'%s  %s(%d steps)%s%n",
                BOLD, GREEN, jobName, RESET, DIM, total, RESET);
        System.out.println();

        NodeHasher hasher = new NodeHasher(jobName);
        CacheStore cache  = new CacheStore(jobName);
        RunRepository repo = new RunRepository();

        long jobStarted = System.currentTimeMillis();
        PanelaLogger.jobStart(jobName, dag.getJobVersion(), total);
        long runId = repo.insertRun(jobName, dag.getJobVersion(), jobStarted);

        int step = 1;
        try {
            for (String nodeName : executionOrder) {
                JobDAG.Node node = dag.getNode(nodeName);
                String nodeHash = hasher.hashNode(node);
                printStepHeader(step++, total, node);

                long nodeStart = System.currentTimeMillis();

                if (node.type() != JobDAG.NodeType.OUTPUT && cache.exists(nodeHash)) {
                    Object result = cache.load(nodeHash);
                    results.put(nodeName, result);
                    long duration = System.currentTimeMillis() - nodeStart;
                    String shape = summarize(result);
                    System.out.printf("  %s↳ cached  %s%s%n%n", YELLOW, shape, RESET);
                    PanelaLogger.nodeSuccess(jobName, nodeName, node.type().name(), duration, shape);
                    repo.insertNodeRun(runId, nodeName, node.type().name(), nodeHash, duration, shape, "SUCCESS", true);
                } else {
                    try {
                        Object result = executeNode(node);
                        long duration = System.currentTimeMillis() - nodeStart;
                        String shape = summarize(result);
                        if (node.type() != JobDAG.NodeType.OUTPUT) {
                            cache.store(nodeHash, result);
                        }
                        PanelaLogger.nodeSuccess(jobName, nodeName, node.type().name(), duration, shape);
                        repo.insertNodeRun(runId, nodeName, node.type().name(), nodeHash, duration, shape, "SUCCESS", false);
                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - nodeStart;
                        PanelaLogger.nodeFailed(jobName, nodeName, node.type().name(), duration, e);
                        repo.insertNodeRun(runId, nodeName, node.type().name(), nodeHash, duration, null, "FAILED", false);
                        throw e;
                    }
                }
            }

            repo.updateRunFinished(runId, System.currentTimeMillis(), "SUCCESS");
            PanelaLogger.jobSuccess(jobName, System.currentTimeMillis() - jobStarted);
            String buildDir = PanelaHome.getInstance().getBuildDir(jobName).toString();
            System.out.printf("%s✔ Done%s  %s→ %s%s%n%n", GREEN + BOLD, RESET, DIM, buildDir, RESET);

        } catch (PanelaException e) {
            System.err.printf("  %s  error: %s%s%n%n", RED, e.getMessage(), RESET);
            repo.updateRunFinished(runId, System.currentTimeMillis(), "FAILED");
            PanelaLogger.jobFailed(jobName, System.currentTimeMillis() - jobStarted, e);
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.printf("  %s  error: %s%s%n%n", RED, msg, RESET);
            repo.updateRunFinished(runId, System.currentTimeMillis(), "FAILED");
            PanelaLogger.jobFailed(jobName, System.currentTimeMillis() - jobStarted, e);
            throw e;
        }
    }

    private Object executeNode(JobDAG.Node node) throws Exception {
        return switch (node.type()) {
            case INPUT -> {
                Input input = (Input) node.data();
                InputRunner runner = inputRunners.get(input.type());
                if (runner == null)
                    throw new IllegalArgumentException("No runner for input type '" + input.type() + "'");
                Object result = runner.execute(input, jobName);
                results.put(input.name(), result);
                System.out.printf("  %s↳ %s%s%n%n", DIM, summarize(result), RESET);
                yield result;
            }
            case TRANSFORM -> {
                Transform transform = (Transform) node.data();
                TransformRunner runner = transformRunners.get(transform.type());
                if (runner == null)
                    throw new IllegalArgumentException("No runner for transform type '" + transform.type() + "'");
                Object inputData = results.get(transform.from());
                if (inputData == null)
                    throw new IllegalArgumentException(
                            "Transform '" + transform.name() + "' references unknown source '" + transform.from() + "'");
                Object result = runner.execute(transform, inputData);
                results.put(transform.name(), result);
                System.out.printf("  %s↳ %s → %s%s%n%n", DIM, summarize(inputData), summarize(result), RESET);
                yield result;
            }
            case OUTPUT -> {
                Output output = (Output) node.data();
                OutputRunner runner = outputRunners.get(output.type());
                if (runner == null)
                    throw new IllegalArgumentException("No runner for output type '" + output.type() + "'");
                Object inputData = results.get(output.from());
                if (inputData == null)
                    throw new IllegalArgumentException(
                            "Output '" + output.name() + "' references unknown source '" + output.from() + "'");
                runner.execute(output, inputData, jobName);
                System.out.printf("  %s↳ written  (%s)%s%n%n", DIM, summarize(inputData), RESET);
                yield inputData;
            }
        };
    }

    private void printStepHeader(int step, int total, JobDAG.Node node) {
        String icon  = getNodeIcon(node.type());
        String color = getNodeColor(node.type());
        System.out.printf("%s[%d/%d]%s %s %s%s%s  %s%s%s%n",
                DIM, step, total, RESET,
                icon,
                color + BOLD, node.name(), RESET,
                DIM, node.type().toString().toLowerCase(), RESET);
    }

    private static String summarize(Object value) {
        if (value == null) return "null";
        if (value instanceof Table t)
            return String.format("Table[%d rows × %d cols]", t.rowCount(), t.colCount());
        String s = value.toString();
        if (s.length() > 60) s = s.substring(0, 57) + "…";
        return s;
    }

    private String getNodeIcon(JobDAG.NodeType type) {
        return switch (type) {
            case INPUT     -> "📥";
            case TRANSFORM -> "⚙️ ";
            case OUTPUT    -> "📤";
        };
    }

    private String getNodeColor(JobDAG.NodeType type) {
        return switch (type) {
            case INPUT     -> GREEN;
            case TRANSFORM -> BLUE;
            case OUTPUT    -> MAGENTA;
        };
    }
}
