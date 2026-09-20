import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class Main {

    public static void main(String[] args) {
        System.out.println("=== Assignment 1: Divide-and-Conquer Algorithm Demo ===\n");

        demoMergeSort();
        demoQuickSort();
        demoDeterministicSelect();
        demoClosestPair();

        System.out.println("\nTo run the full correctness test suite:");
        System.out.println("    java -cp out CorrectnessTests");
        System.out.println("To run the full timed experiment suite (writes results/results.csv):");
        System.out.println("    java -cp out Experiment");
    }

    private static void demoMergeSort() {
        System.out.println("-- MergeSort demo --");
        int[] a = randomArray(20);
        System.out.println("Input : " + Arrays.toString(a));
        Metrics m = new Metrics();
        MergeSorter.sort(a, m);
        System.out.println("Output: " + Arrays.toString(a));
        System.out.println("Metrics: " + m + "\n");
    }

    private static void demoQuickSort() {
        System.out.println("-- QuickSort demo --");
        int[] a = randomArray(20);
        System.out.println("Input : " + Arrays.toString(a));
        Metrics m = new Metrics();
        QuickSorter.sort(a, m);
        System.out.println("Output: " + Arrays.toString(a));
        System.out.println("Metrics: " + m + "\n");
    }

    private static void demoDeterministicSelect() {
        System.out.println("-- DeterministicSelect (Median-of-Medians) demo --");
        int[] a = randomArray(21);
        int k = a.length / 2;
        System.out.println("Input : " + Arrays.toString(a));
        Metrics m = new Metrics();
        int result = DeterministicSelector.select(a, k, m);
        int[] check = a.clone();
        Arrays.sort(check);
        System.out.println("k=" + k + " -> " + result + " (expected median " + check[k] + ")");
        System.out.println("Metrics: " + m + "\n");
    }

    private static void demoClosestPair() {
        System.out.println("-- ClosestPair demo --");
        Point[] pts = new Point[15];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new Point(ThreadLocalRandom.current().nextDouble(0, 100),
                    ThreadLocalRandom.current().nextDouble(0, 100));
        }
        Metrics m = new Metrics();
        ClosestPairSolver.Pair result = ClosestPairSolver.solve(pts, m);
        ClosestPairSolver.Pair brute = ClosestPairSolver.bruteForceFull(pts);
        System.out.println("Closest pair (D&C): " + result.a + " - " + result.b
                + " dist=" + String.format("%.4f", result.distance));
        System.out.println("Closest pair (brute-force check): dist=" + String.format("%.4f", brute.distance));
        System.out.println("Metrics: " + m + "\n");
    }

    private static int[] randomArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = ThreadLocalRandom.current().nextInt(-100, 100);
        return a;
    }
}