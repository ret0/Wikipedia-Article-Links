package wikipedia.http;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.ImmutableList;


public class TestCategoryMemeberFetcher {


    @Test
    public void testSimple() {
        CategoryMemberFetcher cmf = new CategoryMemberFetcher(ImmutableList.of("Kategorie:Popsänger"), "de");
        final List<String> allPagesInAllCategories = cmf.getAllPagesInAllCategories();
        Assert.assertEquals(1995, allPagesInAllCategories.size());
        for (String pageName: allPagesInAllCategories) {
            System.out.println(pageName);
        }
    }

}
