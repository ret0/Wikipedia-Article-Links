package wikipedia.http;

import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.xml.Api;
import wikipedia.xml.CategoryMember;
import wikipedia.xml.XMLTransformer;

import com.google.common.collect.Maps;

public class CategoryMemberFetcher {

    private final static Logger LOG = LoggerFactory.getLogger(CategoryMemberFetcher.class.getName());

    private final DefaultHttpClient httpclient = new DefaultHttpClient();
    private final WikiAPIClient wikiAPIClient = new WikiAPIClient(httpclient);

    private final List<String> categoryNames;
    private final String lang;


    public CategoryMemberFetcher(final List<String> categoryNames, final String lang) {
        this.categoryNames = categoryNames;
        this.lang = lang;
    }

    public Map<Integer, String> getAllPagesInAllCategories() {
        Map<Integer, String> allPageIDsAndTitles = Maps.newLinkedHashMap();
        for (String categoryName : categoryNames) {
            allPageIDsAndTitles.putAll(getAllPagesInSingleCategory(categoryName));
        }
        return allPageIDsAndTitles;
    }

    private Map<Integer, String> getAllPagesInSingleCategory(final String categoryName) {
        Map<Integer, String> allPageTitles = Maps.newLinkedHashMap();
        Api revisionResult = null;
        String queryContinue = "";
        while(true) {
            final String url = getURL(categoryName, queryContinue);
            LOG.info("Fetching URL: " + url);
            final String xmlResponse = wikiAPIClient.executeHTTPRequest(url);
            revisionResult = XMLTransformer.getRevisionFromXML(xmlResponse);
            for (CategoryMember member : revisionResult.getQuery().getCategorymembers()) {
                allPageTitles.put(member.getPageid(), member.getTitle());
            }
            if (revisionResult.getQueryContinue() == null) {
                break;
            } else {
                queryContinue = revisionResult.getQueryContinue().getCategorymembers().getCmcontinue();
            }
        }
        return allPageTitles;
    }

    private String getURL(final String categoryName, final String queryContinue) {
        final String encodedCategoryName = HTTPUtil.URLEncode(categoryName);
        final String encodedqueryContinue = HTTPUtil.URLEncode(queryContinue);
        return "http://" + lang +
        ".wikipedia.org/w/api.php?format=xml&action=query&cmlimit=500&list=categorymembers&cmtitle="
        + encodedCategoryName + "&cmnamespace=0&cmcontinue=" + encodedqueryContinue;
    }

}
