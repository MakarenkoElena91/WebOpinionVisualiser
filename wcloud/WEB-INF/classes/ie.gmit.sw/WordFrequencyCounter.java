package ie.gmit.sw;

import ie.gmit.sw.ai.cloud.WordFrequency;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WordFrequencyCounter {
    private static final int MAX_LIMIT = 10;
    private static Map<String, Integer> wordFrequencyMap = new ConcurrentHashMap<>();
    private static WordFrequency[] wordCounts = new WordFrequency[MAX_LIMIT];
    private static WordFrequencyCounter database = null;
    int numberOfMaps = 0;
    public static WordFrequencyCounter getInstance() {
        if (database == null) {
            database = new WordFrequencyCounter();
        }
        return database;
    }

    public Map<String, Integer> getFrequencyMap(String text) throws IOException {
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

                if (Arrays.asList(wordsToIgnore).contains(word.toLowerCase()) || word.length() < 4) {
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

    public static WordFrequency[] getSortedFrequencyMap() {
        List<WordFrequency> wordCountArrayList = new ArrayList<>();

        wordFrequencyMap.entrySet().forEach(entry -> {
            wordCountArrayList.add(new WordFrequency(entry.getKey(), entry.getValue()));
        });
        Collections.sort(wordCountArrayList);

        for (int i = 0; i < wordCounts.length; i++) {
            wordCounts[i] = wordCountArrayList.get(i);
            System.out.println(wordCounts[i] + " ");
        }
        return wordCounts;
    }
}

