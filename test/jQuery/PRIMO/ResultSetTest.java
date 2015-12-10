package jQuery.PRIMO;

import com.exlibris.primo.xsd.commonData.PrimoResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mehmetc on 27/11/15.
 */
public class ResultSetTest {
    private ResultSet resultSet;
    private String recordID = "32LIBIS_ALMA_DS71119964710001471";

    @Before
    public void setUp() throws Exception {
        System.setProperty("log4j.defaultInitOverride","true");

        File file = new File("/Users/mehmetc/Sources/Libis/Primo/jQuery.PRIMO.jar/test/resources/resultset.xml");
        this.resultSet = new ResultSet(PrimoResult.Factory.parse(file));
    }

    @Test
    public void testIncludes() throws Exception {
        assertTrue(resultSet.includes(recordID));
        assertFalse(resultSet.includes("BOGUS_RECORD_ID"));
    }

    @Test
    public void testGetAsPnx() throws Exception {
        String record = resultSet.getAsPnx(recordID);
        assertTrue(record.contains("<recordid>" + recordID + "</recordid>"));
        assertTrue(record.startsWith("<record"));
        assertTrue(record.endsWith("</record>"));
    }

    @Test
    public void testGetAsXml() throws Exception {
     //   String record = resultSet.getAsXml(recordID);
     //   System.out.println(record);
    }

    @Test
    public void testGetAsJSON() throws Exception {
        String record = resultSet.getAsJSON(recordID);
        assertTrue(record.contains("\"recordid\": \""+ recordID + "\""));
        assertTrue(record.startsWith("{"));
        assertTrue(record.endsWith("}"));
    }

    @Test
    public void testGet() throws Exception {
        String record = resultSet.get(recordID, "PNX");
        assertTrue(record.contains("<recordid>" + recordID + "</recordid>"));

        record = resultSet.get(recordID, "JSON");
        assertTrue(record.contains("\"recordid\": \""+ recordID + "\""));

        String data = resultSet.get("*", "PNX");
        assertTrue(data.startsWith("<records>"));

        data = resultSet.get("*", "JSON");
        assertTrue(data.startsWith("["));
        assertTrue(data.endsWith("]"));

    }
}