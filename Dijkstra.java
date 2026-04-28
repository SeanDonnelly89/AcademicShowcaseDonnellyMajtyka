import java.util.*;
 
/**
 * Dijkstra's shortest-path algorithm on a weighted road graph.
 *
 * Time complexity:  O((V + E) log V) with a binary heap priority queue.
 * Space complexity: O(V)
 *
 * Operates on the graph's currently active edges, so edge removals
 * are automatically respected.
 */
public class Dijkstra {
 
    private final Graph graph;
 
    public Dijkstra(Graph graph) {
        this.graph = graph;
    }
 
    /**
     * Finds the shortest path from source to target.
     *
     * @param source  starting vertex id
     * @param target  destination vertex id
     * @return PathResult with path, distance, and nodes visited count
     */
    public PathResult shortestPath(int source, int target) {
        int V = graph.V();
        double[] dist   = new double[V];
        int[]    prev   = new int[V];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        dist[source] = 0.0;
 
        // PQ entries: [distance, vertexId]
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0.0, source});
 
        int nodesVisited = 0;
 
        while (!pq.isEmpty()) {
            double[] top = pq.poll();
            double d  = top[0];
            int    u  = (int) top[1];
 
            if (d > dist[u]) continue; // stale entry
            nodesVisited++;
 
            if (u == target) break;    // found shortest path to target
 
            for (Edge e : graph.activeAdj(u)) {
                int    nb    = e.other(u);
                double newDist = dist[u] + e.weight;
                if (newDist < dist[nb]) {
                    dist[nb] = newDist;
                    prev[nb] = u;
                    pq.offer(new double[]{newDist, nb});
                }
            }
        }
 
        if (dist[target] == Double.POSITIVE_INFINITY) {
            return PathResult.unreachable();
        }
 
        return new PathResult(reconstructPath(prev, source, target), dist[target], nodesVisited);
    }
 
    /**
     * Runs Dijkstra from source to ALL vertices.
     * Useful for computing full distance tables.
     *
     * @return dist[] array indexed by vertex id
     */
    public double[] allDistances(int source) {
        int V = graph.V();
        double[] dist = new double[V];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[source] = 0.0;
 
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0.0, source});
 
        while (!pq.isEmpty()) {
            double[] top = pq.poll();
            double d = top[0];
            int    u = (int) top[1];
            if (d > dist[u]) continue;
 
            for (Edge e : graph.activeAdj(u)) {
                int    nb     = e.other(u);
                double newDist = dist[u] + e.weight;
                if (newDist < dist[nb]) {
                    dist[nb] = newDist;
                    pq.offer(new double[]{newDist, nb});
                }
            }
        }
        return dist;
    }
 
    // ─── Private helpers ───────────────────────────────────────────────────────
 
    private List<Integer> reconstructPath(int[] prev, int source, int target) {
        LinkedList<Integer> path = new LinkedList<>();
        for (int v = target; v != -1; v = prev[v]) {
            path.addFirst(v);
            if (v == source) break;
        }
        return path;
    }
}
 
