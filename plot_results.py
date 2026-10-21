import csv
import math
from pathlib import Path

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
RESULTS = ROOT / 'results'
PLOTS = RESULTS / 'plots'
PLOTS.mkdir(parents=True, exist_ok=True)


def read_rows(name):
    with (RESULTS / name).open(newline='', encoding='utf-8-sig') as source:
        rows = list(csv.DictReader(source))
    for row in rows:
        for key in row:
            if key not in ('algorithm', 'graph'):
                row[key] = float(row[key]) if key == 'time_ms' else int(row[key])
    return rows


rows = read_rows('results.csv')
bonus = read_rows('tarjan.csv')
algorithms = ['Kruskal', 'Prim', 'Kosaraju', 'Kahn', 'DagShortestPath']
sizes = [1000, 3162, 10000, 31623, 100000]
assert len(rows) == 50 and len(bonus) == 10
expected_mst = [147970, 30363, 468911, 97589, 1486139, 309550, 4715515, 968517, 15012094, 3056681]
expected_scc = [27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1]
expected_dag = [396274, 252411, 1271911, 723157, 3941828, 2381342, 12551891, 7376612, 40096539, 23512087]
for algorithm in algorithms + ['Tarjan']:
    selected = [r for r in rows + bonus if r['algorithm'] == algorithm]
    assert {(r['n'], r['m']) for r in selected} == {(n, d*n) for n in sizes for d in (4, 20)}
    selected.sort(key=lambda r: (r['n'], r['m']))
    expected = expected_mst if algorithm in ('Kruskal', 'Prim') else expected_scc if algorithm in ('Kosaraju', 'Tarjan') else expected_dag if algorithm == 'DagShortestPath' else [n for n in sizes for d in (4,20)]
    assert [r['result'] for r in selected] == expected
    for r in selected:
        if algorithm != 'Kruskal':
            assert r['edge_scans'] == r['m'] * (2 if algorithm in ('Prim', 'Kosaraju') else 1)
        else:
            assert r['dsu_ops'] == 2*r['edge_scans'] + 3*(r['n']-1)


def series(algorithm, density):
    return sorted([r for r in rows + bonus if r['algorithm'] == algorithm and r['m'] == density*r['n']], key=lambda r: r['n'])


def slope(data, key):
    x = [math.log10(r['n']) for r in data]
    y = [math.log10(r[key]) for r in data]
    xm, ym = sum(x)/len(x), sum(y)/len(y)
    return sum((a-xm)*(b-ym) for a,b in zip(x,y)) / sum((a-xm)**2 for a in x)


plt.rcParams.update({'font.size': 11, 'axes.spines.top': False, 'axes.spines.right': False, 'figure.facecolor': 'white'})
colors = ['#2864A5', '#D17B16', '#238565', '#A84C91', '#545B66']
markers = ['o', 's', '^', 'D', 'x']
fig, axes = plt.subplots(1, 2, figsize=(13, 5), sharey=True)
for ax, density in zip(axes, (4,20)):
    for algorithm, color, marker in zip(algorithms, colors, markers):
        data = series(algorithm, density)
        work = [r['edge_scans']+r['comparisons']+r['dsu_ops'] for r in data]
        ax.loglog([r['n'] for r in data], work, marker=marker, color=color, label=algorithm, linewidth=1.8, markersize=6)
    ax.set_title(f"{'Sparse' if density == 4 else 'Dense'}: m = {density}n")
    ax.set_xlabel('Vertices n')
    ax.grid(True, which='both', alpha=.18)
axes[0].set_ylabel('Counted operations')
axes[1].legend(fontsize=9)
fig.suptitle('Operations vs graph size', fontsize=16)
fig.text(.5, .015, 'Operations = edge_scans + comparisons + dsu_ops; max_depth is not added. Kahn and DAG paths overlap.', ha='center', fontsize=10)
fig.tight_layout(rect=(0,.05,1,.95))
fig.savefig(PLOTS/'operations_vs_n.png', dpi=180)
plt.close(fig)

fig, axes = plt.subplots(1,2,figsize=(13,5),sharey=True)
for ax, density in zip(axes,(4,20)):
    for algorithm,color,marker in zip(algorithms,colors,markers):
        data=series(algorithm,density)
        ax.semilogx([r['n'] for r in data],[r['edge_scans']/r['m'] for r in data],color=color,marker=marker,label=algorithm,linewidth=1.8,markersize=6)
    ax.set_title(f"{'Sparse' if density == 4 else 'Dense'}: m = {density}n")
    ax.set_xlabel('Vertices n')
    ax.set_ylim(0,2.3)
    ax.grid(True,alpha=.2)
axes[0].set_ylabel('edge_scans / m')
axes[1].legend(fontsize=9,loc='center right')
fig.suptitle('Scans per input edge',fontsize=16)
fig.text(.5,.015,'Prim and Kosaraju overlap at 2; Kahn and DAG paths overlap at 1. Prim scans both adjacency entries.',ha='center',fontsize=10)
fig.tight_layout(rect=(0,.05,1,.95))
fig.savefig(PLOTS/'scans_per_edge.png',dpi=180)
plt.close(fig)

