package jQuery.PRIMO;

import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mehmetc on 22/11/15.
 */
public class RecordTest extends TestCase {
    @Test
    public void testDetermineRecordExt() throws Exception {
        String extension = Record.determineRecordExt("32LIBIS_ALMA_DS_123456789012.xml");
        assertEquals("must return PNX extension", "XML", extension);

        extension = Record.determineRecordExt("32LIBIS_ALMA_DS_123456789012.pnx");
        assertEquals("must return PNX extension", "PNX", extension);

        extension = Record.determineRecordExt("32LIBIS_ALMA_DS_123456789012.json");
        assertEquals("must return JSON extension", "JSON", extension);
        //Any other extension besides XML,PNX should result in PNX
        extension = Record.determineRecordExt("32LIBIS_ALMA_DS_123456789012.csv");
        assertEquals("must return PNX extension", "PNX", extension);

    }

    @Test
    public void testGetRecordExt() throws Exception {
        String extension = Record.getRecordExt("32LIBIS_ALMA_DS_123456789012.xml");
        assertEquals("must return xml extension", "xml", extension);

        extension = Record.getRecordExt("32LIBIS_ALMA_DS_123456789012.json");
        assertEquals("must return json extension", "json", extension);
    }

    @Test
    public void testGetRecordID() throws Exception {
        String recordId = Record.getRecordID("32LIBIS_ALMA_DS_123456789012.xml");
        assertEquals("must return 32LIBIS_ALMA_DS_123456789012", "32LIBIS_ALMA_DS_123456789012", recordId);
    }


    public void testGetOriginalRecord() throws Exception {

    }

    public void testResolveDedupRecord() throws Exception {

    }
}