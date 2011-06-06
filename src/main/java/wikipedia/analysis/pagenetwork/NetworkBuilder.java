package wikipedia.analysis.pagenetwork;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wikipedia.database.DBUtil;
import wikipedia.http.CategoryMemberFetcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NetworkBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(NetworkBuilder.class.getName());

    private final List<String> categories;
    private final String lang;
    private final String revisionDateTime;
    private final DBUtil database = new DBUtil();
    private static final int MIN_INDEGREE = 15;

    private static final int NUM_THREADS = 16;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

    public NetworkBuilder(final List<String> categories, final String lang, final String revisionDateTime) {
        this.categories = categories;
        this.lang = lang;
        this.revisionDateTime = revisionDateTime;
    }

    public static void main(final String[] args) {
        final List<String> categories = ImmutableList.of("Category:American_female_singers",
                                                                  "Category:American_male_singers",
                                                                  "Category:English-language_singers",
                                                                  "Category:1980s_singers",
                                                                  "Category:1990s_singers",
                                                                  "Category:2000s_singers",
                                                                  "Category:2010s_singers");
        final String lang = "en";
        final String revisionDateTime = new DateMidnight(2011, 5, 1).toString(DBUtil.MYSQL_DATETIME_FORMATTER);

        NetworkBuilder nb = new NetworkBuilder(categories, lang, revisionDateTime);
        nb.printNetworkData();
    }

    private void printNetworkData() {
        Map<Integer, String> allPagesInNetwork = new CategoryMemberFetcher(categories, lang).getAllPagesInAllCategories();
        Map<String, String> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork); //key = from, value = to
        printNodeAndLinkInfo(allLinksInNetwork);
    }

    private void printNodeAndLinkInfo(final Map<String, String> allLinksInNetwork) {
        Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
        List<String> output = Lists.<String>newArrayList();

        int count = 0;

        Map<Integer, String> map = new TreeMap<Integer, String>();
        Map<String, Integer> keymap = new TreeMap<String, Integer>();

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

            Iterator<String> it2 = allIncommingLinks.iterator();
            while (it2.hasNext()) {
                String to = it2.next();

                if (!keymap.containsKey(to)) {
                    keymap.put(to, count);
                    map.put(count, to);
                    count++;
                }
            }
        }

        List<String> nodeOutput = Lists.newArrayList();
        output.add("var initialGraph = {");
        output.add("nodes: [");

        Iterator<Integer> iti = map.keySet().iterator();
        while (iti.hasNext()) {
            int id = iti.next();
            String value = map.get(id);
            writeNode(nodeOutput, value);
        }

        output.add(StringUtils.join(nodeOutput, ", \n"));

        output.add("], links:[");

        List<String> edgeOutput = Lists.newArrayList();

        int edges = 0;

        // In-degree matrix
        for (String to : indegreeMatrix.keySet()) {
            List<String> v = indegreeMatrix.get(to);
            Iterator<String> it2 = v.iterator();
            while (it2.hasNext()) {

                String from = it2.next();

                if (from.equals(to)) {
                    continue;
                }
                writeEdge(edgeOutput, keymap, to, from);
                edges++;
            }
        }

        output.add(StringUtils.join(edgeOutput, ", \n"));
        output.add("] };");
        System.out.println(StringUtils.join(output, "\n"));
    }

    private static void writeNode(final List<String> output,
                                  final String name) {
        String fixedName = StringUtils.replace(name, "\"", ""); // FIXME will break graph!
        output.add("{nodeName: \"" + fixedName + "\", group: 1}");
    }

    private static void writeEdge(final List<String> output,
                                  final Map<String, Integer> keymap,
                                  final String to,
                                  final String from) {

        if(keymap.get(from) != null && keymap.get(to) != null) {
            output.add("{source: " + keymap.get(from) + ", target: " + keymap.get(to) + ", value: " + 2 + "}");
        }
    }


    private  Map<String, List<String>> initIndegreeMatrix(final Map<String, String> allLinksInNetwork)  {
        // In-degree matrix key:target_page, value:source_page
        //value: all pages that point to target_page (key)
        Map<String, List<String>> indegreeMatrix = Maps.newHashMap();
        for (Entry<String, String> link : allLinksInNetwork.entrySet()) {
            String from = link.getKey();
            String to = link.getValue();
            if (indegreeMatrix.containsKey(to)) {
                indegreeMatrix.get(to).add(from);
            } else {
                indegreeMatrix.put(to, Lists.newArrayList(from));
            }
        }
        return indegreeMatrix;
    }

    private Map<String, String> buildAllLinksWithinNetwork(final Map<Integer, String> allPagesInNetwork) {
        final Collection<String> allPageNamesInNetwork = allPagesInNetwork.values();
        final Map<String, String> allLinksInNetwork = Maps.newConcurrentMap();
        try {
            for (Entry<Integer, String> entry : allPagesInNetwork.entrySet()) {
                final int pageId = entry.getKey();
                final String pageName = entry.getValue();
                threadPool.execute(new SQLExecutor(pageId, allPageNamesInNetwork, pageName, allLinksInNetwork));
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
        private final Collection<String> allPageNamesInNetwork;
        private final String pageName;
        private final Map<String, String> allLinksInNetwork;

        private SQLExecutor(final int pageId, final Collection<String> allPageNamesInNetwork, final String pageName,
                final Map<String, String> allLinksInNetwork) {
            this.pageId = pageId;
            this.allPageNamesInNetwork = allPageNamesInNetwork;
            this.pageName = pageName;
            this.allLinksInNetwork = allLinksInNetwork;
        }

        public void run() {
            //LOG.info("Getting Links -- Page: " + pageName + " -- Date: " + revisionDateTime);
            List<String> allOutgoingLinksOnPage = database.getAllLinksForRevision(pageId, revisionDateTime);
            for (String outgoingLink : allOutgoingLinksOnPage) {
                if(allPageNamesInNetwork.contains(outgoingLink)) {
                    allLinksInNetwork.put(pageName, outgoingLink);
                }
            }
        }
    }

}
