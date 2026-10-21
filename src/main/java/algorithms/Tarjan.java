package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Tarjan {

    public static SccResult run(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }

        int vertexCount = graph.getVertexCount();

        int[] index = new int[vertexCount];
        int[] lowlink = new int[vertexCount];
        int[] nextEdge = new int[vertexCount];
        int[] component = new int[vertexCount];
        boolean[] onStack = new boolean[vertexCount];

        Arrays.fill(index, -1);
        Arrays.fill(component, -1);

        ArrayDeque<Integer> dfsStack = new ArrayDeque<>();
        ArrayDeque<Integer> activeStack = new ArrayDeque<>();

        int nextIndex = 0;
        int componentCount = 0;

        for (int start = 0; start < vertexCount; start++) {
            if (index[start] != -1) {
                continue;
            }

            index[start] = nextIndex;
            lowlink[start] = nextIndex;
            nextIndex++;

            dfsStack.push(start);
            activeStack.push(start);
            onStack[start] = true;
            metrics.recordDepth(dfsStack.size());

            while (!dfsStack.isEmpty()) {
                int vertex = dfsStack.peek();
                var neighbors = graph.getNeighbors(vertex);

                if (nextEdge[vertex] < neighbors.size()) {
                    int target = neighbors.get(nextEdge[vertex]).getTo();
                    nextEdge[vertex]++;
                    metrics.recordEdgeScan();

                    if (index[target] == -1) {
                        index[target] = nextIndex;
                        lowlink[target] = nextIndex;
                        nextIndex++;

                        dfsStack.push(target);
                        activeStack.push(target);
                        onStack[target] = true;
                        metrics.recordDepth(dfsStack.size());
                    } else if (onStack[target]) {
                        lowlink[vertex] = Math.min(
                                lowlink[vertex], index[target]
                        );
                    }
                } else {
                    dfsStack.pop();

                    if (lowlink[vertex] == index[vertex]) {
                        int member;

                        do {
                            member = activeStack.pop();
                            onStack[member] = false;
                            component[member] = componentCount;
                        } while (member != vertex);

                        componentCount++;
                    }

                    if (!dfsStack.isEmpty()) {
                        int parent = dfsStack.peek();
                        lowlink[parent] = Math.min(
                                lowlink[parent], lowlink[vertex]
                        );
                    }
                }
            }
        }

        return new SccResult(component, componentCount);
    }
}
