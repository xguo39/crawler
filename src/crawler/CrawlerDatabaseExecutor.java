package crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
 
public class CrawlerDatabaseExecutor {
 
	public Connection conn = null;
 
	public CrawlerDatabaseExecutor() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = String.format("jdbc:mysql://localhost:3306/%s?useUnicode=true&characterEncoding=utf-8", CrawlerDatabaseConfig.DATABASE_NAME);
			conn = DriverManager.getConnection(url, CrawlerDatabaseConfig.DATABASE_USER, CrawlerDatabaseConfig.DATABASE_PASSWORD);
			System.out.println("conn built");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
 
	public ResultSet runSql(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.executeQuery(sql);
	}
 
	public boolean runSql2(String sql) throws SQLException {
		Statement sta = conn.createStatement();
		return sta.execute(sql);
	}
 
	@Override
	protected void finalize() throws Throwable {
		if (conn != null || !conn.isClosed()) {
			conn.close();
		}
	}
}
