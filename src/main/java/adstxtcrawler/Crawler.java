package adstxtcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Crawler {

    private static final String PUBLISHERS_PATH = "src/main/resources/publishers.txt";

    public static void loadPublishers() {
        try {
            File publishersFile = new File(PUBLISHERS_PATH);
            BufferedReader br = new BufferedReader(new FileReader(publishersFile));
            String line = "";

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
