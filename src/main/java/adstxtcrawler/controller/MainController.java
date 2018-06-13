package adstxtcrawler.controller;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.util.Crawler;
import adstxtcrawler.models.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static ObjectMapper mapper;

    public static void main(String[] args) {
        mapper = new ObjectMapper();
        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL, DB_USER, DB_PW);
            TableUtils.createTableIfNotExists(connectionSource, Record.class);
            TableUtils.createTableIfNotExists(connectionSource, Publisher.class);

            Crawler crawler = new Crawler(connectionSource);
            crawler.loadPublishers();
            crawler.setupDatabase();

            Queue<String> publishers = crawler.getPublishers();
            while (publishers.size() > 0) {
                String adsUrl = publishers.poll();
                crawler.parseAdsTxt(adsUrl);
            }
            
            // ex: localhost:4567?name=cnn.com
            get(URL_MAPPING, (Request request, Response response) -> {
                String pubName = request.queryParams("name");
                if (pubName != null) {
                    System.out.println("Query Param for publisher: " + pubName);
                    Publisher publisher = crawler.findPublisher(pubName);
                    if (publisher != null) {
                        response.type("application/json");
                        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(publisher.getRecords());
                    }
                    System.out.println("Publisher " + pubName + " was NULL");
                }

                return "Spark out here";
            });

            // Has a shutdown hook, not needed
            // conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
