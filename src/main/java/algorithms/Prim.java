package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static MstResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Prim requires an undirected graph"
            );
        }

        boolean[] visited = new boolean[graph.getVertexCount()];
        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        PriorityQueue<Edge> queue = new PriorityQueue<>(
                (first, second) -> metrics.compareWeights(
                        first.getWeight(),
                        second.getWeight()
                )
        );

        for (int start = 0; start < graph.getVertexCount(); start++) {
            if (visited[start]) {
                continue;
            }

            components++;
            visited[start] = true;
            addOutgoingEdges(graph, start, visited, queue, metrics);

            while (!queue.isEmpty()) {
                Edge edge = queue.poll();
                int vertex = edge.getTo();

                if (visited[vertex]) {
                    continue;
                }

                visited[vertex] = true;
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();

                addOutgoingEdges(graph, vertex, visited, queue, metrics);
            }
        }

        return new MstResult(selectedEdges, totalWeight, components);
    }

    private static void addOutgoingEdges(
            Graph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<Edge> queue,
            Metrics metrics
    ) {
        for (Edge edge : graph.getNeighbors(vertex)) {
            metrics.recordEdgeScan();

            if (!visited[edge.getTo()]) {
                queue.add(edge);
            }
        }
    }
}