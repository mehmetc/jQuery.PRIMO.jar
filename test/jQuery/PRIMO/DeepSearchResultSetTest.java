package jQuery.PRIMO;

import com.exlibris.primo.xsd.commonData.PrimoResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mehmetc on 06/04/16.
 */
public class DeepSearchResultSetTest {
    private ResultSet resultSet;
    private String recordID = "KluwerVS201430239";

    @Before
    public void setUp() throws Exception {
        System.setProperty("log4j.defaultInitOverride","true");

        File file = new File("/Users/mehmetc/Sources/Libis/Primo/jQuery.PRIMO.jar/test/resources/deepsearch_result.xml");
        this.resultSet = new ResultSet(Helpers.parsePrimoResult(PrimoResult.Factory.parse(file)));
    }

    @Test
    public void testIncludes() throws Exception {
        assertTrue(resultSet.includes(recordID));
        assertFalse(resultSet.includes("BOGUS_RECORD_ID"));
    }

    @Test
    public void testAllAsPNX() throws Exception {
        String data = resultSet.get("*", "PNX");
        System.out.println(data);
        assertNotNull(data);
        assertTrue(true);
    }
}
