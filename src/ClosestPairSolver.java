import java.util.Arrays;
import java.util.Comparator;

public final class ClosestPairSolver {

    private ClosestPairSolver() { }

    public static final class Pair {
        public final Point a, b;
        public final double distance;
        public Pair(Point a, Point b, double distance) {
            this.a = a; this.b = b; this.distance = distance;
        }
    }

    public static Pair solve(Point[] points, Metrics m) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Need at least 2 points");
        }
        Point[] byX = points.clone();
        Arrays.sort(byX, Comparator.comparingDouble(p -> p.x));
        Point[] byY = byX.clone();

        return closestRec(byX, byY, 0, byX.length - 1, m);
    }

    private static Pair closestRec(Point[] byX, Point[] byY, int lo, int hi, Metrics m) {
        m.enter();
        try {
            int n = hi - lo + 1;
            if (n <= 3) {
                Pair best = bruteForce(byX, lo, hi, m);
                Point[] slice = Arrays.copyOfRange(byX, lo, hi + 1);
                Arrays.sort(slice, Comparator.comparingDouble(p -> p.y));
                System.arraycopy(slice, 0, byY, lo, slice.length);
                return best;
            }

            int mid = lo + (hi - lo) / 2;
            double midX = byX[mid].x;

            Point[] leftY = new Point[mid - lo + 1];
            Point[] rightY = new Point[hi - mid];

            Pair leftBest = closestRec(byX, byY, lo, mid, m);
            System.arraycopy(byY, lo, leftY, 0, leftY.length);
            Pair rightBest = closestRec(byX, byY, mid + 1, hi, m);
            System.arraycopy(byY, mid + 1, rightY, 0, rightY.length);
            mergeByY(leftY, rightY, byY, lo);

            m.comparisons++;
            Pair best = (leftBest.distance <= rightBest.distance) ? leftBest : rightBest;
            double d = best.distance;
            Point[] strip = new Point[hi - lo + 1];
            int stripCount = 0;
            for (int i = lo; i <= hi; i++) {
                m.comparisons++;
                if (Math.abs(byY[i].x - midX) < d) {
                    strip[stripCount++] = byY[i];
                }
            }

            for (int i = 0; i < stripCount; i++) {
                for (int j = i + 1; j < stripCount && (strip[j].y - strip[i].y) < d; j++) {
                    m.comparisons++;
                    double dist = strip[i].dist(strip[j]);
                    m.comparisons++;
                    if (dist < d) {
                        d = dist;
                        best = new Pair(strip[i], strip[j], dist);
                    }
                }
            }
            return best;
        } finally {
            m.exit();
        }
    }

    private static void mergeByY(Point[] left, Point[] right, Point[] dest, int destLo) {
        int i = 0, j = 0, k = destLo;
        while (i < left.length && j < right.length) {
            if (left[i].y <= right[j].y) dest[k++] = left[i++];
            else dest[k++] = right[j++];
        }
        while (i < left.length) dest[k++] = left[i++];
        while (j < right.length) dest[k++] = right[j++];
    }

    private static Pair bruteForce(Point[] pts, int lo, int hi, Metrics m) {
        Pair best = null;
        for (int i = lo; i <= hi; i++) {
            for (int j = i + 1; j <= hi; j++) {
                m.comparisons++;
                double d = pts[i].dist(pts[j]);
                if (best == null || d < best.distance) {
                    best = new Pair(pts[i], pts[j], d);
                }
            }
        }
        return best;
    }

    public static Pair bruteForceFull(Point[] points) {
        Pair best = null;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double d = points[i].dist(points[j]);
                if (best == null || d < best.distance) {
                    best = new Pair(points[i], points[j], d);
                }
            }
        }
        return best;
    }
}