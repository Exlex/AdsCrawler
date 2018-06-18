
import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import adstxtcrawler.threads.PublisherLoaderService;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class MainTests {

    private static PublisherLoaderService pls;
    private static ConnectionSource connectionSource;
    private static BlockingQueue<String> publishersToProcess;

    @Before
    public void setup() throws SQLException {
        publishersToProcess = new LinkedBlockingDeque<>(10);
        connectionSource = new JdbcPooledConnectionSource("jdbc:h2:mem:test");
        TableUtils.createTable(connectionSource, Record.class);
        TableUtils.createTable(connectionSource, Publisher.class);

        // Create DAO, later fetched by lookupDao()
        DaoManager.createDao(connectionSource, Record.class);
        DaoManager.createDao(connectionSource, Publisher.class);
        pls = new PublisherLoaderService(connectionSource);
    }

    @Test
    public void testPublisherLoaderServiceParseLine() throws InterruptedException {
        System.out.println("Running: testPublisherLoaderServiceParseLine");
        String line = "https://www.bloomberg.com/ads.txt";
        //PublisherLoaderService.parseLine(line);
        String pubName = publishersToProcess.take();
        Assert.assertEquals("bloomberg.com", pubName);
    }
    
    @After
    public void tearDown() throws IOException {
        publishersToProcess = null;
        pls = null;
        connectionSource.close();
    }

}
