package ie.gmit.sw;

import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCloudApp {
    public static AtomicInteger totalLinksCounter = new AtomicInteger();
    int count;
    private String query;
    private ExecutorService executorService = Executors.newWorkStealingPool();
    private List<Future<WordFrequency[]>> jobList = new LinkedList<>();
    WordFrequency[] words;

    public WordCloudApp(String query) {
        this.query = query;
    }

    public WordFrequency[] createWordCloud() throws IOException, InterruptedException {
        Search search = new Search();

//        IgnoreWordsParser ignoreWordsParser = IgnoreWordsParser.getInstance();
        Set<String> links = search.getSearchResults(query);

        System.out.println("total duckduckgo links: " + links.size());
        count = links.size();
        for (String link : links) {
            NodeParser nodeParser = new NodeParser(link, query);
            Future<WordFrequency[]> wf = executorService.submit(nodeParser);
            jobList.add(wf);
        }

        WordFrequencyCounter wordFrequencyCounter = WordFrequencyCounter.getInstance();

        while (!jobList.isEmpty()) {
            jobList.removeIf(this::isDone);
            Thread.sleep(10);
        }
        words = wordFrequencyCounter.getFrequency();
        words = new WeightedFont().getFontSizes(words);
        Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));
        return words;
    }

    private boolean isDone(Future<WordFrequency[]> future) {
        if (future.isDone()) {
            count--;
            System.out.println("left.." + count);
            return true;
        }
        return false;
    }
    public WordFrequency[] removeWords() {
        words = new WordFrequency[0];
        return words;
    }
    public static void main(String[] args)  {
        String query = args[0];
        WordCloudApp app = new WordCloudApp(query);
        WordFrequency[] words = new WordFrequency[0];
        try {
            words = app.createWordCloud();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.stream(words).forEach(System.out::println);
        words = new WeightedFont().getFontSizes(words);

        Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));

        //Spira Mirabilis
        LogarithmicSpiralPlacer placer = new LogarithmicSpiralPlacer(1000, 800);
        for (WordFrequency word : words) {
            placer.place(word); //Place each word on the canvas starting with the largest
        }

        BufferedImage cloud = placer.getImage();
        File outputfile = new File("res/cloud.png");
        try {
            ImageIO.write(cloud, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));
    }
}
