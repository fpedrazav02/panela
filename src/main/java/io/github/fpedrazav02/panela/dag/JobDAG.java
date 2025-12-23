package io.github.fpedrazav02.panela.dag;

import io.github.fpedrazav02.panela.model.*;

import java.util.*;

public class JobDAG {

    private final Job job;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<String>> dependencies = new HashMap<>();

    public JobDAG(Job job) throws Exception {
        this.job = job;
        buildDAG();
        validateDAG();
    }

    private void buildDAG() {
        for (Input input : job.inputs()) {
            nodes.put(input.name(), new Node(input.name(), NodeType.INPUT, input));
            dependencies.put(input.name(), Collections.emptyList());
        }

        for (Transform transform : job.transforms()) {
            nodes.put(transform.name(), new Node(transform.name(), NodeType.TRANSFORM, transform));
            dependencies.put(transform.name(), transform.from() != null ? List.of(transform.from()) : Collections.emptyList());
        }

        for (Output output : job.outputs()) {
            nodes.put(output.name(), new Node(output.name(), NodeType.OUTPUT, output));
            dependencies.put(output.name(), output.from() != null ? List.of(output.from()) : Collections.emptyList());
        }
    }

    private void validateDAG() throws Exception {
        for (Map.Entry<String, List<String>> entry : dependencies.entrySet()) {
            for (String dep : entry.getValue()) {
                if (!nodes.containsKey(dep)) {
                    throw new Exception(String.format("Node '%s' references non-existent dependency '%s'", entry.getKey(), dep));
                }
            }
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : nodes.keySet()) {
            if (hasCycle(node, visited, recursionStack)) {
                throw new Exception("Cycle detected in job DAG");
            }
        }
    }

    private boolean hasCycle(String node, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(node)) {
            return true;
        }

        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (String dep : dependencies.getOrDefault(node, Collections.emptyList())) {
            if (hasCycle(dep, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    public List<String> getExecutionOrder() {
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (String node : nodes.keySet()) {
            topologicalSort(node, visited, order);
        }

        return order;
    }

    private void topologicalSort(String node, Set<String> visited, List<String> order) {
        if (visited.contains(node)) {
            return;
        }

        visited.add(node);

        for (String dep : dependencies.getOrDefault(node, Collections.emptyList())) {
            topologicalSort(dep, visited, order);
        }

        order.add(node);
    }

    public String getJobName(){
        return this.job.name();
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public enum NodeType {
        INPUT, TRANSFORM, OUTPUT
    }

    public record Node(String name, NodeType type, Object data) {
    }
}