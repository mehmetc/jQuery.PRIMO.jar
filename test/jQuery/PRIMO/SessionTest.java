package jQuery.PRIMO;

import com.exlibris.primo.utils.SessionUtils;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.FileReader;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;


/**
 * Created by mehmetc on 10/12/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Helpers.class)
public class SessionTest {
    @Mock
    HttpServletRequest request;

    @Mock
    SessionUtils sessionUtils;

    JSONObject sessionData;
    Session session;

    @Before
    public void setUp() throws Exception {
        sessionData = (JSONObject)new JSONParser().parse(new FileReader("./test/resources/session_not_loggedin.json"));
    }

    @Test
    public void testGetAsHashMap() throws Exception {
        setupMock();
        replay(Helpers.class);
        session = new Session(request);
        HashMap result = session.getAsHashMap();
        verify(Helpers.class);

        assertTrue(result.keySet().contains("user"));
        assertTrue(result.keySet().contains("view"));
        assertTrue(result.keySet().contains("ip"));
        assertTrue(result.keySet().contains("pds"));
        assertTrue(result.keySet().contains("sessionId"));

        assertEquals(result.get("sessionId"), "123456");
    }

    @Test
    public void testGetAsJSON() throws Exception {
        setupMock();

        replay(Helpers.class);
        session = new Session(request);
        String result = session.getAsJSON();
        verify(Helpers.class);

        assertNotNull(result);
        assertEquals(result, sessionData.toJSONString());
    }

    @Test
    public void testGet() throws Exception {
        setupMock();
        replay(Helpers.class);
        session = new Session(request);
        JSONObject result = session.get();
        verify(Helpers.class);

        assertEquals(result, sessionData);
    }


    private void setupMock() {
        mockStatic(Helpers.class);
        EasyMock.expect(Helpers.getSessionID(request)).andReturn("123456");
        EasyMock.expect(Helpers.buildSessionUser(request)).andReturn((JSONObject) sessionData.get("user"));
        EasyMock.expect(Helpers.buildSessionView(request)).andReturn((JSONObject) sessionData.get("view"));
        EasyMock.expect(Helpers.buildSessionIp(request)).andReturn((JSONObject) sessionData.get("ip"));
        EasyMock.expect(Helpers.buildSessionPds(request)).andReturn((JSONObject) sessionData.get("pds"));
    }
}