package wikipedia.xml;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Page {

    @Attribute(required = false)
    private int pageid;

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