package algorithms;

public class DisjointSetUnion {

    private int[] parent;
    private int[] size;
    private int componentCount;
    private long findCalls;
    private long successfulUnions;

    public DisjointSetUnion(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }

        parent = new int[n];
        size = new int[n];
        componentCount = n;
        findCalls = 0;
        successfulUnions = 0;

        for (int vertex = 0; vertex < n; vertex++) {
            parent[vertex] = vertex;
            size[vertex] = 1;
        }
    }

    public int find(int vertex) {
        if (vertex < 0 || vertex >= parent.length) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }

        findCalls++;

        int root = vertex;
        while (root != parent[root]) {
            root = parent[root];
        }

        while (vertex != root) {
            int next = parent[vertex];
            parent[vertex] = root;
            vertex = next;
        }

        return root;
    }

    public boolean union(int first, int second) {
        int firstRoot = find(first);
        int secondRoot = find(second);

        if (firstRoot == secondRoot) {
            return false;
        }

        if (size[firstRoot] < size[secondRoot]) {
            int temporary = firstRoot;
            firstRoot = secondRoot;
            secondRoot = temporary;
        }

        parent[secondRoot] = firstRoot;
        size[firstRoot] += size[secondRoot];
        componentCount--;
        successfulUnions++;

        return true;
    }

    public int components() {
        return componentCount;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getSuccessfulUnions() {
        return successfulUnions;
    }

    public long getDsuOps() {
        return findCalls + successfulUnions;
    }
}