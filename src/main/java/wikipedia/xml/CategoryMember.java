package wikipedia.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "cm")
public class CategoryMember {

    @Attribute
    private int pageid;

    @Attribute
    private String ns;

    @Attribute
    private String title;

    public int getPageid() {
        return pageid;
    }
    public void setPageid(final int pageid) {
        this.pageid = pageid;
    }
    public String getNs() {
        return ns;
    }
    public void setNs(final String ns) {
        this.ns = ns;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(final String title) {
        this.title = title;
    }
}
