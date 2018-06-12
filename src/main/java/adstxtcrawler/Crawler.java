package adstxtcrawler;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Crawler {

    private final String            PUBLISHERS_PATH = "src/main/resources/publishers.txt";
    private Queue<String>           publishers;
    private Scanner                 sc;
    private Validator               validator;
    private Dao<Record,String>      recordDao;
    private ConnectionSource        connectionSource;

    public Crawler(ConnectionSource cs) {
        connectionSource = cs;
        publishers = new LinkedList<>();
        
        try{
            recordDao = DaoManager.createDao(connectionSource, Record.class);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseAdsTxt(String urlString) {
        try {
            URL url = new URL(urlString);
            sc = new Scanner(url.openStream());
            String line = sc.nextLine();
            System.out.println("GOT LINE:\n" + line + "\nFROM THE URL!");
            
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Queue<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Queue<String> publishers) {
        this.publishers = publishers;
    }
    
    
}
