package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<Edge> edges;
    private final List<List<Edge>> adjacency;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex count cannot be negative"
            );
        }

        this.vertexCount = vertexCount;
        this.directed = directed;
        this.edges = new ArrayList<>();
        this.adjacency = new ArrayList<>();

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);

        if (!directed) {
            Edge reverseEdge = new Edge(to, from, weight);
            adjacency.get(to).add(reverseEdge);
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> getNeighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public Graph reversed() {
        Graph reversedGraph = new Graph(vertexCount, directed);

        for (Edge edge : edges) {
            reversedGraph.addEdge(
                    edge.getTo(),
                    edge.getFrom(),
                    edge.getWeight()
            );
        }

        return reversedGraph;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException(
                    "Invalid vertex: " + vertex
            );
        }
    }
}