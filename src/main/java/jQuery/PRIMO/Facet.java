package jQuery.PRIMO;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * Created by mehmetc on 19/03/16.
 */
public class Facet {
    private HashMap<String, String> facets = new HashMap<>();

    public Facet(HttpServletRequest request) {
        try {
            facets = Helpers.parsePrimoResultFacets(Helpers.getSessionSearchResult(request));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Facet(HashMap<String, String> facets){
        this.facets = facets;
    }

    public HashMap get() {
        return facets;
    }

    public String getAsJSON(){
        return ((JSONObject) facets).toJSONString();
    }

    public String getAsXML(){
        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.setTypeHintsEnabled( false );
        xmlSerializer.setRootName("facets");
        xmlSerializer.setElementName( "value" );
        net.sf.json.JSONObject json =  (net.sf.json.JSONObject) JSONSerializer.toJSON(getAsJSON());
        json.getJSONArray("facets").setExpandElements(true);
        return xmlSerializer.write(json);
    }
}
