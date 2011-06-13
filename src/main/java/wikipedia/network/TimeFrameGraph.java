package wikipedia.network;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class TimeFrameGraph {

    private final Map<String, Integer> nameIndexMap;
    private final List<GraphEdge> allEdges;

    public TimeFrameGraph(final Map<String, Integer> nameIndexMap, final List<GraphEdge> allEdges) {
        this.nameIndexMap = nameIndexMap;
        this.allEdges = allEdges;
    }

    public static String generateTimeFrameInformation(final List<TimeFrameGraph> allFrameGraphs) {
        StringBuilder jsonOutput = new StringBuilder();
        jsonOutput.append(printInitialGraph(allFrameGraphs.get(0)));
        jsonOutput.append(printAllDeltaGraphs());
        return jsonOutput.toString();
    }

    private static String printAllDeltaGraphs() {
        // TODO Auto-generated method stub
        return "";

    }

    private static String printInitialGraph(final TimeFrameGraph timeFrameGraph) {
        return printCompleteGraphAsJSON(timeFrameGraph);
    }

    private static String printCompleteGraphAsJSON(final TimeFrameGraph timeFrameGraph) {
        Gson serializer = new Gson();
        return serializer.toJson(timeFrameGraph);
    }

}
