package wikipedia.analysis.pagenetwork;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import wikipedia.network.GraphEdge;

import com.google.common.collect.Lists;

public class GraphDelta {

    private final List<String> add;
    private final List<String> del;
    private final List<GraphEdge> links;

    public GraphDelta(final List<String> add, final List<String> del, final List<GraphEdge> links) {
        this.add = add;
        this.del = del;
        this.links = links;
    }

    public String toJSON() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("add: [").append(printAllAddNodes()).append("], ");
        json.append("del: [").append(printAllDelNodes()).append("], ");
        json.append("links: [").append(printAllLinks()).append("]");
        json.append("}");
        return json.toString();
    }

    private String printAllLinks() {
        List<String> nodes = Lists.newArrayList();
        for (GraphEdge link: links) {
            nodes.add(printLink(link));
        }
        return StringUtils.join(nodes, ", \n");
    }

    private String printLink(final GraphEdge link) {
        return "{ source: \"" + link.getFrom() + "\", target: \"" + link.getTo() + "\", value: 2 }";
    }

    private String printAllDelNodes() {
        List<String> nodes = Lists.newArrayList();
        for (String addName : del) {
            nodes.add(printNode(addName));
        }
        return StringUtils.join(nodes, ", \n");
    }

    private String printAllAddNodes() {
        List<String> nodes = Lists.newArrayList();
        for (String addName : add) {
            nodes.add(printNode(addName));
        }
        return StringUtils.join(nodes, ", \n");
    }

    private String printNode(final String nodeName) {
        return "{ nodeName: \"" + nodeName + "\" }";
    }

}
