/**
 * Represents a road segment (edge) between two intersections.
 * Edges are undirected and weighted by geographic distance.
 */
public class Edge {
    public final int u;          // endpoint vertex id
    public final int v;          // endpoint vertex id
    public final String road;    // road label (e.g. "US9", "NY155")
    public final double weight;  // distance in miles

    public Edge(int u, int v, String road, double weight) {
        this.u = u;
        this.v = v;
        this.road = road;
        this.weight = weight;
    }

    /** Returns the other endpoint given one endpoint. */
    public int other(int vertex) {
        if (vertex == u) return v;
        if (vertex == v) return u;
        throw new IllegalArgumentException("Vertex " + vertex + " not on this edge.");
    }

    @Override
    public String toString() {
        return u + " <--[" + road + " / " + String.format("%.3f mi", weight) + "--> " + v;
    }
}