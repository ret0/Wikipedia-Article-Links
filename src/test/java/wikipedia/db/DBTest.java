package wikipedia.db;

import org.junit.Test;

import wikipedia.database.DBUtil;


public class DBTest {

    @Test
    public void insertPage() {
        DBUtil dbu = new DBUtil();
        //PageLinkInfoFetcher plif = new PageLinkInfoFetcher("Michael Jackson", "en", new DateTime().withYear(2004), dbu.getJdbcTemplate());
        //PageLinkInfo pliToBeStored = plif.getLinkInformation();
        //dbu.storePageLinkInfo(pliToBeStored);
    }

}
