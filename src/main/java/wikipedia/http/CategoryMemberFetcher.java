package wikipedia.http;

import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.analysis.pagenetwork.CategoryLists;
import wikipedia.database.DBUtil;
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

    private final DBUtil database;


    public CategoryMemberFetcher(final List<String> categoryNames, final String lang, final DBUtil database) {
        this.categoryNames = categoryNames;
        this.lang = lang;
        this.database = database;
    }

    public static void main(final String[] args) {
        // manually refresh all category members
        CategoryMemberFetcher cmf = new CategoryMemberFetcher(CategoryLists.ENGLISH_MUSIC, "en", new DBUtil());
        cmf.updateAllCategoryMembersInDB();
    }

    private void updateAllCategoryMembersInDB() {
        for (String categoryName : categoryNames) {
            final Map<Integer, String> allPageTitles = downloadCategoryMembers(categoryName);
            database.storeAllCategoryMemberPages(categoryName, lang, allPageTitles);
        }

    }

    public Map<Integer, String> getAllPagesInAllCategories() {
        Map<Integer, String> allPageIDsAndTitles = Maps.newLinkedHashMap();
        for (String categoryName : categoryNames) {
            allPageIDsAndTitles.putAll(getAllPagesInSingleCategory(categoryName));
        }
        return allPageIDsAndTitles;
    }

    private Map<Integer, String> getAllPagesInSingleCategory(final String categoryName) {
        if(categoryMembersInCache(categoryName)) {
            return getMembersFromDBCache(categoryName);
        } else {
            final Map<Integer, String> allPageTitles = downloadCategoryMembers(categoryName);
            database.storeAllCategoryMemberPages(categoryName, lang, allPageTitles);
            return allPageTitles;
        }
    }

    private boolean categoryMembersInCache(final String categoryName) {
        return database.categoryMembersInDatabase(categoryName);
    }

    private Map<Integer, String> getMembersFromDBCache(final String categoryName) {
        return database.getCategoryMembersByCategoryName(categoryName);
    }

    private Map<Integer, String> downloadCategoryMembers(final String categoryName) {
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
