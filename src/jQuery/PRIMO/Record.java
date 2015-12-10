package jQuery.PRIMO;

import com.exlibris.jaguar.xsd.search.DOCDocument;
import com.exlibris.primo.context.ContextAccess;
import com.exlibris.primo.domain.entities.HRemoteSourceRecord;
import com.exlibris.primo.domain.entities.HSourceRecord;
import com.exlibris.primo.domain.entities.OriginalSourceRecord;
import com.exlibris.primo.jsonld.PnxRestApiHandler;
import com.exlibris.primo.jsonld.RestBriefsearchInputParams;
import com.exlibris.primo.utils.CommonUtil;
import com.exlibris.primo.utils.SessionUtils;
import com.exlibris.primo.xsd.commonData.PrimoResult;
import org.apache.xmlbeans.XmlOptions;
import org.json.simple.JSONArray;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Gets original record. Mostly MARCXML
     *
     * @param id the id as defined in the result set
     * @return the original record
     */
    public static String getOriginalRecord(String id) {
        try {
            List resultList = new ArrayList();

            if (CommonUtil.isNotLocalRecord(id)) {
                resultList = ContextAccess.getInstance().getPersistenceManager().find("from HRemoteSourceRecord record where record.recordId = ?", new Object[]{id});

                if (resultList.size() > 0) {
                    return ((HRemoteSourceRecord) resultList.get(0)).getXmlContent();
                }
            } else {
                resultList = ContextAccess.getInstance().getPersistenceManager().find("from OriginalSourceRecord record where record.recordID = ?", new Object[]{id});

                if (resultList.size() > 0) {
                    return ((OriginalSourceRecord) resultList.get(0)).getSourceRecord();
                }
            }

            throw new RuntimeException("original record not found");
        } catch(Exception e) {
            //logger.info(e.getMessage());
            throw e;
        }
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

                List<String> dedupList = new ArrayList();

                List resultList = new ArrayList();
                long recordID = Long.parseLong(dedupID.replaceAll("dedupmrg", ""));

                resultList = ContextAccess.getInstance().getPersistenceManager().find("from HSourceRecord record where record.matchId = ?", new Object[]{recordID});

                if (resultList.size() > 0) {
                    for (Object record: resultList){
                        if (!dedupList.contains(((HSourceRecord)record).getSourceId())) {
                            dedupList.add(((HSourceRecord)record).getSourceId());
                        }
                    }
                }

                obj.addAll(dedupList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            obj.add(e.getMessage());
        }
        return obj.toJSONString();
    }

    /**
     * Lookup a record and return the PNX record as a PrimoResult
     *
     * @param recordID record id to lookup
     * @param request the request
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
            inputParameters.setLanguages("eng");

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
     * @param request the request
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


}
