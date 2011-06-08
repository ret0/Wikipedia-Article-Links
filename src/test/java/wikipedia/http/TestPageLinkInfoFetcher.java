package wikipedia.http;

import org.junit.Test;

import wikipedia.database.DBUtil;

public class TestPageLinkInfoFetcher {

    @Test
    public void getLinksByDate() {
        DBUtil dbu = new DBUtil();
        //PageLinkInfoFetcher plf = new PageLinkInfoFetcher("Barack Obama", "en", new DateTime().withYear(2004), dbu.getJdbcTemplate());
        //PageLinkInfoFetcher plf2 = new PageLinkInfoFetcher("Måns Zelmerlöw", "en", new DateTime().withYear(2010), dbu.getJdbcTemplate());
        //System.out.println(plf.getLinkInformation());
        //System.out.println(plf2.getLinkInformation());
    }

    @Test
    public void getFirstDate() {
//        FirstRevisionFetcher frf = new FirstRevisionFetcher("en", "Barack Obama");
//        System.out.println(frf.getFirstRevisionDate());
//        FirstRevisionFetcher frf2 = new FirstRevisionFetcher("en", "Måns Zelmerlöw");
//        System.out.println(frf2.getFirstRevisionDate());
    }
}
