package jQuery.PRIMO;

import com.exlibris.primo.authentication.PdsRequestInputParams;
import com.exlibris.primo.authentication.PrimoAuthenticationProfile;
import com.exlibris.primo.authentication.SingleAuthenticationProfile;
import com.exlibris.primo.authentication.UserAuthenticationServices;
import com.exlibris.primo.authentication.authuserinfo.AuthUserInfoManager;
import com.exlibris.primo.authentication.authuserinfo.PDSAuthUserInfoManager;
import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.delivery.Institution;
import com.exlibris.primo.domain.delivery.InstitutionIP;
import com.exlibris.primo.domain.views.Views;
import com.exlibris.primo.facade.InstitutionsManagementFacade;
import com.exlibris.primo.pds.PdsUserInfo;
import com.exlibris.primo.server.facade.ViewsManagementFacade;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.utils.UserContext;
import net.sf.json.xml.XMLSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

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
            sessionData.put("sessionId", SessionUtils.getSessionId(request));
            sessionData.put("user", buildUser(request));
            sessionData.put("view", buildView(request));
            sessionData.put("pds", buildPds(request));
            sessionData.put("ip", buildIp(request));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject buildView(HttpServletRequest request) {
        JSONObject viewObj = new JSONObject();
        try {
            String viewId = SessionUtils.getSessionViewId(request);
            viewObj.put("code", viewId);

            JSONObject viewInstitutionObj = new JSONObject();
            ViewsManagementFacade viewFacade = (ViewsManagementFacade) ContextAccess.getInstance().getBean("viewsManagementFacade");
            try {
                List<Views> viewList = viewFacade.findViewByViewCode(viewId);
                Views view = viewList.get(0);

                Institution institution = view.getInstitutions();
                viewInstitutionObj.put("name", institution.getInstitutionName());
                viewInstitutionObj.put("code", institution.getInstitutionCode());
            } catch (Exception e) {
                viewInstitutionObj.put("name", "");
                viewInstitutionObj.put("code", "");
            }

            viewObj.put("institution", viewInstitutionObj);
            viewObj.put("interfaceLanguage", SessionUtils.getChosenInterfaceLanguage(request));
            viewObj.put("frontEndID", request.getLocalName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return viewObj;
    }

    private JSONObject buildPds(HttpServletRequest request) {
        JSONObject pdsObj = new JSONObject();


        try {
            String pdsHandle = SessionUtils.getPdsHandle(request);

            if (pdsHandle != null && pdsHandle.length() > 0) {
                pdsObj.put("handle", pdsHandle);
            }

            String institutionCode = SessionUtils.getInstitutionCode(request);
            String borInfo = "";

            pdsObj.put("institution", institutionCode);
            try {
                String pdsBaseUrl = "";

                UserAuthenticationServices userAuthenticationServices = (UserAuthenticationServices) ContextAccess.getInstance().getBean("userAuthenticationServices");
                if (userAuthenticationServices != null) {
                    PrimoAuthenticationProfile profile = userAuthenticationServices.getProfile(institutionCode, request);

                    if (profile != null && ((SingleAuthenticationProfile) profile).getParameterValue("AUTHENTICATION_METHOD").equals("PDS")) {
                        pdsBaseUrl = ((SingleAuthenticationProfile) profile).getParameterValue("PDS_URL");

                        PdsRequestInputParams e = new PdsRequestInputParams(pdsBaseUrl + "?", "bor-info", (String) null, pdsHandle, (String) null, institutionCode, (String) null);
                        String borInfoUrl = PDSAuthUserInfoManager.buildPdsRequestUrl(e);
                        pdsObj.put("baseUrl", pdsBaseUrl);
                        pdsObj.put("url", borInfoUrl);
                        borInfo = AuthUserInfoManager.getUrlContents(borInfoUrl);

                        if (borInfo != null && !borInfo.contains("<error>")) {
                            borInfo = new XMLSerializer().read(borInfo).toString(2);
                            JSONParser parser = new JSONParser();
                            JSONObject pds = (JSONObject) parser.parse(borInfo);
                            pdsObj.put("borInfo", pds.get("bor-info"));
                        }
                        //pdsObj.put("borInfoRaw", borInfo);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdsObj;
    }

    private JSONObject buildIp(HttpServletRequest request) {
        JSONObject ipObj = new JSONObject();
        try {
            JSONObject ipInstitutionObj = new JSONObject();
            InetAddress ipAddress = InetAddress.getByName(request.getRemoteAddr());
            ipObj.put("address", ipAddress.getHostAddress());

            InstitutionsManagementFacade institutionFacade = (InstitutionsManagementFacade) ContextAccess.getInstance().getBean("institutionsManagementFacade");
            try {
                InstitutionIP institutionByIP = institutionFacade.findInstitution(ipAddress);

                Institution institution = institutionByIP.getInstitution();

                if (institution != null) {
                    ipInstitutionObj.put("name", institution.getInstitutionName());
                    ipInstitutionObj.put("code", institution.getInstitutionCode());
                }
            } catch (Exception e) {
                ipInstitutionObj.put("name", "");
                ipInstitutionObj.put("code", "");
            }

            ipObj.put("institution", ipInstitutionObj);


        } catch (Exception e) {
            e.printStackTrace();

        }
        return ipObj;
    }

    private JSONObject buildUser(HttpServletRequest request) {
        PdsUserInfo userInfo = SessionUtils.getUserInfo(request);
        JSONObject userObj = new JSONObject();

        try {
            if (userInfo != null) {
                userObj.put("id", userInfo.getUserId());
                userObj.put("name", userInfo.getUserName());
                userObj.put("email", userInfo.getEmail());
                userObj.put("isOnCampus", Boolean.valueOf(UserContext.isOnCampus(request)).booleanValue());
                userObj.put("isLoggedIn", SessionUtils.getIsLoggedIn(request));

                JSONObject rankingObj = new JSONObject();
                try {
                    rankingObj.put("categories", SessionUtils.getPyrCategories(request));
                    rankingObj.put("prefer_new", SessionUtils.getPyrRecentness(request));

                    userObj.put("ranking", rankingObj);
                } catch (Exception e) {
                    userObj.put("ranking", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userObj;
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
