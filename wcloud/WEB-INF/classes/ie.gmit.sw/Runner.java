package ie.gmit.sw;

import java.io.File;
import java.io.IOException;

public class Runner {

    public static void main(String[] args) throws IOException {
        Search search = new Search();
//        search.getSearchResults("Galway city");
        search.hardcoded("Galway");
    }
}
