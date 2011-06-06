package wikipedia.network;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import util.Const;

public class PageLinkInfo {

    private final String pageTitle;
    private final int pageID;
    private final DateTime revisionDate;
    private final List<String> outgoingLinks;
    private final int revisionID;

    public PageLinkInfo(final String pageName, final DateTime revisionDate, final List<String> outgoingLinks, final int pageID, final int revisionID) {
        this.pageTitle = pageName;
        this.revisionDate = revisionDate;
        this.outgoingLinks = outgoingLinks;
        this.pageID = pageID;
        this.revisionID = revisionID;

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

    public int getRevisionID() {
        return revisionID;
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
