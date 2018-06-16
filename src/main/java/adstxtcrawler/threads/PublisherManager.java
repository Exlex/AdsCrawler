package adstxtcrawler.threads;

import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class PublisherManager implements Runnable {

    private static final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";
    private final BlockingQueue<String> publishersToProcess;
    
    private static BufferedReader bufferedReader;
    private boolean done;
    private long publisherCount;
    
    public PublisherManager(ConnectionSource connectionSource, BlockingQueue<String> publishers){
        this.done = false;
        this.publisherCount = 0;
        this.publishersToProcess = publishers;
    }
    
    @Override
    public void run() {
        loadPublishers();
        done = true;
    }
    
    /* Reads in a list of publishers from resources/publishers.txt into Queue*/
    private void loadPublishers() {
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            bufferedReader = new BufferedReader(new FileReader(publishersFile));
            String line = "";
            
            while((line = bufferedReader.readLine()) != null) {
                if (line != null) {
                    // PRODUCER
                    while(!publishersToProcess.offer(line.trim())){
                        System.out.println("Producer's 'to crawl' queue is full!");
                    }
                    System.out.println("Added: " + line);
                    publisherCount++;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    /* GETTERS AND SETTERS */
    public boolean isDone() {
        return done;
    }

    public long getPublisherCount() {
        return publisherCount;
    }
    
}
