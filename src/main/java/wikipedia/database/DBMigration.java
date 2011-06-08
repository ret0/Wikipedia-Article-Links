package wikipedia.database;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import util.Const;
import wikipedia.http.CategoryMemberFetcher;

import com.google.common.collect.Lists;

public class DBMigration {

    public static final String MYSQL_DATETIME = "YYYY-MM-dd HH:mm:ss";
    public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormat
            .forPattern(MYSQL_DATETIME);

    private final static Logger LOG = LoggerFactory.getLogger(DBMigration.class.getName());
    private static final int DELTA_MONTHS = 1;
    private static final int MAX_YEARS = 1;

    private final SimpleJdbcTemplate jdbcTemplate;

    public DBMigration() {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("context.xml"));
        jdbcTemplate = new SimpleJdbcTemplate((BasicDataSource) beanFactory.getBean("dataSource"));
    }

    public List<String> getAllLinksForRevision(final int pageId,
                                               final String dateTime) {
        try {
            String allLinksString = jdbcTemplate
                    .queryForObject(
                            "SELECT revision_links FROM page_revisions WHERE revision_timestamp = ? AND page_id = ?",
                            String.class, new Object[] { dateTime, pageId });
            return Lists.newArrayList(StringUtils.split(allLinksString, Const.LINK_SEPARATOR));
        } catch (EmptyResultDataAccessException e) {
            LOG.info("NO LINKS! -- PageID : " + pageId + " -- Date: " + dateTime);
            return Lists.newArrayList();
        }
    }

    private List<DateTime> getAllDatesForHistory(final DateMidnight latestDate) {
        List<DateTime> allDatesToFetch = Lists.newArrayList();
        DateTime earliestDate = latestDate.minusYears(MAX_YEARS).toDateTime();
        while (earliestDate.isBefore(latestDate)) {
            allDatesToFetch.add(earliestDate);
            earliestDate = earliestDate.plusMonths(DELTA_MONTHS);
        }
        return allDatesToFetch;
    }

    public static void main(final String[] args) {
        final DBMigration dbm = new DBMigration();
        List<String> categories = Lists.newArrayList("Category:World_Music_Awards_winners");
        final Map<Integer, String> allPagesInAllCategories = new CategoryMemberFetcher(categories,
                "en").getAllPagesInAllCategories();
        DateMidnight startDate = new DateMidnight(2011, 6, 1);
        final List<DateTime> allDatesForHistory = dbm.getAllDatesForHistory(startDate);

        ExecutorService threadPool = Executors.newFixedThreadPool(16);

        for (final Entry<Integer, String> entry : allPagesInAllCategories.entrySet()) {
            threadPool.execute(new Runnable() {
                public void run() {

                    for (final DateTime datetime : allDatesForHistory) {
                        final String revisionDateTime = datetime
                                .toString(DBUtil.MYSQL_DATETIME_FORMATTER);
                        LOG.info("Work: " + entry.getValue());
                        final Integer pageID = entry.getKey();
                        List<String> allLinksForRevision = dbm.getAllLinksForRevision(pageID,
                                revisionDateTime);
                        for (String outgoingLink : allLinksForRevision) {
                            if (!StringUtils.startsWith(outgoingLink, "File:")) {
                                dbm.storeOutgoingLink(pageID, outgoingLink, revisionDateTime);
                            }
                        }
                    }

                }
            });
        }

    }

    private void storeOutgoingLink(final Integer pageID,
                                   final String outgoingLink,
                                   final String revisionDateTime) {
        int numberOfRevisionEntries = jdbcTemplate
                .queryForInt(
                        "SELECT COUNT(0) FROM outgoing_links WHERE src_page_id = ? AND target_page_title = ? AND revision_date = ?",
                        new Object[] { pageID, outgoingLink, revisionDateTime });

        if (numberOfRevisionEntries == 0) {
            jdbcTemplate
                    .update("INSERT INTO outgoing_links (src_page_id, target_page_title, revision_date) VALUES (?, ?, ?)",
                            new Object[] { pageID, outgoingLink, revisionDateTime });
        }

    }

}
