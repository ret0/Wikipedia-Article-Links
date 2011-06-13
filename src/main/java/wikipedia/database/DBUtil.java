package wikipedia.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbcp.BasicDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import wikipedia.network.PageLinkInfo;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DBUtil {

    private static final int MAX_TITLE_LENGTH = 256;
    public static final String MYSQL_DATETIME = "YYYY-MM-dd HH:mm:ss";
    public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormat
            .forPattern(MYSQL_DATETIME);

    private final static Logger LOG = LoggerFactory.getLogger(DBUtil.class.getName());

    /**
     * SQL Stmts
     */
    private static final String SELECT_PAGE_COUNT = "SELECT count(0) FROM pages WHERE page_id = ?";
    private static final String INSERT_PAGE_STMT = "INSERT INTO pages (page_id, page_title, creation_date) VALUES (?, ?, ?)";

    private final SimpleJdbcTemplate jdbcTemplate;

    public DBUtil() {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("context.xml"));
        jdbcTemplate = new SimpleJdbcTemplate((BasicDataSource) beanFactory.getBean("dataSource"));
    }

    public void storePageLinkInfo(final PageLinkInfo pliToBeStored,
                                  final DateTime firstRevisionDate) {
        // if page entry already there, only store revision
        // FIXME performance
        int numberOfPageEntries = jdbcTemplate.queryForInt(SELECT_PAGE_COUNT,
                new Object[] { pliToBeStored.getPageID() });
        final String timeStamp = pliToBeStored.getTimeStamp().toString(MYSQL_DATETIME_FORMATTER);
        final String firstRevisionDateTime = firstRevisionDate.toString(MYSQL_DATETIME_FORMATTER);

        if (numberOfPageEntries == 0) {
            jdbcTemplate.update(INSERT_PAGE_STMT, new Object[] { pliToBeStored.getPageID(),
                    pliToBeStored.getPageTitle(), firstRevisionDateTime });
        }

        for (String outgoingLink : pliToBeStored.getLinks()) {
            try {
                jdbcTemplate
                        .queryForInt(
                                "SELECT src_page_id FROM outgoing_links WHERE target_page_title = ? AND revision_date = ? AND src_page_id = ?",
                                new Object[] { outgoingLink, timeStamp, pliToBeStored.getPageID() });
            } catch (EmptyResultDataAccessException e) {
                if(outgoingLink.length() < MAX_TITLE_LENGTH) {
                    jdbcTemplate
                    .update("INSERT INTO outgoing_links (src_page_id, target_page_title, revision_date) VALUES (?, ?, ?)",
                            new Object[] { pliToBeStored.getPageID(), outgoingLink, timeStamp });
                }
            }
        }

        /*
         * int numberOfRevisionEntries =
         * jdbcTemplate.queryForInt(SELECT_REVISION_COUNT, new Object[]
         * {timeStamp, pliToBeStored.getPageID()});
         *
         * if(numberOfRevisionEntries == 0) {
         * jdbcTemplate.update(INSERT_REVISION_STMT, new Object[] {
         * pliToBeStored.getRevisionID(), timeStamp, pliToBeStored.getPageID(),
         * pliToBeStored.getLinksAsString() }); }
         */

    }

    public String getFirstRevisionDate(final int pageId,
                                       final String lang) {
        try {
            return jdbcTemplate.queryForObject("SELECT creation_date FROM pages WHERE page_id = ?",
                    String.class, new Object[] { pageId });
        } catch (EmptyResultDataAccessException e) {
            return "";
        }

    }

    public boolean localDataForRecordUnavailable(final int pageId,
                                                 final DateTime revisionDate) {
        int numRows = jdbcTemplate.queryForInt(
                "SELECT COUNT(0) FROM outgoing_links WHERE revision_date = ? AND src_page_id = ?",
                new Object[] {
                        revisionDate.toString(DateTimeFormat.forPattern(DBUtil.MYSQL_DATETIME)),
                        pageId });
        return numRows == 0;
    }

    public Collection<String> getAllLinksForRevision(final int pageId,
                                                     final String dateTime) {
        try {
            List<Map<String, Object>> allLinksString = jdbcTemplate
                    .queryForList("SELECT target_page_title FROM outgoing_links WHERE src_page_id = ? AND revision_date = ?",
                            pageId, dateTime);
            return Collections2.transform(allLinksString,
                    new Function<Map<String, Object>, String>() {
                        public String apply(final Map<String, Object> input) {
                            return input.get("target_page_title").toString();
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            LOG.info("NO LINKS! -- PageID : " + pageId + " -- Date: " + dateTime);
            return Lists.newArrayList();
        }
    }

    public void storeAllCategoryMemberPages(final String categoryName,
                                            final Map<Integer, String> allPageTitles) {
        int categoryID = -1;
        try {
            categoryID = jdbcTemplate.queryForInt(
                    "SELECT category_id FROM categories WHERE category_name = ?",
                    new Object[] { categoryName });
        } catch (EmptyResultDataAccessException e) {
            jdbcTemplate.update("INSERT INTO categories (category_name) VALUES (?)",
                    new Object[] { categoryName });
        }
        categoryID = jdbcTemplate.queryForInt(
                "SELECT category_id FROM categories WHERE category_name = ?",
                new Object[] { categoryName });
        for (Entry<Integer, String> entry : allPageTitles.entrySet()) {
            LOG.info("-- " + entry);
            try {
                jdbcTemplate.update(
                        "INSERT INTO pages_in_categories (page_id, category_id) VALUES (?, ?)",
                        new Object[] { entry.getKey(), categoryID });
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                LOG.error("IGNORING ARTICLE: " + entry + " (probably too new!)");
            }
        }
    }

    public boolean categoryMembersInDatabase(final String categoryName) {
        try {
            int categoryID = jdbcTemplate.queryForInt(
                    "SELECT category_id FROM categories WHERE category_name = ?",
                    new Object[] { categoryName });
            return categoryID > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Map<Integer, String> getCategoryMembersByCategoryName(final String categoryName) {
        String sqlStmt = "SELECT pages.page_title, pages.page_id FROM pages_in_categories " +
        "JOIN pages ON pages.page_id = pages_in_categories.page_id " +
        "JOIN categories ON categories.category_id = pages_in_categories.category_id " +
        "WHERE categories.category_name = ?";
        List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(sqlStmt, categoryName);
        Map<Integer, String> categoryMembers = Maps.newHashMap();
        for (Map<String, Object> resultRow : sqlResult) {
            categoryMembers.put((Integer) resultRow.get("page_id"), (String) resultRow.get("page_title"));
        }
        return categoryMembers;
    }

}
