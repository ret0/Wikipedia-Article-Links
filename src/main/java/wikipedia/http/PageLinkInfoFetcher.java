package wikipedia.http;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.database.DBUtil;
import wikipedia.network.PageLinkInfo;
import wikipedia.xml.Api;
import wikipedia.xml.Page;
import wikipedia.xml.Rev;
import wikipedia.xml.XMLTransformer;

import com.google.common.collect.Lists;

public class PageLinkInfoFetcher {

    private final static Logger LOG = LoggerFactory.getLogger(PageLinkInfoFetcher.class.getName());

    private final DefaultHttpClient httpclient = new DefaultHttpClient();
    private final WikiAPIClient wikiAPIClient = new WikiAPIClient(httpclient);

    private final String lang;
    private final String pageName;
    private final DateTime revisionDate;
    private final int pageId;
    private final DBUtil dbUtil;

    public PageLinkInfoFetcher(final String pageName, final int pageId, final String lang, final DateTime revisionDate, final DBUtil dbUtil) {
        this.pageName = pageName;
        this.pageId = pageId;
        this.lang = lang;
        this.revisionDate = revisionDate;
        this.dbUtil = dbUtil;
    }

    public PageLinkInfo getLinkInformation() {
        final String url = getURL();
        LOG.debug("Fetching URL: " + url);
        String xmlResponse = wikiAPIClient.executeHTTPRequest(url);
        Api revisionFromXML = XMLTransformer.getRevisionFromXML(xmlResponse);
        final Page relevantPageInfo = revisionFromXML.getQuery().getPages().get(0);
        if (relevantPageInfo.getRevisions() != null) {
            final Rev relevantRevision = relevantPageInfo.getRevisions().get(0);
            final String pageText = relevantRevision.getValue();
            if(StringUtils.isEmpty(pageText)) {// text blank at revision
                return new PageLinkInfo(pageName, revisionDate, new LinkedList<String>(), relevantPageInfo.getPageid(), relevantRevision.getRevid());
            } else {
                final List<String> allInternalLinksOnPage = getAllInternalLinks(pageText);
                return new PageLinkInfo(pageName, revisionDate, allInternalLinksOnPage, relevantPageInfo.getPageid(), relevantRevision.getRevid());
            }
        } else {
            throw new RuntimeException("Problem while getting revisions from Wikipedia API, URL was: " + url);
        }
    }

    private List<String> getAllInternalLinks(final String pageText) {
        List<String> allInternalLinks = Lists.newArrayList();
        final String internalLinkRegexp = "\\[{2}.+?\\]{2}";
        Matcher matcher = Pattern.compile(internalLinkRegexp, Pattern.DOTALL).matcher(pageText);
        while (matcher.find()) {
            final String link = matcher.group(matcher.groupCount());
            allInternalLinks.add(extractPageName(link));
        }
        return allInternalLinks;
    }

    private String extractPageName(String link) {
        link = StringUtils.removeStart(link, "[[");
        link = StringUtils.removeEnd(link, "]]");
        return StringUtils.substringBefore(link, "|");
    }

    private String getURL() {
        final String timestamp = revisionDate.toString(ISODateTimeFormat.dateHourMinuteSecond()) + "Z";
        final String encodedPageName = HTTPUtil.URLEncode(pageName);
        final String revisionProperties = HTTPUtil.URLEncode("content|ids");
        return "http://" + lang +
        ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles=" + encodedPageName +
        "&rvlimit=1&rvprop=" + revisionProperties + "&rvstart=" + timestamp;
    }

    public boolean localDataUnavailable() {
        return dbUtil.localDataForRecordUnavailable(pageId, revisionDate);
    }


}
