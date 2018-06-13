package adstxtcrawler.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

@DatabaseTable(tableName = "publishers")
public class Publisher {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, unique = true)
    private String name;
    @ForeignCollectionField(eager = false)
    private ForeignCollection<Record> records;
    @DatabaseField
    private long expiresAt;

    public Publisher() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Publisher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ForeignCollection<Record> getRecords() {
        return records;
    }

    public void setRecords(ForeignCollection<Record> records) {
        this.records = records;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

}
