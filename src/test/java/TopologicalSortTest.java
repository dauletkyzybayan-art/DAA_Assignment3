import algorithms.Kahn;
import graph.Graph;
import graph.GraphReader;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortTest {

    @Test
    void returnsValidOrderForDag() throws IOException {
        Graph graph = GraphReader.read(Path.of("data", "dag.txt"));
        Metrics metrics = new Metrics();

        List<Integer> order = Kahn.run(graph, metrics);

        assertEquals(5, order.size());
        assertTrue(Kahn.isValidOrder(graph, order));
        assertEquals(5L, metrics.getEdgeScans());
    }

    @Test
    void detectsCycle() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "directed_cycle.txt")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Kahn.run(graph, new Metrics())
        );
    }

    @Test
    void handlesParallelEdgesAndIsolatedVertices() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 1, 7);

        Metrics metrics = new Metrics();
        List<Integer> order = Kahn.run(graph, metrics);

        assertTrue(Kahn.isValidOrder(graph, order));
        assertEquals(4, order.size());
        assertEquals(2L, metrics.getEdgeScans());
    }

    @Test
    void detectsSelfLoop() {
        Graph graph = new Graph(1, true);
        graph.addEdge(0, 0, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kahn.run(graph, new Metrics())
        );
    }

    @Test
    void rejectsInvalidOrders() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);

        assertTrue(Kahn.isValidOrder(graph, List.of(0, 1, 2)));
        assertFalse(Kahn.isValidOrder(graph, List.of(1, 0, 2)));
        assertFalse(Kahn.isValidOrder(graph, List.of(0, 1, 1)));
        assertFalse(Kahn.isValidOrder(graph, List.of(0, 1)));
        assertFalse(Kahn.isValidOrder(graph, List.of(0, 1, 3)));
    }

    @Test
    void handlesEmptyGraph() {
        Graph graph = new Graph(0, true);
        Metrics metrics = new Metrics();

        List<Integer> order = Kahn.run(graph, metrics);

        assertTrue(order.isEmpty());
        assertTrue(Kahn.isValidOrder(graph, order));
        assertEquals(0L, metrics.getEdgeScans());
    }
}
