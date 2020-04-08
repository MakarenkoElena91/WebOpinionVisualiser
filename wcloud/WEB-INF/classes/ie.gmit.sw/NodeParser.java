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
    private static final int MAX_LINKS_FORM_DOCUMENT = 10;//100
    private static final int TITLE_WEIGHT = 50;//<title>
    private static final int HEADING_WEIGHT = 20;//h1
    private static final int PARAGRAPH_WEIGHT = 1;//body

    private String searchTerm;
    private String url;

    private Set<String> closed = new ConcurrentSkipListSet<>();
    private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));//sort this queue
    WordFrequencyCounter wordFrequencyCounter = WordFrequencyCounter.getInstance();

    public NodeParser(String url, String searchTerm ) {
        this.searchTerm = searchTerm;
        this.url = url;
    }

    @Override
    public WordFrequency[] call() {
        Document doc;
        int score;

        try {
            doc = Jsoup.connect(this.url)
                    .timeout(50000)
                    .ignoreHttpErrors(true)
                    .get();
            score = getHeuristicScore(doc);
            closed.add(this.url);
            queue.offer(new DocumentNode(doc, score));
            process();

        } catch (IOException e) {
            System.err.println("JSoup didin't connect: "+ e.getMessage());
            e.printStackTrace();
        }

        return wordFrequencyCounter.getFrequency();
    }

    public void process() {
        while (!queue.isEmpty() && closed.size() <= MAX_LINKS_FORM_DOCUMENT) {
            DocumentNode node = queue.poll();
            Document doc = node.getDocument();
            Elements edges = doc.select("a[href]");
            for (Element e : edges) {
                // link counter here
                WordCloudApp.totalLinksCounter.incrementAndGet();
                String link = e.absUrl("href");
                if (link != null && closed.size() <= MAX_LINKS_FORM_DOCUMENT && !closed.contains(link)) {
                    //if (link.contains(this.searchTerm)) {
                        try {

                            Document child = Jsoup.connect(link).get();
                            int score = getHeuristicScore(child);
                            if (score > 60) {
                                wordFrequencyCounter.add(child.body().text());
//                                index(child.body().text());
                                closed.add(link);
                                queue.offer(new DocumentNode(child, score));
                            }
                        } catch (IOException | IllegalArgumentException ex) {
                            System.err.println("Failed to open a link: " + ex.getMessage() + " - " + link);
                        }
                    //}
                }
            }
        }
    }
    //gets the 'best' links according to heuristic score
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

    //counts how many times the search term appears on the page (either in title, or heading or body)
    public int getFrequency(String s, String target) {
        return (int) Arrays.stream(s.split("[ ,\\.]")).filter(e -> e.equals(target)).count();
    }

//    private WordFrequency[] index(String text) throws IOException {
//        WordFrequency[] wordCounts =  wordFrequencyCounter.getFrequencyMap();
//        for (int i = 0; i < wordCounts.length; i++) {
//           // System.out.println("in NodeParse  "+ wordCounts[i] + " ");
//        }
//        return wordCounts;
//    }
}
