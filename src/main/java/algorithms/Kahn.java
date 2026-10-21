package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Kahn {

    public static List<Integer> run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kahn requires a directed graph"
            );
        }

        int vertexCount = graph.getVertexCount();
        int[] inDegree = new int[vertexCount];

        for (Edge edge : graph.getEdges()) {
            inDegree[edge.getTo()]++;
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (inDegree[vertex] == 0) {
                queue.addLast(vertex);
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!queue.isEmpty()) {
            int vertex = queue.removeFirst();
            order.add(vertex);

            for (Edge edge : graph.getNeighbors(vertex)) {
                metrics.recordEdgeScan();

                int neighbor = edge.getTo();
                inDegree[neighbor]--;

                if (inDegree[neighbor] == 0) {
                    queue.addLast(neighbor);
                }
            }
        }

        if (order.size() != vertexCount) {
            throw new IllegalArgumentException(
                    "Graph contains a directed cycle"
            );
        }

        return order;
    }

    public static boolean isValidOrder(
            Graph graph,
            List<Integer> order
    ) {
        int vertexCount = graph.getVertexCount();

        if (!graph.isDirected() || order.size() != vertexCount) {
            return false;
        }

        int[] position = new int[vertexCount];
        boolean[] seen = new boolean[vertexCount];

        for (int index = 0; index < order.size(); index++) {
            int vertex = order.get(index);

            if (vertex < 0 || vertex >= vertexCount || seen[vertex]) {
                return false;
            }

            seen[vertex] = true;
            position[vertex] = index;
        }

        for (Edge edge : graph.getEdges()) {
            if (position[edge.getFrom()] >= position[edge.getTo()]) {
                return false;
            }
        }

        return true;
    }
}