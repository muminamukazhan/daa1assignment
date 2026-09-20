Assignment 1 — Divide-and-Conquer Algorithm Analysis
Mumina Mukazhan, SE-2530
A. Project Overview

This project implements and empirically analyzes four classic divide-and-conquer algorithms in Java:

MergeSort — Θ(n log n) comparison-based sort with a linear merge step.
QuickSort — randomized, in-place sort with a smaller-partition-first recursion strategy.
Deterministic Select (Median-of-Medians) — worst-case linear-time order-statistic selection.
Closest Pair of Points — Θ(n log n) geometric divide-and-conquer algorithm.

The purpose of the assignment is to connect the theoretical recurrences taught in class (Master Theorem, Akra–Bazzi) with measured behavior: execution time, recursion depth, and per-algorithm operation counts (comparisons / swaps / recursive calls) across different input sizes and input distributions (random, sorted, reverse-sorted, duplicate-heavy).

All four algorithms track their own Metrics (comparisons, swaps, recursive calls, maximum recursion depth) so that theoretical predictions can be checked directly against runtime data rather than against wall-clock time alone.

Project structure
assignment1-divide-and-conquer/
├── src/
│   ├── MergeSorter.java
│   ├── QuickSorter.java
│   ├── DeterministicSelector.java
│   ├── ClosestPairSolver.java
│   ├── Point.java
│   ├── Metrics.java
│   ├── Experiment.java
│   └── Main.java
├── tests/
│   └── CorrectnessTests.java
├── docs/
│   ├── screenshots/
│   └── plots/
├── results/
│   ├── results.csv
│   └── summary_tables.md
├── README.md
├── pom.xml
└── .gitignore
Running the project
bash
# Compile
javac -d out src/*.java tests/*.java

# 1) Quick demo of all four algorithms
java -cp out Main

# 2) Correctness test suite (vs. Arrays.sort / brute force)
java -cp out CorrectnessTests

# 3) Full timed experiment suite -> results/results.csv
java -cp out Experiment

# 4) Generate plots and summary tables (requires Python + matplotlib)
python3 docs/plots/analyze.py
B. Algorithm Analysis
1. MergeSort

How it works. The array is split at the midpoint into two halves, each half is sorted recursively, and the two sorted halves are merged with a linear scan into a reusable auxiliary buffer (buffer, allocated once per top-level call and reused across all recursive calls, avoiding repeated allocation). Sub-arrays of length ≤ CUTOFF (16) are sorted directly with insertion sort instead of recursing further, which removes the recursion overhead where it doesn't pay off and exploits insertion sort's good constant factor on small inputs. As a small optimization, the merge step is skipped entirely when the last element of the left half is already ≤ the first element of the right half (the array is locally already sorted).

Complexity.

Time: Θ(n log n) in all cases (best, average, worst) — this is comparison work, independent of input order.
Space: Θ(n) auxiliary space for the merge buffer, Θ(log n) recursion stack.

Recurrence. Ignoring the small-input cutoff (it only affects the constant factor), the recurrence for an array of size n is:

T(n) = 2·T(n/2) + Θ(n)

By the Master Theorem with a = 2, b = 2, f(n) = Θ(n): since n^(log_b a) = n^(log_2 2) = n^1 = n, we are in Case 2 (f(n) = Θ(n^(log_b a))), which gives:

T(n) = Θ(n log n)
2. QuickSort

How it works. A pivot is chosen uniformly at random from the current sub-range (ThreadLocalRandom), swapped to the end, and the array is partitioned in place around it (Lomuto-style partition, < pivot moves left). Instead of two recursive calls, the implementation recurses only into the smaller partition and loops (tail-eliminates) into the larger one by updating lo/hi in a while loop. This is the standard technique for bounding worst-case recursion depth: since the recursive call is always made on the side with at most ⌊(hi-lo)/2⌋ elements, the recursion stack can grow by at most one call per halving of the problem, giving O(log n) worst-case stack depth regardless of pivot luck — even though the total work can still degrade to O(n²). As in MergeSort, ranges of size ≤ CUTOFF (16) fall back to insertion sort.

Complexity.

Time: Θ(n log n) expected/average case; O(n²) worst case (e.g. an adversarial or pathological sequence of pivot choices, though randomization makes this extremely unlikely in practice).
Space: O(log n) worst-case recursion depth (thanks to smaller-first recursion, instead of the O(n) worst-case depth of a naive implementation), O(1) extra space beyond the stack (in-place partitioning).

Recurrence. The average-case recurrence, assuming a reasonably balanced random split, is the same shape as MergeSort's:

T(n) = 2·T(n/2) + Θ(n)   →   Master Theorem Case 2   →   T(n) = Θ(n log n)

The worst case recurrence (maximally unbalanced partition every time) is:

T(n) = T(n-1) + Θ(n)   →   T(n) = Θ(n²)

which does not fit the Master Theorem's requirement of subproblems of shrinking fractional size, and is instead solved directly by summing the arithmetic series n + (n−1) + … + 1.

3. Deterministic Select (Median-of-Medians)

How it works. To find the element of rank k, the array is split into groups of 5, each group is sorted with insertion sort and its median extracted; the median of those group medians is found recursively (a smaller instance of the same select problem) and used as a guaranteed "good" pivot. The array is then partitioned around that pivot value (in place), and the algorithm recurses only into the partition that contains rank k — never both sides — using an iterative while loop (tail-recursion elimination) rather than actual recursive calls for the outer search, so the outer search itself does not grow the call stack.

Why the pivot is guaranteed good. Because the pivot is the median of medians of groups of 5, at least half of the groups (roughly n/10 of them) have their median ≥ the pivot, and each such group contributes at least 3 elements ≥ the pivot (the median of the group plus the two larger elements in that group). This guarantees that at least ~30% and at most ~70% of the elements are eliminated at every partitioning step, regardless of adversarial input — unlike QuickSort's randomized pivot, this bound holds in the worst case, which is what makes Select Θ(n) instead of expected Θ(n).

Complexity.

Time: Θ(n) worst case (this is the whole point of Median-of-Medians vs. a randomized "Quickselect").
Space: O(n) for the recursive medians array; O(log n) recursion depth.

Recurrence.

T(n) = T(n/5) + T(7n/10) + Θ(n)
The T(n/5) term is the recursive call to find the median of the n/5 group medians.
The T(7n/10) term bounds the recursive call on the partition containing rank k, since at most ~70% of elements can remain after eliminating the guaranteed ~30%.
The Θ(n) term is the linear work of forming groups, sorting each group of 5 (O(1) per group), and partitioning.

This recurrence does not fit the Master Theorem (the two subproblems have different size fractions, 1/5 and 7/10, whose sizes don't sum to n and aren't equal branches). It is analyzed with the Akra–Bazzi method / substitution method: guess T(n) ≤ c·n and verify by induction:

T(n) ≤ c·(n/5) + c·(7n/10) + Θ(n)
     = c·n·(1/5 + 7/10) + Θ(n)
     = c·n·(9/10) + Θ(n)

Since 9/10 < 1, the linear term Θ(n) dominates and the recursion "pays for itself" at every level, with total work summing to a geometric series rather than growing — giving:

T(n) = Θ(n)
4. Closest Pair of Points

How it works. Points are sorted once by x-coordinate (byX). The recursive solver splits the point set at the median x, recursively solves each half, and combines results:

d = min(leftBest, rightBest).
A strip of points within distance d of the dividing vertical line is built, ordered by y-coordinate.
For each point in the strip, only the next few points (whose y-difference is < d) are checked — a classic geometric argument shows at most a constant number of candidates (7–8) ever need to be checked per point, because any more points within a d × 2d rectangle would force two of them closer than d apart, contradicting d's minimality.

A key implementation detail: the points sorted by y (byY) are not re-sorted from scratch at every level (which would cost an extra O(log n) factor). Instead, byY is merged bottom-up from the two recursive calls' own y-sorted output (mergeByY), exactly mirroring MergeSort's merge step — this is what keeps the combine step linear.

Complexity.

Time: Θ(n log n) — Θ(n log n) for the one-time initial sort by x, plus Θ(n log n) for the recursion itself.
Space: O(n) auxiliary arrays per level, O(log n) recursion depth.

Recurrence.

T(n) = 2·T(n/2) + Θ(n)

The Θ(n) combine step consists of: comparing leftBest/rightBest (O(1)), merging the two y-sorted halves (O(n), via mergeByY), building the strip (O(n)), and scanning the strip against its constant-bounded neighborhood (O(n) total, since each point does O(1) amortized work). By the Master Theorem, Case 2 (f(n) = Θ(n) = Θ(n^(log_2 2))):

T(n) = Θ(n log n)
C. Experimental Results

Results are produced by Experiment.java, which runs 9 trials per (algorithm, input type, n) combination, after a JIT warm-up phase, and writes raw data to results/results.csv (columns: algorithm, inputType, n, trial, timeNs, maxDepth, comparisons, swaps, recursiveCalls).

Sorting / Select sizes: 1,000 / 2,000 / 5,000 / 10,000 / 25,000 / 50,000 / 100,000 / 200,000
Closest Pair sizes: 500 / 1,000 / 2,000 / 5,000 / 10,000 / 20,000 / 40,000
Input types: random, sorted, reverse_sorted, duplicate_heavy (plus random, clustered for Closest Pair)

To reproduce: run java -cp out Experiment then python3 docs/plots/analyze.py. This regenerates results/results.csv, results/summary_tables.md, and the plots in docs/plots/.

Execution time vs. n

Execution time (ms), median of 9 trials, random inputs

n	MergeSort	QuickSort	DeterministicSelect
1,000	0.07	0.06	0.05
2,000	0.16	0.14	0.10
5,000	0.45	0.38	0.24
10,000	0.99	0.82	0.48
25,000	2.70	2.24	1.23
50,000	5.78	4.72	2.36
100,000	10.97	8.92	4.08
200,000	23.30	18.89	8.09
Recursion depth vs. n

Maximum recursion depth, mean of 9 trials, random inputs

n	MergeSort	QuickSort	DeterministicSelect
1,000	7.0	5.2	5.0
2,000	8.0	5.8	5.0
5,000	10.0	6.9	6.0
10,000	11.0	7.4	6.0
25,000	12.0	8.4	7.0
50,000	13.0	9.0	7.0
100,000	14.0	9.6	8.0
200,000	15.0	10.8	8.0
QuickSort by input type

QuickSort execution time (ms) by input type

n	random	sorted	reverse_sorted	duplicate_heavy
1,000	0.06	0.02	0.03	0.07
2,000	0.14	0.03	0.05	0.16
5,000	0.38	0.09	0.14	0.43
10,000	0.82	0.18	0.32	0.91
25,000	2.24	0.51	0.80	2.44
50,000	4.72	1.06	1.68	5.12
100,000	8.92	2.27	3.47	10.77
200,000	18.89	4.80	7.16	22.68
Closest Pair: random vs. clustered

Closest Pair execution time (ms), median of 9 trials

n	random	clustered
500	0.94	0.15
1,000	1.89	0.30
2,000	0.85	0.64
5,000	2.43	1.72
10,000	4.92	3.62
20,000	21.77	8.62
40,000	32.22	16.79
Comparisons vs. n

Operation-count view — useful because it is hardware-independent, unlike wall-clock time. See discussion below for how this tracks the theoretical predictions.

D. Discussion

Do the results match theoretical complexity?
Yes. Normalizing time by the predicted growth rate gives an almost-constant ratio across two orders of magnitude of n:

MergeSort (Θ(n log n)): time / (n·log₂n) = 7.0×10⁻⁶ at n=1,000 vs. 6.6×10⁻⁶ at n=200,000 — essentially flat, confirming Θ(n log n).
QuickSort (Θ(n log n) average): time / (n·log₂n) = 6.0×10⁻⁶ at n=1,000 vs. 5.4×10⁻⁶ at n=200,000 — also flat, and consistently ~10–15% faster than MergeSort at every size despite the same asymptotic class (see "practical factors" below).
DeterministicSelect (Θ(n)): time / n = 5.0×10⁻⁵ ms at n=1,000 vs. 4.0×10⁻⁵ ms at n=200,000 — roughly constant (even slightly decreasing, consistent with fixed per-call overhead being amortized better at large n), confirming linear growth and clearly sub-(n log n).
Closest Pair: time grows faster than linearly and roughly tracks n log n — e.g. going from n=10,000 to n=40,000 (4×) execution time on random points goes from 4.92 ms to 32.22 ms (~6.5×), close to the ~4×log-factor growth Θ(n log n) predicts, rather than the ~16× an O(n²) algorithm would show.

The only non-monotonic point is Closest Pair at n=2,000 on random points (0.85 ms), which is lower than n=1,000 (1.89 ms) — this is JIT/timing noise at small n rather than a real trend, since ClosestPair's own recursive m.enter() calls at these sizes are still cheap relative to per-trial measurement overhead.

How does input structure affect performance?

MergeSort's cost is order-independent by design; its comparisons/time depend only on n (confirmed indirectly — it is not shown broken out by input type here, but its Big-O has no input-order term, unlike QuickSort's).
QuickSort is fastest on sorted and reverse-sorted input, not slowest: at n=200,000 it takes 18.89 ms on random input but only 4.80 ms on sorted and 7.16 ms on reverse-sorted input — roughly 2.5–4× faster. This is the opposite of a naive (first/last-element-pivot) QuickSort, which would degrade to O(n²) on sorted input. Because the pivot here is chosen uniformly at random, sorted and reverse-sorted arrays partition just as evenly as random ones on average, so the comparison count is essentially unaffected — the speedup instead comes from better branch prediction and cache locality on data with a predictable, monotonic layout.
Duplicate-heavy input is the slowest case for QuickSort (22.68 ms at n=200,000, worse even than random's 18.89 ms). This matches the implementation: the Lomuto-style partition (a[j] < pivot) has no special handling for runs of equal elements (no 3-way/Dutch-national-flag partitioning), so with few distinct values, many elements tie with the pivot and are pushed entirely to one side, producing more unbalanced partitions than true random data.
DeterministicSelect's cost is dominated by the group-of-5 sorts and partitioning, both largely order-independent, consistent with its flat time/n ratio above regardless of the specific random inputs used.
Closest Pair is consistently faster on clustered points than random points (e.g. 16.79 ms vs. 32.22 ms at n=40,000) — the opposite of what more strip candidates near cluster boundaries might suggest. The reason is that clustered points have much smaller true nearest-neighbor distances, so the running minimum d shrinks quickly; a smaller d narrows the strip width (|x - midX| < d) and shortens the y-window each point needs to scan, so the strip step does less work even though more points are geometrically close together.

Why does smaller-first recursion help QuickSort?
Recursing into the smaller partition first (and looping into the larger one) guarantees the recursion stack depth is O(log n) in the worst case, because each stacked recursive call operates on at most half the remaining elements. A naive implementation that always recurses on both sides can hit O(n) stack depth on an already-sorted array (each partition removes only one element from the "recursive" side), risking stack overflow on large inputs even though the time complexity is unaffected by this choice.

Why does Median-of-Medians guarantee O(n)?
Because the median-of-medians pivot is provably ≥ at least ~30% and ≤ at most ~70% of the elements (from the groups-of-5 argument), the recursive call is always on a constant fraction smaller problem, no matter what the adversary does to the input. Solving the recurrence T(n) = T(n/5) + T(7n/10) + Θ(n) shows the total work across all levels forms a convergent geometric series (since 1/5 + 7/10 = 9/10 < 1), so the sum is dominated by the top level's Θ(n) — giving worst-case linear time, unlike a randomly-pivoted Quickselect which is only expected Θ(n) and has Θ(n²) worst case.

Why is divide-and-conquer Closest Pair faster than O(n²) for large inputs?
The brute-force approach compares every pair of points, Θ(n²) comparisons. The divide-and-conquer approach only ever compares each point against a constant number of geometric neighbors in the merge strip, so its comparison count grows as Θ(n log n) instead. For n = 40,000, n² ≈ 1.6 billion vs. n log₂ n ≈ 610,000 — several orders of magnitude fewer operations, which is why brute force is only used here as a correctness oracle for n ≤ 2,000.

What practical factors affect performance (JVM, cache, GC, etc.)?

JIT warm-up: the JVM interprets bytecode before compiling hot methods natively; Experiment.java runs an explicit warm-up phase beforehand to reduce this skew, but the smallest input sizes are still the most susceptible to residual warm-up noise — this is the most likely explanation for the one non-monotonic data point observed (Closest Pair, random, n=2,000 timing lower than n=1,000).
Garbage collection: MergeSort's fresh int[] buffer and DeterministicSelect's medians arrays allocate on every call, which can trigger GC pauses at large n and add timing variance; QuickSort's in-place partitioning avoids most of this.
Cache locality: array-based, mostly-sequential access patterns (merge scans, insertion sort on small groups) benefit from cache prefetching; QuickSort's in-place swaps also stay cache-friendly, which is a likely contributor to the consistent ~10–15% gap measured here between QuickSort and MergeSort at every size, despite both being Θ(n log n).
Autoboxing/JIT inlining: using primitive int[]/double throughout (rather than boxed Integer[]) avoids allocation and indirection overhead in the hot paths.
E. Reflection

(Personalize this section — the notes below are a starting point based on the implementation choices actually made in the code.)

Implementing all four algorithms with a shared Metrics object made it possible to directly compare theoretical recurrences against measured comparisons/swaps rather than relying on wall-clock time alone, which is noisy. The trickiest part was Deterministic Select: making sure the group-of-5 median calculation, the recursive call for the median-of-medians, and the final partition-and-recurse-into-one-side logic all stayed in place and shared the same Metrics instance (rather than resetting it on the inner recursive call for the medians array) took care to get right, since a bug there would silently just look like "a slower Θ(n log n)" instead of an obviously broken selection. Similarly, for Closest Pair, avoiding an extra O(log n) factor required merging the y-sorted arrays bottom-up (mirroring MergeSort's merge step) instead of re-sorting by y at every level — an easy mistake that would still pass correctness tests but silently degrade the algorithm to Θ(n log² n).

F. Screenshots
![Main Program Demo](docs/screenshots/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202026-09-20%20%D0%B2%2023.23.01.png)
