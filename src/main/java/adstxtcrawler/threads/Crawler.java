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
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Crawler implements Runnable {

    private final long WEEK_IN_MS = 86400000 * 7; // DAY_IN_MS * 7

    private final Dao<Record, String> recordDao;
    private final Dao<Publisher, String> publisherDao;
    private final ConnectionSource connectionSource;
    
    private static BufferedReader bufferedReader; 
    private String targetUrl;

    public Crawler(ConnectionSource cs) {
        this.connectionSource = cs;
        this.publisherDao = DaoManager.lookupDao(connectionSource, Publisher.class);
        this.recordDao = DaoManager.lookupDao(connectionSource, Record.class);
        if(publisherDao == null)
            System.out.println("pubDao null");
        if(recordDao == null)
            System.out.println("recordDao null");
    }

    @Override
    public void run() {
        parseAdsTxt(targetUrl);
    }

    /* Parses the ads.txt of a given url */
    private void parseAdsTxt(String targetUrl) {
        try {
            URL url = new URL(targetUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "text/plain");

            // Cache time
            long expires = connection.getExpiration();
            if (expires == 0) {  // expires header missing, default 7 days
                expires = new Date().getTime() + WEEK_IN_MS;
            }

            String pubName = getHostName(url.getHost());
            Publisher pub = findPublisher(pubName);
            if (pub == null) {
                pub = new Publisher(pubName);
                pub.setExpiresAt(expires);
                publisherDao.create(pub);
            } else {
                pub.setExpiresAt(expires);
                publisherDao.update(pub);
            }
            System.out.println(pubName + " expires at: " + new Date(pub.getExpiresAt()));
            
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
    public Publisher findPublisher(String pubName) {
        try {

            QueryBuilder<Publisher, String> queryBuilder = publisherDao.queryBuilder();
            queryBuilder.where().eq("name", pubName);
            PreparedQuery<Publisher> preparedQuery = queryBuilder.prepare();
            List<Publisher> publisherList = publisherDao.query(preparedQuery);
            System.out.println(publisherList.size());
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

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

}
