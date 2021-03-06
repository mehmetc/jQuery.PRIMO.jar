package jQuery.PRIMO;

import net.sf.json.xml.XMLSerializer;
import org.json.simple.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

/**
 * This file is part of jQuery.PRIMO
 * Record contains tools to retrieve the original record, resolve a dedup record id or search for a record
 *
 * MIT license
 * KULeuven/LIBIS (c) 2015
 * Created by mehmetc on 20/11/15.
 */
public class Record {
    /**
     * Find out what to return
     *
     * @param data the data
     * @return extension type
     */
    public static String determineRecordExt(String data) {
        String[] supportedExtensions = {"PNX", "XML", "JSON"};

        for (String e : supportedExtensions) {
            if (e.toLowerCase().equals(getRecordExt(data).toLowerCase())) {
                return e;
            }
        }
        return supportedExtensions[0];
    }

    /**
     * Gets record ext.
     *
     * @param data blob of data
     * @return the record ext
     */
    public static String getRecordExt(String data) {
        return data.substring(data.toLowerCase().lastIndexOf(".") + 1);
    }

    /**
     * Get record id.
     *
     * @param data blob of data
     * @return the record id
     */
    public static String getRecordID(String data) {
        return data.replace("." + getRecordExt(data), "");
    }

    public static String get(String recordId, String recordExt) {
        String result = "";
        try {
            switch (recordExt) {
                case "JSON":
                    result = new XMLSerializer().read(Helpers.getPNXRecordFromDB(recordId)).toString(2);
                    break;
                case "XML":
                    result = getOriginalRecord(recordId);
                    break;
                default:
                    result = Helpers.getPNXRecordFromDB(recordId);
            }
        } catch(Exception e) {
            throw e;
        }

        return result;
    }

    /**
     * Gets original record. Mostly MARCXML
     *
     * @param recordID the id as defined in the result set
     * @return the original record
     */
    public static String getOriginalRecord(String recordID) {
        try {
            String record = Helpers.getOriginalRecord(recordID);

            if (record == null ) {
                throw new RuntimeException("original record not found");
            }

            return record;
        } catch(Exception e) {
            throw e;
        }
    }


    /**
     * Gets original record
     *
     * @param recordID the id as defined in the result set
     * @param ext return data as format
     * @return the original record
     */
    public static String getOriginalRecord(String recordID, String ext) {
        String result = "";
        try {
            String record = getOriginalRecord(recordID);

            switch (ext) {
                case "JSON":
                    result = new XMLSerializer().read(record).toString(2);
                    break;
                default:
                    result = record;
            }

        } catch(Exception e) {
            throw e;
        }

        return result;
    }

    /**
     * Resolve dedup record string.
     *
     * @param dedupID the dedup id
     * @param request the request
     * @return a JSON array of record id's
     */
    public static String resolveDedupRecord(String dedupID, HttpServletRequest request) {
        JSONArray obj = new JSONArray();

        try {

            if ((dedupID != null) && (dedupID.length() > 0)) {
                obj.addAll(Helpers.resolveDedupRecord(dedupID,request));
            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.add(e.getMessage());
        }
        return obj.toJSONString();
    }

    public static String search(String recordID, HttpServletRequest request) {
        return Helpers.searchReturnString(recordID, request);
    }

}
