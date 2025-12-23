package io.github.fpedrazav02.panela.dag;

import io.github.fpedrazav02.panela.model.*;

import java.util.*;

public class DAGPrinter {

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String DIM = "\u001B[2m";

    private final JobDAG dag;

    public DAGPrinter(JobDAG dag) {
        this.dag = dag;
    }

    public void print() {
        printHeader();
        printGraph();
    }

    private void printHeader() {
        String border = "═══════════════════════════════════════════════";
        System.out.println();
        System.out.println(CYAN + border + RESET);
        System.out.println(CYAN + "🎋  " + BOLD + this.dag.getJobName() + " DAG Visualization" + RESET);
        System.out.println(CYAN + border + RESET);
        System.out.println();
    }

    private void printGraph() {
        List<String> executionOrder = dag.getExecutionOrder();

        System.out.println(BOLD + "🔀 DAG Flow" + RESET);
        System.out.println(DIM + "───────────" + RESET);

        for (int i = 0; i < executionOrder.size(); i++) {
            String nodeName = executionOrder.get(i);
            JobDAG.Node node = dag.getNode(nodeName);

            String icon = getNodeIcon(node.type());
            String color = getNodeColor(node.type());

            // Print node
            System.out.printf("  %s %s%s%s%n",
                    icon,
                    color + BOLD, nodeName, RESET
            );

            // Print connection to next node
            if (i < executionOrder.size() - 1) {
                System.out.println(DIM + "  │" + RESET);
                System.out.println(DIM + "  ▼" + RESET);
            }
        }

        System.out.println();
    }

    private String getNodeIcon(JobDAG.NodeType type) {
        return switch (type) {
            case INPUT -> "ℹ️";
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