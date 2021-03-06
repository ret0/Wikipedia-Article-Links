package wikipedia.network;

/**
 * String based Graph Edge
 */
public final class GraphEdge {

    private static final int DEFAULT_WEIGHT = 1;

    private final String from;
    private final String to;
    private final int edgeWeight;


    public GraphEdge(final String from, final String to, final int edgeWeight) {
        this.from = from;
        this.to = to;
        this.edgeWeight = edgeWeight;
    }

    public GraphEdge(final String from, final String to) {
        this(from, to, DEFAULT_WEIGHT);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getEdgeWeight() {
        return edgeWeight;
    }

    @Override
    public String toString() {
        return "GraphEdge [from=" + from + ", to=" + to + ", edgeWeight=" + edgeWeight + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GraphEdge other = (GraphEdge) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }


}
