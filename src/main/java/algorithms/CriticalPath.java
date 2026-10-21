package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

public class CriticalPath {

    public static Graph condense(Graph graph, SccResult components) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }

        Graph condensed = new Graph(
                components.getComponentCount(), true
        );

        for (Edge edge : graph.getEdges()) {
            int from = components.getComponent(edge.getFrom());
            int to = components.getComponent(edge.getTo());

            if (from != to) {
                condensed.addEdge(from, to, edge.getWeight());
            }
        }

        return condensed;
    }

    public static long longestPath(Graph graph) {
        List<Integer> order = Kahn.run(graph, new Metrics());
        int vertexCount = graph.getVertexCount();

        int[] indegree = new int[vertexCount];

        for (Edge edge : graph.getEdges()) {
            indegree[edge.getTo()]++;
        }

        long[] distance = new long[vertexCount];
        Arrays.fill(distance, Long.MAX_VALUE);

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (indegree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        long longest = 0;

        for (int vertex : order) {
            for (Edge edge : graph.getNeighbors(vertex)) {
                int target = edge.getTo();
                long candidate = distance[vertex]
                        - (long) edge.getWeight();

                if (candidate < distance[target]) {
                    distance[target] = candidate;
                }
            }

            longest = Math.max(longest, -distance[vertex]);
        }

        return longest;
    }
}
