# DAA Assignment 3: Graph Algorithms

**Student:** Dauletkyzy Bayan  
**Group:** SE-2523

The project implements graph algorithms for a city network and a job scheduler. It uses Java 17, Maven and JUnit 5. Java collections are used for storage and queues; graph algorithms are implemented in the project.

## Requirements

- JDK 17 with `java` available in the terminal.
- Maven with `mvn` available in the terminal.
- JUnit Jupiter 5.10.2 and Maven Surefire 3.2.5 are configured in `pom.xml`.
- Python 3 and Matplotlib are needed only to rebuild report figures.

Run all commands from the project root, where `pom.xml` is located. Maven may download dependencies on the first run.

## Build and test

```text
mvn clean test
```

The tests cover DSU, graph input, MST agreement, SCCs, topological order, DAG shortest paths and the bonus algorithms. The Kosaraju stack-safety test uses a path of 100,000 vertices. The Bellman-Ford implementation is a test reference, not the main DAG algorithm.

In IntelliJ IDEA, open `pom.xml` as a Maven project and select JDK 17. To run tests through the IDE, right-click `src/test/java` and run all tests.

## Run the benchmark

After the build and test command:

```text
java -cp target/classes benchmark.BenchmarkRunner
```

In IntelliJ, open `benchmark.BenchmarkRunner` and run its `main` method. Use the project root as the working directory.

The benchmark generates five sizes (1000, 3162, 10000, 31623, 100000), each with m = 4n and m = 20n. Each graph kind starts with `new Random(42)`. When sampled endpoints coincide, the second endpoint is resampled. Each algorithm is run five times and its median elapsed time is written to CSV.

Outputs are overwritten on each run:

- `results/results.csv`: 50 rows for Kruskal, Prim, Kosaraju, Kahn and DagShortestPath.
- `results/tarjan.csv`: 10 rows for the bonus SCC algorithm.

The run fails if reference values, expected scan counts, MST agreement, topological validity or SCC partition agreement are incorrect. A complete successful run ends with `All reference checks passed.` A CSV from an interrupted or failed run may be incomplete.

CSV columns:

```text
algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result
```

The result is MST weight, SCC count, topological-order length or the sum of DAG distances, depending on the algorithm. Counters come from one run, not a sum of all five runs. The input and algorithm order are fixed, making the counters reproducible; timings vary.

## Rebuild the plots

```text
python -m pip install matplotlib
python plot_results.py
```

The script reads both CSV files, checks their contents and regenerates:

- `results/plots/operations_vs_n.png`
- `results/plots/scans_per_edge.png`
- `results/plots/kruskal_edge_share.png`
- `results/ANALYSIS.md`

Matplotlib is only used for plotting, not for the Java algorithms. Already-generated PNG files can be viewed without Python. The script leaves the input CSV files unchanged. If benchmark data changes, rebuild the figures and update the measured values in `REPORT.pdf`.

## Input graphs

The first line is `n m directed` or `n m undirected`. Each following line contains `from to weight`, with vertices numbered from 0 to n - 1.

Files used by the tests:

- `data/undirected_disconnected.txt`: a disconnected undirected graph.
- `data/directed_cycle.txt`: a directed graph containing a cycle.
- `data/dag.txt`: a DAG with a negative edge weight.

Self-loops, parallel edges and isolated vertices are supported. MST algorithms reject directed graphs and return a forest for disconnected undirected graphs. Kahn and DAG shortest paths reject cyclic directed graphs.

## Implementation

| Location | Responsibility |
| --- | --- |
| `src/main/java/graph` | Edge, adjacency-list Graph and GraphReader |
| `src/main/java/algorithms` | DSU, MST, SCC, topological ordering, DAG paths and bonuses |
| `src/main/java/metrics` | Operation counters and maximum explicit DFS depth |
| `src/main/java/benchmark` | Fixed-seed generation, reference checks and CSV output |
| `src/test/java` | JUnit tests and small reference implementations |
| `results` | Benchmark data, analysis and three PNG figures |
| `REPORT.pdf` | Complexity, correctness, measured results and algorithm selection |

## Counter definitions

- `dsu_ops`: find calls plus successful unions. A union also calls find on both endpoints.
- `edge_scans`: counted edge visits inside the algorithm. Kruskal counts its post-sort loop; Prim counts adjacency entries; Kosaraju counts both DFS passes; Tarjan counts its single DFS pass.
- Kahn counts outgoing edges removed during queue processing. Its initial indegree calculation is not included in this counter.
- DagShortestPath counts the relaxation pass only. Topological preprocessing is included in elapsed time but uses a separate Metrics object.
- `comparisons`: calls comparing edge weights in sorting or the priority queue. Distance relaxations are not included in this weight-comparison counter.
- `max_depth`: maximum explicit DFS call-stack size. It is zero for algorithms without DFS.

Kahn elapsed time includes order validation. DAG elapsed time includes summing the returned distances. SCC partition comparison is outside the timed runs. The operations plot adds edge scans, weight comparisons and DSU operations; it does not add maximum depth.

## Bonus A: Tarjan SCC

Tarjan uses `index`, `lowlink`, a DFS stack and a stack of active vertices. It returns `SccResult`, like Kosaraju. The benchmark checks that both algorithms place the same vertices together even when their component numbers differ. Tarjan scans m edges; Kosaraju scans 2m edges in its DFS passes.

## Bonus B: condensation and critical path

`CriticalPath.condense(graph, components)` replaces each SCC with one vertex. Pass an SCC result computed from the same graph. Internal edges are removed, and inter-component weights and parallel edges are preserved.

`CriticalPath.longestPath(condensed)` returns the maximum path weight from any indegree-zero source to any endpoint, allowing a zero-length path. It uses negated weights and topological relaxation with long arithmetic. Internal SCC weights are not included. The returned number is a longest-path length in the condensation; this is not a solver for longest simple paths inside the original cyclic graph.

## AI-use acknowledgement

I used ChatGPT/Codex to explain the assignment and algorithms, draft and debug code and tests, diagnose the random-generator mismatch, and help prepare plots, analysis and documentation. I ran the benchmark on my computer; the reported measurements come from its CSV output. I am responsible for checking the work and understanding the implementation before the defense.
