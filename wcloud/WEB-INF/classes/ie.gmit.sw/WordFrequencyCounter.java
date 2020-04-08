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
    private static WordFrequencyCounter instance = new WordFrequencyCounter();
    int numberOfMaps = 0;
//    private String text;

    private WordFrequencyCounter() {
    }

    public static WordFrequencyCounter getInstance(){
        return instance;
    }
    public void add(String text) throws IOException {
        String[] wordsToIgnore = IgnoreWordsParser.getIgnoreWords().toArray(new String[0]);
        // int count;
        // read text from the website
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.toLowerCase().getBytes(StandardCharsets.UTF_8))));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.split("[^a-z]");
            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }

                if (Arrays.asList(wordsToIgnore).contains(word.toLowerCase()) || word.length() < 4) {
                    //do nothing
                } else {
                    synchronized (wordFrequencyMap) {
                        if (wordFrequencyMap.containsKey(word)) {
                            int count = wordFrequencyMap.get(word);
                            count++;
                            wordFrequencyMap.put(word, count);
                        } else {
                            wordFrequencyMap.put(word, 1);
                        }
                    }
                }
            }
        }
        numberOfMaps++;
        System.out.println("Pages checked: "+ numberOfMaps);
        reader.close();

        // TODO: prune wordFrequencyMap
//         var entries = wordFrequencyMap.entrySet();
         List<Map.Entry<String, Integer>> list  = new LinkedList<>(wordFrequencyMap.entrySet());
         Collections.sort(list, (f, s) -> s.getValue() -f.getValue());
        //sy
         list.stream()
                 .skip(300)
                 .map(Map.Entry::getKey)
                 .forEach(wordFrequencyMap::remove);
//        wordFrequencyMap.remove()
    }

    public WordFrequency[] getFrequency(){
        // wordFrequencyMap.forEach((K, V) -> System.out.println(K + " " + V));
        List<WordFrequency> wordCountArrayList = new ArrayList<>();

        wordFrequencyMap.entrySet().forEach(entry -> {
            wordCountArrayList.add(new WordFrequency(entry.getKey(), entry.getValue()));
        });
        Collections.sort(wordCountArrayList);

        for (int i = 0; i < wordCounts.length; i++) {
            wordCounts[i] = wordCountArrayList.get(i);
//           System.out.println(wordCounts[i] + " ");
        }
        return wordCounts;
    }
}

