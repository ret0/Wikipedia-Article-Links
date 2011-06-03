package wikipedia.http;

import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.xml.Api;
import wikipedia.xml.XMLTransformer;

public class FirstRevisionFetcher {

    private final static Logger LOG = LoggerFactory.getLogger(FirstRevisionFetcher.class.getName());

    private final DefaultHttpClient httpclient = new DefaultHttpClient();
    private final WikiAPIClient wikiAPIClient = new WikiAPIClient(httpclient);

    private final String pageTitle;
    private final String lang;

    public FirstRevisionFetcher(final String pageTitle, final String lang) {
        this.lang = lang;
        this.pageTitle = pageTitle;
    }

    public DateTime getFirstRevisionDate() {
        final String url = getURL();
        LOG.debug("Fetching URL: " + url);
        String xmlResponse = wikiAPIClient.executeHTTPRequest(url);
        Api revisionFromXML = XMLTransformer.getRevisionFromXML(xmlResponse);
        String timeStamp = revisionFromXML.getQuery().getPages().get(0).getRevisions().get(0).getTimestamp();
        return new DateTime(timeStamp);
    }

    private String getURL() {
        final String encodedPageName = HTTPUtil.URLEncode(pageTitle);
        return "http://" + lang +
        ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles=" + encodedPageName +
        "&rvlimit=1&rvprop=timestamp&rvdir=newer";
    }

}
