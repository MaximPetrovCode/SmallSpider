import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WikiSmallSpider {
    private final static List<String> visited = new ArrayList<String>();
    private final static WikiFetcher wf = new WikiFetcher();

    public static void main(String[] args) throws Exception {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * runs search
     *
     * @param destination
     * @param source
     * @param limit
     * @throws Exception
     */
    private static void testConjecture(String destination, String source, int limit) throws Exception {
        if (visited.contains(source)) {
            throw new Exception("This link has been visited already. " + source);
        }

        URL url = new URL(source);
        Elements paragraphs = wf.fetchWikipedia(url.toString());
        visited.add(source);
        if (destination.equals(source)) {
            System.out.println("Philosophy reached");
            IntStream.range(0, visited.size()).forEachOrdered(index -> {
                System.out.println(MessageFormat.format("{0} {1}", index, visited.get(index)));
            });
            System.out.println("Limit for iterations: " + (++limit));
            return;
        }

        Element firstRealLinkElement = null;
        for (Element paragraph : paragraphs) {
            firstRealLinkElement = recursiveDFSearch(paragraph, destination);
            if (firstRealLinkElement != null) {
                break;
            }
        }

        if (firstRealLinkElement == null) {
            throw new Exception("Links not found");
        }

        testConjecture(destination, collectURIstring(url, firstRealLinkElement), --limit);
    }

    private static String collectURIstring(URL url, Element firstRealLinkElement) {
        String href = firstRealLinkElement.attr("href");
        return url.getProtocol() + "://" + url.getHost() + href;
    }

    /**
     * DFS recursive search for getting first real link
     *
     * @param paragraph
     * @param destionation
     * @throws Exception
     */
    private static Element recursiveDFSearch(Element paragraph, String destionation) {
        Elements linkElements = paragraph.getElementsByTag("a");
        linkElements = getRealLinks(linkElements);
        if (linkElements.size() > 0) {
            // возвращаем первую действиетльную ссылку в параграфах
            return linkElements.get(0);
        }
        for (Element childNode : paragraph.children()) {
            recursiveDFSearch(childNode, destionation);
        }
        return null;
    }

    /**
     * filtering links by criteria
     *
     * @param linkElements
     * @return
     */
    private static Elements getRealLinks(Elements linkElements) {
        return linkElements.stream().filter(linkElement -> {
            boolean cursive = isCursive(linkElement);
            boolean hasBrackets = hasBrackets(linkElement);
            return !cursive && !hasBrackets;
        }).collect(Collectors.toCollection(Elements::new));
    }

    /**
     * checks for brackets
     *
     * @param linkElement link element
     */
    private static boolean hasBrackets(Element linkElement) {
        final String regex = "\\[[^\\[]*\\]";
        return linkElement.text().matches(regex);
    }

    /**
     * checks for cursive
     *
     * @param linkElement
     */
    private static boolean isCursive(Element linkElement) {
        Elements parents = linkElement.parents();
        for (Element parent : parents) {
            Elements i = parent.getElementsByTag("i");
            Elements em = parent.getElementsByTag("em");
            return (i == null || em == null);
        }
        return false;
    }
}
