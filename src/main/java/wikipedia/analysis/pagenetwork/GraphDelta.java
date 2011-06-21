package wikipedia.analysis.pagenetwork;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import wikipedia.network.GraphEdge;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Bean to create JSON output for the differences between two animation frames.
 */
public final class GraphDelta {

    private static final String ITEM_SEPARATOR = ", \n";
    private final List<String> add;
    private final List<String> del;
    private final List<GraphEdge> links;
    private final String formattedDate;

    public GraphDelta(final List<String> add,
                      final List<String> del,
                      final List<GraphEdge> links,
                      final String formattedDate) {
        this.add = add;
        this.del = del;
        this.links = links;
        this.formattedDate = formattedDate;
    }

    public String toJSON() {
        return new StringBuilder()
        .append("{")
        .append("date: \"").append(formattedDate).append("\", ")
        .append("add: [").append(printAllAddNodes()).append("], ")
        .append("del: [").append(printAllDelNodes()).append("], ")
        .append("links: [").append(printAllLinks()).append("]")
        .append("}").toString();
    }

    private String printAllLinks() {
        return StringUtils.join(Collections2.transform(links, new LinkPrinter()), ITEM_SEPARATOR);
    }

    private String printAllDelNodes() {
        return StringUtils.join(Collections2.transform(del, new NodePrinter()), ITEM_SEPARATOR);
    }

    private String printAllAddNodes() {
        return StringUtils.join(Collections2.transform(add, new NodePrinter()), ITEM_SEPARATOR);
    }

    /** Functor to print node string to json */
    private final class NodePrinter implements Function<String, String> {
        public String apply(final String input) {
            return "{ nodeName: \"" + input + "\" }";
        }
    }

    /** Functor to print link string to json */
    private final class LinkPrinter implements Function<GraphEdge, String> {
        public String apply(final GraphEdge link) {
            return "{ source: \"" + link.getFrom() + "\", target: \"" + link.getTo() + "\", value: 1 }";
        }
    }
}
