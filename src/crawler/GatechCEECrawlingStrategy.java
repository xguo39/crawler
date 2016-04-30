package crawler;

import org.jsoup.nodes.Element;

public class GatechCEECrawlingStrategy implements CrawlingStrategy {
	private String _tableNameForVisitedUrls;
	private String _tableNameForWebPages;
	private String _seedUrl;
	
	public GatechCEECrawlingStrategy(String tableNameForVisitedUrls, String tableNameForWebPages, String seedUrl) {
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
	
	public boolean checkShouldStoreHTML(Element link) {
		return link.attr("abs:href").matches(String.format("%s[^#]*cv$", _seedUrl));
	}
	
	public boolean checkShouldCrawl(Element link) {
		return link.attr("abs:href").matches(String.format("%s[^#]*", _seedUrl));
	}	
}
