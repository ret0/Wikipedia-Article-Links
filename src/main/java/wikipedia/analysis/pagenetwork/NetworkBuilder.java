package wikipedia.analysis.pagenetwork;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wikipedia.database.DBUtil;
import wikipedia.http.CategoryMemberFetcher;
import wikipedia.network.GraphEdge;
import wikipedia.network.TimeFrameGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class NetworkBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkBuilder.class.getName());

    private final String revisionDateTime;
    private final DBUtil database = new DBUtil();
    private static final int MIN_INDEGREE = 120;

    private static final int NUM_THREADS = 8;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    private final Map<Integer, String> allPagesInNetwork;

    public NetworkBuilder(final List<String> categories, final String lang,
            final DateMidnight dateMidnight) {
        this.revisionDateTime = dateMidnight.toString(DBUtil.MYSQL_DATETIME_FORMATTER);
        allPagesInNetwork = new CategoryMemberFetcher(categories, lang, database)
                .getAllPagesInAllCategories();
    }

    public TimeFrameGraph getGraphAtDate() {
        List<GraphEdge> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork);
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
        Map<String, Integer> nameIndexMap = Maps.newLinkedHashMap();
        int nodeIndex = 0;
        for (String targetPage : indegreeMatrix.keySet()) {
            if (nodeQualifiedForGraph(indegreeMatrix, targetPage)
                    && !nameIndexMap.containsKey(targetPage)) {
                nameIndexMap.put(targetPage, nodeIndex++);
            }
        }

        List<GraphEdge> edgeOutput = Lists.newArrayList();
        // TODO loop not optimal
        for (Entry<String, List<String>> entry : indegreeMatrix.entrySet()) {
            String targetPageName = entry.getKey();
            List<String> incommingLinks = entry.getValue();
            for (String sourcePageName : incommingLinks) {
                if (sourcePageName.equals(targetPageName)) {
                    continue;
                }
                if (nameIndexMap.get(sourcePageName) != null
                        && nameIndexMap.get(targetPageName) != null) {
                    edgeOutput.add(new GraphEdge(sourcePageName, targetPageName));
                }
            }
        }
        return new TimeFrameGraph(nameIndexMap, edgeOutput);
    }


    private boolean nodeQualifiedForGraph(final Map<String, List<String>> indegreeMatrix,
                                          final String targetPage) {
        List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
        int inDegree = allIncommingLinks.size();
        final boolean nodeQualified = inDegree >= MIN_INDEGREE;
        return nodeQualified;
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
        final List<GraphEdge> allLinksInNetwork = Collections.synchronizedList(Lists
                .<GraphEdge> newArrayList());
        LOG.info("Number of Tasks: " + allPageNamesInNetwork.size());
        int taskCounter = 1;
        try {
            for (Entry<Integer, String> entry : allPagesInNetwork.entrySet()) {
                final int pageId = entry.getKey();
                final String pageName = entry.getValue();
                threadPool.execute(new SQLExecutor(pageId, allPageNamesInNetwork, pageName,
                        allLinksInNetwork, taskCounter++));
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

    private final class SQLExecutor implements Runnable {
        private static final int LOG_MODULO = 4000;
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
            if (counter % LOG_MODULO == 0) {
                LOG.info("Task: " + counter);
            }
            Collection<String> allOutgoingLinksOnPage = database.getAllLinksForRevision(pageId,
                    revisionDateTime);
            for (String outgoingLink : allOutgoingLinksOnPage) {
                if (allPageNamesInNetwork.contains(outgoingLink)) {
                    allLinksInNetwork.add(new GraphEdge(pageName, outgoingLink));
                }
            }
        }
    }

}
