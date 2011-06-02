package wikipedia.network;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public class PageLinkInfo {

    private final String pageName;
    private final DateTime revisionDate;
    private final List<String> outgoingLinks;

    public PageLinkInfo(final String pageName, final DateTime revisionDate, final List<String> outgoingLinks) {
        this.pageName = pageName;
        this.revisionDate = revisionDate;
        this.outgoingLinks = outgoingLinks;
    }

    @Override
    public String toString() {
        return "PageLinkInfo [pageName=" + pageName + ", revisionDate=" + revisionDate
                + ", outgoingLinks=" + StringUtils.join(outgoingLinks, "\n") + "]";
    }


}
