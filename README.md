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

Recurrence.

T(n) = 2·T(n/2) + Θ(n)

By the Master Theorem with a = 2, b = 2, f(n) = Θ(n): since n^(log_b a) = n^(log_2 2) = n^1 = n, we are in Case 2 (f(n) = Θ(n^(log_b a))), which gives:

T(n) = Θ(n log n)
2. QuickSort

How it works. A pivot is chosen uniformly at random from the current sub-range (ThreadLocalRandom), swapped to the end, and the array is partitioned in place around it (Lomuto-style partition, < pivot moves left). Instead of two recursive calls, the implementation recurses only into the smaller partition and loops (tail-eliminates) into the larger one by updating lo/hi in a while loop. This bounds worst-case recursion depth: since the recursive call is always made on the side with at most ⌊(hi-lo)/2⌋ elements, the stack can grow by at most one call per halving, giving O(log n) worst-case stack depth regardless of pivot luck — even though total work can still degrade to O(n²). As in MergeSort, ranges of size ≤ CUTOFF (16) fall back to insertion sort.

Complexity.

Time: Θ(n log n) expected/average case; O(n²) worst case (e.g. an adversarial or pathological sequence of pivot choices, though randomization makes this extremely unlikely in practice).
Space: O(log n) worst-case recursion depth (thanks to smaller-first recursion, instead of O(n) worst-case depth of a naive implementation), O(1) extra space beyond the stack (in-place partitioning).

Recurrence. Average case:

T(n) = 2·T(n/2) + Θ(n)   →   Master Theorem Case 2   →   T(n) = Θ(n log n)

Worst case (maximally unbalanced partition every time):

T(n) = T(n-1) + Θ(n)   →   T(n) = Θ(n²)

which does not fit the Master Theorem's requirement of subproblems of shrinking fractional size, and is instead solved directly by summing the arithmetic series n + (n−1) + … + 1.

3. Deterministic Select (Median-of-Medians)

How it works. To find the element of rank k, the array is split into groups of 5, each group is sorted with insertion sort and its median extracted; the median of those group medians is found recursively (a smaller instance of the same select problem) and used as a guaranteed "good" pivot. The array is then partitioned around that pivot value (in place), and the algorithm recurses only into the partition that contains rank k — never both sides — using an iterative while loop (tail-recursion elimination) so the outer search itself does not grow the call stack.

Why the pivot is guaranteed good. Because the pivot is the median of medians of groups of 5, at least half of the groups (roughly n/10 of them) have their median ≥ the pivot, and each such group contributes at least 3 elements ≥ the pivot. This guarantees that at least ~30% and at most ~70% of the elements are eliminated at every partitioning step, regardless of adversarial input — this bound holds in the worst case, which is what makes Select Θ(n) instead of expected Θ(n).

Complexity.

Time: Θ(n) worst case.
Space: O(n) for the recursive medians array; O(log n) recursion depth.

Recurrence.

T(n) = T(n/5) + T(7n/10) + Θ(n)
T(n/5) — recursive call to find the median of the n/5 group medians.
T(7n/10) — bounds the recursive call on the partition containing rank k.
Θ(n) — linear work of forming groups, sorting each group of 5, and partitioning.

This does not fit the Master Theorem. It is analyzed with the Akra–Bazzi / substitution method: guess T(n) ≤ c·n and verify by induction:

T(n) ≤ c·(n/5) + c·(7n/10) + Θ(n)
     = c·n·(1/5 + 7/10) + Θ(n)
     = c·n·(9/10) + Θ(n)

Since 9/10 < 1, the linear term dominates, giving:

T(n) = Θ(n)
4. Closest Pair of Points

How it works. Points are sorted once by x-coordinate (byX). The recursive solver splits at the median x, recursively solves each half, and combines:

d = min(leftBest, rightBest).
A strip of points within distance d of the dividing vertical line is built, ordered by y-coordinate.
For each point in the strip, only the next few points (y-difference < d) are checked — at most a constant number of candidates (7–8) ever need checking per point.

A key detail: byY is not re-sorted from scratch at every level. Instead, it's merged bottom-up from the two recursive calls' own y-sorted output (mergeByY), mirroring MergeSort's merge step — this keeps the combine step linear.

Complexity.

Time: Θ(n log n) — Θ(n log n) for the initial sort by x, plus Θ(n log n) for the recursion.
Space: O(n) auxiliary arrays per level, O(log n) recursion depth.

Recurrence.

T(n) = 2·T(n/2) + Θ(n)

By the Master Theorem, Case 2:

T(n) = Θ(n log n)
C. Experimental Results

Results are produced by Experiment.java, which runs 9 trials per (algorithm, input type, n) combination, after a JIT warm-up phase, and writes raw data to results/results.csv (columns: algorithm, inputType, n, trial, timeNs, maxDepth, comparisons, swaps, recursiveCalls).

