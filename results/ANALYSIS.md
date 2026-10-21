# Benchmark analysis

Source: results.csv and tarjan.csv from the student benchmark run. All 50 main rows and 10 Tarjan rows passed reference-value and scan-count checks.

## Fitted slopes

Ordinary least squares: log10(counter) against log10(n), using all five sizes separately for each density.

| Algorithm | m/n | edge_scans slope | comparisons slope |
|---|---:|---:|---:|
| Kruskal | 4 | 1.0550 | 1.0545 |
| Kruskal | 20 | 1.1194 | 1.0159 |
| Prim | 4 | 1.0000 | 1.1079 |
| Prim | 20 | 1.0000 | 1.0903 |
| Kosaraju | 4 | 1.0000 | N/A (zero) |
| Kosaraju | 20 | 1.0000 | N/A (zero) |
| Kahn | 4 | 1.0000 | N/A (zero) |
| Kahn | 20 | 1.0000 | N/A (zero) |
| DagShortestPath | 4 | 1.0000 | N/A (zero) |
| DagShortestPath | 20 | 1.0000 | N/A (zero) |
| Tarjan | 4 | 1.0000 | N/A (zero) |
| Tarjan | 20 | 1.0000 | N/A (zero) |

The assignment attributes a log factor to Kruskal edge scans. Sorting contributes to comparisons, not to the post-sort edge_scans counter. Therefore measured scan slopes must not be presented as evidence of sorting complexity. Bounded random weights (1–1000), repeated keys and finite input sizes can also affect the observed comparison slope. Empirical data supports behavior on these inputs, not a proof of an asymptotic bound.

## Counter/formula ratios

Each row gives ratios for n = 1000, 3162, 10000, 31623, 100000, in that order.

| Algorithm | m/n | Counter / formula | Ratios |
|---|---:|---|---|
| Kruskal | 4 | comparisons / (m log2(m)) | 0.8914, 0.9069, 0.8692, 0.8091, 0.7447 |
| Kruskal | 4 | dsu_ops / m | 2.2902, 2.5189, 2.6818, 2.7317, 2.7483 |
| Kruskal | 20 | comparisons / (m log2(m)) | 0.9027, 0.8464, 0.7839, 0.7200, 0.6662 |
| Kruskal | 20 | dsu_ops / m | 0.5800, 0.6062, 0.5647, 0.7540, 0.8936 |
| Prim | 4 | comparisons / (m log2(m)) | 1.8291, 1.8654, 1.8952, 1.9210, 1.9344 |
| Prim | 20 | comparisons / (m log2(m)) | 1.8248, 1.8468, 1.8657, 1.8779, 1.8896 |
| Kosaraju | 4 | edge_scans / (n + m) | 1.6000, 1.6000, 1.6000, 1.6000, 1.6000 |
| Kosaraju | 20 | edge_scans / (n + m) | 1.9048, 1.9048, 1.9048, 1.9048, 1.9048 |
| Kahn | 4 | edge_scans / (n + m) | 0.8000, 0.8000, 0.8000, 0.8000, 0.8000 |
| Kahn | 20 | edge_scans / (n + m) | 0.9524, 0.9524, 0.9524, 0.9524, 0.9524 |
| DagShortestPath | 4 | edge_scans / (n + m) | 0.8000, 0.8000, 0.8000, 0.8000, 0.8000 |
| DagShortestPath | 20 | edge_scans / (n + m) | 0.9524, 0.9524, 0.9524, 0.9524, 0.9524 |
| Tarjan | 4 | edge_scans / (n + m) | 0.8000, 0.8000, 0.8000, 0.8000, 0.8000 |
| Tarjan | 20 | edge_scans / (n + m) | 0.9524, 0.9524, 0.9524, 0.9524, 0.9524 |

DSU counts calls, not parent-pointer traversals, so dsu_ops/m does not directly measure inverse-Ackermann cost. Early stopping changes the number of calls per input edge. The exact connected-graph identity here is dsu_ops = 2 * edge_scans + 3 * (n - 1). Ratios need not be exactly constant, especially for early-stopping Kruskal.

## Kruskal scan shares

| n | m = 4n | m = 20n |
|---:|---:|---:|
| 1000 | 77.0500% | 21.5100% |
| 3162 | 88.4567% | 22.8147% |
| 10000 | 96.5925% | 20.7360% |
| 31623 | 99.0861% | 30.2008% |
| 100000 | 99.9170% | 37.1786% |

## Tarjan bonus

On all ten configurations, Tarjan scans m edges and Kosaraju scans 2m. Tarjan saves exactly 50% of counted edge scans by finding SCCs in one DFS pass. Kosaraju uses two DFS passes and additionally constructs a reversed graph. Graph reversal is not included in this edge_scans counter. A smaller scan count does not guarantee the same time ratio.

## Timing

Times are medians of five runs in milliseconds. Kahn timing includes order validation. DAG timing includes topological preprocessing and distance summation, while its edge_scans counts only relaxation. SCC partition comparison is outside timed runs. Runs have no separate warm-up phase, so JIT compilation, garbage collection and background load can affect results, especially small inputs.

| Algorithm | n=100000, m=400000 (ms) | n=100000, m=2000000 (ms) |
|---|---:|---:|
| Kruskal | 231.178 | 904.623 |
| Prim | 344.007 | 2551.825 |
| Kosaraju | 151.130 | 749.980 |
| Kahn | 20.126 | 79.361 |
| DagShortestPath | 37.519 | 254.991 |
| Tarjan | 70.805 | 230.981 |

## Reproduce the figures

Place plot_results.py in the project root beside pom.xml. Input files are results/results.csv and results/tarjan.csv. Run:

```text
python -m pip install matplotlib
python plot_results.py
```

Matplotlib is used only for report figures. Java graph algorithms use no additional libraries. The script validates inputs, writes three PNG plots and regenerates this analysis. It does not modify the input CSV files.

This is supporting analysis, not the complete required 3–5 page REPORT. Complexity, correctness arguments, algorithm selection, Bonus B discussion and final submission checks remain to be incorporated.
