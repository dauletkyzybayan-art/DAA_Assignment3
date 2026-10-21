import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MstTest {

    @Test
    void algorithmsAgreeOn100RandomConnectedGraphs() {
        Random random = new Random(42);

        for (int trial = 0; trial < 100; trial++) {
            int vertexCount = 2 + random.nextInt(49);
            Graph graph = new Graph(vertexCount, false);

            for (int vertex = 1; vertex < vertexCount; vertex++) {
                int parent = random.nextInt(vertex);
                int weight = random.nextInt(101) - 50;
                graph.addEdge(parent, vertex, weight);
            }

            for (int index = 0; index < vertexCount * 3; index++) {
                int from = random.nextInt(vertexCount);
                int to = random.nextInt(vertexCount);
                int weight = random.nextInt(101) - 50;
                graph.addEdge(from, to, weight);
            }

            MstResult kruskal = Kruskal.run(graph, new Metrics());
            MstResult prim = Prim.run(graph, new Metrics());

            assertEquals(
                    kruskal.getTotalWeight(),
                    prim.getTotalWeight(),
                    "Different weights in trial " + trial
            );

            assertEquals(vertexCount - 1, kruskal.getEdges().size());
            assertEquals(vertexCount - 1, prim.getEdges().size());
            assertEquals(1, kruskal.getComponents());
            assertEquals(1, prim.getComponents());
        }
    }

    @Test
    void primReturnsForestFromInputFile() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "undirected_disconnected.txt")
        );

        Metrics metrics = new Metrics();
        MstResult result = Prim.run(graph, metrics);

        assertEquals(11L, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
        assertEquals(2, result.getComponents());
        assertEquals(6L, metrics.getEdgeScans());
        assertEquals(0L, metrics.getDsuOps());
        assertEquals(0, metrics.getMaxDepth());
        assertTrue(metrics.getComparisons() > 0);
    }

    @Test
    void primHandlesLoopsAndParallelEdges() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, 8);
        graph.addEdge(0, 1, -2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 2, 10);

        MstResult result = Prim.run(graph, new Metrics());

        assertEquals(1L, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
        assertEquals(1, result.getComponents());
    }

    @Test
    void primHandlesIsolatedVertices() {
        Graph graph = new Graph(3, false);
        MstResult result = Prim.run(graph, new Metrics());

        assertEquals(0L, result.getTotalWeight());
        assertTrue(result.getEdges().isEmpty());
        assertEquals(3, result.getComponents());
    }

    @Test
    void primHandlesEmptyGraph() {
        Graph graph = new Graph(0, false);
        MstResult result = Prim.run(graph, new Metrics());

        assertEquals(0L, result.getTotalWeight());
        assertTrue(result.getEdges().isEmpty());
        assertEquals(0, result.getComponents());
    }

    @Test
    void primRejectsDirectedGraph() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> Prim.run(graph, new Metrics())
        );
    }
}