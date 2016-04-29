package crawler;

import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class CrawlerStoreServiceImpl implements CrawlerStoreService {
  private CrawlerDatabaseExecutor _crawlerDatabaseExecutor;
  
  public CrawlerStoreServiceImpl(CrawlerDatabaseExecutor crawlerDatabaseExecutor) {
	  _crawlerDatabaseExecutor = crawlerDatabaseExecutor;
  }
  
  public void createTableForVisitedUrls(String tableName) throws SQLException {
	  String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` ("
	  		+ "`RecordID` INT(11) NOT NULL AUTO_INCREMENT,"
	  		+ "`URL` text NOT NULL,"
	  		+ "PRIMARY KEY (`RecordID`)"
	  		+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;", tableName);
	  _crawlerDatabaseExecutor.runSql2(sql);
  }
  
  public void truncateTableForVisitedUrls(String tableName) throws SQLException {
	  _crawlerDatabaseExecutor.runSql2(String.format("TRUNCATE %s", tableName));
  }
    
  public boolean checkIfUrlVisited(String tableName, String url) throws SQLException {
	  String sql = String.format("select * from %s where URL = '%s'", tableName, url);
	  ResultSet rs = _crawlerDatabaseExecutor.runSql(sql);
	  if(rs.next()) {
		  return true;
	  } else {
		  return false;
	  }
  }
  
  public Optional<String> getUrlWithRecordID(String tableName, int index) throws SQLException, IOException {
	  String sql = String.format("select * from %s where RecordID = '%s'", tableName, index);
	  ResultSet rs = _crawlerDatabaseExecutor.runSql(sql);
	  if(rs.next()) {
		  StringBuilder sb = new StringBuilder();
		  Reader in = rs.getCharacterStream("URL");
		  int buf = -1;
		  while((buf = in.read()) > -1) {
		        sb.append((char)buf);
		  }
		  in.close();
		  return Optional.of(sb.toString());
	  } else {
		  return Optional.empty();
	  }
  }
  
  public void storeUrlAsVisited(String tableName, String url) throws SQLException {
	  //store the URL to database to avoid parsing again
	  String sql = String.format("INSERT INTO  `%s`.`%s` (`URL`) VALUES (?);",
			  CrawlerDatabaseConfig.DATABASE_NAME,
			  tableName);
	  PreparedStatement stmt = _crawlerDatabaseExecutor.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	  stmt.setString(1, url);
	  stmt.execute();
  }
  
  public void createTableForWebPages(String tableName) throws SQLException {
	  String sql = String.format("CREATE TABLE IF NOT EXISTS `%s` ("
	  		+ "`RecordID` INT(11) NOT NULL AUTO_INCREMENT,"
	  		+ "`URL` text NOT NULL,"
	  		+ "`HTML` longtext,"
	  		+ "PRIMARY KEY (`RecordID`)"
	  		+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;", tableName);
	  _crawlerDatabaseExecutor.runSql2(sql);
  }
  
  public void truncateTableForWebPages(String tableName) throws SQLException {
	  _crawlerDatabaseExecutor.runSql2(String.format("TRUNCATE %s", tableName));
  }
  
  public void storeWebPage(String tableName, String url, String webPageHtml) throws SQLException {
		//store the URL and HTML to database
	  String sql = String.format("INSERT INTO  `%s`.`%s` (`URL`, `HTML`) VALUES (?, ?);", 
			  CrawlerDatabaseConfig.DATABASE_NAME,
			  tableName);
		PreparedStatement stmtHTML = _crawlerDatabaseExecutor.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		stmtHTML.setString(1, url);
		stmtHTML.setString(2, webPageHtml);
		stmtHTML.execute();
  }
}
