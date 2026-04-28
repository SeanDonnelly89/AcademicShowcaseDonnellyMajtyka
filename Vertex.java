/**
 * Represents a road intersection (node) in the transportation graph.
 * Each vertex has a label (intersection name), latitude, and longitude.
 */
public class Vertex {
    public final int id;
    public final String label;
    public final double lat;
    public final double lon;

    public Vertex(int id, String label, double lat, double lon) {
        this.id = id;
        this.label = label;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Haversine distance in miles to another vertex (used as A* heuristic).
     */
    public double distanceTo(Vertex other) {
        final double R = 3958.8; // Earth radius in miles
        double dLat = Math.toRadians(other.lat - this.lat);
        double dLon = Math.toRadians(other.lon - this.lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public String toString() {
        return "[" + id + "] " + label;
    }
}