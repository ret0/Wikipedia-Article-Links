package wikipedia.xml;

import org.simpleframework.xml.Attribute;

/**
 * Wikipedia API Search Results
 */
public final class Search {

    @Attribute
    private int ns;

    @Attribute
    private String title;

    public int getNs() {
        return ns;
    }

    public void setNs(final int ns) {
        this.ns = ns;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}
