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
            // ignorewords = new TreeSet<>();
            File dir = new File("ignorewords.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(dir)));
            String line;
            while ((line = br.readLine()) != null) {
                ignorewords.add(line.trim().toLowerCase());
            }
            //System.out.println(ignorewords);

            //call uniqueSortedWords.toArray() to have output in an array
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Ignore words was added.");
        return ignorewords;
    }
    //add method which checks to ignore word or not

}
