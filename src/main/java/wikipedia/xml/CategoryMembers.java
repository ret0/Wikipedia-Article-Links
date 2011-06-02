package wikipedia.xml;

import org.simpleframework.xml.Attribute;

public class CategoryMembers {

    @Attribute
    private String cmcontinue;

    public String getCmcontinue() {
        return cmcontinue;
    }

    public void setCmcontinue(final String cmcontinue) {
        this.cmcontinue = cmcontinue;
    }
}
