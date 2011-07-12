package wikipedia.analysis.pagenetwork;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.MultiGraph;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.MapSorter;
import wikipedia.database.DBUtil;
import wikipedia.network.GraphEdge;
import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Builds a Network structure based on all pages in the given categories
 */
public final class NetworkBuilder {

    private static final int MAX_NODES = 50;
    /**
     * Constants for Node-Filtering
     */
    private static final int INDEG_MULTIPLICATOR = 1000;
    private static final double MIN_INDEG_FOR_DIRECT_NEIGHBOR = 1.5;
    private static final int MIN_INDEG_FOR_MUTALLY_CONNECTED = 2;

    private static final Logger LOG = LoggerFactory.getLogger(NetworkBuilder.class.getName());
    private static final int NUM_THREADS = 8;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    private final String searchTerm;
    private final DBUtil database;
    private final Map<Integer, String> allPagesInNetwork;

    public NetworkBuilder(final Map<Integer, String> allPagesInNetwork,
                          final DBUtil database,
                          final String searchTerm) {
        this.database = database;
        this.allPagesInNetwork = allPagesInNetwork;
        this.searchTerm = searchTerm;
    }

    public TimeFrameGraph getGraphAtDate(final DateTime dateTime) {
        String revisionDateTime = dateTime.toString(DBUtil.MYSQL_DATETIME_FORMATTER);
        List<GraphEdge> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork, revisionDateTime);
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
        Map<String, Integer> nameIndexMap = Maps.newLinkedHashMap();

        final int numberOfLinks = allLinksInNetwork.size();
        Map<String, Float> pageIndegMap = Maps.newHashMap();
        for (String targetPage : indegreeMatrix.keySet()) {
            final float indegValue = calculateIndegree(numberOfLinks, indegreeMatrix.get(targetPage));
            pageIndegMap.put(targetPage, indegValue);
        }

        Set<String> mutuallyConnectedNeighbors = Sets.newHashSet();
        for (GraphEdge graphEdge : allLinksInNetwork) {
            if (allLinksInNetwork.contains(new GraphEdge(graphEdge.getTo(), graphEdge.getFrom()))) {
                if (graphEdge.getFrom().equals(searchTerm) || graphEdge.getTo().equals(searchTerm)) {
                    mutuallyConnectedNeighbors.add(graphEdge.getFrom());
                    mutuallyConnectedNeighbors.add(graphEdge.getTo());
                }
            }
        }

        DefaultGraph graph = prepareGraph(allLinksInNetwork);
        Dijkstra shortestPath = prepareShortestPath(graph);

        int nodeIndex = 0;
        Map<String, Float> allPagesOrderedByIndeg = new MapSorter<String, Float>().sortByValue(pageIndegMap);

        //direct neighbors
        for (String pageName : pageIndegMap.keySet()) {
            if (!nameIndexMap.containsKey(pageName)) {
                final boolean importantDirectNeighbor = importantDirectNeighbor(
                        allPagesOrderedByIndeg, pageName, shortestPath, graph);
                final boolean mutuallyConnectedNeighbor = mutuallyConnectedNeighbor(
                        mutuallyConnectedNeighbors, allPagesOrderedByIndeg,
                        pageName);
                if (importantDirectNeighbor || mutuallyConnectedNeighbor) {
                    nameIndexMap.put(pageName, nodeIndex++);
                }
            }
        }

        //indeg
        while (nodeIndex < MAX_NODES) {
            Iterator<Entry<String, Float>> iterator = allPagesOrderedByIndeg.entrySet().iterator();
            Entry<String, Float> entry = iterator.next();
            final String pageName = entry.getKey();
            if (!nameIndexMap.containsKey(pageName)) {
                nameIndexMap.put(pageName, nodeIndex++);
            }
        }

        //calculate mini graph centrality
        /*DefaultGraph graph2 = new MultiGraph("g2", false, true);
        for (GraphEdge edge : allLinksInNetwork) {
            if(pageIndegMap.get(edge.getFrom()) != null &&  pageIndegMap.get(edge.getFrom()) > 1 ||
                    pageIndegMap.get(edge.getTo()) != null && pageIndegMap.get(edge.getTo()) > 1) {
                graph2.addEdge(edge.getFrom() + edge.getTo() + r.nextDouble(), edge.getFrom(), edge.getTo(), true);
            }
        }*/

