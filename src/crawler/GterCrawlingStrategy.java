package crawler;

import org.jsoup.nodes.Element;

public class GterCrawlingStrategy implements CrawlingStrategy {
	private String _tableNameForVisitedUrls;
	private String _tableNameForWebPages;
	private String _seedUrl;
	
	public GterCrawlingStrategy(String tableNameForVisitedUrls, String tableNameForWebPages, String seedUrl) {
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
		if (link.previousSibling() != null && link.previousSibling().previousSibling() != null) {
			return link.previousSibling().previousSibling().toString().contains("Offer榜");
		} else {
			return false;
		}
	}
	
	public boolean checkShouldCrawl(Element link) {
		if (link.previousSibling() != null && link.previousSibling().previousSibling() != null) {
			boolean val = link.previousSibling().previousSibling().toString().contains("Offer榜");
			return val || link.attr("abs:href").matches("http:\\/\\/bbs\\.gter\\.net\\/forum\\.php\\?mod=forumdisplay&fid=49&typeid=158&filter=typeid&typeid=158&page=[0-9]+");
		} else {
			return false;
		}
	}	
}
