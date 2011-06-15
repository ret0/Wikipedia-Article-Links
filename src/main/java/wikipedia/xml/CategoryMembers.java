package wikipedia.xml;

import org.simpleframework.xml.Attribute;

/**
 * XML Element returned from Wikipedia API
 * see http://en.wikipedia.org/w/api.php
 */
public final class CategoryMembers {

    @Attribute
    private String cmcontinue;

    public String getCmcontinue() {
        return cmcontinue;
    }

    public void setCmcontinue(final String cmcontinue) {
        this.cmcontinue = cmcontinue;
    }
}