        List<GraphEdge> edgeOutput = Lists.newArrayList();
        for (Entry<String, List<String>> entry : indegreeMatrix.entrySet()) {
            String targetPageName = entry.getKey();
            List<String> incommingLinks = entry.getValue();
            for (String sourcePageName : incommingLinks) {
                if (sourcePageName.equals(targetPageName)) {
                    continue;
                }
                if (nameIndexMap.get(sourcePageName) != null && nameIndexMap.get(targetPageName) != null) {
                    edgeOutput.add(new GraphEdge(sourcePageName, targetPageName));
                }
            }
        }
        return new TimeFrameGraph(nameIndexMap, edgeOutput, dateTime);
    }

    private Dijkstra prepareShortestPath(final DefaultGraph graph) {
        Dijkstra shortestPath = new Dijkstra(Dijkstra.Element.edge, "weight", searchTerm);
        shortestPath.init(graph);
        shortestPath.compute();
        return shortestPath;
    }

    private DefaultGraph prepareGraph(final List<GraphEdge> allLinksInNetwork) {
        // create graph using gs
        DefaultGraph graph = new MultiGraph("graphForShortestPath", false, true);
        Random r = new Random();
        for (GraphEdge edge : allLinksInNetwork) {
            graph.addEdge(edge.getFrom() + edge.getTo() + r.nextDouble(), edge.getFrom(), edge.getTo(), true);
        }
        return graph;
    }

    private float calculateIndegree(final int numberOfLinks,
                                    final List<String> allIncommingLinks) {
        int totalNumberOfLinks = allIncommingLinks.size();
        return ((float) totalNumberOfLinks / (float) numberOfLinks) * INDEG_MULTIPLICATOR;
    }

    private boolean mutuallyConnectedNeighbor(final Set<String> mutuallyConnectedNeighbors,
                                              final Map<String, Float> allPagesOrderedByIndeg,
                                              final String pageName) {
        return mutuallyConnectedNeighbors.contains(pageName) &&
               allPagesOrderedByIndeg.get(pageName) >= MIN_INDEG_FOR_MUTALLY_CONNECTED;
    }

    private boolean importantDirectNeighbor(final Map<String, Float> allPagesOrderedByIndeg,
                                            final String pageName,
                                            final Dijkstra shortestPath,
                                            final DefaultGraph graph) {
        double shortestPathLength = shortestPath.getShortestPathLength(graph.getNode(pageName));
        return shortestPathLength <= 1 &&
               allPagesOrderedByIndeg.get(pageName) >= MIN_INDEG_FOR_DIRECT_NEIGHBOR;
    }


    /*private boolean nodeQualifiedForGraph(final Map<String, List<String>> indegreeMatrix,
                                          final String targetPage, final List<String> nodeDebug, final int numberOfLinks) {
        List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
        int totalNumberOfLinks = allIncommingLinks.size();
        //final boolean nodeQualified = inDegree >= MIN_INDEGREE;
        final float magicNumber = ((float) totalNumberOfLinks / (float) numberOfLinks) * 1000;
        final boolean nodeQualified = magicNumber >= 1.35f;
        if (magicNumber > 1.25) {
            nodeDebug.add(revisionDateTime + "=" + targetPage + "=" + totalNumberOfLinks + "=" + magicNumber);
        }
        return nodeQualified;
    }*/

    /**
     * @param allLinksInNetwork
     * @return In-degree matrix key:target_page value: all pages that point to target_page (key)
     */
    private Map<String, List<String>> initIndegreeMatrix(final List<GraphEdge> allLinksInNetwork) {
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

    private List<GraphEdge> buildAllLinksWithinNetwork(final Map<Integer, String> allPagesInNetworkList,
                                                       final String revisionDateTime) {
        Set<String> allPageNamesInNetwork = Sets.newHashSet(allPagesInNetworkList.values());
        final List<GraphEdge> allLinksInNetwork = Collections.synchronizedList(Lists.<GraphEdge>newArrayList());
        LOG.info("Number of Tasks: " + allPageNamesInNetwork.size());
        int taskCounter = 1;
        try {
            for (Entry<Integer, String> entry : allPagesInNetworkList.entrySet()) {
                final int pageId = entry.getKey();
                final String pageName = entry.getValue();
                threadPool.execute(new SQLExecutor(pageId, allPageNamesInNetwork, pageName,
                        allLinksInNetwork, taskCounter++, revisionDateTime));
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
            LOG.debug("Waiting for all Tasks to terminate");
        }
    }

    /**
     * Fetches Information from DB and calculates all incomming links to a given
     * page in the network
     */
    private final class SQLExecutor implements Runnable {
        private static final int LOG_MODULO = 4000;
        private final int pageId;
        private final Set<String> allPageNamesInNetwork;
        private final String pageName;
        private final List<GraphEdge> allLinksInNetwork;
        private final int counter;
        private final String revisionDateTime;

        private SQLExecutor(final int pageId, final Set<String> allPageNamesInNetwork,
                final String pageName, final List<GraphEdge> allLinksInNetwork, final int counter,
                final String revisionDateTime) {
            this.pageId = pageId;
            this.allPageNamesInNetwork = allPageNamesInNetwork;
            this.pageName = pageName;
            this.allLinksInNetwork = allLinksInNetwork;
            this.counter = counter;
            this.revisionDateTime = revisionDateTime;
        }

        @Override
        public void run() {
            if (counter % LOG_MODULO == 0) {
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
