public final class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distSq(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    public double dist(Point other) {
        return Math.sqrt(distSq(other));
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", x, y);
    }
}