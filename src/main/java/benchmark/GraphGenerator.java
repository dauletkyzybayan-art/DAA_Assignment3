package benchmark;

import graph.Graph;

import java.util.Random;

public class GraphGenerator {

    public static Graph generate(int vertexCount, int edgeCount, String kind) {
        if (vertexCount < 2 || edgeCount < 0) {
            throw new IllegalArgumentException("Invalid graph size");
        }

        if (!kind.equals("undirected")
                && !kind.equals("directed")
                && !kind.equals("dag")) {
            throw new IllegalArgumentException("Unknown graph kind");
        }

        boolean undirected = kind.equals("undirected");
        boolean dag = kind.equals("dag");

        if (undirected && edgeCount < vertexCount - 1) {
            throw new IllegalArgumentException(
                    "A connected graph needs at least n - 1 edges"
            );
        }

        Random random = new Random(42);
        Graph graph = new Graph(vertexCount, !undirected);

        int[] order = new int[vertexCount];

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            order[vertex] = vertex;
        }

        if (undirected || dag) {
            for (int index = vertexCount - 1; index > 0; index--) {
                int other = random.nextInt(index + 1);

                int temporary = order[index];
                order[index] = order[other];
                order[other] = temporary;
            }
        }

        if (undirected) {
            for (int index = 1; index < vertexCount; index++) {
                int from = order[random.nextInt(index)];
                int to = order[index];
                int weight = 1 + random.nextInt(1000);

                graph.addEdge(from, to, weight);
            }
        }

        while (graph.getEdgeCount() < edgeCount) {
            int from = random.nextInt(vertexCount);
            int to = random.nextInt(vertexCount);

            while (from == to) {
                to = random.nextInt(vertexCount);
            }

            if (dag && order[from] > order[to]) {
                int temporary = from;
                from = to;
                to = temporary;
            }

            int weight = 1 + random.nextInt(1000);
            graph.addEdge(from, to, weight);
        }

        return graph;
    }
}