package adstxtcrawler;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import spark.Request;
import spark.Response;
import spark.Route;
import static spark.Spark.get;
import static spark.Spark.post;

public class Main {

    private static final String URL_MAPPING = "/";
    private static final String DATABASE_URL = "jdbc:h2:./src/main/resources/storage";
    private static final String DB_USER = "admin";
    private static final String DB_PW = "";

    public static void main(String[] args) {
        try {
            get(URL_MAPPING, (Request request, Response response) -> "Spark out here");
            
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL, DB_USER, DB_PW);
            TableUtils.createTableIfNotExists(connectionSource, Record.class);
            Dao<Record,String> recordDao = DaoManager.createDao(connectionSource, Record.class);
            Record r = new Record();
            r.setExchange("test");
            r.setAuthId("test");
            r.setPubId("test");
            r.setRelationship("DIRECT");
            recordDao.create(r);
            System.out.println("added to database");

            // Has a shutdown hook, not needed
            // conn.close();
        } catch (SQLException e) {
            System.out.println("SHOULDT BE THIS WAY");
            e.printStackTrace();
        }
    }
}
