package adstxtcrawler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "records")
public class Record {

    @DatabaseField(generatedId = true)
    private int id;
    @JsonIgnore
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Publisher publisher;
    @DatabaseField
    private String exchange;
    @DatabaseField
    private String pubId;
    @DatabaseField
    private String relationship;
    @DatabaseField
    private String authId;

    public Record() {
    }

    public Record(Publisher publisher, String exchange, String pubId, String relationship) {
        this.publisher = publisher;
        this.exchange = exchange;
        this.pubId = pubId;
        this.relationship = relationship;
        this.authId = null;
    }

    public Record(Publisher publisher, String exchange, String pubId, String relationship, String authId) {
        this.publisher = publisher;
        this.exchange = exchange;
        this.pubId = pubId;
        this.relationship = relationship;
        this.authId = authId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
    
    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPubId() {
        return pubId;
    }

    public void setPubId(String pubId) {
        this.pubId = pubId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

}
