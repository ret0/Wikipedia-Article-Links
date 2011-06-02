package wikipedia.http;

import org.joda.time.DateTime;
import org.junit.Test;

public class TestPageLinkInfoFetcher {

    @Test
    public void getLinksByDate() {
        PageLinkInfoFetcher plf = new PageLinkInfoFetcher("Barack Obama", "en", new DateTime().withYear(2004));
        PageLinkInfoFetcher plf2 = new PageLinkInfoFetcher("Måns Zelmerlöw", "en", new DateTime().withYear(2010));
        System.out.println(plf.getLinkInformation());
        System.out.println(plf2.getLinkInformation());
    }
}
