import java.util.*;

/**
 * Simulates progressive edge failures in the road network and measures
 * the impact on routing efficiency and connectivity.
 *
 * For each simulated failure:
 *   - Removes the edge from the graph
 *   - Re-runs shortest path between the chosen source/target pair
 *   - Records: new distance, path length change, nodes visited change,
 *     and whether the network became disconnected
 */
public class FailureSimulation {

    private final Graph    graph;
    private final Dijkstra dijkstra;
    private final AStar    astar;

    public FailureSimulation(Graph graph) {
        this.graph    = graph;
        this.dijkstra = new Dijkstra(graph);
        this.astar    = new AStar(graph);
    }

    // ─── Single-edge impact ────────────────────────────────────────────────────

    /**
     * Measures the effect of removing one edge on the source→target path.
     *
     * @param edge   the edge to remove
     * @param source source vertex
     * @param target target vertex
     * @return SimResult describing the impact
     */
    public SimResult measureImpact(Edge edge, int source, int target,
                                   PathResult baseline) {
        graph.removeEdge(edge);

        PathResult afterDijk  = dijkstra.shortestPath(source, target);
        PathResult afterAstar = astar.shortestPath(source, target);

        graph.restoreEdge(edge);

        double distDelta = afterDijk.reachable
                ? afterDijk.distance - baseline.distance
                : Double.POSITIVE_INFINITY;

        return new SimResult(edge, baseline, afterDijk, afterAstar, distDelta);
    }

    // ─── Progressive failure sequence ─────────────────────────────────────────

    /**
     * Progressively removes edges one by one (in the provided order),
     * recording routing stats after each removal. Edges are NOT restored
     * between steps — this models cumulative infrastructure degradation.
     *
     * @param edgesToRemove ordered list of edges to remove
     * @param source        source vertex for routing queries
     * @param target        target vertex for routing queries
     * @return list of SimResults, one per removal step
     */
    public List<SimResult> progressiveFailure(List<Edge> edgesToRemove,
                                               int source, int target) {
        graph.restoreAllEdges();
        List<SimResult> results = new ArrayList<>();

        PathResult baseline = dijkstra.shortestPath(source, target);
        System.out.printf("Baseline  : %s%n%n", baseline);

        for (int step = 0; step < edgesToRemove.size(); step++) {
            Edge e = edgesToRemove.get(step);
            graph.removeEdge(e);

            PathResult dijk  = dijkstra.shortestPath(source, target);
            PathResult astr  = astar.shortestPath(source, target);
            double distDelta = dijk.reachable
                    ? dijk.distance - baseline.distance
                    : Double.POSITIVE_INFINITY;

            SimResult r = new SimResult(e, baseline, dijk, astr, distDelta);
            results.add(r);

            System.out.printf("Step %2d — Removed edge %s%n", step + 1, e);
            System.out.printf("  Dijkstra : %s%n", dijk.reachable
                    ? String.format("%.3f mi (%+.3f), %d hops, %d visited",
                            dijk.distance, distDelta, dijk.hops(), dijk.nodesVisited)
                    : "DISCONNECTED");
            System.out.printf("  A*       : %s%n%n", astr.reachable
                    ? String.format("%.3f mi, %d hops, %d visited",
                            astr.distance, astr.hops(), astr.nodesVisited)
                    : "DISCONNECTED");

            // Stop if target is now completely unreachable
            if (!dijk.reachable) {
                System.out.println("  *** Network fully disconnected between "
                        + source + " and " + target + " ***");
                break;
            }
        }

        graph.restoreAllEdges();
        return results;
    }

    // ─── Inner result class ────────────────────────────────────────────────────

    public static class SimResult {
        public final Edge       removedEdge;
        public final PathResult baseline;
        public final PathResult dijkstraResult;
        public final PathResult astarResult;
        public final double     distanceDelta;   // positive = longer, Inf = disconnected

        public SimResult(Edge removedEdge, PathResult baseline,
                         PathResult dijkstraResult, PathResult astarResult,
                         double distanceDelta) {
            this.removedEdge    = removedEdge;
            this.baseline       = baseline;
            this.dijkstraResult = dijkstraResult;
            this.astarResult    = astarResult;
            this.distanceDelta  = distanceDelta;
        }

        public boolean causedDisconnection() {
            return !dijkstraResult.reachable;
        }
    }
}

