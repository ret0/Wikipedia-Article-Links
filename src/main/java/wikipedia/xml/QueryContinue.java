package wikipedia.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class QueryContinue {

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
