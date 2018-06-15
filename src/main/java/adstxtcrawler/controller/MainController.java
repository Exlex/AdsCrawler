package adstxtcrawler.controller;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.threads.Crawler;
import adstxtcrawler.models.Record;
import adstxtcrawler.util.Endpoint;
import adstxtcrawler.threads.PublisherManager;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MainController {

    /* Properties */
    private static final String DATABASE_URL = "jdbc:h2:./src/main/resources/storage";
    private static final String DB_USER = "admin";
    private static final String DB_PW = "";
    private static final int MAX_CRAWLER_THREADS = 10;
    private static final int QUEUE_MAX_SIZE = 100;

    private static BlockingQueue<String> publishersToProcess;
    private static ConnectionSource connectionSource;
    
    public static void main(String[] args) {
        initDb();
        publishersToProcess = new LinkedBlockingDeque<>(QUEUE_MAX_SIZE);

        PublisherManager publisherManager = new PublisherManager(connectionSource, publishersToProcess);
        publisherManager.run();
        
        Endpoint endpoint = new Endpoint();
        endpoint.serve(connectionSource);
        System.out.println("Should be false: " + publisherManager.isDone());
        while (!publisherManager.isDone() || publishersToProcess.size() > 0) {
            System.out.println("There are: " + publishersToProcess.size() + " publishers left to process.");
            try {
                Crawler crawler = new Crawler(connectionSource);
                String adsUrl = publishersToProcess.take();
                crawler.setTargetUrl(adsUrl);
                crawler.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initDb() {
        try {
            // DB init
            connectionSource = new JdbcPooledConnectionSource(DATABASE_URL, DB_USER, DB_PW);

            TableUtils.dropTable(connectionSource, Record.class, true);
            TableUtils.dropTable(connectionSource, Publisher.class, true);
            TableUtils.createTable(connectionSource, Record.class);
            TableUtils.createTable(connectionSource, Publisher.class);

            // Create DAO, later fetched by lookupDao()
            DaoManager.createDao(connectionSource, Record.class);
            DaoManager.createDao(connectionSource, Publisher.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
