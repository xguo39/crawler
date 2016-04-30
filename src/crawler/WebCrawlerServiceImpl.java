package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.jna.StringArray;

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
			_crawlerStoreService.storeUrlAsVisited(crawlingStrategy.getTableNameForVisitedUrls(), 
					crawlingStrategy.getSeedUrl(), Boolean.toString(false));
			
			int index = 1;
			Optional<String[]> mayBeUrlAndStoreOption = _crawlerStoreService
					.getUrlAndStoreOptionWithRecordID(crawlingStrategy.getTableNameForVisitedUrls(), index);
			
			while(mayBeUrlAndStoreOption.isPresent()) {
				System.out.println("Crawling the url at: " + index);
				processPage(mayBeUrlAndStoreOption.get()[0], Boolean.valueOf(mayBeUrlAndStoreOption.get()[1]), crawlingStrategy);
				if (Boolean.valueOf(mayBeUrlAndStoreOption.get()[1])) {
					String temp = _crawlerStoreService.getHTML(crawlingStrategy.getTableNameForWebPages(), 
							mayBeUrlAndStoreOption.get()[0]).get();
					int a = 1;
				}
				index++;
				mayBeUrlAndStoreOption = _crawlerStoreService
						.getUrlAndStoreOptionWithRecordID(crawlingStrategy.getTableNameForVisitedUrls(), index);
			}
		} catch (SQLException | IOException e) {
			System.out.println(String.format("Throw exception, %s", e));
		}

	}
	
	private void processPage(String urlString, Boolean shoudlStore, CrawlingStrategy crawlingStrategy) {
		//get useful information
		try {			
			System.out.println(urlString + " " + shoudlStore);
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(urlString);
			request.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "gbk"));
            			
			StringBuffer result = new StringBuffer();
			String line1 = "";
			while ((line1 = rd.readLine()) != null) {
				result.append(line1);
			}
			String html = result.toString();
			// System.out.println(html);
			Document doc = Jsoup.parse(html);

			if (shoudlStore) {
				//store the URL and HTML to database
				String aString = new String(html.getBytes(), "UTF-8");
				_crawlerStoreService.storeWebPage(crawlingStrategy.getTableNameForWebPages(), urlString, new String(html.getBytes(), "UTF-8"));
			}
		
			//get all links and recursively call the processPage method
			Elements urlCandidates = doc.select("a[href]");
			for(Element link: urlCandidates){
				if (crawlingStrategy.checkShouldCrawl(link)) {
					String potentialUrl = link.attr("abs:href");
					potentialUrl = potentialUrl.replace("&amp;", "&");
					potentialUrl = java.net.URLDecoder.decode(potentialUrl, "UTF-8");
					
					String tableName = crawlingStrategy.getTableNameForVisitedUrls();
			
					//check if the potentialUrl is already in database
					if(!_crawlerStoreService.checkIfUrlVisited(tableName, potentialUrl)) {
						//store the URL to database to be crawled in the future
						if (crawlingStrategy.checkShouldStoreHTML(link)) {
							_crawlerStoreService.storeUrlAsVisited(tableName, potentialUrl, Boolean.toString(true));
						} else {
							_crawlerStoreService.storeUrlAsVisited(tableName, potentialUrl, Boolean.toString(false));
						}
					}
				}
			}
		} catch (SQLException | IOException e) {
			System.out.println(String.format("Throw exception for %s, %s", urlString, e));
	    }
	}
}
