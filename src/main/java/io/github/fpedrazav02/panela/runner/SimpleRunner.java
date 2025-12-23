package io.github.fpedrazav02.panela.runner;

import io.github.fpedrazav02.panela.dag.JobDAG;
import io.github.fpedrazav02.panela.model.*;
import io.github.fpedrazav02.panela.runner.input.*;
import io.github.fpedrazav02.panela.runner.output.FileOutputRunner;
import io.github.fpedrazav02.panela.runner.output.JavaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.LuaOutputRunner;
import io.github.fpedrazav02.panela.runner.output.OutputRunner;
import io.github.fpedrazav02.panela.runner.transform.*;
import io.github.fpedrazav02.panela.runner.transform.TransformRunner;

import java.util.*;

public class SimpleRunner implements DagRunner {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String DIM = "\u001B[2m";

    private final JobDAG dag;
    private final Map<String, Object> results = new HashMap<>();

    private final Map<String, InputRunner> inputRunners = new HashMap<>();
    private final Map<String, TransformRunner> transformRunners = new HashMap<>();
    private final Map<String, OutputRunner> outputRunners = new HashMap<>();

    public SimpleRunner(JobDAG dag) {
        this.dag = dag;
        registerRunners();
    }

    private void registerRunners() {
        // Input runners
        registerInputRunner(new ValueInputRunner());
        registerInputRunner(new LuaInputRunner());
        registerInputRunner(new JavaInputRunner());

        // Transform runners
        registerTransformRunner(new EchoTransformRunner());
        registerTransformRunner(new LuaTransformRunner());
        registerTransformRunner(new JavaTransformRunner());

        // Output runners
        registerOutputRunner(new FileOutputRunner());
        registerOutputRunner(new LuaOutputRunner());
        registerOutputRunner(new JavaOutputRunner());
    }

    public void registerInputRunner(InputRunner runner) {
        inputRunners.put(runner.getType(), runner);
    }

    public void registerTransformRunner(TransformRunner runner) {
        transformRunners.put(runner.getType(), runner);
    }

    public void registerOutputRunner(OutputRunner runner) {
        outputRunners.put(runner.getType(), runner);
    }

    @Override
    public void run() throws Exception {
        System.out.println();
        System.out.println(BOLD + "🚀 Starting job execution..." + RESET);
        System.out.println();

        List<String> executionOrder = dag.getExecutionOrder();

        for (String nodeName : executionOrder) {
            JobDAG.Node node = dag.getNode(nodeName);
            executeNode(node);
        }

        System.out.println();
        System.out.println(GREEN + "✅ Job completed successfully!" + RESET);
        System.out.println();
    }

    private void executeNode(JobDAG.Node node) throws Exception {
        String icon = getNodeIcon(node.type());
        String color = getNodeColor(node.type());

        System.out.printf("%s %s%s%s %s[%s]%s%n",
                icon,
                color + BOLD, node.name(), RESET,
                DIM, node.type(), RESET
        );

        switch (node.type()) {
            case INPUT -> executeInput((Input) node.data());
            case TRANSFORM -> executeTransform((Transform) node.data());
            case OUTPUT -> executeOutput((Output) node.data());
        }
    }

    private void executeInput(Input input) throws Exception {
        InputRunner runner = inputRunners.get(input.type());
        if (runner == null) {
            throw new Exception("No runner found for input type: " + input.type());
        }

        Object result = runner.execute(input);
        results.put(input.name(), result);

        System.out.printf("  %s→ Result:%s %s%n%n", DIM, RESET, result);
    }

    private void executeTransform(Transform transform) throws Exception {
        TransformRunner runner = transformRunners.get(transform.type());
        if (runner == null) {
            throw new Exception("No runner found for transform type: " + transform.type());
        }

        Object inputData = results.get(transform.from());
        System.out.printf("  %s← Input:%s %s%n", DIM, RESET, inputData);

        Object result = runner.execute(transform, inputData);
        results.put(transform.name(), result);

        System.out.printf("  %s→ Result:%s %s%n%n", DIM, RESET, result);
    }

    private void executeOutput(Output output) throws Exception {
        OutputRunner runner = outputRunners.get(output.type());
        if (runner == null) {
            throw new Exception("No runner found for output type: " + output.type());
        }

        Object inputData = results.get(output.from());
        System.out.printf("  %s← Input:%s %s%n", DIM, RESET, inputData);

        runner.execute(output, inputData);
        System.out.printf("  %s✓ Written%s%n%n", GREEN, RESET);
    }

    private String getNodeIcon(JobDAG.NodeType type) {
        return switch (type) {
            case INPUT -> "📥";
            case TRANSFORM -> "⚙️";
            case OUTPUT -> "📤";
        };
    }

    private String getNodeColor(JobDAG.NodeType type) {
        return switch (type) {
            case INPUT -> GREEN;
            case TRANSFORM -> BLUE;
            case OUTPUT -> MAGENTA;
        };
    }
}