package wikipedia.analysis.pagenetwork;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.BetweennessCentrality.Progress;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wikipedia.database.DBUtil;
import wikipedia.http.CategoryMemberFetcher;
import wikipedia.network.GraphEdge;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class NetworkBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(NetworkBuilder.class.getName());

    private final List<String> categories;
    private final String lang;
    private final String revisionDateTime;
    private final DBUtil database = new DBUtil();
    private static final int MIN_INDEGREE = 120;

    private static final int NUM_THREADS = 8;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    public NetworkBuilder(final List<String> categories, final String lang,
            final String revisionDateTime) {
        this.categories = categories;
        this.lang = lang;
        this.revisionDateTime = revisionDateTime;
    }

    public static void main(final String[] args) throws IOException {
        final String lang = "en";
        final String revisionDateTime = new DateMidnight(2011, 5, 1).toString(DBUtil.MYSQL_DATETIME_FORMATTER);
        NetworkBuilder nb = new NetworkBuilder(CategoryLists.ENGLISH_MUSIC, lang, revisionDateTime);
        nb.printNetworkData();
    }

    private void printNetworkData() throws IOException {
        Map<Integer, String> allPagesInNetwork = new CategoryMemberFetcher(categories, lang, database)
                .getAllPagesInAllCategories(); //key = wiki page id, value = name
        List<GraphEdge> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork);

        //reduce nodes!
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
        System.out.println("Before reduction: " + indegreeMatrix.size());
        List<String> reducedPages = Lists.newArrayList();
        for (Entry<String, List<String>> incommingInfo : indegreeMatrix.entrySet()) {
            if(incommingInfo.getValue().size() > 8) {
                reducedPages.add(incommingInfo.getKey());
            }
        }
        System.out.println("After reduction: " + reducedPages.size());

        Graph graph = new SingleGraph("Betweenness Test");
        for (String nodeName : reducedPages) {
            graph.addNode(nodeName);
        }

        Set<GraphEdge> allEdgesAsSet = Sets.newHashSet();
        for (GraphEdge graphEdge : allLinksInNetwork) {
            allEdgesAsSet.add(graphEdge);
        }

        for (GraphEdge graphEdge : allEdgesAsSet) {
            String from = graphEdge.getFrom();
            String to = graphEdge.getTo();
            String id = from + " -> " + to;
            String reverseID = to + " -> " + from;
            if(graph.getEdge(id) == null && graph.getEdge(reverseID) == null && reducedPages.contains(from) && reducedPages.contains(to)) {
                graph.addEdge(id, from, to, true);
            }
        }

      //  System.out.println("SIZE as SET: " + allEdgesAsSet.size());

        System.out.println("Edges added");
        BetweennessCentrality bcb = new BetweennessCentrality();
        bcb.registerProgressIndicator(new Progress() {

            public void progress(final float percent) {
                LOG.info("Working: " + percent + "%");
            }
        });
        bcb.setUnweighted();
        bcb.init(graph);
        bcb.compute();

        for (org.graphstream.graph.Node graphNode : graph.getNodeSet()) {
            System.out.println(graphNode.getId() + "=" + graphNode.getInDegree() + "=" + graphNode.getAttribute("Cb"));
        }
        //printNodeAndLinkInfo(allLinksInNetwork);
    }



    private void printNodeAndLinkInfo(final List<GraphEdge> allLinksInNetwork) throws IOException {
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
        Map<String, Integer> nameIndexMap = Maps.newTreeMap();
        int nodeIndex = 0;
        List<String> allQualifiedNodesInfo = Lists.newArrayList();
        for (String targetPage : indegreeMatrix.keySet()) {
            if(nodeQualifiedForGraph(indegreeMatrix, targetPage, allQualifiedNodesInfo) && !nameIndexMap.containsKey(targetPage)) {
                nameIndexMap.put(targetPage, nodeIndex++);
            }
        }
        writeQualifiedNodesToFile(allQualifiedNodesInfo);
        List<String> output = writeJSONResult(indegreeMatrix, nameIndexMap);
        FileUtils.writeLines(new File("out/bla.txt"), output);
    }

    private boolean nodeQualifiedForGraph(final Map<String, List<String>> indegreeMatrix, final String targetPage, final List<String> allQualifiedNodesInfo) throws IOException {
        List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
        int inDegree = allIncommingLinks.size();
        final boolean nodeQualified = inDegree >= MIN_INDEGREE;
        if(nodeQualified) {
            allQualifiedNodesInfo.add(targetPage + "=" + inDegree);
        }
        return nodeQualified;
    }

    private void writeQualifiedNodesToFile(final List<String> allQualifiedNodesInfo) throws IOException {
        final DateTime dateTime = new DateTime();
        String fileName = "out/qualified_nodes_" + dateTime.getHourOfDay() + ":" + dateTime.getMinuteOfHour();
        FileUtils.writeLines(new File(fileName), allQualifiedNodesInfo);
    }

    private List<String> writeJSONResult(final Map<String, List<String>> indegreeMatrix,
                                         final Map<String, Integer> nameIndexMap) {
        List<String> output = Lists.<String> newArrayList();
        output.add("var initialGraph = {");
        output.add("nodes: [");
        output.add(writeAllNodes(nameIndexMap.keySet()));
        output.add("], links:[");
        output.add(writeAllEdges(indegreeMatrix, nameIndexMap));
        output.add("] };");
        return output;
    }

    private String writeAllEdges(final Map<String, List<String>> indegreeMatrix,
                                 final Map<String, Integer> nameIndexMap) {
        List<String> edgeOutput = Lists.newArrayList();
        // TODO loop not optimal
        for (Entry<String, List<String>> entry : indegreeMatrix.entrySet()) {
            String targetPageName = entry.getKey();
            List<String> incommingLinks = entry.getValue();
            for (String sourcePageName : incommingLinks) {
                if (sourcePageName.equals(targetPageName)) {
                    continue;
                }
                if (nameIndexMap.get(sourcePageName) != null && nameIndexMap.get(targetPageName) != null) {
                    edgeOutput.add("{source: " + nameIndexMap.get(sourcePageName) + ", target: " + nameIndexMap.get(targetPageName) + ", value: " + 1 + "}");
                }
            }
        }
        return StringUtils.join(edgeOutput, ", \n");
    }

    private String writeAllNodes(final Set<String> allPageNames) {
        List<String> nodeOutput = Lists.newArrayList();
        for (String nodeName : allPageNames) {
            writeNode(nodeOutput, nodeName);
        }
        return StringUtils.join(nodeOutput, ", \n");
    }

    private void writeNode(final List<String> output,
                                  final String name) {
        String fixedName = StringUtils.replace(name, "\"", ""); // FIXME will
                                                                // break graph!
        output.add("{nodeName: \"" + fixedName + "\", group: 1}");
    }

    private Map<String, List<String>> initIndegreeMatrix(final List<GraphEdge> allLinksInNetwork) {
        // In-degree matrix key:target_page, value:source_page
        // value: all pages that point to target_page (key)
        Map<String, List<String>> indegreeMatrix = Maps.newHashMap();
        for (GraphEdge link : allLinksInNetwork) {
            String from = link.getFrom();
            String to = link.getTo();
            if (indegreeMatrix.containsKey(to)) {
                indegreeMatrix.get(to).add(from);
            } else {
                indegreeMatrix.put(to, Lists.newArrayList(from));
            }
        }
        return indegreeMatrix;
    }

    private List<GraphEdge> buildAllLinksWithinNetwork(final Map<Integer, String> allPagesInNetwork) {
        Set<String> allPageNamesInNetwork = Sets.newHashSet(allPagesInNetwork.values());
        final List<GraphEdge> allLinksInNetwork = Collections.synchronizedList(Lists.<GraphEdge>newArrayList());
        LOG.info("Number of Tasks: " + allPageNamesInNetwork.size());
        int taskCounter = 1;
        try {
            for (Entry<Integer, String> entry : allPagesInNetwork.entrySet()) {
                final int pageId = entry.getKey();
                final String pageName = entry.getValue();
                threadPool.execute(new SQLExecutor(pageId, allPageNamesInNetwork, pageName, allLinksInNetwork, taskCounter++));
            }
        } finally {
            shutdownThreadPool();
        }
        return allLinksInNetwork;
    }

    private void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Error while shutting down Threadpool", e);
        }
        while (!threadPool.isTerminated()) {
            // wait for all tasks or timeout
        }
    }

    private final class SQLExecutor implements Runnable {
        private final int pageId;
        private final Set<String> allPageNamesInNetwork;
        private final String pageName;
        private final List<GraphEdge> allLinksInNetwork;
        private final int counter;

        private SQLExecutor(final int pageId, final Set<String> allPageNamesInNetwork,
                final String pageName, final List<GraphEdge> allLinksInNetwork2, final int counter) {
            this.pageId = pageId;
            this.allPageNamesInNetwork = allPageNamesInNetwork;
            this.pageName = pageName;
            this.allLinksInNetwork = allLinksInNetwork2;
            this.counter = counter;
        }

        public void run() {
            if (counter % 1000 == 0) {
                LOG.info("Task: " + counter);
            }
            Collection<String> allOutgoingLinksOnPage = database.getAllLinksForRevision(pageId, revisionDateTime);
            for (String outgoingLink : allOutgoingLinksOnPage) {
                if (allPageNamesInNetwork.contains(outgoingLink)) {
                    allLinksInNetwork.add(new GraphEdge(pageName, outgoingLink));
                }
            }
        }
    }

}
