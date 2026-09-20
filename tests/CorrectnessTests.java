import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class CorrectnessTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testMergeSort();
        testQuickSort();
        testDeterministicSelect();
        testClosestPair();

        System.out.println();
        System.out.println("=== Summary: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testMergeSort() {
        System.out.println("-- MergeSort --");
        for (int[] a : sortingFixtures()) {
            int[] expected = a.clone();
            Arrays.sort(expected);
            int[] actual = a.clone();
            MergeSorter.sort(actual, new Metrics());
            check("MergeSort matches Arrays.sort for n=" + a.length, Arrays.equals(expected, actual));
        }
    }

    private static void testQuickSort() {
        System.out.println("-- QuickSort --");
        for (int[] a : sortingFixtures()) {
            int[] expected = a.clone();
            Arrays.sort(expected);
            int[] actual = a.clone();
            QuickSorter.sort(actual, new Metrics());
            check("QuickSort matches Arrays.sort for n=" + a.length, Arrays.equals(expected, actual));
        }
    }

    private static int[][] sortingFixtures() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int[] empty = {};
        int[] single = {42};
        int[] two = {5, 1};
        int[] sorted = new int[200];
        for (int i = 0; i < sorted.length; i++) sorted[i] = i;
        int[] reverse = new int[200];
        for (int i = 0; i < reverse.length; i++) reverse[i] = reverse.length - i;
        int[] duplicates = new int[500];
        for (int i = 0; i < duplicates.length; i++) duplicates[i] = r.nextInt(0, 3);
        int[] random = new int[1000];
        for (int i = 0; i < random.length; i++) random[i] = r.nextInt(-10000, 10000);
        int[] allSame = new int[100];
        Arrays.fill(allSame, 7);

        return new int[][]{empty, single, two, sorted, reverse, duplicates, random, allSame};
    }

    private static void testDeterministicSelect() {
        System.out.println("-- DeterministicSelect (>=100 random trials) --");
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int trials = 200;
        boolean allOk = true;
        for (int t = 0; t < trials; t++) {
            int n = r.nextInt(1, 500);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = r.nextInt(-1000, 1000);
            int k = r.nextInt(0, n);

            int[] sortedCopy = a.clone();
            Arrays.sort(sortedCopy);
            int expected = sortedCopy[k];

            int actual = DeterministicSelector.select(a, k, new Metrics());
            if (actual != expected) {
                allOk = false;
                System.out.println("  MISMATCH at trial " + t + " n=" + n + " k=" + k
                        + " expected=" + expected + " actual=" + actual);
            }
        }
        check("DeterministicSelect matches Arrays.sort(a)[k] over " + trials + " random trials", allOk);

        check("select single element", DeterministicSelector.select(new int[]{9}, 0, new Metrics()) == 9);
        int[] dup = {4, 4, 4, 4, 4};
        check("select on all-duplicates", DeterministicSelector.select(dup, 2, new Metrics()) == 4);
    }


    private static void testClosestPair() {
        System.out.println("-- ClosestPair (n <= 2000 vs O(n^2) brute force) --");
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int[] sizes = {2, 3, 5, 20, 100, 500, 1000, 2000};
        boolean allOk = true;
        for (int n : sizes) {
            Point[] pts = new Point[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new Point(r.nextDouble(0, 10000), r.nextDouble(0, 10000));
            }
            ClosestPairSolver.Pair dnc = ClosestPairSolver.solve(pts, new Metrics());
            ClosestPairSolver.Pair brute = ClosestPairSolver.bruteForceFull(pts);
            double diff = Math.abs(dnc.distance - brute.distance);
            if (diff > 1e-6) {
                allOk = false;
                System.out.println("  MISMATCH at n=" + n + " dnc=" + dnc.distance + " brute=" + brute.distance);
            }
        }
        check("ClosestPair D&C matches brute force for all tested sizes", allOk);

        Point[] samePoint = {new Point(1, 1), new Point(1, 1), new Point(5, 5)};
        ClosestPairSolver.Pair res = ClosestPairSolver.solve(samePoint, new Metrics());
        check("ClosestPair handles coincident points (distance 0)", res.distance == 0.0);
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + description);
        } else {
            failed++;
            System.out.println("  [FAIL] " + description);
        }
    }
}