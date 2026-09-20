
import csv
import statistics
from collections import defaultdict
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROWS = []
with open("results/results.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        row["n"] = int(row["n"])
        row["trial"] = int(row["trial"])
        row["timeNs"] = int(row["timeNs"])
        row["maxDepth"] = int(row["maxDepth"])
        row["comparisons"] = int(row["comparisons"])
        row["swaps"] = int(row["swaps"])
        row["recursiveCalls"] = int(row["recursiveCalls"])
        ROWS.append(row)


def aggregate(rows, keyfields, valuefield):
    groups = defaultdict(list)
    for r in rows:
        key = tuple(r[k] for k in keyfields)
        groups[key].append(r[valuefield])
    return {k: statistics.median(v) for k, v in groups.items()}


def series_for(rows, algorithm, input_type, valuefield, sizes):
    filtered = [r for r in rows if r["algorithm"] == algorithm and r["inputType"] == input_type]
    agg = aggregate(filtered, ["n"], valuefield)
    return [agg.get((n,), None) for n in sizes]


sort_sizes = sorted(set(r["n"] for r in ROWS if r["algorithm"] in
                        ("MergeSort", "QuickSort", "DeterministicSelect")))

plt.figure(figsize=(8, 5.5))
for algo, marker in [("MergeSort", "o"), ("QuickSort", "s"), ("DeterministicSelect", "^")]:
    times_ms = [t / 1e6 if t is not None else None
                for t in series_for(ROWS, algo, "random", "timeNs", sort_sizes)]
    plt.plot(sort_sizes, times_ms, marker=marker, label=algo)
plt.xlabel("Input size n")
plt.ylabel("Execution time (ms)")
plt.title("Execution Time vs. n (random inputs)")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("docs/plots/time_vs_n.png", dpi=150)
plt.close()

plt.figure(figsize=(8, 5.5))
for algo, marker in [("MergeSort", "o"), ("QuickSort", "s"), ("DeterministicSelect", "^")]:
    depths = series_for(ROWS, algo, "random", "maxDepth", sort_sizes)
    plt.plot(sort_sizes, depths, marker=marker, label=algo)
plt.xlabel("Input size n")
plt.ylabel("Maximum recursion depth")
plt.title("Recursion Depth vs. n (random inputs)")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("docs/plots/depth_vs_n.png", dpi=150)
plt.close()

plt.figure(figsize=(8, 5.5))
for itype, marker in [("random", "o"), ("sorted", "s"), ("reverse_sorted", "^"), ("duplicate_heavy", "d")]:
    times_ms = [t / 1e6 if t is not None else None
                for t in series_for(ROWS, "QuickSort", itype, "timeNs", sort_sizes)]
    plt.plot(sort_sizes, times_ms, marker=marker, label=itype)
plt.xlabel("Input size n")
plt.ylabel("Execution time (ms)")
plt.title("QuickSort: Execution Time vs. n, by Input Type")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("docs/plots/quicksort_by_inputtype.png", dpi=150)
plt.close()

cp_sizes = sorted(set(r["n"] for r in ROWS if r["algorithm"] == "ClosestPair"))
plt.figure(figsize=(8, 5.5))
for itype, marker in [("random", "o"), ("clustered", "s")]:
    times_ms = [t / 1e6 if t is not None else None
                for t in series_for(ROWS, "ClosestPair", itype, "timeNs", cp_sizes)]
    plt.plot(cp_sizes, times_ms, marker=marker, label=itype)
plt.xlabel("Number of points n")
plt.ylabel("Execution time (ms)")
plt.title("Closest Pair (D&C): Execution Time vs. n")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("docs/plots/closest_pair_time_vs_n.png", dpi=150)
plt.close()

plt.figure(figsize=(8, 5.5))
for algo, marker in [("MergeSort", "o"), ("QuickSort", "s"), ("DeterministicSelect", "^")]:
    comps = series_for(ROWS, algo, "random", "comparisons", sort_sizes)
    plt.plot(sort_sizes, comps, marker=marker, label=algo)
plt.xlabel("Input size n")
plt.ylabel("Comparisons")
plt.title("Comparisons vs. n (random inputs)")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("docs/plots/comparisons_vs_n.png", dpi=150)
plt.close()

print("All plots written to docs/plots/")

def fmt_ms(ns):
    return f"{ns / 1e6:.2f}"

with open("results/summary_tables.md", "w") as f:
    f.write("### Execution time (ms), median of trials, random inputs\n\n")
    f.write("| n | MergeSort | QuickSort | DeterministicSelect |\n")
    f.write("|---|---|---|---|\n")
    for n in sort_sizes:
        row = [n]
        for algo in ("MergeSort", "QuickSort", "DeterministicSelect"):
            filtered = [r for r in ROWS if r["algorithm"] == algo and r["inputType"] == "random" and r["n"] == n]
            row.append(fmt_ms(statistics.median(r["timeNs"] for r in filtered)))
        f.write(f"| {row[0]} | {row[1]} | {row[2]} | {row[3]} |\n")

    f.write("\n### Maximum recursion depth, mean of trials, random inputs\n\n")
    f.write("| n | MergeSort | QuickSort | DeterministicSelect |\n")
    f.write("|---|---|---|---|\n")
    for n in sort_sizes:
        row = [n]
        for algo in ("MergeSort", "QuickSort", "DeterministicSelect"):
            filtered = [r for r in ROWS if r["algorithm"] == algo and r["inputType"] == "random" and r["n"] == n]
            row.append(f"{statistics.mean(r['maxDepth'] for r in filtered):.1f}")
        f.write(f"| {row[0]} | {row[1]} | {row[2]} | {row[3]} |\n")

    f.write("\n### QuickSort execution time (ms) by input type\n\n")
    f.write("| n | random | sorted | reverse_sorted | duplicate_heavy |\n")
    f.write("|---|---|---|---|---|\n")
    for n in sort_sizes:
        row = [n]
        for itype in ("random", "sorted", "reverse_sorted", "duplicate_heavy"):
            filtered = [r for r in ROWS if r["algorithm"] == "QuickSort" and r["inputType"] == itype and r["n"] == n]
            row.append(fmt_ms(statistics.median(r["timeNs"] for r in filtered)))
        f.write(f"| {row[0]} | {row[1]} | {row[2]} | {row[3]} | {row[4]} |\n")

    f.write("\n### Closest Pair execution time (ms), median of trials\n\n")
    f.write("| n | random | clustered |\n")
    f.write("|---|---|---|\n")
    for n in cp_sizes:
        row = [n]
        for itype in ("random", "clustered"):
            filtered = [r for r in ROWS if r["algorithm"] == "ClosestPair" and r["inputType"] == itype and r["n"] == n]
            row.append(fmt_ms(statistics.median(r["timeNs"] for r in filtered)))
        f.write(f"| {row[0]} | {row[1]} | {row[2]} |\n")

print("Summary tables written to results/summary_tables.md")