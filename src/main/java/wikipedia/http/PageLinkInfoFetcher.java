package wikipedia.http;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HTTPUtil;
import wikipedia.network.PageLinkInfo;
import wikipedia.xml.Api;
import wikipedia.xml.Page;
import wikipedia.xml.Rev;
import wikipedia.xml.XMLTransformer;

import com.google.common.collect.Lists;

/**
 * Dowloads the information of all outgoing links (to other wikipedia pages) of a wikipedia page
 */
public final class PageLinkInfoFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(PageLinkInfoFetcher.class.getName());

    private final String lang;
    private final String pageName;
    private final DateTime revisionDate;

    private final WikiAPIClient wikiAPIClient;

    public PageLinkInfoFetcher(final String pageName, final String lang,
            final DateTime revisionDate, final WikiAPIClient wikiAPIClient) {
        this.pageName = pageName;
        this.lang = lang;
        this.revisionDate = revisionDate;
        this.wikiAPIClient = wikiAPIClient;
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
            if (StringUtils.isEmpty(pageText)) { // text blank at revision
                return new PageLinkInfo(pageName, revisionDate, new LinkedList<String>(),
                        relevantPageInfo.getPageid(), relevantRevision.getRevid());
            } else {
                final List<String> allInternalLinksOnPage = getAllInternalLinks(pageText);
                return new PageLinkInfo(pageName, revisionDate, allInternalLinksOnPage,
                        relevantPageInfo.getPageid(), relevantRevision.getRevid());
            }
        } else {
            throw new RuntimeException(
                    "Problem while getting revisions from Wikipedia API, URL was: " + url);
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

    private String extractPageName(final String link) {
        String fixedLink = StringUtils.removeStart(link, "[[");
        fixedLink = StringUtils.removeEnd(link, "]]");
        return StringUtils.substringBefore(fixedLink, "|");
    }

    private String getURL() {
        final String timestamp = revisionDate.toString(ISODateTimeFormat.dateHourMinuteSecond())
                + "Z";
        final String encodedPageName = HTTPUtil.urlEncode(pageName);
        final String revisionProperties = HTTPUtil.urlEncode("content|ids");
        return "http://" + lang
                + ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles="
                + encodedPageName + "&rvlimit=1&rvprop=" + revisionProperties + "&rvstart="
                + timestamp;
    }

}
