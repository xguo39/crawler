package crawler;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawlerServiceImpl implements WebCrawlerService {
	private CrawlerStoreService _crawlerStoreService;
	private final Map<CrawlingTask, CrawlingStrategy> _strategyMap;
	
	public WebCrawlerServiceImpl(CrawlerStoreService crawlerStoreService, Map<CrawlingTask, CrawlingStrategy> strategyMap) {
		_crawlerStoreService = crawlerStoreService;
		_strategyMap = strategyMap;
	}

	public void crawl(CrawlingTask crawlingTask) {
		if(_strategyMap.get(crawlingTask) == null) {
			System.out.println("Illegal crawling task: " + crawlingTask.toString());
			return;
		}
		
		CrawlingStrategy crawlingStrategy = _strategyMap.get(crawlingTask);
		
		try {
			//Create the tables if not exist
			_crawlerStoreService.createTableForVisitedUrls(crawlingStrategy.getTableNameForVisitedUrls());
			_crawlerStoreService.createTableForWebPages(crawlingStrategy.getTableNameForWebPages());
			
			// Current we clear the existing data in tables before we start the new crawling. However, we may want to keep the 
			// existing data in the future.
			_crawlerStoreService.truncateTableForVisitedUrls(crawlingStrategy.getTableNameForVisitedUrls());
			_crawlerStoreService.truncateTableForWebPages(crawlingStrategy.getTableNameForWebPages());
			
			// Start the crawling loop
			crawlingLoop(crawlingStrategy);
		} catch (SQLException e) {
			System.out.println(String.format("Throw exception for crawlingTask: %s, %s", crawlingTask.toString(), e));
		}
	}
	
	private void crawlingLoop(CrawlingStrategy crawlingStrategy) {
		if (crawlingStrategy.getSeedUrl() == null) {
			System.out.println(String.format("Seed url is null for crawlingStrategy: %s", crawlingStrategy.toString()));
			return;
		}
		
		try {
			//store the seed URL to database as initialization
			_crawlerStoreService.storeUrlAsVisited(crawlingStrategy.getTableNameForVisitedUrls(), crawlingStrategy.getSeedUrl());
			
			int index = 1;
			Optional<String> mayBeUrl = _crawlerStoreService.getUrlWithRecordID(crawlingStrategy.getTableNameForVisitedUrls(), index);
			while(mayBeUrl.isPresent()) {
				processPage(mayBeUrl.get(), crawlingStrategy);
				index++;
				mayBeUrl = _crawlerStoreService.getUrlWithRecordID(crawlingStrategy.getTableNameForVisitedUrls(), index);
			}
		} catch (SQLException | IOException e) {
			System.out.println(String.format("Throw exception, %s", e));
		}

	}
	
	private void processPage(String url, CrawlingStrategy crawlingStrategy) {
		//Print the URL for check
		System.out.println(url);
		
		//get useful information
		//Document doc = Jsoup.connect(url).get();
		try {
			Document doc = Jsoup.parse(new URL(url).openStream(), "gbk", url);
		
			if (crawlingStrategy.checkShouldStoreHTML(url)) {
				//store the URL and HTML to database
				_crawlerStoreService.storeWebPage(crawlingStrategy.getTableNameForWebPages(), url, doc.toString());
			}
		
			//get all links and recursively call the processPage method
			Elements urlCandidates = doc.select("a[href]");
			for(Element link: urlCandidates){
				if (crawlingStrategy.checkShouldCrawl(link)) {
					String potentialUrl = link.attr("abs:href");
					String tableName = crawlingStrategy.getTableNameForVisitedUrls();
			
					//check if the potentialUrl is already in database
					if(!_crawlerStoreService.checkIfUrlVisited(tableName, potentialUrl)) {			
						//store the URL to database to be crawled in the future
						_crawlerStoreService.storeUrlAsVisited(tableName, potentialUrl);
					}
				}
			}
		} catch (SQLException | IOException e) {
			System.out.println(String.format("Throw exception for %s, %s", url, e));
	    }
	}
}
