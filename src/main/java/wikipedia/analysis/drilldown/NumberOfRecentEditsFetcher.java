package wikipedia.analysis.drilldown;

import util.HTTPUtil;

public class NumberOfRecentEditsFetcher {

    private final String pageTitle;
    private final String lang;

    public NumberOfRecentEditsFetcher(final String lang, final String pageTitle) {
        this.lang = lang;
        this.pageTitle = pageTitle;
    }

    public int getNumberOfEditsInLastWeeks(final int numberOfWeeks) {
        String dateStringNow;
        String dateStringBackThen;
        return -1;
    }

    private String getURL(final String startDate, final String endDate) {
        final String encodedPageName = HTTPUtil.urlEncode(pageTitle);
        return "http://" + lang +
        ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles=" + encodedPageName +
        "&rvlimit=1&rvprop=timestamp&rvdir=newer";
    }

}
