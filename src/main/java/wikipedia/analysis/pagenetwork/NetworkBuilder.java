package wikipedia.analysis.pagenetwork;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
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

    public static void main(final String[] args) {
        final String lang = "en";
        final String revisionDateTime = new DateMidnight(2011, 5, 1).toString(DBUtil.MYSQL_DATETIME_FORMATTER);
        NetworkBuilder nb = new NetworkBuilder(CategoryLists.ENGLISH_MUSIC, lang, revisionDateTime);
        nb.printNetworkData();
    }

    private void printNetworkData() {
        Map<Integer, String> allPagesInNetwork = new CategoryMemberFetcher(categories, lang, database)
                .getAllPagesInAllCategories();
        List<GraphEdge> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork);
        printNodeAndLinkInfo(allLinksInNetwork);
    }

    private void printNodeAndLinkInfo(final List<GraphEdge> allLinksInNetwork) {
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);

        Map<Integer, String> map = new TreeMap<Integer, String>();
        Map<String, Integer> keymap = new TreeMap<String, Integer>();
        int count = 0;
        for (String targetPage : indegreeMatrix.keySet()) {
            List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
            int inDegree = allIncommingLinks.size();
            if (inDegree < MIN_INDEGREE) {
                continue;
            }
            if (!keymap.containsKey(targetPage)) {
                keymap.put(targetPage, count);
                map.put(count, targetPage);
                count++;
            }
        }

        List<String> output = writeJSONResult(indegreeMatrix, map, keymap);
        try {
            FileUtils.writeLines(new File("out/bla.txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> writeJSONResult(final Map<String, List<String>> indegreeMatrix,
                                         final Map<Integer, String> map,
                                         final Map<String, Integer> keymap) {
        List<String> output = Lists.<String> newArrayList();
        output.add("var initialGraph = {");
        output.add("nodes: [");
        output.add(writeAllNodes(map));
        output.add("], links:[");
        output.add(writeAllEdges(indegreeMatrix, keymap));
        output.add("] };");
        return output;
    }

    private String writeAllEdges(final Map<String, List<String>> indegreeMatrix,
                                 final Map<String, Integer> keymap) {
        List<String> edgeOutput = Lists.newArrayList();
        for (Entry<String, List<String>> entry : indegreeMatrix.entrySet()) {
            String targetPageName = entry.getKey();
            List<String> incommingLinks = entry.getValue();
            for (String sourcePageName : incommingLinks) {
                if (sourcePageName.equals(targetPageName)) {
                    continue;
                }
                writeEdge(edgeOutput, keymap, targetPageName, sourcePageName);
            }
        }
        return StringUtils.join(edgeOutput, ", \n");
    }

    private String writeAllNodes(final Map<Integer, String> map) {
        List<String> nodeOutput = Lists.newArrayList();
        for (String nodeName : map.values()) {
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

    private void writeEdge(final List<String> output,
                                  final Map<String, Integer> keymap,
                                  final String to,
                                  final String from) {
        if (keymap.get(from) != null && keymap.get(to) != null) {
            output.add("{source: " + keymap.get(from) + ", target: " + keymap.get(to) + ", value: " + 1 + "}");
        }
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
