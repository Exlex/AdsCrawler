package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.threads.Crawler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.support.ConnectionSource;
import java.util.Date;
import spark.Request;
import spark.Response;
import static spark.Spark.get;

/*  HTTP Endpoint Class
    To access db, send GET request to port 4567.
    ex: localhost:4567?name=cnn.com
 */
public class Endpoint {

    private static final String URL_MAPPING = "/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void serve(ConnectionSource connectionSource) {
        get(URL_MAPPING, (Request request, Response response) -> {
            String pubName = request.queryParams("name");
            if (pubName != null) {
                System.out.println("Query Param for publisher: " + pubName);
                Publisher publisher = Crawler.findPublisher(pubName);

                if (publisher != null) {
                    // Check if cache expired before serving
                    if (isPublisherExpired(publisher.getExpiresAt())) {
                        Crawler crawler = new Crawler(connectionSource);
                        System.out.println("The publisher cache for " + publisher.getName() + " has expired. Refetching...");
                        crawler.fetch("https://" + publisher.getName() + "/ads.txt");
                        crawler.run(); // done in same-thread because must wait for new info
                    }
                    // return the JSON
                    response.type("application/json");
                    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(publisher.getRecords());
                }
                System.out.println("Publisher " + pubName + " was NULL");
            }

            return "Spark out here";
        });
    }
    
        /* Helper method for endpoint*/
    public static boolean isPublisherExpired(long pubExpTime) {
        Date now = new Date();
        System.out.println("Now its: " + now
                + "\nExpires at: " + new Date(pubExpTime));
        return now.getTime() > pubExpTime;
    }
}
