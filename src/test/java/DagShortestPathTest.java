import algorithms.DagShortestPath;
import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import algorithms.CriticalPath;
import algorithms.Kosaraju;

import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathTest {

    @Test
    void findsDistancesAndRestoresPath() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "dag.txt"));
        Metrics metrics = new Metrics();

        DagShortestPath result = DagShortestPath.run(graph, metrics);

        assertEquals(0L, result.getDistance(0));
        assertEquals(0L, result.getDistance(1));
        assertEquals(2L, result.getDistance(2));
        assertEquals(-1L, result.getDistance(3));
        assertEquals(0L, result.getDistance(4));

        assertEquals(List.of(1, 2, 3, 4), result.getPath(4));
        assertEquals(List.of(0), result.getPath(0));
        assertEquals(1L, result.getDistanceSum());
        assertEquals(5L, metrics.getEdgeScans());
    }

    @Test
    void matchesBellmanFordOnRandomDags() {
        Random random = new Random(42);

        for (int trial = 0; trial < 100; trial++) {
            int vertexCount = 2 + random.nextInt(29);
            Graph graph = new Graph(vertexCount, true);

            for (int index = 0; index < vertexCount * 4; index++) {
                int first = random.nextInt(vertexCount);
                int second = random.nextInt(vertexCount);

                if (first == second) {
                    continue;
                }

                int from = Math.min(first, second);
                int to = Math.max(first, second);
                int weight = random.nextInt(41) - 20;

                graph.addEdge(from, to, weight);
            }

            long[] expected = bellmanFord(graph);
            Metrics metrics = new Metrics();
            DagShortestPath result = DagShortestPath.run(graph, metrics);

            for (int vertex = 0; vertex < vertexCount; vertex++) {
                assertEquals(
                        expected[vertex],
                        result.getDistance(vertex),
                        "Trial " + trial + ", vertex " + vertex
                );
            }

            assertEquals(
                    (long) graph.getEdgeCount(),
                    metrics.getEdgeScans()
            );
        }
    }

    @Test
    void handlesIsolatedVertices() {
        Graph graph = new Graph(3, true);
        DagShortestPath result =
                DagShortestPath.run(graph, new Metrics());

        for (int vertex = 0; vertex < 3; vertex++) {
            assertEquals(0L, result.getDistance(vertex));
            assertEquals(List.of(vertex), result.getPath(vertex));
        }
    }

    @Test
    void handlesEmptyGraph() {
        Graph graph = new Graph(0, true);
        Metrics metrics = new Metrics();

        DagShortestPath result = DagShortestPath.run(graph, metrics);

        assertEquals(0L, result.getDistanceSum());
        assertEquals(0L, metrics.getEdgeScans());
    }

    @Test
    void rejectsCycle() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 0, -1);

        assertThrows(
                IllegalArgumentException.class,
                () -> DagShortestPath.run(graph, new Metrics())
        );
    }

    private long[] bellmanFord(Graph graph) {
        int vertexCount = graph.getVertexCount();
        int[] inDegree = new int[vertexCount];

        for (Edge edge : graph.getEdges()) {
            inDegree[edge.getTo()]++;
        }

        long[] distance = new long[vertexCount];
        Arrays.fill(distance, Long.MAX_VALUE);

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (inDegree[vertex] == 0) {
                distance[vertex] = 0;
            }
        }

        for (int pass = 0; pass < vertexCount - 1; pass++) {
            boolean changed = false;

            for (Edge edge : graph.getEdges()) {
                int from = edge.getFrom();
                int to = edge.getTo();

                if (distance[from] == Long.MAX_VALUE) {
                    continue;
                }

                long candidate = distance[from] + edge.getWeight();

                if (candidate < distance[to]) {
                    distance[to] = candidate;
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        return distance;
    }
    @Test
    void findsCriticalPathAfterCondensation() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 0, 3);
        graph.addEdge(1, 2, 4);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(3, 4, 6);
        graph.addEdge(0, 4, 3);

        var components = Kosaraju.run(graph, new Metrics());
        Graph condensed = CriticalPath.condense(graph, components);

        assertEquals(3, condensed.getVertexCount());
        assertEquals(3, condensed.getEdgeCount());
        assertEquals(10L, CriticalPath.longestPath(condensed));
    }

    @Test
    void criticalPathHandlesEmptyGraphAndExtremeWeight() {
        assertEquals(
                0L,
                CriticalPath.longestPath(new Graph(0, true))
        );

        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, Integer.MIN_VALUE);
        graph.addEdge(0, 2, 7);

        assertEquals(7L, CriticalPath.longestPath(graph));
    }
}