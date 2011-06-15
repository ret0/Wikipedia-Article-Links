package wikipedia.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * XML Element returned from Wikipedia API
 * see http://en.wikipedia.org/w/api.php
 */
@Root(strict = false)
public final class Api {

    @Element
    private Query query;

    @Element(name = "query-continue", required = false)
    private QueryContinue queryContinue;

    public QueryContinue getQueryContinue() {
        return queryContinue;
    }

    public void setQueryContinue(final QueryContinue queryContinue) {
        this.queryContinue = queryContinue;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(final Query query) {
        this.query = query;
    }

    public boolean isLastPageInRequestSeries() {
        return queryContinue == null;
    }

}
