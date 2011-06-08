package wikipedia.database;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
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
import wikipedia.network.PageLinkInfo;

import com.google.common.collect.Lists;

public class DBUtil {

    public static final String MYSQL_DATETIME = "YYYY-MM-dd HH:mm:ss";
    public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormat.forPattern(MYSQL_DATETIME);

    private final static Logger LOG = LoggerFactory.getLogger(DBUtil.class.getName());

    /**
     * SQL Stmts
     */
    private static final String SELECT_REVISION_COUNT = "SELECT count(0) FROM page_revisions WHERE revision_timestamp = ? AND page_id = ?";
    private static final String SELECT_PAGE_COUNT = "SELECT count(0) FROM pages WHERE page_id = ?";

    private static final String INSERT_PAGE_STMT = "INSERT INTO pages (page_id, page_title, creation_date) VALUES (?, ?, ?)";
    private static final String INSERT_REVISION_STMT = "INSERT INTO page_revisions (revision_id, revision_timestamp, page_id, revision_links) "
            + "VALUES (?, ?, ?, ?)";

    private final SimpleJdbcTemplate jdbcTemplate;

    public DBUtil() {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("context.xml"));
        jdbcTemplate = new SimpleJdbcTemplate((BasicDataSource) beanFactory.getBean("dataSource"));
    }

    public void storePageLinkInfo(final PageLinkInfo pliToBeStored, final DateTime firstRevisionDate) {
        //if page entry already there, only store revision
        // FIXME performance
        int numberOfPageEntries = jdbcTemplate.queryForInt(SELECT_PAGE_COUNT, new Object[]{pliToBeStored.getPageID()});
        final String timeStamp = pliToBeStored.getTimeStamp().toString(MYSQL_DATETIME_FORMATTER);
        final String firstRevisionDateTime = firstRevisionDate.toString(MYSQL_DATETIME_FORMATTER);

        if(numberOfPageEntries == 0) {
            jdbcTemplate.update(INSERT_PAGE_STMT,
                    new Object[] { pliToBeStored.getPageID(), pliToBeStored.getPageTitle(), firstRevisionDateTime });
        }

        int numberOfRevisionEntries = jdbcTemplate.queryForInt(SELECT_REVISION_COUNT, new Object[] {timeStamp, pliToBeStored.getPageID()});

        if(numberOfRevisionEntries == 0) {
            jdbcTemplate.update(INSERT_REVISION_STMT, new Object[] { pliToBeStored.getRevisionID(),
                    timeStamp, pliToBeStored.getPageID(), pliToBeStored.getLinksAsString() });
        }

    }

    public String getFirstRevisionDate(final int pageId,
                                     final String lang) {
        try {
            return jdbcTemplate.queryForObject("SELECT creation_date FROM pages WHERE page_id = ?", String.class, new Object[] {pageId});
        } catch (EmptyResultDataAccessException e) {
            return "";
        }

    }

    public boolean localDataForRecordUnavailable(final int pageId,
                                                 final DateTime revisionDate) {
        try {
            jdbcTemplate.queryForInt("SELECT page_id FROM page_revisions WHERE revision_timestamp = ? AND page_id = ?",
                    new Object[] {revisionDate.toString(
                            DateTimeFormat.forPattern(DBUtil.MYSQL_DATETIME)), pageId});
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
        return false;
    }

    public List<String> getAllLinksForRevision(final int pageId, final String dateTime) {
        try {
            String allLinksString = jdbcTemplate.queryForObject(
                    "SELECT revision_links FROM page_revisions WHERE revision_timestamp = ? AND page_id = ?",
                    String.class, new Object[] {dateTime, pageId});
            return Lists.newArrayList(StringUtils.split(allLinksString, Const.LINK_SEPARATOR));
        } catch (EmptyResultDataAccessException e) {
            LOG.info("NO LINKS! -- PageID : " + pageId + " -- Date: " + dateTime);
            return Lists.newArrayList();
        }
    }

}
