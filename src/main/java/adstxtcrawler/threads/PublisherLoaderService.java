package adstxtcrawler.threads;

import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.commons.validator.routines.UrlValidator;

public class PublisherLoaderService implements Runnable {

    private static final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";
    private static final int QUEUE_MAX_SIZE = 100;
    private static final int SECONDS_TO_WAIT_BETWEEN_ATTEMPTS = 15;
    private static BlockingQueue<String> publishersToProcess;
    private static UrlValidator urlValidator;

    private static BufferedReader bufferedReader;
    private static volatile boolean done;
    private static long publisherCount;

    public PublisherLoaderService(ConnectionSource connectionSource) {
        done = false;
        publisherCount = 0;
        if (publishersToProcess == null) {
            publishersToProcess = new LinkedBlockingDeque<>(QUEUE_MAX_SIZE);
        }
        if (urlValidator == null) {
            urlValidator = new UrlValidator();
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
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            bufferedReader = new BufferedReader(new FileReader(publishersFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                parseLine(line);
            }

            System.out.println("### PUBLISHER LOADING COMPLETE, ADDING POISON PILL ###");
            publishersToProcess.put(Crawler.getPill());
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Error reading in publishers.txt");
            e.printStackTrace();
        }
    }

    private static void parseLine(String line) throws InterruptedException {
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
