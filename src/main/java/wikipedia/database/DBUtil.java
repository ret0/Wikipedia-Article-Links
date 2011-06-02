package wikipedia.database;

import org.joda.time.format.DateTimeFormat;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import wikipedia.network.PageLinkInfo;

public class DBUtil {

    private static final String MYSQL_DATETIME = "YYYY-MM-dd HH:mm:ss";
    private static final String INSERT_PAGE_STMT = "INSERT INTO pages (page_id, page_title) VALUES (?,?)";
    private static final String INSERT_REVISION_STMT = "INSERT INTO page_revisions (revision_id, revision_timestamp, page_id, revision_links) "
            + "VALUES (?, ?, ?, ?)";
    private final SimpleJdbcTemplate jdbcTemplate;

    public DBUtil() {
        DriverManagerDataSource dataSource = prepareDataSource();
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private DriverManagerDataSource prepareDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/page_link_revisions");
        dataSource.setUsername("wikiCache");
        dataSource.setPassword("cache");
        return dataSource;
    }

    public void storePageLinkInfo(final PageLinkInfo pliToBeStored) {
        //if page entry already there, only store revision
        int numberOfPageEntries = this.jdbcTemplate.queryForInt("SELECT count(0) FROM pages WHERE page_id = ?", new Object[]{pliToBeStored.getPageID()});

        if(numberOfPageEntries == 0) {
            jdbcTemplate.update(INSERT_PAGE_STMT,
                    new Object[] { pliToBeStored.getPageID(), pliToBeStored.getPageTitle() });
        }

        final String timeStamp = pliToBeStored.getTimeStamp().toString(
                DateTimeFormat.forPattern(MYSQL_DATETIME));
        jdbcTemplate.update(INSERT_REVISION_STMT, new Object[] { pliToBeStored.getRevisionID(),
                timeStamp, pliToBeStored.getPageID(), pliToBeStored.getLinksAsString() });
    }

}
