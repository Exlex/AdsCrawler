package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.threads.Crawler;
import adstxtcrawler.threads.PublisherLoaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.support.ConnectionSource;
import java.util.concurrent.CountDownLatch;
import spark.Request;
import spark.Response;
import static spark.Spark.get;

/*  HTTP Endpoint Class
    To access db, send GET request to port 4567.
    ex: localhost:4567?name=cnn.com
 */
public class Endpoint {

    private static final String URL_MAPPING = "/";
    private static final ObjectMapper mapper = new ObjectMapper();

    public void serve(ConnectionSource connectionSource) {
        get(URL_MAPPING, (Request request, Response response) -> {
            // Get a personal crawler thread
            Crawler crawler = new Crawler(connectionSource);

            String pubName = request.queryParams("name");
            if (pubName != null) {
                System.out.println("Query Param for publisher: " + pubName);
                Publisher publisher = crawler.findPublisher(pubName);

                if (publisher != null) {
                    // Check if cache expired before serving
                    if (PublisherLoaderService.isPublisherExpired(publisher.getExpiresAt())) {
                        System.out.println("The publisher cache for " + publisher.getName() + " has expired. Refetching...");
                        crawler.fetch("https://" + publisher.getName());
                        crawler.run(); // done in same-thread because must wait for new info
                    }
                    // return the JSON
                    response.type("application/json");
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(publisher.getRecords());
                }
                System.out.println("Publisher " + pubName + " was NULL");
            }

            return "Spark out here";
        });
    }
}
