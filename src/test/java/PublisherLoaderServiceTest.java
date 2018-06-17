package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ValidatorTest {

    private Publisher pub;

    @Test
    public void testRecordValidation() {
        System.out.println("Running: testRecordValidation");
        String recordString;
        Record record;

        recordString = "tremorhub.com, ug5m3, DIRECT, 1a4e959a1b50034a";
        record = Validator.validateRecord(pub, recordString);
        assertNotNull(record);
        Assert.assertEquals("tremorhub.com", record.getExchange());
        Assert.assertEquals("ug5m3", record.getPubId());
        Assert.assertEquals("DIRECT", record.getRelationship());
        Assert.assertEquals("1a4e959a1b50034a", record.getAuthId());

        recordString = "spotxchange.com,   \t    184125,   RESELLER, 7842df1d2fe2db34";
        record = Validator.validateRecord(pub, recordString);
        assertNotNull(record);
        Assert.assertEquals("spotxchange.com", record.getExchange());
        Assert.assertEquals("184125", record.getPubId());
        Assert.assertEquals("RESELLER", record.getRelationship());
        Assert.assertEquals("7842df1d2fe2db34", record.getAuthId());

        recordString = "kargo.com, 105, DIRECT # banner";
        record = Validator.validateRecord(pub, recordString);
        assertNotNull(record);
        Assert.assertEquals("kargo.com", record.getExchange());
        Assert.assertEquals("105", record.getPubId());
        Assert.assertEquals("DIRECT", record.getRelationship());
        Assert.assertNull(record.getAuthId());
    }

    @Test
    public void testBadValidation() {
        System.out.println("Running: testBadRecordValidation");
        String badRecordString;
        Record record;
        
        badRecordString = "tremorhub, ug5m3, DIRECT, 1a4e959a1b50034a";
        record = Validator.validateRecord(pub, badRecordString);
        assertNull(record);
        
        badRecordString = "tremorhub.com, ug4512s,, DIRECT, 1a4e959a1b50034a";
        record = Validator.validateRecord(pub, badRecordString);
        assertNull(record);
        
        badRecordString = "tremorhub.com, ug45\t12s,, DIRE\nCT, 1a4e959a1b50034a";
        record = Validator.validateRecord(pub, badRecordString);
        assertNull(record);
        
        badRecordString = "MA#LF,O#RM,ED STRI,NG 5125125 ASFF, , , ASFAS SAFASF ,";
        record = Validator.validateRecord(pub, badRecordString);
        assertNull(record);
    }
    
    

}
