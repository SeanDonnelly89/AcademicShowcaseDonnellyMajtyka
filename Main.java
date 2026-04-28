import java.io.*;
import java.util.*;
 
/**
 * Main entry point for the Transportation Network Resilience project.
 *
 * Runs all four analyses on both the 2-mile and 10-mile METAL graphs:
 *   1. Dijkstra baseline
 *   2. A* comparison
 *   3. Progressive edge-failure simulation
 *   4. Full criticality ranking
 *
 * Usage:
 *   javac src/*.java -d out
 *   java -cp out Main <path-to-2mi.tmg> <path-to-10mi.tmg>
 *
 * Or to run just one graph:
 *   java -cp out Main <path-to-graph.tmg>
 */
public class Main {
 
    public static void main(String[] args) throws IOException {
 
        if (args.length < 1) {
            System.out.println("Usage: java Main <graph.tmg> [graph2.tmg ...]");
            System.exit(1);
        }
 
        for (String tmgFile : args) {
            System.out.println("\n");
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║   TRANSPORTATION NETWORK RESILIENCE ANALYSIS             ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("File: " + tmgFile);
 
            Graph graph = Graph.fromTMG(tmgFile);
            System.out.println("Loaded: " + graph);
            System.out.println();
 
            int V = graph.V();
            // Find a reachable (source, target) pair automatically.
            // Try vertex 0 → V-1, fall back to first reachable pair found.
            int[] pair = findReachablePair(graph, 0, V - 1);
            int source = pair[0];
            int target = pair[1];
 
            // ─── SECTION 1: Dijkstra Baseline ─────────────────────────────────
            printHeader("1. DIJKSTRA BASELINE");
            Dijkstra dijkstra = new Dijkstra(graph);
            PathResult dBaseline = dijkstra.shortestPath(source, target);
            System.out.println("Source : " + graph.getVertex(source));
            System.out.println("Target : " + graph.getVertex(target));
            System.out.println();
            System.out.println(dBaseline);
            printVertexLabels(dBaseline, graph);
 
            // ─── SECTION 2: A* Comparison ─────────────────────────────────────
            printHeader("2. A* SEARCH (GEOGRAPHIC HEURISTIC)");
            AStar astar = new AStar(graph);
            PathResult aBaseline = astar.shortestPath(source, target);
            System.out.println("Source : " + graph.getVertex(source));
            System.out.println("Target : " + graph.getVertex(target));
            System.out.println();
            System.out.println(aBaseline);
            printVertexLabels(aBaseline, graph);
 
            // Comparison summary
            System.out.println();
            System.out.println("── Algorithm Comparison (intact graph) ──────────────────");
            System.out.printf("  Dijkstra : %.3f mi, %d nodes visited%n",
                    dBaseline.distance, dBaseline.nodesVisited);
            System.out.printf("  A*       : %.3f mi, %d nodes visited%n",
                    aBaseline.distance, aBaseline.nodesVisited);
            System.out.printf("  A* efficiency gain: %.1f%% fewer nodes explored%n",
                    100.0 * (dBaseline.nodesVisited - aBaseline.nodesVisited)
                            / Math.max(1, dBaseline.nodesVisited));
 
            // ─── SECTION 3: Edge Failure Simulation ───────────────────────────
            printHeader("3. PROGRESSIVE EDGE FAILURE SIMULATION");
            System.out.println("Removing edges one by one from highest-traffic corridors.");
            System.out.println("Source : " + graph.getVertex(source));
            System.out.println("Target : " + graph.getVertex(target));
            System.out.println();
 
            // Use up to 10 edges for the simulation (avoid enormous output)
            List<Edge> allEdges = graph.allEdges();
            int simCount = Math.min(10, allEdges.size());
            List<Edge> edgesToRemove = new ArrayList<>(allEdges.subList(0, simCount));
 
            FailureSimulation sim = new FailureSimulation(graph);
            List<FailureSimulation.SimResult> simResults =
                    sim.progressiveFailure(edgesToRemove, source, target);
 
            // Summary table
            System.out.println("── Simulation Summary ──────────────────────────────────");
            System.out.printf("%-5s  %-10s  %-12s  %-10s  %-10s  %s%n",
                    "Step", "Road", "Dist(mi)", "Δ Dist", "D-Visited", "Status");
            System.out.println("─".repeat(65));
            for (int i = 0; i < simResults.size(); i++) {
                FailureSimulation.SimResult r = simResults.get(i);
                System.out.printf("%-5d  %-10s  %-12s  %-10s  %-10s  %s%n",
                        i + 1,
                        r.removedEdge.road,
                        r.dijkstraResult.reachable
                                ? String.format("%.3f", r.dijkstraResult.distance) : "N/A",
                        r.causedDisconnection() ? "∞"
                                : String.format("%+.3f", r.distanceDelta),
                        r.dijkstraResult.reachable
                                ? r.dijkstraResult.nodesVisited : "-",
                        r.causedDisconnection() ? "DISCONNECTED" : "OK");
            }
 
            // ─── SECTION 4: Criticality Analysis ──────────────────────────────
            printHeader("4. EDGE CRITICALITY ANALYSIS");
            int sampleSize = Math.min(V, V < 30 ? V : 20); // full sample for small graphs
            System.out.println("Sample size: " + sampleSize + " of " + V + " vertices");
            System.out.println();
 
            CriticalityAnalysis ca = new CriticalityAnalysis(graph);
            List<CriticalityAnalysis.CriticalityResult> ranked = ca.rankEdges(sampleSize);
            CriticalityAnalysis.printTopK(ranked, Math.min(15, ranked.size()), graph);
 
            // Alert on bridge edges (disconnection-causing)
            System.out.println("\n── Bridge Edges (removal disconnects the network) ─────");
            int bridgeCount = 0;
            for (CriticalityAnalysis.CriticalityResult r : ranked) {
                if (r.disconnections > 0) {
                    System.out.printf("  [%s] %s ↔ %s  (disconnects %d pairs)%n",
                            r.edge.road,
                            graph.getVertex(r.edge.u).label,
                            graph.getVertex(r.edge.v).label,
                            r.disconnections);
                    bridgeCount++;
                }
            }
            if (bridgeCount == 0) System.out.println("  None — graph is 2-edge-connected.");
        }
 
        System.out.println("\nAnalysis complete.");
    }
 
