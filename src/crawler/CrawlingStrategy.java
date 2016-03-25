package crawler;

import java.util.Set;

import org.jsoup.nodes.Element;

public interface CrawlingStrategy {
  String getTableNameForVisitedUrls();
  String getTableNameForWebPages();
  String getSeedUrl();
  boolean checkShouldCrawl(Element element);
  boolean checkShouldStoreHTML(String url);
}
