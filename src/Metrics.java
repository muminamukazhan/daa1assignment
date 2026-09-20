public final class Metrics {
    public long comparisons = 0;
    public long swaps = 0;
    public long recursiveCalls = 0;
    public int maxDepth = 0;
    private int currentDepth = 0;

    public void enter() {
        currentDepth++;
        recursiveCalls++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    public void exit() {
        currentDepth--;
    }

    public void reset() {
        comparisons = 0;
        swaps = 0;
        recursiveCalls = 0;
        maxDepth = 0;
        currentDepth = 0;
    }

    @Override
    public String toString() {
        return String.format("comparisons=%d, swaps=%d, calls=%d, maxDepth=%d",
                comparisons, swaps, recursiveCalls, maxDepth);
    }
}