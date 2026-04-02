package io.github.fpedrazav02.panela.runner;

import io.github.fpedrazav02.panela.PanelaHome;
import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.exceptions.PanelaException;
import io.github.fpedrazav02.panela.logging.PanelaLogger;
import io.github.fpedrazav02.panela.model.*;
import io.github.fpedrazav02.panela.model.tabular.Table;
import io.github.fpedrazav02.panela.runner.input.*;
import io.github.fpedrazav02.panela.runner.output.FileOutputRunner;
import io.github.fpedrazav02.panela.runner.output.JavaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.LuaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.OutputRunner;
import io.github.fpedrazav02.panela.runner.transform.*;
import io.github.fpedrazav02.panela.runner.transform.TransformRunner;

import java.util.*;

public class SimpleRunner implements DagRunner {

    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String GREEN   = "\u001B[32m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RED     = "\u001B[31m";
    private static final String DIM     = "\u001B[2m";

    private final JobDAG dag;
    private final String jobName;
    private final Map<String, Object> results = new HashMap<>();

    private final Map<String, InputRunner>    inputRunners    = new HashMap<>();
    private final Map<String, TransformRunner> transformRunners = new HashMap<>();
    private final Map<String, OutputRunner>   outputRunners   = new HashMap<>();

    public SimpleRunner(JobDAG dag, String jobName) {
        this.dag = dag;
        this.jobName = jobName;
        registerRunners();
    }

    private void registerRunners() {
        registerInputRunner(new ValueInputRunner());
        registerInputRunner(new LuaInputRunner());
        registerInputRunner(new JavaInputRunner());
        registerInputRunner(new FileInputRunner());

        registerTransformRunner(new EchoTransformRunner());
        registerTransformRunner(new TableTransformRunner());
        registerTransformRunner(new LuaTransformRunner());
        registerTransformRunner(new JavaTransformRunner());

        registerOutputRunner(new FileOutputRunner());
        registerOutputRunner(new LuaOutputRunner());
        registerOutputRunner(new JavaOutputRunner());
    }

    public void registerInputRunner(InputRunner runner)       { inputRunners.put(runner.getType(), runner); }
    public void registerTransformRunner(TransformRunner runner) { transformRunners.put(runner.getType(), runner); }
    public void registerOutputRunner(OutputRunner runner)     { outputRunners.put(runner.getType(), runner); }

    @Override
    public void run() throws Exception {
        List<String> executionOrder = dag.getExecutionOrder();
        int total = executionOrder.size();

        System.out.println();
        System.out.printf("%s▶ Running job %s'%s'%s  %s(%d steps)%s%n",
                BOLD, GREEN, jobName, RESET, DIM, total, RESET);
        System.out.println();

        PanelaLogger.jobStart(jobName, dag.getJobVersion(), total);
        long jobStart = System.currentTimeMillis();

        int step = 1;
        for (String nodeName : executionOrder) {
            JobDAG.Node node = dag.getNode(nodeName);
            printStepHeader(step++, total, node);
            try {
                executeNode(node);
            } catch (PanelaException e) {
                System.err.printf("  %s  error: %s%s%n%n", RED, e.getMessage(), RESET);
                PanelaLogger.jobFailed(jobName, System.currentTimeMillis() - jobStart, e);
                throw e;
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                System.err.printf("  %s  error: %s%s%n%n", RED, msg, RESET);
                PanelaLogger.jobFailed(jobName, System.currentTimeMillis() - jobStart, e);
                throw e;
            }
        }

        PanelaLogger.jobSuccess(jobName, System.currentTimeMillis() - jobStart);

        String buildDir = PanelaHome.getInstance().getBuildDir(jobName).toString();
        System.out.printf("%s✔ Done%s  %s→ %s%s%n%n", GREEN + BOLD, RESET, DIM, buildDir, RESET);
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

    private void executeNode(JobDAG.Node node) throws Exception {
        switch (node.type()) {
            case INPUT    -> executeInput((Input) node.data());
            case TRANSFORM -> executeTransform((Transform) node.data());
            case OUTPUT   -> executeOutput((Output) node.data());
        }
    }

    private void executeInput(Input input) throws Exception {
        InputRunner runner = inputRunners.get(input.type());
        if (runner == null) {
            throw new IllegalArgumentException("No runner for input type '" + input.type() + "'");
        }
        PanelaLogger.nodeStart(jobName, input.name(), "INPUT");
        long start = System.currentTimeMillis();
        try {
            Object result = runner.execute(input, this.jobName);
            long duration = System.currentTimeMillis() - start;
            results.put(input.name(), result);
            PanelaLogger.nodeSuccess(jobName, input.name(), "INPUT", duration, summarize(result));
            System.out.printf("  %s↳ %s%s%n%n", DIM, summarize(result), RESET);
        } catch (Exception e) {
            PanelaLogger.nodeFailed(jobName, input.name(), "INPUT", System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private void executeTransform(Transform transform) throws Exception {
        TransformRunner runner = transformRunners.get(transform.type());
        if (runner == null) {
            throw new IllegalArgumentException("No runner for transform type '" + transform.type() + "'");
        }
        Object inputData = results.get(transform.from());
        if (inputData == null) {
            throw new IllegalArgumentException(
                    "Transform '" + transform.name() + "' references unknown source '" + transform.from() + "'");
        }
        PanelaLogger.nodeStart(jobName, transform.name(), "TRANSFORM");
        long start = System.currentTimeMillis();
        try {
            Object result = runner.execute(transform, inputData);
            long duration = System.currentTimeMillis() - start;
            results.put(transform.name(), result);
            PanelaLogger.nodeSuccess(jobName, transform.name(), "TRANSFORM", duration, summarize(result));
            System.out.printf("  %s↳ %s → %s%s%n%n", DIM, summarize(inputData), summarize(result), RESET);
        } catch (Exception e) {
            PanelaLogger.nodeFailed(jobName, transform.name(), "TRANSFORM", System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private void executeOutput(Output output) throws Exception {
        OutputRunner runner = outputRunners.get(output.type());
        if (runner == null) {
            throw new IllegalArgumentException("No runner for output type '" + output.type() + "'");
        }
        Object inputData = results.get(output.from());
        if (inputData == null) {
            throw new IllegalArgumentException(
                    "Output '" + output.name() + "' references unknown source '" + output.from() + "'");
        }
        PanelaLogger.nodeStart(jobName, output.name(), "OUTPUT");
        long start = System.currentTimeMillis();
        try {
            runner.execute(output, inputData, jobName);
            long duration = System.currentTimeMillis() - start;
            PanelaLogger.nodeSuccess(jobName, output.name(), "OUTPUT", duration, summarize(inputData));
            System.out.printf("  %s↳ written  (%s)%s%n%n", DIM, summarize(inputData), RESET);
        } catch (Exception e) {
            PanelaLogger.nodeFailed(jobName, output.name(), "OUTPUT", System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    /** Single-line human summary of a result value. */
    private static String summarize(Object value) {
        if (value == null) return "null";
        if (value instanceof Table t) {
            return String.format("Table[%d rows × %d cols]", t.rowCount(), t.colCount());
        }
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

