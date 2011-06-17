package wikipedia.network;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import util.Const;

/**
 * Infos about the outgoing links of a page a the given point in time
 */
public final class PageLinkInfo {

    private final String pageTitle;
    private final int pageID;
    private final DateTime revisionDate;
    private final List<String> outgoingLinks;

    public PageLinkInfo(final String pageName, final DateTime revisionDate,
            final List<String> outgoingLinks, final int pageID) {
        this.pageTitle = pageName;
        this.revisionDate = revisionDate;
        this.outgoingLinks = outgoingLinks;
        this.pageID = pageID;
    }

    @Override
    public String toString() {
        return "PageLinkInfo [pageName=" + pageTitle + ", revisionDate=" + revisionDate
                + ", outgoingLinks=" + StringUtils.join(outgoingLinks, "\n") + "]";
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public int getPageID() {
        return pageID;
    }

    public DateTime getTimeStamp() {
        return revisionDate;
    }

    public String getLinksAsString() {
        return StringUtils.join(outgoingLinks, Const.LINK_SEPARATOR);
    }

    public List<String> getLinks() {
        return outgoingLinks;
    }

}
