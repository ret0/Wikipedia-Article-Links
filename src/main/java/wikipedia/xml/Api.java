package wikipedia.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Api {

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
