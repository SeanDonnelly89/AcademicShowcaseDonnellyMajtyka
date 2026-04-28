import java.io.*;
import java.util.*;
 
/**
 * Weighted undirected graph of road intersections.
 * Parses TMG 1.0 collapsed format files.
 * Supports dynamic edge removal for failure simulations.
 */
public class Graph {
 
    private final int V;                         // number of vertices
    private final Vertex[] vertices;             // vertex array indexed by id
    private final List<Edge>[] adj;              // adjacency lists
    private final Set<Edge> removedEdges;        // edges currently removed
 
    @SuppressWarnings("unchecked")
    public Graph(int numVertices) {
        this.V = numVertices;
        this.vertices = new Vertex[V];
        this.adj = new ArrayList[V];
        for (int i = 0; i < V; i++) adj[i] = new ArrayList<>();
        this.removedEdges = new HashSet<>();
    }
 
    // ─── Construction ──────────────────────────────────────────────────────────
 
    public void setVertex(int id, String label, double lat, double lon) {
        vertices[id] = new Vertex(id, label, lat, lon);
    }
 
    public void addEdge(Edge e) {
        adj[e.u].add(e);
        adj[e.v].add(e);
    }
 
    // ─── TMG Parser ────────────────────────────────────────────────────────────
 
    /**
     * Parses a TMG 1.0 collapsed file and returns a Graph.
     *
     * Format:
     *   TMG 1.0 collapsed
     *   <V> <E>
     *   <label> <lat> <lon>   (V lines)
     *   <u> <v> <road> [lat lon]?  (E lines — optional waypoint coords ignored)
     */
    public static Graph fromTMG(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
 
        // Header
        String header = br.readLine().trim();
        if (!header.startsWith("TMG 1.0")) {
            throw new IOException("Not a valid TMG file: " + filename);
        }
 
        // V E
        StringTokenizer st = new StringTokenizer(br.readLine());
        int V = Integer.parseInt(st.nextToken());
        int E = Integer.parseInt(st.nextToken());
 
        Graph g = new Graph(V);
 
        // Vertex lines
        for (int i = 0; i < V; i++) {
            st = new StringTokenizer(br.readLine());
            String label = st.nextToken();
            double lat   = Double.parseDouble(st.nextToken());
            double lon   = Double.parseDouble(st.nextToken());
            g.setVertex(i, label, lat, lon);
        }
 
        // Edge lines
        for (int i = 0; i < E; i++) {
            String line = br.readLine();
            if (line == null) break;
            st = new StringTokenizer(line);
            int u       = Integer.parseInt(st.nextToken());
            int v       = Integer.parseInt(st.nextToken());
            String road = st.nextToken();
            // optional waypoint lat/lon — ignored for weight, use endpoint distance
            double w = g.vertices[u].distanceTo(g.vertices[v]);
            g.addEdge(new Edge(u, v, road, w));
        }
 
        br.close();
        return g;
    }
 
    // ─── Edge Removal (for failure simulation) ─────────────────────────────────
 
    public void removeEdge(Edge e)  { removedEdges.add(e); }
    public void restoreEdge(Edge e) { removedEdges.remove(e); }
    public void restoreAllEdges()   { removedEdges.clear(); }
    public boolean isActive(Edge e) { return !removedEdges.contains(e); }
 
    // ─── Accessors ─────────────────────────────────────────────────────────────
 
    public int V()                          { return V; }
    public Vertex getVertex(int id)         { return vertices[id]; }
    public Vertex[] getVertices()           { return vertices; }
 
    /** Active (non-removed) neighbours of vertex v. */
    public List<Edge> activeAdj(int v) {
        List<Edge> active = new ArrayList<>();
        for (Edge e : adj[v]) {
            if (isActive(e)) active.add(e);
        }
        return active;
    }
 
    /** All edges in the graph (including removed ones). */
    public List<Edge> allEdges() {
        Set<Edge> seen = new HashSet<>();
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < V; i++) {
            for (Edge e : adj[i]) {
                if (seen.add(e)) edges.add(e);
            }
        }
        return edges;
    }
 
    /** All currently active edges. */
    public List<Edge> activeEdges() {
        List<Edge> result = new ArrayList<>();
        for (Edge e : allEdges()) {
            if (isActive(e)) result.add(e);
        }
        return result;
    }
 
    public int numActiveEdges() { return activeEdges().size(); }
 
    @Override
    public String toString() {
        return "Graph(" + V + " vertices, " + allEdges().size() + " edges)";
    }
}