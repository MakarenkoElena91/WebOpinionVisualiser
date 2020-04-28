package ie.gmit.sw;

import ie.gmit.sw.ai.cloud.WordFrequency;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

public class NodeParser implements Callable<WordFrequency[]> {
    private static final int MAX_NUMBER_OF_LINKS = 10;
    private static final int TITLE_WEIGHT = 50;//<title>
    private static final int HEADING_WEIGHT = 20;//h1
    private static final int PARAGRAPH_WEIGHT = 1;//body
    int linksCounter = 0;
    private String searchTerm;
    private String url;

    private Set<String> closed = new ConcurrentSkipListSet<>();
    private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));//sort this queue
    WordFrequencyCounter wordFrequencyCounter = WordFrequencyCounter.getInstance();

    private double effectivebranchingFactor;
    private int maxSearchDepth = 0;

    public double getEffectivebranchingFactor() {
        System.out.println("branchingFactor in get "+ effectivebranchingFactor);
        return effectivebranchingFactor;

    }

    public int getMaxSearchDepth() {
        return maxSearchDepth;
    }

    public void setEffectivebranchingFactor(double effectivebranchingFactor) {
        this.effectivebranchingFactor = effectivebranchingFactor;
    }

    public void setMaxSearchDepth(int maxSearchDepth) {
        this.maxSearchDepth = maxSearchDepth;
    }

    public NodeParser(String url, String searchTerm) {
        this.searchTerm = searchTerm;
        this.url = url;
    }

    /**
     * each 'thread' works with its duckduckgo link (expands its children)
     * @return
     */
    @Override
    public WordFrequency[] call() {
        Document doc;
        int score;
        try {
            doc = Jsoup.connect(url)
                    .timeout(50000)
                    .ignoreHttpErrors(true)
                    .get();
            score = getHeuristicScore(doc);
            closed.add(url);
            queue.offer(new DocumentNode(doc, score));
            process();
            //Arrays.stream(closed.toArray()).forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("JSoup couldn't connect: " + e.getMessage());
            e.printStackTrace();
        }

        return wordFrequencyCounter.getFrequency();
    }

    /**
     * Greedy Best First
     * Performance of the algorithm depends on how well the cost function is designed (fuzzy logic).
     * expands node if score is greater than 20
     */
    public void process() {
        while (!queue.isEmpty() && closed.size() <= MAX_NUMBER_OF_LINKS) {
            DocumentNode node = queue.poll();
            Document doc = node.getDocument();
            Elements edges = doc.select("a[href]");
            maxSearchDepth++;
            for (Element e : edges) {
                linksCounter = App.totalLinksCounter.incrementAndGet();
                String link = e.absUrl("href");
                if (link != null && closed.size() <= MAX_NUMBER_OF_LINKS && !closed.contains(link)) {
                    try {
                        Document child = Jsoup.connect(link).get();
                        int score = getHeuristicScore(child);

                        if (score > 20) {
                            wordFrequencyCounter.add(child.body().text());
                            //System.out.println("Child node " + link);
                            closed.add(link);
                            queue.offer(new DocumentNode(child, score));
                        }
                    } catch (IOException | IllegalArgumentException ex) {
                        System.err.println("Failed to open link: " + ex.getMessage() + " - " + link);
                    }
                }
            }
        }
        int visitedNodes = closed.size();
        double power = 1.0/(maxSearchDepth);
        effectivebranchingFactor = Math.pow(visitedNodes,power);
        System.out.println("branchingFactor "+ effectivebranchingFactor);
        System.out.println("maxSearchDepth "+ maxSearchDepth);
        System.out.println("Number of links " + linksCounter);
    }

    /**
     * gets the 'best' links according to heuristic score
     * @param doc html page
     * @return score calculated based on fuzzy logic
     * @throws IOException
     */
    private int getHeuristicScore(Document doc) throws IOException {
        int score;
        String title = doc.title();
        int titleScore = getFrequency(title, searchTerm) * TITLE_WEIGHT;
        //System.out.println("Title: " + " " + titleScore);

        int headingScore = 0;
        Elements headings = doc.select("h1, h2, h3");
        String heading = "";
        for (Element h : headings) {
            heading = h.text();
            headingScore += getFrequency(heading, searchTerm) * HEADING_WEIGHT;
        }
        //System.out.println("Heading: " + " " + headingScore);
        int bodyScore = 0;
        String body = doc.body().text();
        bodyScore += getFrequency(body, searchTerm) * PARAGRAPH_WEIGHT;
        //System.out.println("Text: " + " " + bodyScore);

        score = FuzzyLogic.getScore(titleScore, headingScore, bodyScore);
        //System.out.println("Score " + score);

        return score;
    }

    /**
     * counts how many times the search term appears on the page (either in title, or heading or body)
     * @param s - word frequency of which needs to be calculated
     * @param target - text where that word needs to be found
     * @return frequency of occurrence of specified word in specified text
     */
    public int getFrequency(String s, String target) {
        return (int) Arrays.stream(s.split("[ ,\\.]")).filter(e -> e.equals(target)).count();
    }
}
