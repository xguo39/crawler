package crawler;

import org.jsoup.nodes.Element;

public class QuantnetCrawlingStrategy implements CrawlingStrategy {
	private String _tableNameForVisitedUrls;
	private String _tableNameForWebPages;
	private String _seedUrl;
	
	public QuantnetCrawlingStrategy(String tableNameForVisitedUrls, String tableNameForWebPages, String seedUrl) {
		_tableNameForVisitedUrls = tableNameForVisitedUrls;
		_tableNameForWebPages = tableNameForWebPages;
		_seedUrl = seedUrl;
	}
	
	public String getTableNameForVisitedUrls() {
		return _tableNameForVisitedUrls;
	}
	
	public String getTableNameForWebPages() {
		return _tableNameForWebPages;
	}
	
	public String getSeedUrl() {
		return _seedUrl;
	}
	
	public boolean checkShouldStoreHTML(String url) {
		return url.matches(String.format("%s[^#]*/detail$", _seedUrl));
	}
	
	public boolean checkShouldCrawl(Element link) {
		return link.attr("abs:href").matches(String.format("%s[^#]*/detail$", _seedUrl)) || 
				link.attr("abs:href").matches(String.format("%s[^#]*/?page=[0-9]+$", _seedUrl));
	}	
}
