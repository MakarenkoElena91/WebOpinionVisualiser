package ie.gmit.sw;

import ie.gmit.sw.ai.cloud.WordFrequency;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class NodeParser implements Runnable {
    private static final int MAX = 10;//100
    private static final int MAX_LIMIT = 10;
    private static final int TITLE_WEIGHT = 50;//<title>
    private static final int HEADING_WEIGHT = 20;//h1
    private static final int PARAGRAPH_WEIGHT = 1;//body

    private String searchTerm;
    private String url;

    private Set<String> closed = new ConcurrentSkipListSet<>();
    private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));//sort this queue
    WordFrequencyCounter wordFrequencyCounter = WordFrequencyCounter.getInstance();

    public NodeParser(String url, String searchTerm) {
        this.searchTerm = searchTerm;
        this.url = url;
    }

    @Override
    public void run() {
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
            e.printStackTrace();
        }
        // System.out.println("Searched "+this.url+" links" + i);
    }

    public void process() {
        while (!queue.isEmpty() && closed.size() <= MAX) {
            DocumentNode node = queue.poll();
            Document doc = node.getDocument();
            Elements edges = doc.select("a[href]");
            for (Element e : edges) {
                String link = e.absUrl("href");
                if (link != null && closed.size() <= MAX && !closed.contains(link)) {
                    if (link.contains(this.searchTerm)) {
                        try {
                            closed.add(link);
//                            System.out.println("URL: " + closed);
                            Document child = Jsoup.connect(link).get();
                            int score = getHeuristicScore(child);
                            queue.offer(new DocumentNode(child, score));
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
    }

    //gets the 'best' links according to heuristic score
    private int getHeuristicScore(Document doc) throws IOException {
        int score = 0;
        String title = doc.title();
        int titleScore = getFrequency(title, searchTerm) * TITLE_WEIGHT;
        System.out.println("Title: " + " " + titleScore);

        int headingScore = 0;
        Elements headings = doc.select("h1");
        String h1 = "";
        for (Element heading : headings) {
            h1 = heading.text();
            headingScore += getFrequency(h1, searchTerm) * HEADING_WEIGHT;
        }
        System.out.println("Heading: " + " " + headingScore);
        int bodyScore = 0;
        String body = doc.body().text();
        bodyScore += getFrequency(body, searchTerm) * PARAGRAPH_WEIGHT;
        System.out.println("Text: " + " " + bodyScore);

        score = FuzzyLogic.getScore(titleScore, headingScore, bodyScore);
        System.out.println("Score " + score);

        if (score > 60) {
            index(title, h1, body);
        }
        return score;
    }

    //counts how many times the search term appears on the page (either in title, or heading or body)
    public int getFrequency(String s, String target) {
        return (int) Arrays.stream(s.split("[ ,\\.]")).filter(e -> e.equals(target)).count();
    }

    private WordFrequency[] index(String... text) throws IOException {
        String allTexts = "";
        for (int i = 0; i < text.length; i++) {
            allTexts = allTexts.concat(text[i]);
        }
        wordFrequencyCounter.getFrequencyMap(allTexts);
        WordFrequency[] wordCounts = wordFrequencyCounter.getSortedFrequencyMap();
        return wordCounts;
    }
}
