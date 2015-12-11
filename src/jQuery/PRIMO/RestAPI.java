package jQuery.PRIMO;

import com.exlibris.primo.logger.PrimoLogger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * This file is part of jQuery.PRIMO
 * RestAPI to session and record
 *
 * MIT license
 * KULeuven/LIBIS (c) 2015
 * Created by mehmetc on 20/11/15.
 */
@Path("/")
public class RestAPI {
    private PrimoLogger logger = PrimoLogger.getPrimoLogger(Record.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;

    /**
     * Gets session parameters.
     *
     * @return the session
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/session")
    public Response getSession() {
        try {
            Session session = new Session(request);
            return Response.status(200).entity(session.getAsJSON()).build();
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/record/resolve/{data}")
    public Response resolveDedup(@PathParam("data") String data, @QueryParam("id") String id){
        String result = "";
        try{
            data = id != null && id.length() > 0 ? id : data;
            String recordID = Record.getRecordID(data);
            ResultSet resultSet = new ResultSet(request);

            if (resultSet.includes(recordID)) {
                result = Record.resolveDedupRecord(recordID, request);
            } else {
                return Response.status(404).entity("record not in result set").build();
            }

        } catch (Exception e) {
            logger.info(e.getMessage());
            return Response.serverError().build();
        }

        return Response.status(200).entity(result).build();
    }


    /**
     * Gets record data.
     *
     * @param data the record id
     * @return the record
     */
//$.get('/primo_library/libweb/jqp/session', function(d){console.log(d)}, 'json')
//$.get('/primo_library/libweb/jqp/record/' + $.PRIMO.records[0].id +'.XML', function(d){console.log(d)}, 'xml')
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/record/{data:.*}")
    public Response getRecord(@PathParam("data") String data, @QueryParam("id") String id) {
        String result = "";

        data = id != null && id.length() > 0 ? id : data;
        String recordID = Record.getRecordID(data);
        String recordExt = Record.determineRecordExt(data);

        try {
            ResultSet resultSet = new ResultSet(request);

            if (!resultSet.includes(recordID)) {
                resultSet = new ResultSet(Helpers.parsePrimoResult(Helpers.searchReturnPrimoResult(recordID, request)));
            }

            if (resultSet != null) {
                result = resultSet.get(recordID, recordExt);
            }

            if (result == null || result.length() == 0) {
                return Response.status(404).entity("record not in result set").build();
            }

            String mediaType = "";

            switch (recordExt){
                case "JSON":
                    mediaType = MediaType.APPLICATION_JSON;
                    break;
                default:
                    mediaType = MediaType.TEXT_XML;
                    if (!result.startsWith("<?xml")) {
                        result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + result;
                    }
            }

            return Response.status(200).type(mediaType).entity(result).build();
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Response.serverError().build();
        }
    }


    /**
     * Get version.
     *
     * @return version
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/version")
    public Response getVersion(){
        return Response.status(200).entity("1.0.0").build();
    }
}
