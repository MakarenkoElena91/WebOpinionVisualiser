package ie.gmit.sw;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordFrequency {
    private static final int MAX_LIMIT = 10;
    private long time = System.currentTimeMillis();
    private static Map<String, Integer> wordFrequencyMap = new ConcurrentHashMap<>();
    private static WordCount[] wordCounts = new WordCount[MAX_LIMIT];
    private static WordFrequency database = null;
    private static ConcurrentHashMap<String, Integer> globalWordFrequencyMap = new ConcurrentHashMap<>();
    int numberOfMaps = 0;
    public static WordFrequency getInstance() {
        if (database == null) {
            database = new WordFrequency();
        }
        return database;
    }

    public synchronized Map<String, Integer> getFrequencyMap(String text) throws IOException {
        Object[] wordsToIgnore = IgnoreWordsParser.getIgnoreWords().toArray(new String[0]);
        int count;
        //Create BufferedReader so the words can be counted
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.toLowerCase().getBytes(StandardCharsets.UTF_8))));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.split("[^A-Za-z]");
            for (String word : words) {
                if ("".equals(word)) {
                    continue;
                }

                if (Arrays.asList(wordsToIgnore).contains(word.toLowerCase())) {
                    //do nothing
                } else {
                    if (wordFrequencyMap.containsKey(word)) {
                        count = wordFrequencyMap.get(word);
                        count++;
                        wordFrequencyMap.put(word, count);
                    } else {
                        wordFrequencyMap.put(word, 1);
                    }
                }
            }
        }
        numberOfMaps++;
        System.out.println(numberOfMaps);
        reader.close();
       // wordFrequencyMap.forEach((K, V) -> System.out.println(K + " " + V));
        return wordFrequencyMap;
    }

    public static WordCount[] getSortedFrequencyMap() {
        List<WordCount> wordCountArrayList = new ArrayList<>();

        wordFrequencyMap.entrySet().forEach(entry -> {
            wordCountArrayList.add(new WordCount(entry.getKey(), entry.getValue()));
        });
        Collections.sort(wordCountArrayList);

        for (int i = 0; i < wordCounts.length; i++) {
            wordCounts[i] = wordCountArrayList.get(i);
            System.out.println(wordCounts[i] + " ");
        }

        return wordCounts;
    }
}

