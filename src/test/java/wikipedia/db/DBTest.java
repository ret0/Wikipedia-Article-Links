package wikipedia.db;

import org.joda.time.DateTime;
import org.junit.Test;

import wikipedia.database.DBUtil;
import wikipedia.http.PageLinkInfoFetcher;
import wikipedia.network.PageLinkInfo;


public class DBTest {

    @Test
    public void insertPage() {
        DBUtil dbu = new DBUtil();
        PageLinkInfoFetcher plif = new PageLinkInfoFetcher("Michael Jackson", "en", new DateTime().withYear(2004));
        PageLinkInfo pliToBeStored = plif.getLinkInformation();
        dbu.storePageLinkInfo(pliToBeStored);
    }

}
