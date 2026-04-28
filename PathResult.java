import java.util.List;

/**
 * Result returned by Dijkstra or A* after a shortest-path query.
 */
public class PathResult {
    public final List<Integer> path;   // vertex ids in order
    public final double distance;      // total path weight (miles)
    public final int nodesVisited;     // nodes popped from priority queue
    public final boolean reachable;    // false if no path exists

    public PathResult(List<Integer> path, double distance, int nodesVisited) {
        this.path = path;
        this.distance = distance;
        this.nodesVisited = nodesVisited;
        this.reachable = (path != null && !path.isEmpty());
    }

    /** Unreachable sentinel. */
    public static PathResult unreachable() {
        return new PathResult(null, Double.POSITIVE_INFINITY, 0);
    }

    public int hops() {
        return reachable ? path.size() - 1 : -1;
    }

    @Override
    public String toString() {
        if (!reachable) return "No path found (disconnected).";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Distance : %.3f miles%n", distance));
        sb.append(String.format("Hops     : %d%n", hops()));
        sb.append(String.format("Visited  : %d nodes%n", nodesVisited));
        sb.append("Path     : ").append(path.toString());
        return sb.toString();
    }
}