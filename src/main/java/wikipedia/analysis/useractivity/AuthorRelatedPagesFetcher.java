package wikipedia.analysis.useractivity;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.MapSorter;
import wikipedia.network.GraphEdge;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AuthorRelatedPagesFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorRelatedPagesFetcher.class.getName());

    private final Set<String> allPagesInNetwork;
    private final String lang;

    public AuthorRelatedPagesFetcher(final Set<String> allPagesInNetwork, final String lang) {
        this.allPagesInNetwork = allPagesInNetwork;
        this.lang = lang;
    }

    public Set<String> getRelatedPages() {
        Map<String, List<String>> topAuthors = collectTopAuthorsOfPages();

        Set<String> activeAuthors = Sets.newHashSet();
        for (Entry<String, List<String>> topAuthor : topAuthors.entrySet()) {
            for (String authorName : topAuthor.getValue()) {
                activeAuthors.add(authorName);
            }
        }

        UsertalkNetworkFetcher talkFetcher = new UsertalkNetworkFetcher(lang, Lists.newArrayList(activeAuthors));
        List<GraphEdge> allUserTalkEdges = talkFetcher.getNetwork();

        DefaultGraph graph = new SingleGraph("graphForShortestPath");
        //graph.setAutoCreate(true);
        for (String authorName : activeAuthors) {
            graph.addNode(authorName);
        }
        Random r = new Random();
        for (GraphEdge edge : allUserTalkEdges) {
            graph.addEdge(edge.getFrom() + edge.getTo() + r.nextDouble(), edge.getFrom(), edge.getTo(), true);
        }

        BetweennessCentrality bcb = new BetweennessCentrality();
        bcb.setWeightAttributeName("weight");
        bcb.init(graph);
        LOG.info("Calulation: BetweennessCentrality");
        bcb.compute();

        Map<String, Double> nodeBetweenness = Maps.newHashMap();
        for (Node node : graph) {
            System.out.println(node.getId() + " - " + node.getAttribute("Cb"));
            nodeBetweenness.put(node.getId(), (Double) node.getAttribute("Cb"));
        }
        nodeBetweenness = new MapSorter<String, Double>().sortByValue(nodeBetweenness);
        List<String> subList = Lists.newArrayList(nodeBetweenness.keySet()).subList(0, 10);
        System.out.println(subList);
        return Sets.newHashSet();


    }

    private Map<String, List<String>> collectTopAuthorsOfPages() {
        Map<String, List<String>> topAuthors = Maps.newHashMap();
        for (String pageName : allPagesInNetwork) {
            PageRevisionFetcher revisionFetcher = new PageRevisionFetcher(lang, pageName);
            Revisions articleRevisions = revisionFetcher.getArticleRevisions();
            List<ArticleContributions> topContributions = AuthorRankingSorter
                    .generateTopAuthorRanking(articleRevisions, 5);
            List<String> topUsers = Lists.newArrayList();
            for (ArticleContributions contribution : topContributions) {
                topUsers.add(contribution.getUserID());
            }
            topAuthors.put(pageName, topUsers);
        }
        return topAuthors;
    }

}