Sorting / Select sizes: 1,000 / 2,000 / 5,000 / 10,000 / 25,000 / 50,000 / 100,000 / 200,000
Closest Pair sizes: 500 / 1,000 / 2,000 / 5,000 / 10,000 / 20,000 / 40,000
Input types: random, sorted, reverse_sorted, duplicate_heavy (plus random, clustered for Closest Pair)

To reproduce: run java -cp out Experiment then python3 docs/plots/analyze.py.

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

MergeSort (Θ(n log n)): time / (n·log₂n) = 7.0×10⁻⁶ at n=1,000 vs. 6.6×10⁻⁶ at n=200,000 — essentially flat.
QuickSort (Θ(n log n) average): time / (n·log₂n) = 6.0×10⁻⁶ at n=1,000 vs. 5.4×10⁻⁶ at n=200,000 — also flat, and consistently ~10–15% faster than MergeSort at every size.
DeterministicSelect (Θ(n)): time / n = 5.0×10⁻⁵ ms at n=1,000 vs. 4.0×10⁻⁵ ms at n=200,000 — roughly constant, confirming linear growth.
Closest Pair: going from n=10,000 to n=40,000 (4×) execution time goes from 4.92 ms to 32.22 ms (~6.5×), close to the ~4×log-factor growth Θ(n log n) predicts, rather than the ~16× an O(n²) algorithm would show.

The only non-monotonic point is Closest Pair at n=2,000 on random points (0.85 ms), lower than n=1,000 (1.89 ms) — JIT/timing noise at small n rather than a real trend.

How does input structure affect performance?

MergeSort's cost is order-independent by design.
QuickSort is fastest on sorted and reverse-sorted input, not slowest: at n=200,000 it takes 18.89 ms on random input but only 4.80 ms on sorted and 7.16 ms on reverse-sorted — because the pivot is random, comparison count is unaffected; the speedup comes from better branch prediction and cache locality.
Duplicate-heavy input is the slowest case for QuickSort (22.68 ms at n=200,000). The Lomuto-style partition has no special handling for runs of equal elements, so many ties get pushed entirely to one side.
DeterministicSelect's cost is dominated by the group-of-5 sorts and partitioning, largely order-independent.
Closest Pair is consistently faster on clustered points (16.79 ms vs. 32.22 ms at n=40,000) — clustered points have smaller nearest-neighbor distances, so d shrinks fast, narrowing the strip and shortening the y-window scanned.

Why does smaller-first recursion help QuickSort?
It guarantees recursion stack depth is O(log n) in the worst case, because each stacked call operates on at most half the remaining elements. A naive both-sides implementation can hit O(n) stack depth on an already-sorted array, risking stack overflow — though time complexity is unaffected.

Why does Median-of-Medians guarantee O(n)?
The pivot is provably ≥ at least ~30% and ≤ at most ~70% of the elements, so the recursive call is always on a constant fraction smaller problem regardless of adversarial input. Solving T(n) = T(n/5) + T(7n/10) + Θ(n) gives a convergent geometric series (1/5 + 7/10 = 9/10 < 1), dominated by the top level's Θ(n) — giving worst-case linear time, unlike randomly-pivoted Quickselect (expected Θ(n), worst case Θ(n²)).

Why is divide-and-conquer Closest Pair faster than O(n²) for large inputs?
Brute force compares every pair, Θ(n²). Divide-and-conquer only compares each point against a constant number of geometric neighbors in the merge strip, giving Θ(n log n). For n = 40,000, n² ≈ 1.6 billion vs. n log₂n ≈ 610,000 — several orders of magnitude fewer operations.

What practical factors affect performance (JVM, cache, GC, etc.)?

JIT warm-up: Experiment.java runs an explicit warm-up phase, but the smallest input sizes are still susceptible to residual noise.
Garbage collection: MergeSort's fresh int[] buffer and DeterministicSelect's medians arrays allocate on every call, triggering GC pauses at large n; QuickSort's in-place partitioning avoids most of this.
Cache locality: sequential access patterns benefit from prefetching; QuickSort's in-place swaps stay cache-friendly, likely contributing to the ~10–15% gap vs. MergeSort.
Autoboxing/JIT inlining: primitive int[]/double throughout avoids allocation and indirection overhead.
E. Reflection

Implementing all four algorithms with a shared Metrics object made it possible to directly compare theoretical recurrences against measured comparisons/swaps rather than relying on wall-clock time alone, which is noisy. The trickiest part was Deterministic Select: making sure the group-of-5 median calculation, the recursive call for the median-of-medians, and the final partition-and-recurse-into-one-side logic all stayed in place and shared the same Metrics instance took care to get right, since a bug there would silently just look like "a slower Θ(n log n)" instead of an obviously broken selection. Similarly, for Closest Pair, avoiding an extra O(log n) factor required merging the y-sorted arrays bottom-up instead of re-sorting by y at every level — an easy mistake that would still pass correctness tests but silently degrade the algorithm to Θ(n log² n).

F. Screenshots
![Main Program Demo](docs/screenshots/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202026-09-20%20%D0%B2%2023.23.01.png)
