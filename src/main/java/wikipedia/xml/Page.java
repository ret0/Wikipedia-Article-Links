package wikipedia.xml;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * XML Element returned from Wikipedia API
 * see http://en.wikipedia.org/w/api.php
 */
@Root(strict = false)
public final class Page {

    @Attribute(required = false)
    private int pageid;

    @Attribute(required = false)
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getPageid() {
        return pageid;
    }

    public void setPageid(final int pageid) {
        this.pageid = pageid;
    }

    @ElementList(required = false)
    private List<Rev> revisions;

    public void setRevisions(final List<Rev> revisions) {
        this.revisions = revisions;
    }

    public List<Rev> getRevisions() {
        return revisions;
    }

}