    // ─── Formatting helpers ────────────────────────────────────────────────────
 
    private static int[] findReachablePair(Graph graph, int preferSrc, int preferTgt) {
        Dijkstra d = new Dijkstra(graph);
        PathResult r = d.shortestPath(preferSrc, preferTgt);
        if (r.reachable) return new int[]{preferSrc, preferTgt};
        // Fall back: BFS to find any connected component, pick farthest pair
        int V = graph.V();
        for (int s = 0; s < V; s++) {
            double[] dists = d.allDistances(s);
            int farthest = -1;
            double maxD = 0;
            for (int t = 0; t < V; t++) {
                if (t != s && dists[t] != Double.POSITIVE_INFINITY && dists[t] > maxD) {
                    maxD = dists[t];
                    farthest = t;
                }
            }
            if (farthest != -1) return new int[]{s, farthest};
        }
        return new int[]{0, V - 1}; // last resort
    }
 
    private static void printHeader(String title) {
        System.out.println();
        System.out.println("+---------------------------------------------------------+");
        System.out.printf ("|  %-55s  |%n", title);
        System.out.println("+---------------------------------------------------------+");
    }
 
    private static void printVertexLabels(PathResult result, Graph graph) {
        if (!result.reachable) return;
        System.out.print("Labels   : ");
        List<Integer> path = result.path;
        for (int i = 0; i < path.size(); i++) {
            System.out.print(graph.getVertex(path.get(i)).label);
            if (i < path.size() - 1) System.out.print(" → ");
        }
        System.out.println();
    }
}