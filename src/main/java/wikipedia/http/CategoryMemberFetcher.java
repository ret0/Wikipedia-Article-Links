package wikipedia.http;

import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<String> getAllPagesInCategories() {
        List<String> allPageTitles = Lists.newArrayList();

        return allPageTitles;
    }

}
