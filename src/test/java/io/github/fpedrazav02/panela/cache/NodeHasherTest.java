package io.github.fpedrazav02.panela.cache;

import io.github.fpedrazav02.panela.model.Input;
import io.github.fpedrazav02.panela.model.Transform;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class NodeHasherTest {

    private static Input valueInput(String name, String data) {
        return new Input(name, "value", null, null, Map.of("data", data, "type", "string"));
    }

    @Test
    public void sameInputProducesSameHash() throws Exception {
        NodeHasher h1 = new NodeHasher("job");
        NodeHasher h2 = new NodeHasher("job");

        Input input = valueInput("raw", "hello");
        assertEquals(h1.hashNode(asNode(input)), h2.hashNode(asNode(input)));
    }

    @Test
    public void differentInputProducesDifferentHash() throws Exception {
        NodeHasher hasher = new NodeHasher("job");

        String h1 = hasher.hashNode(asNode(valueInput("a", "foo")));
        NodeHasher hasher2 = new NodeHasher("job");
        String h2 = hasher2.hashNode(asNode(valueInput("b", "bar")));

        assertNotEquals(h1, h2);
    }

    @Test
    public void transformHashChangesWhenUpstreamChanges() throws Exception {
        Transform transform = new Transform("clean", "echo", "raw", null, null, Map.of());

        NodeHasher h1 = new NodeHasher("job");
        h1.hashNode(asNode(valueInput("raw", "version-1")));
        String hash1 = h1.hashNode(asTransformNode(transform));

        NodeHasher h2 = new NodeHasher("job");
        h2.hashNode(asNode(valueInput("raw", "version-2")));
        String hash2 = h2.hashNode(asTransformNode(transform));

        assertNotEquals(hash1, hash2);
    }

    private static io.github.fpedrazav02.panela.dag.JobDAG.Node asNode(Input input) {
        return new io.github.fpedrazav02.panela.dag.JobDAG.Node(
                input.name(), io.github.fpedrazav02.panela.dag.JobDAG.NodeType.INPUT, input);
    }

    private static io.github.fpedrazav02.panela.dag.JobDAG.Node asTransformNode(Transform t) {
        return new io.github.fpedrazav02.panela.dag.JobDAG.Node(
                t.name(), io.github.fpedrazav02.panela.dag.JobDAG.NodeType.TRANSFORM, t);
    }
}
