package wikipedia.analysis.pagenetwork;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;

import wikipedia.network.GraphEdge;
import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DeltaPrinter {

    public static void main(final String[] args) {
        List<DateMidnight> allTimeFrames = Lists.newArrayList(new DateMidnight(2010, 12, 1),
                                                          new DateMidnight(2011, 1, 1),
                                                          new DateMidnight(2011, 2, 1));//,
//                                                          new DateMidnight(2011, 3, 1),
//                                                          new DateMidnight(2011, 4, 1));

        final List<String> categories = CategoryLists.ENGLISH_MUSIC;
        Map<DateMidnight, TimeFrameGraph> dateGraphMap = Maps.newHashMap();

        for (DateMidnight dateTime : allTimeFrames) {
            dateGraphMap.put(dateTime, new NetworkBuilder(categories, "en", dateTime).getGraphAtDate());
        }

        System.out.println(generateTimeFrameInformation(Lists.newArrayList(dateGraphMap.values())));
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
        int index = 1;
        TimeFrameGraph old = allFrameGraphs.get(index - 1);
        TimeFrameGraph current = allFrameGraphs.get(index);

        GraphDelta test1 = new GraphDelta(prepareAddList(old, current), prepareDelList(old, current), current.getAllEdges());

        index++;
        old = allFrameGraphs.get(index - 1);
        current = allFrameGraphs.get(index);

        GraphDelta test2 = new GraphDelta(prepareAddList(old, current), prepareDelList(old, current), current.getAllEdges());
        return Lists.newArrayList(test1, test2);
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
