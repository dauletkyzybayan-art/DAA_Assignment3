package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BenchmarkRunner {

    private static final int[] SIZES = {
            1000, 3162, 10000, 31623, 100000
    };

    private static final long[] MST_WEIGHTS = {
            147970, 30363,
            468911, 97589,
            1486139, 309550,
            4715515, 968517,
            15012094, 3056681
    };

    private static final long[] SCC_COUNTS = {
            27, 1,
            137, 1,
            373, 1,
            1188, 1,
            3923, 1
    };

    private static final long[] DAG_SUMS = {
            396274, 252411,
            1271911, 723157,
            3941828, 2381342,
            12551891, 7376612,
            40096539, 23512087
    };

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("results"));

        try (
                PrintWriter results = new PrintWriter(
                        Files.newBufferedWriter(
                                Path.of("results", "results.csv")
                        )
                );
                PrintWriter bonus = new PrintWriter(
                        Files.newBufferedWriter(
                                Path.of("results", "tarjan.csv")
                        )
                )
        ) {
            String header = "algorithm,graph,n,m,time_ms,"
                    + "dsu_ops,edge_scans,comparisons,max_depth,result";

            results.println(header);
            bonus.println(header);

            int configuration = 0;

            for (int vertexCount : SIZES) {
                for (int density : new int[]{4, 20}) {
                    int edgeCount = density * vertexCount;

                    System.out.println(
                            "\nGraph: n=" + vertexCount + ", m=" + edgeCount
                    );

                    Graph undirected = GraphGenerator.generate(
                            vertexCount, edgeCount, "undirected"
                    );

                    long kruskalWeight = measure(
                            "Kruskal", "undirected", undirected,
                            MST_WEIGHTS[configuration], results
                    );

                    long primWeight = measure(
                            "Prim", "undirected", undirected,
                            MST_WEIGHTS[configuration], results
                    );

                    require(
                            kruskalWeight == primWeight,
                            "Kruskal and Prim disagree"
                    );

                    Graph directed = GraphGenerator.generate(
                            vertexCount, edgeCount, "directed"
                    );

                    measure(
                            "Kosaraju", "directed", directed,
                            SCC_COUNTS[configuration], results
                    );

                    measure(
                            "Tarjan", "directed", directed,
                            SCC_COUNTS[configuration], bonus
                    );

                    checkComponents(directed);

                    Graph dag = GraphGenerator.generate(
                            vertexCount, edgeCount, "dag"
                    );

                    measure(
                            "Kahn", "dag", dag,
                            vertexCount, results
                    );

                    measure(
                            "DagShortestPath", "dag", dag,
                            DAG_SUMS[configuration], results
                    );

                    results.flush();
                    bonus.flush();
                    configuration++;
                }
            }
        }

        System.out.println("\nAll reference checks passed.");
        System.out.println("Saved: results/results.csv");
        System.out.println("Saved: results/tarjan.csv");
    }

    private static long measure(
            String algorithm,
            String kind,
            Graph graph,
            long expected,
            PrintWriter writer
    ) {
        long[] times = new long[5];
        Metrics metrics = null;
        long result = 0;

        for (int run = 0; run < times.length; run++) {
            metrics = new Metrics();

            long start = System.nanoTime();
            result = runAlgorithm(algorithm, graph, metrics);
            times[run] = System.nanoTime() - start;

            require(
                    result == expected,
                    algorithm + ": expected " + expected + ", got " + result
            );

            long expectedScans = -1;

            if (algorithm.equals("Kosaraju")
                    || algorithm.equals("Prim")) {
                expectedScans = 2L * graph.getEdgeCount();
            } else if (algorithm.equals("Kahn")
                    || algorithm.equals("DagShortestPath")
                    || algorithm.equals("Tarjan")) {
                expectedScans = graph.getEdgeCount();
            }

            if (expectedScans >= 0) {
                require(
                        metrics.getEdgeScans() == expectedScans,
                        algorithm + ": incorrect edge scan count"
                );
            }
        }

        Arrays.sort(times);
        double medianMilliseconds = times[2] / 1_000_000.0;

        writer.printf(
                Locale.US,
                "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
                algorithm,
                kind,
                graph.getVertexCount(),
                graph.getEdgeCount(),
                medianMilliseconds,
                metrics.getDsuOps(),
                metrics.getEdgeScans(),
                metrics.getComparisons(),
                metrics.getMaxDepth(),
                result
        );

        System.out.printf(
                Locale.US,
                "%s: result=%d, scans=%d, median=%.3f ms%n",
                algorithm,
                result,
                metrics.getEdgeScans(),
                medianMilliseconds
        );

        return result;
    }

    private static long runAlgorithm(
            String algorithm,
            Graph graph,
            Metrics metrics
    ) {
        switch (algorithm) {
            case "Kruskal":
                return Kruskal.run(graph, metrics).getTotalWeight();

            case "Prim":
                return Prim.run(graph, metrics).getTotalWeight();

            case "Kosaraju":
                return Kosaraju.run(graph, metrics).getComponentCount();

            case "Tarjan":
                return Tarjan.run(graph, metrics).getComponentCount();

            case "Kahn":
                List<Integer> order = Kahn.run(graph, metrics);

                require(
                        Kahn.isValidOrder(graph, order),
                        "Invalid topological order"
                );

                return order.size();

            case "DagShortestPath":
                return DagShortestPath.run(graph, metrics).getDistanceSum();

            default:
                throw new IllegalArgumentException("Unknown algorithm");
        }
    }

    private static void checkComponents(Graph graph) {
        SccResult kosaraju = Kosaraju.run(graph, new Metrics());
        SccResult tarjan = Tarjan.run(graph, new Metrics());

        require(
                kosaraju.getComponentCount() == tarjan.getComponentCount(),
                "SCC counts disagree"
        );

        int[] mapping = new int[kosaraju.getComponentCount()];
        int[] reverseMapping = new int[tarjan.getComponentCount()];

        Arrays.fill(mapping, -1);
        Arrays.fill(reverseMapping, -1);

        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            int first = kosaraju.getComponent(vertex);
            int second = tarjan.getComponent(vertex);

            if (mapping[first] == -1) {
                mapping[first] = second;
            }

            if (reverseMapping[second] == -1) {
                reverseMapping[second] = first;
            }

            require(
                    mapping[first] == second
                            && reverseMapping[second] == first,
                    "Kosaraju and Tarjan return different components"
            );
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}