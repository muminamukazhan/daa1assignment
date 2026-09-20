import java.util.concurrent.ThreadLocalRandom;

public final class QuickSorter {

    public static final int CUTOFF = 16;

    private QuickSorter() { }

    public static void sort(int[] a, Metrics m) {
        sort(a, 0, a.length - 1, m);
    }

    private static void sort(int[] a, int lo, int hiIn, Metrics m) {
        int hi = hiIn;
        while (lo < hi) {
            m.enter();
            try {
                if (hi - lo + 1 <= CUTOFF) {
                    insertionSort(a, lo, hi, m);
                    return;
                }

                int p = partition(a, lo, hi, m);

                int leftSize = p - lo;
                int rightSize = hi - p - 1;

                if (leftSize < rightSize) {
                    sort(a, lo, p - 1, m);
                    lo = p + 1;
                } else {
                    sort(a, p + 1, hi, m);
                    hi = p - 1;
                }
            } finally {
                m.exit();
            }
        }
    }

    private static int partition(int[] a, int lo, int hi, Metrics m) {
        int pivotIndex = lo + ThreadLocalRandom.current().nextInt(hi - lo + 1);
        swap(a, pivotIndex, hi, m);
        int pivot = a[hi];

        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            m.comparisons++;
            if (a[j] < pivot) {
                i++;
                swap(a, i, j, m);
            }
        }
        swap(a, i + 1, hi, m);
        return i + 1;
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        m.swaps++;
    }

    private static void insertionSort(int[] a, int lo, int hi, Metrics m) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= lo) {
                m.comparisons++;
                if (a[j] <= key) break;
                a[j + 1] = a[j];
                m.swaps++;
                j--;
            }
            a[j + 1] = key;
        }
    }
}