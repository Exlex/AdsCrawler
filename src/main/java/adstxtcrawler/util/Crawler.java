package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Crawler {

    private final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";
    private final long WEEK_IN_MS = 86400000 * 7; // DAY_IN_MS * 7
    private Queue<String> publishers;
    private Scanner scanner;
    private Validator validator;
    private Dao<Record, String> recordDao;
    private Dao<Publisher, String> publisherDao;
    private ConnectionSource connectionSource;

    public Crawler(ConnectionSource cs) {
        connectionSource = cs;
        publishers = new LinkedList<>();
        validator = new Validator();
    }

    public void setupDatabaseAccess() {
        // setup database access objects
        try {
            recordDao = DaoManager.createDao(connectionSource, Record.class);
            publisherDao = DaoManager.createDao(connectionSource, Publisher.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Reads in a list of publishers from resources/publishers.txt into Queue*/
    public void loadPublishers() {
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            scanner = new Scanner(publishersFile);
            String line = "";

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line != null) {
                    publishers.add(line.trim());
                    System.out.println("Added: " + line);
                }

            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Parses the ads.txt of a given url */
    public void parseAdsTxt(String urlString) {
        try {
            URL url = new URL(urlString);
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

            scanner = new Scanner(connection.getInputStream());

            String line;
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                Record record = validator.validateRecord(pub, line);
                if (record == null) {
                    //System.out.println("Line discaded: " + line);
                    continue;// invalid or malformed line, skip it
                }
                recordDao.create(record);
            }
            scanner.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /* Cleanup pubName after url.getHost() */
    public String getHostName(String pubName) {
        return pubName.startsWith("www.") ? pubName.substring(4) : pubName;
    }

    /* Searches DB for the Publisher object by name */
    public Publisher findPublisher(String pubName) {
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

    /* GETTERS AND SETTERS */
    public Queue<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Queue<String> publishers) {
        this.publishers = publishers;
    }

    public Dao<Record, String> getRecordDao() {
        return recordDao;
    }

    public void setRecordDao(Dao<Record, String> recordDao) {
        this.recordDao = recordDao;
    }

    public Dao<Publisher, String> getPublisherDao() {
        return publisherDao;
    }

    public void setPublisherDao(Dao<Publisher, String> publisherDao) {
        this.publisherDao = publisherDao;
    }

}
