package wikipedia.analysis.pagenetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import wikipedia.http.PageHistoryFetcher;
import wikipedia.network.GraphEdge;
import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DeltaPrinter {

    private static final int NUM_REVISIONS = 5;

    public static void main(final String[] args) throws IOException {
        List<DateTime> allTimeFrames = PageHistoryFetcher.
        getAllDatesForHistory(NUM_REVISIONS, PageHistoryFetcher.MOST_RECENT_DATE.toDateTime());

        final List<String> categories = CategoryLists.ENGLISH_MUSIC;
        Map<DateMidnight, TimeFrameGraph> dateGraphMap = Maps.newHashMap();

        for (DateTime dateTime : allTimeFrames) {
            DateMidnight dateMidnight = dateTime.toDateMidnight();
            dateGraphMap.put(dateMidnight, new NetworkBuilder(categories, "en", dateMidnight).getGraphAtDate());
        }

        String completeJSONForPage = generateTimeFrameInformation(Lists.newArrayList(dateGraphMap.values()));
        FileUtils.write(new File("out/initialGraph.js"), completeJSONForPage);
    }


    public static String generateTimeFrameInformation(final List<TimeFrameGraph> allFrameGraphs) {
        StringBuilder jsonOutput = new StringBuilder();
        jsonOutput.append(printInitialGraph(allFrameGraphs.get(0)));
        jsonOutput.append(printAllDeltaGraphs(allFrameGraphs));
        return jsonOutput.toString();
    }

    private static String printAllDeltaGraphs(final List<TimeFrameGraph> allFrameGraphs) {
        List<GraphDelta> allDeltas = prepareGraphDeltas(allFrameGraphs);
        StringBuilder allDeltasJson = new StringBuilder();
        List<String> deltaElements = Lists.newArrayList();
        allDeltasJson.append("var frameInformation = {");
        int deltaCounter = 1;
        for (GraphDelta graphDelta : allDeltas) {
            deltaElements.add(deltaCounter++ + " : " + graphDelta.toJSON());
        }
        allDeltasJson.append(StringUtils.join(deltaElements, ", \n"));
        allDeltasJson.append("};");
        return allDeltasJson.toString();

    }

    private static List<GraphDelta> prepareGraphDeltas(final List<TimeFrameGraph> allFrameGraphs) {
        //int index = 1;
        ArrayList<GraphDelta> allDeltas = Lists.newArrayList();
        for (int index = 1; index < allFrameGraphs.size(); index++) {
            TimeFrameGraph old = allFrameGraphs.get(index - 1);
            TimeFrameGraph current = allFrameGraphs.get(index);
            allDeltas.add(new GraphDelta(prepareAddList(old, current), prepareDelList(old, current), current.getAllEdges()));
        }
        return allDeltas;
    }

    private static List<String> prepareDelList(final TimeFrameGraph old,
                                               final TimeFrameGraph current) {
        Set<String> oldSet = old.getNameIndexMap().keySet();
        Set<String> newSet = current.getNameIndexMap().keySet();
        return Lists.newArrayList(Sets.difference(oldSet, newSet));
    }


    private static List<String> prepareAddList(final TimeFrameGraph old,
                                               final TimeFrameGraph current) {
        Set<String> oldSet = old.getNameIndexMap().keySet();
        Set<String> newSet = current.getNameIndexMap().keySet();
        return Lists.newArrayList(Sets.difference(newSet, oldSet));
    }


    private static String printInitialGraph(final TimeFrameGraph timeFrameGraph) {
        return printCompleteGraphAsJSON(timeFrameGraph);
    }

    private static String printCompleteGraphAsJSON(final TimeFrameGraph timeFrameGraph) {
        StringBuilder completeGraphJson = new StringBuilder();

        completeGraphJson.append("var initialGraph = {");
        completeGraphJson.append("nodes: [");

        //print all nodes
        List<String> allNodes = Lists.newArrayList();
        Set<String> allNodeNames = timeFrameGraph.getNameIndexMap().keySet();
        for (String nodeName : allNodeNames) {
            String fixedName = StringUtils.replace(nodeName, "\"", ""); // FIXME will break graph
            allNodes.add("{nodeName: \"" + fixedName + "\", group: 1}");
        }
        completeGraphJson.append(StringUtils.join(allNodes, ", \n"));
        completeGraphJson.append("], links:[");

        //print all edges
        List<String> allEdges = Lists.newArrayList();
        Map<String, Integer> nameIndexMap = timeFrameGraph.getNameIndexMap();
        for (GraphEdge edge : timeFrameGraph.getAllEdges()) {
            allEdges.add("{source: " + nameIndexMap.get(edge.getFrom()) + ", target: " + nameIndexMap.get(edge.getTo()) + ", value: " + 1 + "}");
        }
        completeGraphJson.append(StringUtils.join(allEdges, ", \n"));

        completeGraphJson.append("] };");
        return completeGraphJson.toString();
    }

}
