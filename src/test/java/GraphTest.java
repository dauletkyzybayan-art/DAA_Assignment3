import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    @Test
    void createsGraphWithIsolatedVertices() {
        Graph graph = new Graph(3, true);

        assertEquals(3, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        assertTrue(graph.isDirected());

        for (int vertex = 0; vertex < 3; vertex++) {
            assertTrue(graph.getNeighbors(vertex).isEmpty());
        }
    }

    @Test
    void storesDirectedEdgeInOneDirection() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 7);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getNeighbors(0).size());
        assertTrue(graph.getNeighbors(1).isEmpty());

        Edge edge = graph.getNeighbors(0).get(0);

        assertEquals(0, edge.getFrom());
        assertEquals(1, edge.getTo());
        assertEquals(7, edge.getWeight());
    }

    @Test
    void storesUndirectedEdgeInBothDirections() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, 7);

        assertFalse(graph.isDirected());
        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getNeighbors(0).size());
        assertEquals(1, graph.getNeighbors(1).size());

        Edge reverseEdge = graph.getNeighbors(1).get(0);

        assertEquals(1, reverseEdge.getFrom());
        assertEquals(0, reverseEdge.getTo());
        assertEquals(7, reverseEdge.getWeight());
        assertTrue(graph.getNeighbors(2).isEmpty());
    }

    @Test
    void supportsLoopsParallelEdgesAndNegativeWeights() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 0, -2);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 9);

        assertEquals(3, graph.getEdgeCount());
        assertEquals(3, graph.getNeighbors(0).size());
        assertEquals(-2, graph.getNeighbors(0).get(0).getWeight());
        assertEquals(5, graph.getNeighbors(0).get(1).getWeight());
        assertEquals(9, graph.getNeighbors(0).get(2).getWeight());
        assertTrue(graph.getNeighbors(2).isEmpty());
    }

    @Test
    void storesUndirectedLoopTwiceInAdjacency() {
        Graph graph = new Graph(1, false);
        graph.addEdge(0, 0, 4);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(2, graph.getNeighbors(0).size());

        for (Edge edge : graph.getNeighbors(0)) {
            assertEquals(0, edge.getFrom());
            assertEquals(0, edge.getTo());
            assertEquals(4, edge.getWeight());
        }
    }

    @Test
    void reversesDirectedEdgesWithoutChangingOriginal() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 7);
        graph.addEdge(2, 1, -3);

        Graph reversed = graph.reversed();

        assertTrue(reversed.isDirected());
        assertEquals(4, reversed.getVertexCount());
        assertEquals(2, reversed.getEdgeCount());
        assertTrue(reversed.getNeighbors(0).isEmpty());
        assertEquals(2, reversed.getNeighbors(1).size());
        assertTrue(reversed.getNeighbors(3).isEmpty());

        Edge first = reversed.getNeighbors(1).get(0);
        Edge second = reversed.getNeighbors(1).get(1);

        assertEquals(1, first.getFrom());
        assertEquals(0, first.getTo());
        assertEquals(7, first.getWeight());

        assertEquals(1, second.getFrom());
        assertEquals(2, second.getTo());
        assertEquals(-3, second.getWeight());

        assertEquals(1, graph.getNeighbors(0).size());
        assertTrue(graph.getNeighbors(1).isEmpty());
    }

    @Test
    void rejectsInvalidVertexNumbers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Graph(-1, true)
        );

        Graph graph = new Graph(3, true);

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge(-1, 1, 5)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addEdge(0, 3, 5)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.getNeighbors(3)
        );

        assertEquals(0, graph.getEdgeCount());
        assertTrue(graph.getNeighbors(0).isEmpty());
    }

    @Test
    void protectsListsFromExternalChanges() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 5);

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getEdges().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getNeighbors(0).clear()
        );

        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getNeighbors(0).size());
    }
}