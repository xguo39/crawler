package crawler;
import java.sql.SQLException;

public interface CrawlerStoreService {
	// VisitedUrl table methods
	void createTableForVisitedUrls(String tableName) throws SQLException;
	void truncateTableForVisitedUrls(String tableName) throws SQLException;
	boolean checkIfUrlVisited(String tableName, String url) throws SQLException;
	void storeUrlAsVisited(String tableName, String url) throws SQLException;
	
	// WebPages table methods
	void createTableForWebPages(String tableName) throws SQLException;
	void truncateTableForWebPages(String tableName) throws SQLException;
	void storeWebPage(String tableName, String url, String webPageHtml) throws SQLException;
	
}
