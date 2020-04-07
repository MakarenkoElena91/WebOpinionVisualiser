package ie.gmit.sw;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Search {
    private final static String DUCK_DUCK_GO_SEARCH_URL = "https://duckduckgo.com/html/?q=";
    private final static int MAX_LIMIT = 50;
    private Set<String> links = new ConcurrentSkipListSet<>();
    Thread t;

    public Set<String> hardcoded(String query) {
        links.add("https://en.wikipedia.org/wiki/Galway");
        links.add("https://www.tripadvisor.com/Attractions-g186609-Activities-Galway_County_Galway_Western_Ireland.html");
        links.add("https://wikitravel.org/en/Galway,");
        links.add("https://twitter.com/galwaygaillimh");
        links.add("https://galwaydaily.com/");
        links.add("https://www.facebook.com/GalwayUnitedFC/");
        links.add("https://www.youtube.com/video/87gWaABqGYs");
        links.add("https://www.galwaygaa.ie/");
        links.add("https://www.tripadvisor.ie/Tourism-g186609-Galway_County_Galway_Western_Ireland-Vacations.html");
        links.add("https://www.galwaytourism.ie/");
        links.add("https://www.eurocentres.com/language-school-galway");
        links.add("https://en.wiktionary.org/wiki/Galway");
        links.add("https://galway2020.ie/");
        links.add("http://www.galwayairport.com/");
        links.add("https://www.reddit.com/r/galway/");
        links.add("https://www.advertiser.ie/");
        links.add("https://www.galwayraces.com/");
        links.add("https://www.booking.com/city/ie/galway.en-gb.html");
        links.add("https://www.nuigalway.ie/");
        links.add("https://www.galwaycitymuseum.ie/");
        links.add("https://www.wikiwand.com/en/Galway");
        links.add("http://www.galwaylanguage.com/");
        links.add("https://www.urbandictionary.com/define.php?term=Galway");
        links.add("https://ihworld.com/schools/countries/ireland/ih-galway/");

        for (String link : links) {
            NodeParser nodeParser = new NodeParser(link, query);
            t = new Thread(nodeParser);
            t.start();
        }
        return links;
    }

    public Set<String> getSearchResults(String query) throws IOException {
        Document doc;
        try {
            doc = Jsoup.connect(DUCK_DUCK_GO_SEARCH_URL + query)
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .get();
            Elements results = doc.getElementById("links").getElementsByClass("results_links");
            for (Element result : results) {
                Element e = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
                String link = e.absUrl("href");
                if (link != null && links.size() <= MAX_LIMIT && !links.contains(link)) {
                    links.add(link);
                    NodeParser nodeParser = new NodeParser(link, query);
                    t = new Thread(nodeParser);
                    t.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }
}