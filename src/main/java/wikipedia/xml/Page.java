package wikipedia.xml;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Page {

	@ElementList
	private List<Rev> revisions;

	public void setRevisions(final List<Rev> revisions) {
		this.revisions = revisions;
	}

	public List<Rev> getRevisions() {
		return revisions;
	}

}