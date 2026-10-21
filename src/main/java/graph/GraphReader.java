package graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class GraphReader {

    public static Graph read(Path path) throws IOException {
        try (Scanner scanner = new Scanner(path)) {
            int vertexCount = scanner.nextInt();
            int edgeCount = scanner.nextInt();
            String graphType = scanner.next();

            if (edgeCount < 0) {
                throw new IllegalArgumentException(
                        "Edge count cannot be negative"
                );
            }

            boolean directed;

            if (graphType.equals("directed")) {
                directed = true;
            } else if (graphType.equals("undirected")) {
                directed = false;
            } else {
                throw new IllegalArgumentException(
                        "Unknown graph type: " + graphType
                );
            }

            Graph graph = new Graph(vertexCount, directed);

            for (int index = 0; index < edgeCount; index++) {
                int from = scanner.nextInt();
                int to = scanner.nextInt();
                int weight = scanner.nextInt();

                graph.addEdge(from, to, weight);
            }

            if (scanner.hasNext()) {
                throw new IllegalArgumentException(
                        "Unexpected data after the last edge"
                );
            }

            return graph;
        }
    }
}