package ie.gmit.sw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.TreeSet;

public class IgnoreWordsParser {
    private static String fileName = "res/ignoreWords.txt";

    /**
     * reads ignorewords.txt file and populates a TreeSet from that file
     * @return set of words to ignore
     */
    public static TreeSet<String> getIgnoreWords() {
        TreeSet<String> ignorewords = new TreeSet<>();
        try {
            File dir = new File(fileName);

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(dir)));
            String line;
            while ((line = br.readLine()) != null) {
                ignorewords.add(line.trim().toLowerCase());
            }
            //System.out.println(ignorewords);

        } catch (Exception e) {
            e.printStackTrace();
        }
       // System.out.println("Ignore words were added.");
        return ignorewords;
    }
    //add method which checks to ignore word or not
}
