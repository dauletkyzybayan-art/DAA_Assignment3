package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MstResult run(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kruskal requires an undirected graph"
            );
        }

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());

        sortedEdges.sort((first, second) ->
                metrics.compareWeights(
                        first.getWeight(),
                        second.getWeight()
                )
        );

        DisjointSetUnion dsu =
                new DisjointSetUnion(graph.getVertexCount());

        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            if (dsu.components() <= 1) {
                break;
            }

            metrics.recordEdgeScan();

            int from = edge.getFrom();
            int to = edge.getTo();

            if (dsu.find(from) != dsu.find(to)) {
                dsu.union(from, to);
                selectedEdges.add(edge);
                totalWeight += edge.getWeight();
            }
        }

        metrics.setDsuOps(dsu.getDsuOps());

        return new MstResult(
                selectedEdges,
                totalWeight,
                dsu.components()
        );
    }
}