package jQuery.PRIMO;

import com.exlibris.jaguar.xsd.search.DOCDocument;
import com.exlibris.jaguar.xsd.search.FACETDocument;
import com.exlibris.primo.authentication.PdsRequestInputParams;
import com.exlibris.primo.authentication.PrimoAuthenticationProfile;
import com.exlibris.primo.authentication.SingleAuthenticationProfile;
import com.exlibris.primo.authentication.UserAuthenticationServices;
import com.exlibris.primo.authentication.authuserinfo.AuthUserInfoManager;
import com.exlibris.primo.authentication.authuserinfo.PDSAuthUserInfoManager;
import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.delivery.Institution;
import com.exlibris.primo.domain.delivery.InstitutionIP;
import com.exlibris.primo.domain.entities.HRemoteSourceRecord;
import com.exlibris.primo.domain.entities.HSourceRecord;
import com.exlibris.primo.domain.entities.OriginalSourceRecord;
import com.exlibris.primo.domain.views.Views;
import com.exlibris.primo.facade.InstitutionsManagementFacade;
import com.exlibris.primo.jsonld.PnxRestApiHandler;
import com.exlibris.primo.jsonld.RestBriefsearchInputParams;
import com.exlibris.primo.logger.PrimoLogger;
import com.exlibris.primo.pds.PdsUserInfo;
import com.exlibris.primo.server.facade.ViewsManagementFacade;
import com.exlibris.primo.utils.CommonUtil;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.utils.UserContext;
import com.exlibris.primo.xsd.commonData.PrimoResult;
import com.sun.deploy.xml.XMLParser;
import net.sf.json.xml.XMLSerializer;
import org.apache.xmlbeans.XmlOptions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This file is part of jQuery.PRIMO
 * Bridge to Primo
 * <p/>
 * MIT license
 * KULeuven/LIBIS (c) 2015
 * Created by mehmetc on 20/11/15.
 */
public class Helpers {
    private PrimoLogger logger = PrimoLogger.getPrimoLogger(this.getClass());

