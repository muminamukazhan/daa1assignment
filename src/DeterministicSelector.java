public final class DeterministicSelector {

    private static final int GROUP_SIZE = 5;

    private DeterministicSelector() { }

    public static int select(int[] input, int k, Metrics m) {
        int[] a = input.clone();
        return selectInPlace(a, 0, a.length - 1, k, m);
    }

    private static int selectInPlace(int[] a, int lo, int hi, int k, Metrics m) {
        m.enter();
        try {
            while (true) {
                if (lo == hi) return a[lo];

                int n = hi - lo + 1;
                if (n <= GROUP_SIZE) {
                    insertionSort(a, lo, hi, m);
                    return a[lo + k];
                }

                int pivot = medianOfMedians(a, lo, hi, m);
                int pivotIndex = partitionAroundValue(a, lo, hi, pivot, m);
                int rank = pivotIndex - lo;

                m.comparisons++;
                if (k == rank) {
                    return a[pivotIndex];
                } else if (k < rank) {
                    hi = pivotIndex - 1;
                } else {
                    k = k - rank - 1;
                    lo = pivotIndex + 1;
                }
            }
        } finally {
            m.exit();
        }
    }

    private static int medianOfMedians(int[] a, int lo, int hi, Metrics m) {
        int n = hi - lo + 1;
        int numGroups = (n + GROUP_SIZE - 1) / GROUP_SIZE;
        int[] medians = new int[numGroups];

        for (int g = 0; g < numGroups; g++) {
            int groupLo = lo + g * GROUP_SIZE;
            int groupHi = Math.min(groupLo + GROUP_SIZE - 1, hi);
            insertionSort(a, groupLo, groupHi, m);
            int mid = groupLo + (groupHi - groupLo) / 2;
            medians[g] = a[mid];
        }

        if (numGroups == 1) return medians[0];
        return selectInPlace(medians, 0, numGroups - 1, (numGroups - 1) / 2, m);
    }

    private static int partitionAroundValue(int[] a, int lo, int hi, int pivotValue, Metrics m) {
        int pivotIndex = -1;
        for (int i = lo; i <= hi; i++) {
            m.comparisons++;
            if (a[i] == pivotValue) {
                pivotIndex = i;
                break;
            }
        }
        swap(a, pivotIndex, hi, m);

        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            m.comparisons++;
            if (a[j] < pivotValue) {
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