import java.util.*;

/**
 * Ranks every edge in the road network by its structural importance.
 *
 * CRITICALITY SCORE for edge e is computed as:
 *
 *   score(e) = Σ over sampled (s,t) pairs:
 *                 [ dist_without_e(s,t) - dist_baseline(s,t) ]
 *
 * Where dist = ∞ if the pair becomes disconnected (penalised heavily).
 *
 * This is an approximation of edge betweenness centrality — edges
 * that appear on many shortest paths and whose removal forces large
 * detours (or total disconnection) receive high scores.
 *
 * Full O(V² · E) exact computation is expensive for large graphs;
 * we use a configurable sample of source vertices for scalability.
 */
public class CriticalityAnalysis {

    private final Graph    graph;
    private final Dijkstra dijkstra;

    // Penalty added per pair when removal causes disconnection
    private static final double DISCONNECTION_PENALTY = 1000.0;

    public CriticalityAnalysis(Graph graph) {
        this.graph    = graph;
        this.dijkstra = new Dijkstra(graph);
    }

    // ─── Main analysis ─────────────────────────────────────────────────────────

    /**
     * Ranks all edges by criticality.
     *
     * @param sampleSize number of source vertices to sample (use V for exact)
     * @return list of CriticalityResult sorted descending by score
     */
    public List<CriticalityResult> rankEdges(int sampleSize) {
        List<Edge> edges = graph.allEdges();
        int V = graph.V();

        // Sample source vertices (evenly spaced if sampleSize < V)
        List<Integer> sources = sampleSources(V, sampleSize);

        // Baseline: full distance tables from each sampled source
        System.out.println("Computing baseline distances from "
                + sources.size() + " source vertices...");
        double[][] baseline = new double[sources.size()][];
        for (int i = 0; i < sources.size(); i++) {
            baseline[i] = dijkstra.allDistances(sources.get(i));
        }

        // Score each edge
        System.out.println("Scoring " + edges.size() + " edges...");
        List<CriticalityResult> results = new ArrayList<>();

        for (int ei = 0; ei < edges.size(); ei++) {
            Edge e = edges.get(ei);
            graph.removeEdge(e);

            double totalPenalty  = 0.0;
            int    disconnections = 0;

            for (int i = 0; i < sources.size(); i++) {
                double[] afterDist = dijkstra.allDistances(sources.get(i));
                for (int t = 0; t < V; t++) {
                    if (t == sources.get(i)) continue;
                    double before = baseline[i][t];
                    double after  = afterDist[t];
                    if (before == Double.POSITIVE_INFINITY) continue; // unreachable in baseline too
                    if (after == Double.POSITIVE_INFINITY) {
                        totalPenalty += DISCONNECTION_PENALTY;
                        disconnections++;
                    } else {
                        totalPenalty += (after - before);
                    }
                }
            }

            graph.restoreEdge(e);
            results.add(new CriticalityResult(e, totalPenalty, disconnections));

            if ((ei + 1) % 50 == 0 || ei == edges.size() - 1) {
                System.out.printf("  Scored %d / %d edges...%n", ei + 1, edges.size());
            }
        }

        // Sort descending by score
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results;
    }

    // ─── Report printer ────────────────────────────────────────────────────────

    /**
     * Prints the top-K most critical edges with full details.
     */
    public static void printTopK(List<CriticalityResult> results, int k, Graph graph) {
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("  TOP " + k + " MOST CRITICAL ROADS");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.printf("%-4s  %-10s  %-30s  %-30s  %-12s  %-14s  %s%n",
                "Rank", "Road", "From", "To", "Length(mi)", "Score", "Disconnections");
        System.out.println("─".repeat(115));

        int rank = 1;
        for (CriticalityResult r : results) {
            if (rank > k) break;
            Vertex u = graph.getVertex(r.edge.u);
            Vertex v = graph.getVertex(r.edge.v);
            System.out.printf("%-4d  %-10s  %-30s  %-30s  %-12.3f  %-14.2f  %d%n",
                    rank++,
                    r.edge.road,
                    truncate(u.label, 30),
                    truncate(v.label, 30),
                    r.edge.weight,
                    r.score,
                    r.disconnections);
        }
        System.out.println("═══════════════════════════════════════════════════");
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private List<Integer> sampleSources(int V, int sampleSize) {
        if (sampleSize >= V) {
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < V; i++) all.add(i);
            return all;
        }
        List<Integer> sample = new ArrayList<>();
        double step = (double) V / sampleSize;
        for (int i = 0; i < sampleSize; i++) {
            sample.add((int) (i * step));
        }
        return sample;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ─── Inner result class ────────────────────────────────────────────────────

    public static class CriticalityResult {
        public final Edge   edge;
        public final double score;          // total extra miles imposed across all pairs
        public final int    disconnections; // number of pairs disconnected

        public CriticalityResult(Edge edge, double score, int disconnections) {
            this.edge           = edge;
            this.score          = score;
            this.disconnections = disconnections;
        }
    }
}