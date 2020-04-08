package ie.gmit.sw;

import org.jsoup.nodes.Document;

public class DocumentNode {
    private final Document document;
    private final int score;

    public DocumentNode(Document d, int score) {
        this.document = d;
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public int getScore() {
        return score;
    }
}
