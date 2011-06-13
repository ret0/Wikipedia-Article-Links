package wikipedia.network;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

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

    public Map<Integer, Integer> getEdgesAsIndices() {
        Map<Integer, Integer> allEdgesIndexBased = Maps.newHashMap();
        for (GraphEdge edge : allEdges) {
            allEdgesIndexBased.put(nameIndexMap.get(edge.getFrom()), nameIndexMap.get(edge.getTo()));
        }
        return allEdgesIndexBased;
    }

}
