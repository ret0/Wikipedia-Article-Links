package wikipedia.network;

import java.util.List;
import java.util.Map;

public class TimeFrameGraph {

    private final Map<String, Integer> nameIndexMap;
    private final List<GraphEdge> allEdges;

    public TimeFrameGraph(final Map<String, Integer> nameIndexMap, final List<GraphEdge> allEdges) {
        this.nameIndexMap = nameIndexMap;
        this.allEdges = allEdges;
    }

    public List<GraphEdge> getAllEdges() {
        return allEdges;
    }

    public Map<String, Integer> getNameIndexMap() {
        return nameIndexMap;
    }

}