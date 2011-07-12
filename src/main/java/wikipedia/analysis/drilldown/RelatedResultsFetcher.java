package wikipedia.analysis.drilldown;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Const;
import util.MapSorter;
import wikipedia.analysis.pagenetwork.DeltaPrinter;
import wikipedia.database.DBUtil;
import wikipedia.http.PageHistoryFetcher;
import wikipedia.http.PageLinkInfoFetcher;
import wikipedia.http.WikiAPIClient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public final class RelatedResultsFetcher {

    private static final int MAX_SEARCHRESULTS = 12;

    private static final int NBR_WEEKS = 4;

    private static final Logger LOG = LoggerFactory.getLogger(RelatedResultsFetcher.class.getName());

    private final String lang;
    private final String searchTerm;
    private final DBUtil database = new DBUtil();

    private final WikiAPIClient wikiAPIClient;

    private final List<DateTime> allTimeFrames;
    private final DateTime mostRecentDate;

    public RelatedResultsFetcher(final String searchTerm, final String lang, final int numberOfWeeksBack) {
        this.searchTerm = searchTerm;
        this.lang = lang;
        wikiAPIClient = new WikiAPIClient(new DefaultHttpClient());
        mostRecentDate = new DateMidnight(2011, 6, 1).toDateTime();
        allTimeFrames = PageHistoryFetcher.getAllDatesForHistory(numberOfWeeksBack, mostRecentDate);
    }

    private Map<String, Integer> getActivityMap(final int topResults) {
        final Collection<String> searchResults = new BasicSearch(lang, searchTerm).executeSearch();
        Map<String, Integer> activityResults = Maps.newHashMap();
        final NumberOfRecentEditsFetcher numberOfRecentEditsFetcher = new NumberOfRecentEditsFetcher(lang);
        for (String pageTitle : searchResults) {
            int nbrEdits = numberOfRecentEditsFetcher.getNumberOfEditsInLastWeeks(NBR_WEEKS, pageTitle);
            activityResults.put(pageTitle, nbrEdits);
        }
        return getTopEntries(topResults, activityResults);
    }

    public static void main(final String[] args) throws IOException {
        final int numberOfWeeksBack = 1;
        RelatedResultsFetcher fetcher = new RelatedResultsFetcher("Justin Biber", "en", numberOfWeeksBack);
        fetcher.buildCompleteGraph();
    }

    public void buildCompleteGraph() {
        Set<String> allSeenNodes = Sets.newHashSet();
        Map<String, Integer> initialSearchResults = getActivityMap(MAX_SEARCHRESULTS);
        allSeenNodes.addAll(initialSearchResults.keySet());

        for (String topEntry : initialSearchResults.keySet()) {
            PageLinkInfoFetcher plif = new PageLinkInfoFetcher(topEntry, lang, mostRecentDate, wikiAPIClient);
            allSeenNodes.addAll(plif.getLinkInformation().getFilteredLinks());
        }
        Map<Integer, String> allPages = prepareNodesForNetwork(allSeenNodes);

        DeltaPrinter dp = new DeltaPrinter(allPages, allTimeFrames);
        String completeJSONForPage = dp.buildNetworksAndGenerateInfo(searchTerm);
        writeToFile(completeJSONForPage);
    }

    private void writeToFile(final String completeJSONForPage) {
        try {
            final String fileName = "out/" + StringUtils.replace(searchTerm, " ", "_") + ".json";
            FileUtils.write(new File(fileName), completeJSONForPage, Const.ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, String> prepareNodesForNetwork(final Set<String> allSeenNodes) {
        Map<Integer, String> idsAndPages = Maps.newHashMap();
        for (String pageTitle : allSeenNodes) {
            pageTitle = StringUtils.replace(pageTitle, "_", " ");
            try {
                int id = database.getPageIDFromCache(pageTitle, lang);
                if (id != 0) {
                    idsAndPages.put(id, pageTitle);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.info("Problem while getting: " + pageTitle);
            }
        }
        new PageHistoryFetcher(idsAndPages, "en", allTimeFrames).fetchCompleteCategories();
        return idsAndPages;
    }

    private Map<String, Integer> getTopEntries(final int nbrResults,
                                      final Map<String, Integer> results) {
        LinkedHashMap<String, Integer> sortetedResults = Maps
                .newLinkedHashMap(new MapSorter<String, Integer>().sortByValue(results));
        LinkedHashMap<String, Integer> filteredResults = Maps.newLinkedHashMap();
        List<String> sortedKeys = Lists.newArrayList(sortetedResults.keySet());
        for (int i = 0; i < nbrResults; i++) {
            final String currentKey = sortedKeys.get(i);
            filteredResults.put(currentKey, sortetedResults.get(currentKey));
        }
        return filteredResults;
    }
}
