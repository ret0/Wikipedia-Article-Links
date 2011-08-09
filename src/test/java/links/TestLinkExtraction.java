package links;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import wikipedia.http.PageLinkInfoFetcher;

public final class TestLinkExtraction {

    @Test
    @Ignore
    public void testLinks() throws IOException {
        String pageText = FileUtils.readFileToString(new File("testinput/beenieman.txt"));
        List<String> allInternalLinks = PageLinkInfoFetcher.getAllInternalLinks(pageText);
        System.out.println(StringUtils.join(allInternalLinks, "\n"));
    }

    @Test
    public void testLangLinksRegxp() throws IOException {
        Pattern pattern = Pattern.compile("^:?[a-z\\-]{2,10}:.*$");
        Matcher matcher = pattern.matcher("bla:fos");
        Matcher matcher2 = pattern.matcher("iu-vro:sdsd");
        Matcher matcher3 = pattern.matcher(":iu-vro:sdsd");
        System.out.println(matcher.matches());
        System.out.println(matcher2.matches());
        System.out.println(matcher3.matches());
    }

    @Test
    public void testMultiLinks() throws IOException {
        String pageText = "[[Michael Jackson]][[Michael Jackson]] bla[[Michael Jackson]]";
        List<String> allInternalLinks = PageLinkInfoFetcher.getAllInternalLinks(pageText);
        System.out.println(allInternalLinks);
    }





}
