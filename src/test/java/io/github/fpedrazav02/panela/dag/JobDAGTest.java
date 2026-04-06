package io.github.fpedrazav02.panela.dag;

import io.github.fpedrazav02.panela.model.Input;
import io.github.fpedrazav02.panela.model.Job;
import io.github.fpedrazav02.panela.model.Output;
import io.github.fpedrazav02.panela.model.Transform;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JobDAGTest {

    private static Input input(String name) {
        return new Input(name, "value", null, null, Map.of("data", "x", "type", "string"));
    }

    private static Transform transform(String name, String from) {
        return new Transform(name, "echo", from, null, null, Map.of());
    }

    private static Output output(String name, String from) {
        return new Output(name, "file", from, null, null, Map.of("format", "csv"));
    }

    @Test
    public void validDagBuildsSuccessfully() throws Exception {
        Job job = new Job("test", "0.1", List.of(input("raw")),
                List.of(transform("clean", "raw")),
                List.of(output("result", "clean")));
        JobDAG dag = new JobDAG(job);

        List<String> order = dag.getExecutionOrder();
        assertTrue(order.indexOf("raw") < order.indexOf("clean"));
        assertTrue(order.indexOf("clean") < order.indexOf("result"));
    }

    @Test(expected = Exception.class)
    public void missingDependencyThrows() throws Exception {
        Job job = new Job("test", "0.1", List.of(input("raw")),
                List.of(transform("clean", "nonexistent")),
                List.of());
        new JobDAG(job);
    }

    @Test(expected = Exception.class)
    public void cycleDetectionThrows() throws Exception {
        Job job = new Job("test", "0.1", List.of(),
                List.of(transform("a", "b"), transform("b", "a")),
                List.of());
        new JobDAG(job);
    }
}
