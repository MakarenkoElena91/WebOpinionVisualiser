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
    private static final int MAX_LIMIT = 20;
    private static Map<String, Integer> wordFrequencyMap = new ConcurrentHashMap<>();
    private static WordFrequency[] wordCounts = new WordFrequency[MAX_LIMIT];
    private static WordFrequencyCounter instance = new WordFrequencyCounter();
    int numberOfMaps = 0;

    private WordFrequencyCounter() {
    }

    public static WordFrequencyCounter getInstance(){
        return instance;
    }

    /**
     * reads text from each website and counts frequency of each word, sorts it, keeps just the first 100 of the most
     * frequent ones, ignores all words < 4 letters long and containing in ignorewords.txt file
     * @param text - text from a website (for now just body)
     * @throws IOException
     */
    public void add(String text) throws IOException {
        String[] wordsToIgnore = IgnoreWordsParser.getIgnoreWords().toArray(new String[0]);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.toLowerCase()
                .getBytes(StandardCharsets.UTF_8))));
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
                        //if word ends in 'es' it is most likely either plural nouns or verbs in the third person singular
                        //replace it with 'e', in order not to display similar words, e.g. language and languages
                        word = word.replaceAll("es$","e");
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
        System.out.println("Total number of nodes visited: "+ numberOfMaps);
        reader.close();

        // prune wordFrequencyMap
         List<Map.Entry<String, Integer>> list  = new LinkedList<>(wordFrequencyMap.entrySet());
         Collections.sort(list, (f, s) -> s.getValue() - f.getValue());

         list.stream()
                 .skip(100)
                 .map(Map.Entry::getKey)
                 .forEach(wordFrequencyMap::remove);
    }

    /**
     * Converts map to WordFrequency[], each object in an array contains Word and its Frequency
     * @return WordFrequency[]
     */
    public WordFrequency[] getFrequency(){
        // wordFrequencyMap.forEach((K, V) -> System.out.println(K + " " + V));
        List<WordFrequency> wordCountArrayList = new ArrayList<>();

        wordFrequencyMap.entrySet().forEach(entry -> {
            wordCountArrayList.add(new WordFrequency(entry.getKey(), entry.getValue()));
        });
        Collections.sort(wordCountArrayList);

        for (int i = 0; i < wordCounts.length; i++) {
            wordCounts[i] = wordCountArrayList.get(i);
           //System.out.println(wordCounts[i] + " ");
        }
        return wordCounts;
    }
    /**
     * Clears wordFrequencyMap, required for the next search
     */
    public void removeWords() {
        wordFrequencyMap.clear();
    }
}

