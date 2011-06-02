package wikipedia.xml;

import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLTransformer {

    private final static Logger LOG = LoggerFactory.getLogger(XMLTransformer.class.getName());

    private static final Persister PERSISTER = new Persister();

    /**
     * Turns XML String into a Java object
     */
    public static Api getRevisionFromXML(final String xmlContent) {
        try {
            return PERSISTER.read(Api.class, xmlContent);
        } catch (Exception e) {
            LOG.error("Error while Parsing XML!", e);
            throw new RuntimeException(e);
        }
    }

}
