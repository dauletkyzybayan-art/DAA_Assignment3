import algorithms.Kruskal;
import algorithms.MstResult;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class KruskalTest {

    @Test
    void findsMinimumSpanningTree() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 3, 4);
        graph.addEdge(0, 3, 10);

        Metrics metrics = new Metrics();
        MstResult result = Kruskal.run(graph, metrics);

        assertEquals(7L, result.getTotalWeight());
        assertEquals(3, result.getEdges().size());
        assertEquals(1, result.getComponents());

        assertEquals(4L, metrics.getEdgeScans());
        assertEquals(17L, metrics.getDsuOps());
        assertTrue(metrics.getComparisons() > 0);
        assertEquals(0, metrics.getMaxDepth());
    }

    @Test
    void returnsForestForDisconnectedGraph() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "undirected_disconnected.txt")
        );

        MstResult result = Kruskal.run(graph, new Metrics());

        assertEquals(11L, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
        assertEquals(2, result.getComponents());
    }

    @Test
    void handlesLoopsParallelEdgesAndNegativeWeights() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, 8);
        graph.addEdge(0, 1, -2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(0, 2, 10);

        MstResult result = Kruskal.run(graph, new Metrics());

        assertEquals(1L, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
        assertEquals(1, result.getComponents());

        for (graph.Edge edge : result.getEdges()) {
            assertNotEquals(edge.getFrom(), edge.getTo());
        }
    }

    @Test
    void handlesEmptyGraph() {
        Graph graph = new Graph(0, false);
        Metrics metrics = new Metrics();

        MstResult result = Kruskal.run(graph, metrics);

        assertEquals(0L, result.getTotalWeight());
        assertTrue(result.getEdges().isEmpty());
        assertEquals(0, result.getComponents());
        assertEquals(0L, metrics.getEdgeScans());
        assertEquals(0L, metrics.getDsuOps());
    }

    @Test
    void rejectsDirectedGraph() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kruskal.run(graph, new Metrics())
        );
    }
}