fig,ax=plt.subplots(figsize=(9,5))
for density,color,marker in [(4,colors[0],'o'),(20,colors[1],'s')]:
    data=series('Kruskal',density)
    ax.semilogx([r['n'] for r in data],[r['edge_scans']/r['m'] for r in data],label=f'm = {density}n',color=color,marker=marker,linewidth=2)
ax.set(title='Share of edges examined by Kruskal',xlabel='Vertices n',ylabel='edge_scans / m',ylim=(0,1.08))
ax.grid(True,alpha=.2)
ax.legend()
fig.text(.5,.015,'All m edges are sorted. The scan stops when the spanning tree is complete.',ha='center',fontsize=10)
fig.tight_layout(rect=(0,.05,1,1))
fig.savefig(PLOTS/'kruskal_edge_share.png',dpi=180)
plt.close(fig)

lines=['# Benchmark analysis', '', 'Source: results.csv and tarjan.csv from the student benchmark run. All 50 main rows and 10 Tarjan rows passed reference-value and scan-count checks.', '', '## Fitted slopes', '', 'Ordinary least squares: log10(counter) against log10(n), using all five sizes separately for each density.', '', '| Algorithm | m/n | edge_scans slope | comparisons slope |', '|---|---:|---:|---:|']
for algorithm in algorithms+['Tarjan']:
    for density in (4,20):
        data=series(algorithm,density)
        comparison=f"{slope(data,'comparisons'):.4f}" if data[0]['comparisons'] else 'N/A (zero)'
        lines.append(f"| {algorithm} | {density} | {slope(data,'edge_scans'):.4f} | {comparison} |")
lines += ['', 'The assignment attributes a log factor to Kruskal edge scans. Sorting contributes to comparisons, not to the post-sort edge_scans counter. Therefore measured scan slopes must not be presented as evidence of sorting complexity. Bounded random weights (1–1000), repeated keys and finite input sizes can also affect the observed comparison slope. Empirical data supports behavior on these inputs, not a proof of an asymptotic bound.', '', '## Counter/formula ratios', '', 'Each row gives ratios for n = 1000, 3162, 10000, 31623, 100000, in that order.', '', '| Algorithm | m/n | Counter / formula | Ratios |', '|---|---:|---|---|']
for algorithm in algorithms+['Tarjan']:
    for density in (4,20):
        data=series(algorithm,density)
        if algorithm in ('Kruskal','Prim'):
            values=[r['comparisons']/(r['m']*math.log2(r['m'])) for r in data]
            formula='comparisons / (m log2(m))'
        else:
            values=[r['edge_scans']/(r['n']+r['m']) for r in data]
            formula='edge_scans / (n + m)'
        lines.append(f"| {algorithm} | {density} | {formula} | "+', '.join(f'{v:.4f}' for v in values)+' |')
        if algorithm=='Kruskal':
            values=[r['dsu_ops']/r['m'] for r in data]
            lines.append(f"| Kruskal | {density} | dsu_ops / m | "+', '.join(f'{v:.4f}' for v in values)+' |')
lines += ['', 'DSU counts calls, not parent-pointer traversals, so dsu_ops/m does not directly measure inverse-Ackermann cost. Early stopping changes the number of calls per input edge. The exact connected-graph identity here is dsu_ops = 2 * edge_scans + 3 * (n - 1). Ratios need not be exactly constant, especially for early-stopping Kruskal.', '', '## Kruskal scan shares', '', '| n | m = 4n | m = 20n |', '|---:|---:|---:|']
for a,b in zip(series('Kruskal',4),series('Kruskal',20)):
    lines.append(f"| {a['n']} | {a['edge_scans']/a['m']:.4%} | {b['edge_scans']/b['m']:.4%} |")
lines += ['', '## Tarjan bonus', '', 'On all ten configurations, Tarjan scans m edges and Kosaraju scans 2m. Tarjan saves exactly 50% of counted edge scans by finding SCCs in one DFS pass. Kosaraju uses two DFS passes and additionally constructs a reversed graph. Graph reversal is not included in this edge_scans counter. A smaller scan count does not guarantee the same time ratio.', '', '## Timing', '', 'Times are medians of five runs in milliseconds. Kahn timing includes order validation. DAG timing includes topological preprocessing and distance summation, while its edge_scans counts only relaxation. SCC partition comparison is outside timed runs. Runs have no separate warm-up phase, so JIT compilation, garbage collection and background load can affect results, especially small inputs.', '', '| Algorithm | n=100000, m=400000 (ms) | n=100000, m=2000000 (ms) |', '|---|---:|---:|']
for algorithm in algorithms+['Tarjan']:
    lines.append(f"| {algorithm} | {series(algorithm,4)[-1]['time_ms']:.3f} | {series(algorithm,20)[-1]['time_ms']:.3f} |")
lines += ['', '## Reproduce the figures', '', 'Place plot_results.py in the project root beside pom.xml. Input files are results/results.csv and results/tarjan.csv. Run:', '', '```text', 'python -m pip install matplotlib', 'python plot_results.py', '```', '', 'Matplotlib is used only for report figures. Java graph algorithms use no additional libraries. The script validates inputs, writes three PNG plots and regenerates this analysis. It does not modify the input CSV files.', '', 'This is supporting analysis, not the complete required 3–5 page REPORT. Complexity, correctness arguments, algorithm selection, Bonus B discussion and final submission checks remain to be incorporated.']
(RESULTS/'ANALYSIS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('\n'.join(lines))
