package wikipedia.http;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import wikipedia.database.DBUtil;

import com.google.common.collect.ImmutableList;


public class TestCategoryMemberFetcher {


    @Test
    @Ignore
    public void testSimple() {
//        CategoryMemberFetcher cmf = new CategoryMemberFetcher(ImmutableList.of("Kategorie:Popsänger"), "de");
//        final Map<Integer, String> allPagesInAllCategories = cmf.getAllPagesInAllCategories();
//        Assert.assertEquals(1996, allPagesInAllCategories.size());
//        for (Entry<Integer, String> entry: allPagesInAllCategories.entrySet()) {
//            System.out.println(entry.getKey() + " - " + entry.getValue());
//        }
    }

    @Test
    public void testEnFemaleSingers() {
        CategoryMemberFetcher cmf = new CategoryMemberFetcher(ImmutableList.of("Category:American_female_singers"), "en", new DBUtil());
        final Map<Integer, String> allPagesInAllCategories = cmf.getAllPagesInAllCategories();
//        for (Entry<Integer, String> entry: allPagesInAllCategories.entrySet()) {
//            System.out.println(entry.getKey() + " - " + entry.getValue());
//        }
//        Assert.assertEquals(2751, allPagesInAllCategories.size());
    }

}
