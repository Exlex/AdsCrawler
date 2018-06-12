package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;

public class Validator {
    
    public Record validateRecord(Publisher pub, String r){
        String recordString = "", exchange, pubId, relationship, authId;
        String[] splitRecord;
        //System.out.println("Validating: " + r);
        
        // Handle blank line
        if (r.isEmpty()){
            return null;
        }
        
        // Handle comments
        if(r.contains("#")){
            if(r.indexOf("#") == 0){
                return null; // whole line is a comment, make Crawler skip it
            }
            recordString = r.substring(0, r.indexOf("#")); // remove inline comments
            //System.out.println("Record without comment: " + recordString);
            
        } else {
            recordString = r;
        }
        
        // split on comma to get each field
        splitRecord = recordString.split(",");
        if(splitRecord.length < 3){
            //System.out.println("Too little fields in this record");
            return null;
        }
        
        for(String field: splitRecord){
            field = field.trim();
        }
        exchange = splitRecord[0];
        pubId = splitRecord[1];
        relationship = splitRecord[2];
        if(splitRecord.length == 4){
            authId = splitRecord[3];
            if(validateFields(exchange, pubId, relationship, authId)){
                return new Record(pub, exchange, pubId, relationship, authId);
            }
        } else {
            if(validateFields(exchange, pubId, relationship)){
                return new Record(pub, exchange, pubId, relationship);
            }
        }

        return null;
    }

    private boolean validateFields(String exchange, String pubId, String relationship){
        boolean valid = true;
        if(!validateExchangeDomain(exchange)){
            valid = false;
        } else if(!validatePublisherId(pubId)){
            valid = false;
        } else if(!validateRelationship(relationship)){
            valid = false;
        }
        return valid;
    }
    
    private boolean validateFields(String exchange, String pubId, String relationship, String authId){
        if(validateFields(exchange, pubId, relationship)){
            return validateAuthId(authId);
        }
        return false;
    }
    
    private boolean validateExchangeDomain(String exchange){
        return true;
    }
    
    private boolean validatePublisherId(String pubId){
        return true;
    }
    
    private boolean validateRelationship(String relationship){
        return true;
    }
    
    private boolean validateAuthId(String authId){
        return true;
    }
}
