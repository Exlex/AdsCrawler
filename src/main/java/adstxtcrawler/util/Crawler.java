package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Crawler {

    private final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";
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

    public void setupDatabase() {
        // setup database
        try {
            recordDao = DaoManager.createDao(connectionSource, Record.class);
            publisherDao = DaoManager.createDao(connectionSource, Publisher.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPublishers() {
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            BufferedReader br = new BufferedReader(new FileReader(publishersFile));
            String line = "";

            while ((line = br.readLine()) != null) {
                publishers.add(line);
                System.out.println("Added: " + line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseAdsTxt(String urlString) {
        try {
            URL url = new URL(urlString);
            String pubName = getHostName(url.getHost());
            Publisher pub = findPublisher(pubName);
            if (pub == null) {
                System.out.println("No publisher found");
                pub = new Publisher(pubName);
                publisherDao.create(pub);
                System.out.println("Saved publisher is: " + pub.getName());
            }

            // set headers
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "text/plain");

            scanner = new Scanner(connection.getInputStream());

            String line;
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                Record record = validator.validateRecord(pub, line);
                if (record == null) {
                    System.out.println("Line discaded: " + line);
                    continue;// invalid or malformed line, skip it
                }
                System.out.println("Valid record");
                recordDao.create(record);
            }
            scanner.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public String getHostName(String pubName) {
        return pubName.startsWith("www.") ? pubName.substring(4) : pubName;
    }

    public Publisher findPublisher(String pubName) {
        try {
            QueryBuilder<Publisher, String> queryBuilder = publisherDao.queryBuilder();
            queryBuilder.where().eq("name", pubName);
            PreparedQuery<Publisher> preparedQuery = queryBuilder.prepare();
            List<Publisher> publisherList = publisherDao.query(preparedQuery);
            if (publisherList.size() == 1) {
                //System.out.println("Publisher found!");
                return publisherList.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void parseRecord(String recordString) {

    }

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
