import algorithms.Kosaraju;
import algorithms.SccResult;
import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import algorithms.Tarjan;


import static org.junit.jupiter.api.Assertions.*;

public class SccTest {

    @Test
    void findsComponentsInInputFile() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "directed_cycle.txt")
        );

        Metrics metrics = new Metrics();
        SccResult result = Kosaraju.run(graph, metrics);

        assertEquals(2, result.getComponentCount());
        assertEquals(result.getComponent(0), result.getComponent(1));
        assertEquals(result.getComponent(1), result.getComponent(2));
        assertNotEquals(result.getComponent(2), result.getComponent(3));
        assertEquals(8L, metrics.getEdgeScans());
    }

    @Test
    void matchesBfsOnRandomGraphs() {
        Random random = new Random(42);

        for (int trial = 0; trial < 30; trial++) {
            int vertexCount = 1 + random.nextInt(200);
            Graph graph = new Graph(vertexCount, true);
            int edgeCount = random.nextInt(vertexCount * 4 + 1);

            for (int index = 0; index < edgeCount; index++) {
                int from = random.nextInt(vertexCount);
                int to = random.nextInt(vertexCount);
                graph.addEdge(from, to, 1);
            }

            boolean[][] reachable = new boolean[vertexCount][];

            for (int start = 0; start < vertexCount; start++) {
                reachable[start] = bfs(graph, start);
            }

            Metrics metrics = new Metrics();
            SccResult result = Kosaraju.run(graph, metrics);
            Set<Integer> componentIds = new HashSet<>();

            for (int first = 0; first < vertexCount; first++) {
                int component = result.getComponent(first);

                assertTrue(component >= 0);
                assertTrue(component < result.getComponentCount());
                componentIds.add(component);

                for (int second = 0; second < vertexCount; second++) {
                    boolean expected =
                            reachable[first][second]
                                    && reachable[second][first];

                    boolean actual =
                            result.getComponent(first)
                                    == result.getComponent(second);

                    assertEquals(expected, actual);
                }
            }

            assertEquals(
                    componentIds.size(),
                    result.getComponentCount()
            );

            assertEquals(2L * edgeCount, metrics.getEdgeScans());
        }
    }

    @Test
    void handlesPathOf100000Vertices() {
        int vertexCount = 100000;
        Graph graph = new Graph(vertexCount, true);

        for (int vertex = 0; vertex < vertexCount - 1; vertex++) {
            graph.addEdge(vertex, vertex + 1, 1);
        }

        Metrics metrics = new Metrics();
        SccResult result = Kosaraju.run(graph, metrics);

        assertEquals(vertexCount, result.getComponentCount());
        assertEquals(vertexCount, metrics.getMaxDepth());
        assertEquals(
                2L * (vertexCount - 1),
                metrics.getEdgeScans()
        );
    }

    @Test
    void handlesLoopsParallelEdgesAndIsolatedVertex() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 0, 1);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 0, 4);

        Metrics metrics = new Metrics();
        SccResult result = Kosaraju.run(graph, metrics);

        assertEquals(2, result.getComponentCount());
        assertEquals(result.getComponent(0), result.getComponent(1));
        assertNotEquals(result.getComponent(0), result.getComponent(2));
        assertEquals(8L, metrics.getEdgeScans());
    }

    @Test
    void handlesEmptyGraph() {
        Graph graph = new Graph(0, true);
        Metrics metrics = new Metrics();

        SccResult result = Kosaraju.run(graph, metrics);

        assertEquals(0, result.getComponentCount());
        assertEquals(0L, metrics.getEdgeScans());
        assertEquals(0, metrics.getMaxDepth());
    }

    private boolean[] bfs(Graph graph, int start) {
        boolean[] visited = new boolean[graph.getVertexCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        visited[start] = true;
        queue.addLast(start);

        while (!queue.isEmpty()) {
            int vertex = queue.removeFirst();

            for (Edge edge : graph.getNeighbors(vertex)) {
                int neighbor = edge.getTo();

                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.addLast(neighbor);
                }
            }
        }

        return visited;
    }
    @Test
    void tarjanMatchesKosaraju() {
        Random random = new Random(42);
        for (int trial = 0; trial < 30; trial++) {
            int vertexCount = 1 + random.nextInt(100);
            Graph graph = new Graph(vertexCount, true);

            for (int edge = 0; edge < 4 * vertexCount; edge++) {
                graph.addEdge(
                        random.nextInt(vertexCount),
                        random.nextInt(vertexCount),
                        1
                );
            }

            Metrics kosarajuMetrics = new Metrics();
            Metrics tarjanMetrics = new Metrics();

            var expected = Kosaraju.run(graph, kosarajuMetrics);
            var actual = Tarjan.run(graph, tarjanMetrics);

            assertEquals(
                    expected.getComponentCount(),
                    actual.getComponentCount()
            );

            for (int first = 0; first < vertexCount; first++) {
                for (int second = 0; second < vertexCount; second++) {
                    assertEquals(
                            expected.getComponent(first)
                                    == expected.getComponent(second),
                            actual.getComponent(first)
                                    == actual.getComponent(second)
                    );
                }
            }

            assertEquals(graph.getEdgeCount(), tarjanMetrics.getEdgeScans());
            assertEquals(2L * graph.getEdgeCount(), kosarajuMetrics.getEdgeScans());
        }
    }
}