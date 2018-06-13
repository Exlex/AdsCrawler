package adstxtcrawler.controller;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.util.Crawler;
import adstxtcrawler.models.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.Date;
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
            // DB init
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL, DB_USER, DB_PW);
            TableUtils.createTableIfNotExists(connectionSource, Record.class);
            TableUtils.createTableIfNotExists(connectionSource, Publisher.class);
            TableUtils.clearTable(connectionSource, Record.class);
            TableUtils.clearTable(connectionSource, Publisher.class);

            // Crawler init
            Crawler crawler = new Crawler(connectionSource);
            crawler.loadPublishers();
            crawler.setupDatabaseAccess();

            // Loading publisher list into queue
            Queue<String> publishers = crawler.getPublishers();
            while (publishers.size() > 0) {
                String adsUrl = publishers.poll();
                crawler.parseAdsTxt(adsUrl);
            }

            // HTTP Endpoint
            // ex: localhost:4567?name=cnn.com
            get(URL_MAPPING, (Request request, Response response) -> {
                String pubName = request.queryParams("name");
                if (pubName != null) {
                    System.out.println("Query Param for publisher: " + pubName);
                    Publisher publisher = crawler.findPublisher(pubName);

                    if (publisher != null) {
                        // Check if cache expired before serving
                        if (isPublisherExpired(publisher.getExpiresAt())) {
                            System.out.println("The publisher cache for " + publisher.getName() + " has expired. Refetching...");
                            crawler.parseAdsTxt("https://" + publisher.getName());
                        }
                        // return the JSON
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

    public static boolean isPublisherExpired(long pubExpTime) {
        Date now = new Date();
        System.out.println("Now its: " + now 
                       + "\nExpires at: " + new Date(pubExpTime));
        return now.getTime() > pubExpTime;
    }
}
