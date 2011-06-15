package util;

/**
 * General Constants for dealing with the Wikipedia API
 */
public final class Const {

    private Const() { }

    public static final String ENCODING = "UTF-8";
    public static final String USER_AGENT = "Reto Kleeb, MIT Center for Collective Intelligence, retokl@gmail.com";
    public static final String ANSWER_FORMAT = "&format=xml";

    /*
     * According to
     * http://en.wikipedia.org/wiki/Wikipedia:Page_name#Invalid_page_names the
     * pipe char | is illegal in a page name
     */
    public static final String LINK_SEPARATOR = "|";

}
