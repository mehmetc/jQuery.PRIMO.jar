package jQuery.PRIMO;

import com.google.common.base.Joiner;
import net.sf.json.xml.XMLSerializer;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This file is part of jQuery.PRIMO
 * ResultSet returns records from the result set.
 *
 * MIT license
 * KULeuven/LIBIS (c) 2015
 * Created by mehmetc on 20/11/15.
 */
public class ResultSet {
    private HashMap<String, String> resultSet = new HashMap<>();

    /**
     * Instantiates a new Result set.
     *
     * @param request the request
     */
    public ResultSet(HttpServletRequest request) {
        try {
           resultSet = Helpers.parsePrimoResult(Helpers.getSessionSearchResult(request));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Instantiates a new Result set.
     *
     * @param resultSet the result set
     */
    public ResultSet(HashMap<String, String> resultSet) {
        this.resultSet = resultSet;
    }

    /**
     * Check if record id is in the resultSet.
     *
     * @param id the record id
     * @return boolean boolean
     */
    public boolean includes(String id) {
        return id.equals("*") || resultSet.containsKey(id);
    }


    /**
     * Gets as pnx.
     *
     * @param id the id
     * @return the as pnx
     */
    public String getAsPnx(String id) {
        if (this.includes(id)) {
            return resultSet.get(id);
        } else {
            throw new RuntimeException("record not in result set");
        }
    }

    /**
     * Gets as xml.
     *
     * @param id the id
     * @return the as xml
     */
    public String getAsXml(String id) {
        try {
            if (this.includes(id)) {
                return Record.getOriginalRecord(id);
            } else {
                throw new RuntimeException("record not in result set");
            }
        } catch (Exception e){
            return resultSet.get(id);
        }
    }

    /**
     * Gets as json.
     *
     * @param id the id
     * @return the as json
     */
    public String getAsJSON(String id) {
        String data = "";

        if (this.includes(id)) {
            try {
                return new XMLSerializer().read(this.getAsPnx(id)).toString(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("record not in result set");
        }
        return data;
    }

    /**
     * Get a record.
     *
     * @param id   record id
     * @param type record type
     * @return record string
     */
    public String get(String id, String type){
        String result;
        if (this.includes(id)) {

            if (id.equals("*")){
                List records = new ArrayList();
                for( String key : resultSet.keySet()) {
                    records.add(this.get(key, type));
                }

                switch (type){
                    case "JSON":
                        result = "[" + Joiner.on(",").join(records.iterator()) + "]";
                        break;
                    case "XML":
                    default:
                        result = "<records>" + Joiner.on("").join(records) + "</records>";
                }

            } else {
                switch (type) {
                    case "XML":
                        result = this.getAsXml(id);
                        break;
                    case "JSON":
                        result = this.getAsJSON(id);
                        break;
                    default:
                        result = this.getAsPnx(id);
                }
            }

        } else {
            return null;
            //throw new RuntimeException("record not in result set");
        }

        return result;
    }

    /**
     * Get keys set.
     *
     * @return the set
     */
    public Set<String> getKeys(){
        return resultSet.keySet();
    }

    /**
     * Get values collection.
     *
     * @return the collection
     */
    public Collection<String> getValues(){
        return resultSet.values();
    }

}
