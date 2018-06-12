package adstxtcrawler;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.Queue;
import spark.Request;
import spark.Response;
import static spark.Spark.get;

public class MainController {

    private static final String URL_MAPPING = "/";
    private static final String DATABASE_URL = "jdbc:h2:./src/main/resources/storage";
    private static final String DB_USER = "admin";
    private static final String DB_PW = "";

    public static void main(String[] args) {
        try {
            get(URL_MAPPING, (Request request, Response response) -> "Spark out here");
            
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL, DB_USER, DB_PW);
            TableUtils.createTableIfNotExists(connectionSource, Record.class);
            Crawler crawler = new Crawler(connectionSource);
            crawler.loadPublishers();
            Queue<String> publisher = crawler.getPublishers();
            crawler.parseAdsTxt(publisher.element());

            // Has a shutdown hook, not needed
            // conn.close();
        } catch (SQLException e) {
            System.out.println("SHOULDT BE THIS WAY");
            e.printStackTrace();
        }
    }
}
