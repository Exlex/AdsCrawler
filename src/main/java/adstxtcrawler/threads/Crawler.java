package adstxtcrawler.threads;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import adstxtcrawler.util.Validator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Crawler implements Runnable {

    private static final String POISON_PILL = new String();
    private static final long WEEK_IN_MS = 86400000 * 7; // DAY_IN_MS * 7
    private static Dao<Record, String> recordDao;
    private static Dao<Publisher, String> publisherDao;

    private final ConnectionSource connectionSource;
    private BufferedReader bufferedReader;

    public Crawler(ConnectionSource cs) {
        this.connectionSource = cs;
        if (recordDao == null || publisherDao == null) {
            publisherDao = DaoManager.lookupDao(connectionSource, Publisher.class);
            recordDao = DaoManager.lookupDao(connectionSource, Record.class);
        }
    }

    @Override
    public void run() {
        while (PublisherLoaderService.getPublishersToProcessQueue().size() > 0 || !PublisherLoaderService.isDone()) {
            try {
                //System.out.println("In crawler: " + Thread.currentThread().getName());
                BlockingQueue<String> queue = PublisherLoaderService.getPublishersToProcessQueue();
                String targetUrl = queue.take();
                if (targetUrl == POISON_PILL) { // comparing object not string value
                    queue.add(POISON_PILL); // for other threads waiting to take()
                    //System.out.println("##### Exiting crawler: " + Thread.currentThread().getName() + " #####");
                    Main.getLatch().countDown();
                    return;
                }
                fetch(targetUrl);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* Fetches ads.txt at given url and parses the contents */
    public void fetch(String targetUrl) {
        try {
            URL url = new URL(targetUrl);
            URLConnection connection = getPublisherConnection(url);
            connection.connect();
            
            // Cache time calculation
            long expires = getExpires(connection);

            Publisher pub;
            String pubName;
            // fixes bug where if publishers.txt has repeated publishers it will access same resource
            synchronized (Publisher.class) {
                pubName = getHostName(url.getHost());
                pub = findPublisher(pubName);   // See if publisher already exists in DB (for cache purposes)
                if (pub == null) {
                    pub = new Publisher(pubName);
                    pub.setExpiresAt(expires);
                    publisherDao.create(pub);
                } else {
                    pub.setExpiresAt(expires);
                    publisherDao.update(pub);
                }
            }
            System.out.println("Added: " + pubName + " expires at: " + new Date(pub.getExpiresAt()));

            // Process ads.txt record by record into the db
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Record record = Validator.validateRecord(pub, line);
                if (record == null) {
                    //System.out.println("Line discaded: " + line);
                    continue;// invalid or malformed line, skip it
                }
                recordDao.create(record);
            }
            bufferedReader.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /* Cleanup pubName after url.getHost() */
    private String getHostName(String pubName) {
        return pubName.startsWith("www.") ? pubName.substring(4) : pubName;
    }

    /* Searches DB for the Publisher object by name */
    public static Publisher findPublisher(String pubName) {
        try {

            QueryBuilder<Publisher, String> queryBuilder = publisherDao.queryBuilder();
            queryBuilder.where().eq("name", pubName);
            PreparedQuery<Publisher> preparedQuery = queryBuilder.prepare();
            List<Publisher> publisherList = publisherDao.query(preparedQuery);
            if (publisherList.size() == 1) {
                return publisherList.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPill() {
        return POISON_PILL;
    }

    private URLConnection getPublisherConnection(URL url) {
        URLConnection connection = null;
        try {
            // Open URL Connection with proper headers
            connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Accept", "text/plain");
        } catch (MalformedURLException e) {
            System.out.println("Invalid publisher URL: " + url);
        } catch (IOException e) {
            System.out.println("Couldn't establish connection");
        }
        return connection;
    }

    private long getExpires(URLConnection connection) {
        long expires = connection.getExpiration();
        if (expires == 0) {  // expires header missing, default 7 days
            expires = new Date().getTime() + WEEK_IN_MS;
        }
        return expires;
    }
}
