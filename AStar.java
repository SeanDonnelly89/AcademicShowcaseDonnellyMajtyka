import java.util.*;

/**
 * A* shortest-path algorithm with a geographic (straight-line / Haversine)
 * heuristic.
 *
 * The heuristic h(v) = straight-line distance from v to the target.
 * Because road distances are always >= straight-line distances, h is
 * admissible and A* is guaranteed to find the optimal path.
 *
 * When edges are removed (simulating failures), the heuristic can become
 * misleading — guiding the algorithm toward blocked routes — which inflates
 * the nodes-visited count relative to Dijkstra. This degradation is a key
 * part of our analysis.
 */
public class AStar {

    private final Graph graph;

    public AStar(Graph graph) {
        this.graph = graph;
    }

    /**
     * Finds the shortest path from source to target using A*.
     *
     * @param source  starting vertex id
     * @param target  destination vertex id
     * @return PathResult with path, distance, and nodes visited count
     */
    public PathResult shortestPath(int source, int target) {
        int V = graph.V();
        double[] gScore  = new double[V];   // actual cost from source
        double[] fScore  = new double[V];   // gScore + heuristic
        int[]    prev    = new int[V];
        boolean[] closed = new boolean[V];

        Arrays.fill(gScore, Double.POSITIVE_INFINITY);
        Arrays.fill(fScore, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);

        Vertex targetVertex = graph.getVertex(target);
        gScore[source] = 0.0;
        fScore[source] = heuristic(source, targetVertex);

        // PQ entries: [fScore, vertexId]
        PriorityQueue<double[]> openSet =
                new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        openSet.offer(new double[]{fScore[source], source});

        int nodesVisited = 0;

        while (!openSet.isEmpty()) {
            double[] top = openSet.poll();
            int u = (int) top[1];

            if (closed[u]) continue;
            closed[u] = true;
            nodesVisited++;

            if (u == target) break;

            for (Edge e : graph.activeAdj(u)) {
                int    nb       = e.other(u);
                if (closed[nb]) continue;

                double tentative = gScore[u] + e.weight;
                if (tentative < gScore[nb]) {
                    gScore[nb] = tentative;
                    fScore[nb] = tentative + heuristic(nb, targetVertex);
                    prev[nb]   = u;
                    openSet.offer(new double[]{fScore[nb], nb});
                }
            }
        }

        if (gScore[target] == Double.POSITIVE_INFINITY) {
            return PathResult.unreachable();
        }

        return new PathResult(reconstructPath(prev, source, target),
                              gScore[target], nodesVisited);
    }

    // ─── Heuristic ─────────────────────────────────────────────────────────────

    /**
     * Haversine straight-line distance from vertex v to the target.
     * This is admissible (never overestimates) because road distance >= straight-line.
     */
    private double heuristic(int v, Vertex target) {
        return graph.getVertex(v).distanceTo(target);
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