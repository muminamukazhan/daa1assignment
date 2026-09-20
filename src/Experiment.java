import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class Experiment {

    private static final int[] SIZES = {1000, 2000, 5000, 10000, 25000, 50000, 100000, 200000};
    private static final int[] CLOSEST_PAIR_SIZES = {500, 1000, 2000, 5000, 10000, 20000, 40000};
    private static final String[] INPUT_TYPES = {"random", "sorted", "reverse_sorted", "duplicate_heavy"};
    private static final int TRIALS = 9;

    public static void main(String[] args) throws IOException {
        warmUpJit();

        Path outDir = Paths.get("results");
        Files.createDirectories(outDir);
        Path csvPath = outDir.resolve("results.csv");

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(csvPath))) {
            out.println("algorithm,inputType,n,trial,timeNs,maxDepth,comparisons,swaps,recursiveCalls");

            for (String type : INPUT_TYPES) {
                for (int n : SIZES) {
                    for (int t = 1; t <= TRIALS; t++) {
                        int[] base = generate(n, type);

                        int[] a1 = base.clone();
                        Metrics mMerge = new Metrics();
                        long t0 = System.nanoTime();
                        MergeSorter.sort(a1, mMerge);
                        long mergeTime = System.nanoTime() - t0;
                        assertSorted(a1);
                        out.printf("MergeSort,%s,%d,%d,%d,%d,%d,%d,%d%n",
                                type, n, t, mergeTime, mMerge.maxDepth, mMerge.comparisons, mMerge.swaps, mMerge.recursiveCalls);

                        int[] a2 = base.clone();
                        Metrics mQuick = new Metrics();
                        t0 = System.nanoTime();
                        QuickSorter.sort(a2, mQuick);
                        long quickTime = System.nanoTime() - t0;
                        assertSorted(a2);
                        out.printf("QuickSort,%s,%d,%d,%d,%d,%d,%d,%d%n",
                                type, n, t, quickTime, mQuick.maxDepth, mQuick.comparisons, mQuick.swaps, mQuick.recursiveCalls);

                        int[] a3 = base.clone();
                        Metrics mSel = new Metrics();
                        int k = a3.length / 2;
                        t0 = System.nanoTime();
                        int result = DeterministicSelector.select(a3, k, mSel);
                        long selTime = System.nanoTime() - t0;
                        int[] check = base.clone();
                        Arrays.sort(check);
                        if (result != check[k]) {
                            throw new IllegalStateException("DeterministicSelector mismatch for n=" + n);
                        }
                        out.printf("DeterministicSelect,%s,%d,%d,%d,%d,%d,%d,%d%n",
                                type, n, t, selTime, mSel.maxDepth, mSel.comparisons, mSel.swaps, mSel.recursiveCalls);
                    }
                }
                System.out.println("Finished sorting/select experiments for input type: " + type);
            }

            for (String type : new String[]{"random", "clustered"}) {
                for (int n : CLOSEST_PAIR_SIZES) {
                    for (int t = 1; t <= TRIALS; t++) {
                        Point[] pts = generatePoints(n, type);
                        Metrics mCp = new Metrics();
                        long t0 = System.nanoTime();
                        ClosestPairSolver.Pair result = ClosestPairSolver.solve(pts, mCp);
                        long cpTime = System.nanoTime() - t0;

                        if (n <= 2000) {
                            ClosestPairSolver.Pair brute = ClosestPairSolver.bruteForceFull(pts);
                            if (Math.abs(brute.distance - result.distance) > 1e-6) {
                                throw new IllegalStateException("ClosestPair mismatch for n=" + n
                                        + " dnc=" + result.distance + " brute=" + brute.distance);
                            }
                        }
                        out.printf("ClosestPair,%s,%d,%d,%d,%d,%d,%d,%d%n",
                                type, n, t, cpTime, mCp.maxDepth, mCp.comparisons, mCp.swaps, mCp.recursiveCalls);
                    }
                }
                System.out.println("Finished ClosestPair experiments for input type: " + type);
            }
        }

        System.out.println("Results written to " + csvPath.toAbsolutePath());
    }

    private static void warmUpJit() {
        System.out.println("Warming up JVM (JIT compilation of hot paths)...");
        for (int i = 0; i < 30; i++) {
            int[] a = generate(20000, "random");
            MergeSorter.sort(a.clone(), new Metrics());
            QuickSorter.sort(a.clone(), new Metrics());
            DeterministicSelector.select(a, a.length / 2, new Metrics());
        }
        for (int i = 0; i < 10; i++) {
            Point[] pts = generatePoints(4000, "random");
            ClosestPairSolver.solve(pts, new Metrics());
        }
        System.out.println("Warm-up complete.\n");
    }

    static int[] generate(int n, String type) {
        int[] a = new int[n];
        switch (type) {
            case "random":
                for (int i = 0; i < n; i++) a[i] = ThreadLocalRandom.current().nextInt(0, n * 10 + 1);
                break;
            case "sorted":
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case "reverse_sorted":
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case "duplicate_heavy":
                for (int i = 0; i < n; i++) a[i] = ThreadLocalRandom.current().nextInt(0, Math.max(2, n / 100));
                break;
            default:
                throw new IllegalArgumentException("Unknown input type: " + type);
        }
        return a;
    }

    static Point[] generatePoints(int n, String type) {
        Point[] pts = new Point[n];
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (type.equals("random")) {
            for (int i = 0; i < n; i++) {
                pts[i] = new Point(r.nextDouble(0, 1_000_000), r.nextDouble(0, 1_000_000));
            }
        } else {
            int clusters = Math.max(1, n / 200);
            for (int i = 0; i < n; i++) {
                int c = r.nextInt(clusters);
                double cx = (c * 9973L % 1_000_000);
                double cy = (c * 7919L % 1_000_000);
                pts[i] = new Point(cx + r.nextDouble(-50, 50), cy + r.nextDouble(-50, 50));
            }
        }
        return pts;
    }

    private static void assertSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                throw new IllegalStateException("Array not sorted at index " + i);
            }
        }
    }
}