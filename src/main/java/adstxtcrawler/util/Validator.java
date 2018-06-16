package adstxtcrawler.util;

import adstxtcrawler.models.Publisher;
import adstxtcrawler.models.Record;
import org.apache.commons.validator.routines.UrlValidator;

public class Validator {

    public static Record validateRecord(Publisher pub, String r) {
        String recordString, exchange, pubId, relationship, authId;
        String[] splitRecord;

        // Handle blank line
        if (r.isEmpty()) {
            return null;
        }

        // Handle comments
        if (r.contains("#")) {
            if (r.indexOf("#") == 0) {
                return null; // whole line is a comment, skip it
            }
            recordString = r.substring(0, r.indexOf("#")); // remove inline comments
        } else {
            recordString = r;
        }

        // split on comma to get each field
        splitRecord = recordString.split(",");
        if (splitRecord.length < 3 || splitRecord.length > 4) {
            return null;
        }

        exchange        = splitRecord[0].trim();
        pubId           = splitRecord[1].trim();
        relationship    = splitRecord[2].trim().toUpperCase();
        if (splitRecord.length == 4) {
            authId = splitRecord[3].trim();
            if (validateFields(exchange, pubId, relationship, authId)) {
                return new Record(pub, exchange, pubId, relationship, authId);
            }
        } else {
            if (validateFields(exchange, pubId, relationship)) {
                return new Record(pub, exchange, pubId, relationship);
            }
        }
        return null;
    }

    private static boolean validateFields(String exchange, String pubId, String relationship) {
        boolean valid = true;
        if (!validateExchangeDomain(exchange)) {
            valid = false;
        } else if (!validatePublisherId(pubId)) {
            valid = false;
        } else if (!validateRelationship(relationship)) {
            valid = false;
        }
        return valid;
    }

    private static boolean validateFields(String exchange, String pubId, String relationship, String authId) {
        if (validateFields(exchange, pubId, relationship)) {
            return validateAuthId(authId);
        }
        return false;
    }

    private static boolean validateExchangeDomain(String exchange) {
        UrlValidator uv = new UrlValidator();
        return uv.isValid("https://" + exchange) && exchange.length() > 2;
    }

    private static boolean validatePublisherId(String pubId) {
        return pubId.length() > 0;
    }

    private static boolean validateRelationship(String relationship) {
        return relationship.equalsIgnoreCase("DIRECT") | relationship.equalsIgnoreCase("RESELLER");
    }

    private static boolean validateAuthId(String authId) {
        return true;
    }
}
