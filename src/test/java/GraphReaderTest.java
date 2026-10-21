import graph.Edge;
import graph.Graph;
import graph.GraphReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GraphReaderTest {

    @Test
    void readsUndirectedDisconnectedGraph() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "undirected_disconnected.txt")
        );

        assertEquals(4, graph.getVertexCount());
        assertEquals(3, graph.getEdgeCount());
        assertFalse(graph.isDirected());
        assertEquals(2, graph.getNeighbors(0).size());
        assertTrue(graph.getNeighbors(3).isEmpty());

        Edge edge = graph.getEdges().get(0);

        assertEquals(0, edge.getFrom());
        assertEquals(1, edge.getTo());
        assertEquals(7, edge.getWeight());
    }

    @Test
    void readsDirectedCycleGraph() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "directed_cycle.txt")
        );

        assertEquals(4, graph.getVertexCount());
        assertEquals(4, graph.getEdgeCount());
        assertTrue(graph.isDirected());

        assertEquals(1, graph.getNeighbors(0).get(0).getTo());
        assertEquals(2, graph.getNeighbors(1).get(0).getTo());
        assertEquals(0, graph.getNeighbors(2).get(0).getTo());
        assertTrue(graph.getNeighbors(3).isEmpty());
    }

    @Test
    void readsDagWithNegativeWeight() throws IOException {
        Graph graph = GraphReader.read(
                Path.of("data", "dag.txt")
        );

        assertEquals(5, graph.getVertexCount());
        assertEquals(5, graph.getEdgeCount());
        assertTrue(graph.isDirected());

        Edge edge = graph.getNeighbors(2).get(0);

        assertEquals(2, edge.getFrom());
        assertEquals(3, edge.getTo());
        assertEquals(-3, edge.getWeight());
        assertTrue(graph.getNeighbors(4).isEmpty());
    }
}