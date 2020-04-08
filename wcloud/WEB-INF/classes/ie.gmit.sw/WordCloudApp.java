package ie.gmit.sw;

import ie.gmit.sw.ai.cloud.WordFrequency;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordCloudApp {
    public static AtomicInteger totalLinksCounter =  new AtomicInteger();
    int counter;
    private String query;
    private ExecutorService executorService = Executors.newWorkStealingPool();
    private List<Future<WordFrequency[]>> jobList = new LinkedList<>();
    private static Map<String, Integer> wordFrequencyMap = new ConcurrentHashMap<>();

    public WordCloudApp(String query){
        this.query = query;
    }

    public WordFrequency[] createWordCloud() throws IOException, ExecutionException, InterruptedException {
        WordFrequency[] wordFrequencies;
        Search search = new Search();
        IgnoreWordsParser ignoreWordsParser = new IgnoreWordsParser();
        Set<String> links = search.getSearchResults(query);

        System.out.println("total links: "+ links.size());
    counter = links.size();
        for(String link : links){
            NodeParser nodeParser = new NodeParser(link, query);
            Future<WordFrequency[]> wf = executorService.submit(nodeParser);
            jobList.add(wf);
        }

        WordFrequencyCounter counter = WordFrequencyCounter.getInstance();

        while(!jobList.isEmpty()){
            jobList.removeIf(this::isDone);
            Thread.sleep(10);
        }

        return counter.getFrequency();
    }

    private boolean isDone(Future<WordFrequency[]> future){
        if(future.isDone()){
            counter--;
            System.out.println("left.." + counter);
            return true;
        }
        return false;
    }


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
       // System.out.println("File path: " + new File("\"ignorewords.txt\"").getAbsolutePath());
        WordCloudApp app = new WordCloudApp("Galway");
        WordFrequency[] data = app.createWordCloud();

        Arrays.stream(data).forEach(System.out::println);
    }
}
