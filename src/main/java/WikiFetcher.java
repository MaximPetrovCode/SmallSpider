import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class WikiFetcher {
    private long lastRequestTime = -1;
    private long minInterval = 1000;

    /**
     * Fetches and parses a URL string, returning a list of paragraph elements.
     *
     * @param url url
     * @return paragraphs
     * @throws IOException
     */
    public Elements fetchWikipedia(String url) throws IOException {
        sleepIfNeeded();

        // download and parse the document
        Connection conn = Jsoup.connect(url);
        Document doc = conn.get();

        // select the content text and pull out the paragraphs.
        Element content = doc.getElementById("mw-content-text");

        // TODO: avoid selecting paragraphs from sidebars and boxouts
        Elements paras = content.select("p");
        return paras;
    }

    /**
     * Rate limits by waiting at least the minimum interval between requests.
     */
    private void sleepIfNeeded() {
        if (lastRequestTime != -1) {
            long currentTime = System.currentTimeMillis();
            long nextRequestTime = lastRequestTime + minInterval;
            if (currentTime < nextRequestTime) {
                try {
                    //System.out.println("Sleeping until " + nextRequestTime);
                    Thread.sleep(nextRequestTime - currentTime);
                } catch (InterruptedException e) {
                    System.err.println("Warning: sleep interrupted in fetchWikipedia.");
                }
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
}