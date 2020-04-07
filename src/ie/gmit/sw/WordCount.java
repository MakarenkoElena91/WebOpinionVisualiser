package ie.gmit.sw;

public class WordCount implements Comparable<WordCount> {
    private String word;
    private int count;

    public WordCount(String word, int count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int hashCode() { return word.hashCode(); }

    @Override
    public boolean equals(Object obj) { return word.equals(((WordCount)obj).word); }

    @Override
    public int compareTo(WordCount b) { return b.count - count; }

    public String toString() {
        return "Word: " + word+ "\tFreq: " + count ;
    }
}