    /**
     * Build session view json object.
     *
     * @param request the request
     * @return the json object
     */
    public static JSONObject buildSessionView(HttpServletRequest request) {
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
            //viewObj.put("frontEndID", request.getLocalName().hashCode());
            viewObj.put("frontEndID", request.getLocalName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return viewObj;
    }

    /**
     * Build session pds json object.
     *
     * @param request the request
     * @return the json object
     */
    public static JSONObject buildSessionPds(HttpServletRequest request) {
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

    /**
     * Build session ip json object.
     *
     * @param request the request
     * @return the json object
     */
    public static JSONObject buildSessionIp(HttpServletRequest request) {
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

    /**
     * Build session user json object.
     *
     * @param request the request
     * @return the json object
     */
    public static JSONObject buildSessionUser(HttpServletRequest request) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return userObj;
    }

    /**
     * Gets session id.
     *
     * @param request the request
     * @return the session id
     */
    public static String getSessionID(HttpServletRequest request) {
        return SessionUtils.getSessionId(request);
    }

    /**
     * Gets session search result.
     *
     * @param request the request
     * @return the session search result
     */
    public static PrimoResult getSessionSearchResult(HttpServletRequest request) {
        return SessionUtils.getSearchResult(request);
    }

    /**
     * Parse primo result hash map.
     *
     * @param primoResult the primo result
     * @return the hash map
     */
    public static HashMap<String, String> parsePrimoResult(PrimoResult primoResult) {
        HashMap<String, String> resultSet = new HashMap<>();

        try {
            DOCDocument.DOC[] docArray = primoResult.getSEGMENTS().getJAGROOTArray(0).getRESULT().getDOCSET().getDOCArray().clone();
            for (DOCDocument.DOC doc : docArray) {
                String id = doc.getPrimoNMBib().getRecordArray(0).getControl().getRecordidArray(0);
                XmlOptions xmlOptions = new XmlOptions();
                //HashMap namespaceMap = new HashMap();

                //remove namespace
                //namespaceMap.put("http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib", "");
                //namespaceMap.put("http://www.exlibrisgroup.com/xsd/jaguar/search", "");
    //            namespaceMap.put("http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib", "");
      //          namespaceMap.put("http://www.exlibrisgroup.com/xsd/jaguar/search", "");

                //xmlOptions.setLoadSubstituteNamespaces(namespaceMap);
        //        xmlOptions.setSaveSuggestedPrefixes(namespaceMap);
        //        xmlOptions.setUseDefaultNamespace();
                xmlOptions.setSaveOuter();

               // String tmpRecord = doc.getPrimoNMBib().getRecordArray(0).xmlText(xmlOptions).replaceAll("<record .*?>", "<record>"); //default

                String tmpRecord = removeXmlStringNamespaceAndPreamble(doc.getPrimoNMBib().getRecordArray(0).xmlText(xmlOptions));

                resultSet.put(id, tmpRecord);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    /**
     * Lookup a record and return the PNX record as a PrimoResult
     *
     * @param recordID record id to lookup
     * @param request  the request
     * @return a record as a PrimoResult
     */
    public static PrimoResult searchReturnPrimoResult(String recordID, HttpServletRequest request) {
        PrimoResult result = null;

        try {
            PnxRestApiHandler pnxRestApiHandler = (PnxRestApiHandler)ContextAccess.getInstance().getBean("pnxRestApiHandler");

            RestBriefsearchInputParams inputParameters = new RestBriefsearchInputParams();
            inputParameters.institution = SessionUtils.getInstitutionCode(request);
            inputParameters.scope = (String)request.getSession().getAttribute("defaultLocalScope");
            inputParameters.serviceType = RestBriefsearchInputParams.SERVICE_TYPE.RDF;
            inputParameters.vid = SessionUtils.getSessionViewId(request);
            inputParameters.rtaLinks = false;
            inputParameters.limit = 1;
            inputParameters.offset = 0;
            inputParameters.view = "brief";
            inputParameters.ip = request.getRemoteAddr();
            inputParameters.language = "eng";
            inputParameters.showPnx = true;

            if (recordID.startsWith("TN_")) {
                inputParameters.setContext("PC");
                //inputParameters.setQueryFields("rid,exact,"+recordID.replaceAll("^TN_", ""));
                inputParameters.setQueryFields(recordID);
            } else {
                //inputParameters.setQueryFields("rid,exact,"+recordID);
                inputParameters.setQueryFields(recordID);
            }

            result = pnxRestApiHandler.doSearch(inputParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lookup a record and return the PNX record as text
     *
     * @param recordID record id to lookup
     * @param request  the request
     * @return a record
     */
    public static String searchReturnString(String recordID, HttpServletRequest request) {
        String result = "";
        try {
            PrimoResult primoResult = searchReturnPrimoResult(recordID, request);
            DOCDocument.DOC[] docArray = primoResult.getSEGMENTS().getJAGROOTArray(0).getRESULT().getDOCSET().getDOCArray().clone();
            if (docArray.length > 0) {
                result = docArray[0].getPrimoNMBib().getRecordArray(0).xmlText(new XmlOptions().setSaveOuter());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Gets original record.
     *
     * @param recordID the record id
     * @return the original record
     */
    public static String getOriginalRecord(String recordID) {
        List resultList = new ArrayList();

        try {

            if (CommonUtil.isNotLocalRecord(recordID)) {
                resultList = ContextAccess.getInstance().getPersistenceManager().find("from HRemoteSourceRecord record where record.recordId = ?", new Object[]{recordID});

                if (resultList.size() > 0) {
                    return ((HRemoteSourceRecord) resultList.get(0)).getXmlContent();
                }
            } else {
                resultList = ContextAccess.getInstance().getPersistenceManager().find("from OriginalSourceRecord record where record.recordID = ?", new Object[]{recordID});

                if (resultList.size() > 0) {
                    return ((OriginalSourceRecord) resultList.get(0)).getSourceRecord();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Resolve dedup record list.
     *
     * @param dedupID the dedup id
     * @param request the request
     * @return the list
     */
    public static List<String> resolveDedupRecord(String dedupID, HttpServletRequest request) {
        List<String> dedupList = new ArrayList();

        try {
            List resultList = new ArrayList();
            long recordID = Long.parseLong(dedupID.replaceAll("dedupmrg", ""));

            resultList = ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.matchId = ?", new Object[]{recordID});

            if (resultList.size() > 0) {
                for (Object record : resultList) {
                    if (!dedupList.contains(((HSourceRecord) record).getSourceId())) {
                        dedupList.add(((HSourceRecord) record).getSourceId());
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return dedupList;
    }

    /**
     * Has session boolean.
     *
     * @param request the request
     * @return the boolean
     */
    public static boolean hasSession(HttpServletRequest request){
        return request.isRequestedSessionIdValid();
    }

    public static HashMap parsePrimoResultFacets(PrimoResult primoResult) {
        JSONObject resultFacets = new JSONObject();
        JSONArray facets = new JSONArray();

        FACETDocument.FACET[] rawFacets = primoResult.getSEGMENTS().getJAGROOTArray(0).getRESULT().getFACETLISTArray(0).getFACETArray();

        for (int i = 0; i < rawFacets.length; i++) {
            JSONArray values = new JSONArray();
            JSONObject facet = new JSONObject();

            for (int j = 0; j < rawFacets[i].getFACETVALUESArray().length; j++) {
                JSONObject value = new JSONObject();
                value.put("name", rawFacets[i].getFACETVALUESArray(j).getKEY());
                value.put("count", rawFacets[i].getFACETVALUESArray(j).getVALUE());
                values.add(value);
            }

            facet.put("index", i);
            facet.put("name", "facet_" + rawFacets[i].getNAME());
            facet.put("count", rawFacets[i].getCOUNT());

            facet.put("values", values);

            facets.add(facet);
        }

        resultFacets.put("facets", facets);

        return resultFacets;
    }

    public static HashMap parsePrimoResultFacets2(PrimoResult primoResult) {
        JSONParser parser = new JSONParser();
        try {
            return (HashMap) parser.parse(new XMLSerializer().read(primoResult.getSEGMENTS().getJAGROOTArray(0).getRESULT().getFACETLISTArray(0).xmlText()).toString(2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String removeXmlStringNamespaceAndPreamble(String xmlString) {
        return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
                replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
                .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
                .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
    }

}
