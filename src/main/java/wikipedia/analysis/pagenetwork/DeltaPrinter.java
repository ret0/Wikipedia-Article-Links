package wikipedia.analysis.pagenetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wikipedia.database.DBUtil;
import wikipedia.http.PageHistoryFetcher;
import wikipedia.network.GraphEdge;
import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Prints the JSON Delta information for the frames of a animation
 */
public final class DeltaPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(DeltaPrinter.class.getName());

    private static final int NUM_REVISIONS = 35;
    private final List<String> categories;
    private final List<DateTime> allTimeFrames;

    private static final int NUM_THREADS = 8;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    public DeltaPrinter(final List<String> categories, final List<DateTime> allTimeFrames) {
        this.categories = categories;
        this.allTimeFrames = allTimeFrames;
    }

    public static void main(final String[] args) throws IOException {
        List<DateTime> allTimeFrames = PageHistoryFetcher.getAllDatesForHistory(NUM_REVISIONS,
                PageHistoryFetcher.MOST_RECENT_DATE.toDateTime());
        DeltaPrinter dp = new DeltaPrinter(CategoryLists.CLASSICAL_MUSIC, allTimeFrames);
        String completeJSONForPage = dp.buildNetworksAndGenerateInfo();
        FileUtils.write(new File("out/initialGraph.js"), completeJSONForPage);
    }

    private String buildNetworksAndGenerateInfo() {
        List<TimeFrameGraph> dateGraphMap = Lists.newArrayList();
        List<DateTime> allTimeFramesOldToNew = Lists.reverse(allTimeFrames);
        DBUtil database = new DBUtil();
        for (DateTime dateTime : allTimeFramesOldToNew) {
            DateMidnight dateMidnight = dateTime.toDateMidnight();
            List<String> nodeDebug = Lists.newArrayList();
            dateGraphMap.add(new NetworkBuilder(categories, "en", dateMidnight, database, threadPool).getGraphAtDate(nodeDebug));
//            try {
//                FileUtils.writeLines(new File("out/degreeOutput" + dateMidnight.toString() + ".txt"), nodeDebug);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        shutdownThreadPool();
        return generateTimeFrameInformation(dateGraphMap);
    }

    private void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Error while shutting down Threadpool", e);
        }
        while (!threadPool.isTerminated()) {
            LOG.debug("Waiting for all Tasks to terminate");
        }
    }

    public String generateTimeFrameInformation(final List<TimeFrameGraph> allFrameGraphs) {
        StringBuilder jsonOutput = new StringBuilder();
        jsonOutput.append(printCompleteGraphAsJSON(allFrameGraphs.get(0)));
        jsonOutput.append(printAllDeltaGraphs(allFrameGraphs));
        return jsonOutput.toString();
    }

    private String printAllDeltaGraphs(final List<TimeFrameGraph> allFrameGraphs) {
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

    private List<GraphDelta> prepareGraphDeltas(final List<TimeFrameGraph> allFrameGraphs) {
        ArrayList<GraphDelta> allDeltas = Lists.newArrayList();
        for (int index = 1; index < allFrameGraphs.size(); index++) {
            TimeFrameGraph old = allFrameGraphs.get(index - 1);
            TimeFrameGraph current = allFrameGraphs.get(index);
            allDeltas.add(new GraphDelta(prepareAddList(old, current), prepareDelList(old, current),
                    current.getAllEdges()));
        }
        return allDeltas;
    }

    private List<String> prepareDelList(final TimeFrameGraph old,
                                               final TimeFrameGraph current) {
        Set<String> oldSet = old.getNameIndexMap().keySet();
        Set<String> newSet = current.getNameIndexMap().keySet();
        return Lists.newArrayList(Sets.difference(oldSet, newSet));
    }

    private List<String> prepareAddList(final TimeFrameGraph old,
                                               final TimeFrameGraph current) {
        Set<String> oldSet = old.getNameIndexMap().keySet();
        Set<String> newSet = current.getNameIndexMap().keySet();
        return Lists.newArrayList(Sets.difference(newSet, oldSet));
    }

    private String printCompleteGraphAsJSON(final TimeFrameGraph timeFrameGraph) {
        StringBuilder completeGraphJson = new StringBuilder();

        completeGraphJson.append("var initialGraph = {");
        completeGraphJson.append("nodes: [");

        // print all nodes
        List<String> allNodes = Lists.newArrayList();
        Set<String> allNodeNames = timeFrameGraph.getNameIndexMap().keySet();
        for (String nodeName : allNodeNames) {
            String fixedName = StringUtils.replace(nodeName, "\"", ""); // FIXME
                                                                        // will
                                                                        // break
                                                                        // graph
            allNodes.add("{nodeName: \"" + fixedName + "\", group: 1}");
        }
        completeGraphJson.append(StringUtils.join(allNodes, ", \n"));
        completeGraphJson.append("], links:[");

        // print all edges
        List<String> allEdges = Lists.newArrayList();
        Map<String, Integer> nameIndexMap = timeFrameGraph.getNameIndexMap();
        for (GraphEdge edge : timeFrameGraph.getAllEdges()) {
            allEdges.add("{source: " + nameIndexMap.get(edge.getFrom()) + ", target: "
                    + nameIndexMap.get(edge.getTo()) + ", value: " + 1 + "}");
        }
        completeGraphJson.append(StringUtils.join(allEdges, ", \n"));

        completeGraphJson.append("] };");
        return completeGraphJson.toString();
    }

}
