package wikipedia.xml;

import org.simpleframework.xml.Attribute;

public class Search {

    public int getNs() {
        return ns;
    }

    public void setNs(final int ns) {
        this.ns = ns;
    }


    @Attribute
    private int ns;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }


    @Attribute
    private String title;


}
