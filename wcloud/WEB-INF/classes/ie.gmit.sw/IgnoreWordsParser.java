package ie.gmit.sw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.TreeSet;

public class IgnoreWordsParser {
    public static TreeSet<String> getIgnoreWords() {
        TreeSet<String> ignorewords = new TreeSet<>();
        try {
            ignorewords = new TreeSet<>();
            File dir = new File("ignorewords.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(dir)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] ignorewordsArray = line
                        .split(",\\s+|\\s*\\\"\\s*|\\s+|\\.\\s*|\\s*\\:\\s*");
                for (String word : ignorewordsArray) {
                    ignorewords.add(word.toLowerCase());
                }
            }
            //System.out.println(ignorewords);

            //call uniqueSortedWords.toArray() to have output in an array
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ignorewords;
    }

}
