package adstxtcrawler.threads;

import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.commons.validator.routines.UrlValidator;

public class PublisherLoaderService implements Runnable {

    private static final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";
    private static final int QUEUE_MAX_SIZE = 100;
    private static final int SECONDS_TO_WAIT_BETWEEN_ATTEMPTS = 15;
    private static BlockingQueue<String> publishersToProcess;

    private static BufferedReader bufferedReader;
    private static volatile boolean done;
    private long publisherCount;

    public PublisherLoaderService(ConnectionSource connectionSource) {
        done = false;
        this.publisherCount = 0;
        if (publishersToProcess == null) {
            publishersToProcess = new LinkedBlockingDeque<>(QUEUE_MAX_SIZE);
        }
    }

    @Override
    public void run() {
        try {
            loadPublishers();
        } catch (InterruptedException e) {
            System.out.println("PublisherLoaderService thread was interrupted");
            e.printStackTrace();
        }
        done = true;
    }

    /* Reads in a list of publishers from resources/publishers.txt into Queue*/
    private void loadPublishers() throws InterruptedException {
        //System.out.println("We in loadPublishers() with Thread: " + Thread.currentThread().getId() + " : " + Thread.currentThread().getName());
        UrlValidator urlValidator = new UrlValidator();
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            bufferedReader = new BufferedReader(new FileReader(publishersFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line != null) {
                    line = line.trim();
                    if (urlValidator.isValid(line)) {
                        // PRODUCER
                        if (!publishersToProcess.offer(line, SECONDS_TO_WAIT_BETWEEN_ATTEMPTS, TimeUnit.SECONDS)) {
                            // else we block until we can add
                            publishersToProcess.put(line);
                        }
                        System.out.println("Added: " + line);
                        publisherCount++;
                    }
                }
            }
            System.out.println("### PUBLISHER LOADING COMPLETE, ADDING POISON PILL ###");
            publishersToProcess.put(Crawler.getPill());
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Helper method for endpoint*/
    public static boolean isPublisherExpired(long pubExpTime) {
        Date now = new Date();
        System.out.println("Now its: " + now
                + "\nExpires at: " + new Date(pubExpTime));
        return now.getTime() > pubExpTime;
    }

    /* GETTERS AND SETTERS */
    public static boolean isDone() {
        return done;
    }

    public long getPublisherCount() {
        return publisherCount;
    }

    public static BlockingQueue<String> getPublishersToProcessQueue() {
        return publishersToProcess;
    }

}
