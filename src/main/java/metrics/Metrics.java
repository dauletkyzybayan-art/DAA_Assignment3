package metrics;

public class Metrics {
    private long dsuOps;
    private long edgeScans;
    private long comparisons;
    private int maxDepth;

    public void setDsuOps(long dsuOps) {
        this.dsuOps = dsuOps;
    }

    public void recordEdgeScan() {
        edgeScans++;
    }

    public int compareWeights(int first, int second) {
        comparisons++;
        return Integer.compare(first, second);
    }

    public void recordDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    public long getDsuOps() {
        return dsuOps;
    }

    public long getEdgeScans() {
        return edgeScans;
    }

    public long getComparisons() {
        return comparisons;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}