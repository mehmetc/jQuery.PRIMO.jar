package jQuery.PRIMO;

import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * This file is part of jQuery.PRIMO
 * Session returns a session object
 *
 * MIT license
 * KULeuven/LIBIS (c) 2015
 * Created by mehmetc on 20/11/15.
 */
public class Session {

    private JSONObject sessionData = new JSONObject();

    /**
     * Instantiates a new Session.
     *
     * @param request the request
     */
    public Session(HttpServletRequest request) {
        try {
//GENERAL
            sessionData.put("sessionId", Helpers.getSessionID(request));
            sessionData.put("user", Helpers.buildSessionUser(request));
            sessionData.put("view", Helpers.buildSessionView(request));
            sessionData.put("pds", Helpers.buildSessionPds(request));
            sessionData.put("ip", Helpers.buildSessionIp(request));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets as hash map.
     *
     * @return the as hash map
     */
    public HashMap getAsHashMap() {
        return sessionData;
    }

    /**
     * Gets as json.
     *
     * @return the as json
     */
    public String getAsJSON() {
        return sessionData.toJSONString();
    }

    /**
     * Get session information.
     *
     * @return session info as json
     */
    public JSONObject get() {
        return sessionData;
    }
}
