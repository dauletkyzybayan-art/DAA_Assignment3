package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kosaraju {

    public static SccResult run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kosaraju requires a directed graph"
            );
        }

        int vertexCount = graph.getVertexCount();
        boolean[] visited = new boolean[vertexCount];
        int[] nextEdge = new int[vertexCount];
        List<Integer> finishOrder = new ArrayList<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            stack.push(start);
            metrics.recordDepth(stack.size());

            while (!stack.isEmpty()) {
                int vertex = stack.peek();
                List<Edge> neighbors = graph.getNeighbors(vertex);

                if (nextEdge[vertex] < neighbors.size()) {
                    Edge edge = neighbors.get(nextEdge[vertex]);
                    nextEdge[vertex]++;
                    metrics.recordEdgeScan();

                    int neighbor = edge.getTo();

                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        stack.push(neighbor);
                        metrics.recordDepth(stack.size());
                    }
                } else {
                    stack.pop();
                    finishOrder.add(vertex);
                }
            }
        }

        Graph reversed = graph.reversed();
        int[] component = new int[vertexCount];
        Arrays.fill(component, -1);
        Arrays.fill(nextEdge, 0);
        int componentCount = 0;

        for (int index = finishOrder.size() - 1; index >= 0; index--) {
            int start = finishOrder.get(index);

            if (component[start] != -1) {
                continue;
            }

            component[start] = componentCount;
            stack.push(start);
            metrics.recordDepth(stack.size());

            while (!stack.isEmpty()) {
                int vertex = stack.peek();
                List<Edge> neighbors = reversed.getNeighbors(vertex);

                if (nextEdge[vertex] < neighbors.size()) {
                    Edge edge = neighbors.get(nextEdge[vertex]);
                    nextEdge[vertex]++;
                    metrics.recordEdgeScan();

                    int neighbor = edge.getTo();

                    if (component[neighbor] == -1) {
                        component[neighbor] = componentCount;
                        stack.push(neighbor);
                        metrics.recordDepth(stack.size());
                    }
                } else {
                    stack.pop();
                }
            }

            componentCount++;
        }

        return new SccResult(component, componentCount);
    }
}