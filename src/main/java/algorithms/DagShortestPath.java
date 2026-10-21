package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class DagShortestPath {
    private final long[] distance;
    private final int[] parent;

    private DagShortestPath(long[] distance, int[] parent) {
        this.distance = distance;
        this.parent = parent;
    }

    public static DagShortestPath run(Graph graph, Metrics metrics) {
        List<Integer> order = Kahn.run(graph, new Metrics());
        int vertexCount = graph.getVertexCount();

        int[] inDegree = new int[vertexCount];

        for (Edge edge : graph.getEdges()) {
            inDegree[edge.getTo()]++;
        }

        long[] distance = new long[vertexCount];
        int[] parent = new int[vertexCount];

        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (inDegree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        for (int vertex : order) {
            for (Edge edge : graph.getNeighbors(vertex)) {
                metrics.recordEdgeScan();

                int neighbor = edge.getTo();
                long candidate = distance[vertex] + edge.getWeight();

                if (candidate < distance[neighbor]) {
                    distance[neighbor] = candidate;
                    parent[neighbor] = vertex;
                }
            }
        }

        return new DagShortestPath(distance, parent);
    }

    public long getDistance(int vertex) {
        return distance[vertex];
    }

    public long getDistanceSum() {
        long sum = 0;

        for (long value : distance) {
            sum += value;
        }

        return sum;
    }

    public List<Integer> getPath(int vertex) {
        List<Integer> path = new ArrayList<>();

        while (vertex != -1) {
            path.add(vertex);
            vertex = parent[vertex];
        }

        Collections.reverse(path);
        return path;
    }

}