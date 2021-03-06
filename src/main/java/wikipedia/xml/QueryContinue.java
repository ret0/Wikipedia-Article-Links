package wikipedia.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * XML Element returned from Wikipedia API
 * see http://en.wikipedia.org/w/api.php
 */
@Root(strict = false)
public final class QueryContinue {

    @Element(required = false)
    private Revisions revisions;

    @Element(required = false)
    private CategoryMembers categorymembers;

    public CategoryMembers getCategorymembers() {
        return categorymembers;
    }

    public void setCategorymembers(final CategoryMembers categorymembers) {
        this.categorymembers = categorymembers;
    }

    public Revisions getRevisions() {
        return revisions;
    }

    public void setRevisions(final Revisions revisions) {
        this.revisions = revisions;
    }
}
