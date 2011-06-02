package wikipedia.http;

import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.xml.Api;
import wikipedia.xml.CategoryMember;
import wikipedia.xml.XMLTransformer;

import com.google.common.collect.Lists;

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

    public List<String> getAllPagesInAllCategories() {
        List<String> allPageTitles = Lists.newArrayList();
        for (String categoryName : categoryNames) {
            allPageTitles.addAll(getAllPagesInSingleCategory(categoryName));
        }
        return allPageTitles;
    }

    private List<String> getAllPagesInSingleCategory(final String categoryName) {
        List<String> allPageTitles = Lists.newArrayList();
        Api revisionResult = null;
        String queryContinue = "";
        while(true) {
            final String url = getURL(categoryName, queryContinue);
            LOG.info("Fetching URL: " + url);
            final String xmlResponse = wikiAPIClient.executeHTTPRequest(url);
            revisionResult = XMLTransformer.getRevisionFromXML(xmlResponse);
            for (CategoryMember member : revisionResult.getQuery().getCategorymembers()) {
                allPageTitles.add(member.getTitle());
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
        + encodedCategoryName + "&cmcontinue=" + encodedqueryContinue;
    }

}
