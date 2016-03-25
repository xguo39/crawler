package crawler;

public interface WebCrawlerService {
	//This method crawls all pages that the seedUrl links and store the HTMLs in database
    void crawl(CrawlingTask crawlingtask);
}
