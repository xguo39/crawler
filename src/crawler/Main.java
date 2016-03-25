package crawler;

import java.util.HashMap;
import java.util.Map;

/* This program will automatically generate MySQL tables to store HTML and visited urls. Please make sure to install
 * MySQL and set the user and password in CrawlerDatabaseConfig.java before running the codes.
 * 
 * If you want to add more crawling task, please add your task name in the CrawlingTask.java and implement the interface
 * of CrawlingStrategy.java. You also need to change the parameter that is passed to webCrawlerService.crawl method
 * in Main.java
 */
public class Main {
	public static CrawlerStoreService crawlerStoreService = new CrawlerStoreServiceImpl(new CrawlerDatabaseExecutor());
	public static Map<CrawlingTask, CrawlingStrategy> strategyMap = createStrategyMap();
	
	public static WebCrawlerService webCrawlerService = new WebCrawlerServiceImpl(crawlerStoreService, strategyMap);
	
	public static Map<CrawlingTask, CrawlingStrategy> createStrategyMap() {
		Map<CrawlingTask, CrawlingStrategy> strategyMap = new HashMap<>();
		strategyMap.put(CrawlingTask.GATECH_CEE, new GatechCEECrawlingStrategy(
				"Gatech_CEE_FacultyProfile_Visited_Urls", 
				"Gatech_CEE_FacultyProfile_Web_Pages", 
				"http://www.ce.gatech.edu/people"));
		strategyMap.put(CrawlingTask.QUANTNET, new QuantnetCrawlingStrategy(
				"Quantnet_MFE_Visited_Urls", 
				"Quantnet_MFE_Web_Pages", 
				"https://www.quantnet.com/tracker"));		
		return strategyMap;
	}
 
	public static void main(String[] args) {
		webCrawlerService.crawl(CrawlingTask.QUANTNET);
	}
}