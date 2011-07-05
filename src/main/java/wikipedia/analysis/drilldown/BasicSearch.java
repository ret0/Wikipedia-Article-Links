package wikipedia.analysis.drilldown;

import java.util.Collection;

import org.apache.http.impl.client.DefaultHttpClient;

import util.HTTPUtil;
import wikipedia.http.WikiAPIClient;
import wikipedia.xml.Api;
import wikipedia.xml.Search;
import wikipedia.xml.XMLTransformer;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public final class BasicSearch {

    private static final int NUMBER_OF_RESULTS = 20;

    private final WikiAPIClient wikiAPIClient = new WikiAPIClient(new DefaultHttpClient());

    private final String searchTerm;
    private final String lang;

    public BasicSearch(final String lang, final String searchTerm) {
        this.lang = lang;
        this.searchTerm = searchTerm;
    }

    public Collection<String> executeSearch() {
        final String xmlResponse = wikiAPIClient.executeHTTPRequest(getURL());
        final Api revisionResult = XMLTransformer.getRevisionFromXML(xmlResponse);
        return Collections2.transform(revisionResult.getQuery().getSearch(), new Function<Search, String>() {
            @Override
            public String apply(final Search input) {
                return input.getTitle();
            }
        });
    }

    private String getURL() {
        final String encodedSearchTerm = HTTPUtil.urlEncode(searchTerm);
        return "http://" + lang + ".wikipedia.org/w/api.php?format=xml&action=query&list=search&srsearch=" +
                encodedSearchTerm + "&srlimit=" + NUMBER_OF_RESULTS + "&srprop=";
    }

}
