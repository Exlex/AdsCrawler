package adstxtcrawler.threads;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.threads.Crawler;
import adstxtcrawler.models.Record;
import adstxtcrawler.util.Endpoint;
import adstxtcrawler.threads.PublisherLoaderService;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    /* Properties */
    private static final String DATABASE_URL = "jdbc:h2:./src/main/resources/storage";
    private static final String DB_USER = "admin";
    private static final String DB_PW = "";
    private static final int MAX_CRAWLER_THREADS = 5;

    private static ConnectionSource connectionSource;
    private static CountDownLatch latch;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        initDb();
        PublisherLoaderService publisherLoaderService = new PublisherLoaderService(connectionSource);

        ExecutorService publisherExecutor = Executors.newSingleThreadExecutor();
        ExecutorService crawlerExecutor = Executors.newFixedThreadPool(MAX_CRAWLER_THREADS);
        latch = new CountDownLatch(MAX_CRAWLER_THREADS);

        // Start threads
        initThreads(crawlerExecutor);
        publisherExecutor.submit(publisherLoaderService);

        // Wait until tasks are done and quit.
        publisherExecutor.shutdown();
        gracefulShutdown(crawlerExecutor);
        // Open up the HTTP endpoint
        Endpoint endpoint = new Endpoint();
        endpoint.serve(connectionSource);

        // Runtime counter to help optimize # of threads
        printTime(getRuntime(startTime));
    }

    private static void initDb() {
        try {
            // DB init
            connectionSource = new JdbcPooledConnectionSource(DATABASE_URL, DB_USER, DB_PW);

            // CAN BE MODIFIED LATER AT PRODUCTION TIME
            // TO NOT REDROP TABLES.
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

    private static void initThreads(ExecutorService pool) {
        Crawler[] crawlerPool;
        crawlerPool = new Crawler[MAX_CRAWLER_THREADS];
        for (Crawler crawler : crawlerPool) {
            crawler = new Crawler(connectionSource);
            pool.submit(crawler);
        }
    }

    private static void gracefulShutdown(ExecutorService pool) {
        try {
            System.out.println("Main waits for other threads to finish...");
            latch.await();
            pool.shutdown();
            System.out.println("Main resumed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static long getRuntime(long startTime) {
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public static CountDownLatch getLatch() {
        return latch;
    }
    
    public static void printTime(long runtime){
        try {
            Thread.sleep(1500); // so that the spark server startup output gets printed first
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Crawling took: " + runtime + " ms with " + MAX_CRAWLER_THREADS + " threads.");
    }

}
