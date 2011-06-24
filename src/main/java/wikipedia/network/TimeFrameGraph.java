package wikipedia.network;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Encapsulates the state of the graph at a given frame
 */
public final class TimeFrameGraph {

    private final Map<String, Integer> nameIndexMap;
    private final List<GraphEdge> allEdges;
    private final DateTime timeOfGraph;

    public TimeFrameGraph(final Map<String, Integer> nameIndexMap,
                          final List<GraphEdge> allEdges,
                          final DateTime timeOfGraph) {
        this.nameIndexMap = nameIndexMap;
        this.allEdges = allEdges;
        this.timeOfGraph = timeOfGraph;
    }

    public List<GraphEdge> getAllEdges() {
        return allEdges;
    }

    public Map<String, Integer> getNameIndexMap() {
        return nameIndexMap;
    }

    public String getFormatedDate() {
        return timeOfGraph.toString(DateTimeFormat.shortDate());
    }

}
