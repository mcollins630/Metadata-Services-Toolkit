/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.action.oai;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import xc.mst.action.BaseActionSupport;
import xc.mst.bo.service.Service;
import xc.mst.constants.Constants;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.dao.service.ServiceDAO;
import xc.mst.oai.Facade;
import xc.mst.oai.OaiRequestBean;
import xc.mst.utils.LogWriter;
import xc.mst.utils.MSTConfiguration;

/**
 * A simple servlet that parses an OAI request and passes the parameters to the Facade class for processing.
 * 
 * @author Eric Osisek
 */
public class OaiRepository extends BaseActionSupport implements ServletRequestAware, ServletResponseAware {
    /** Used for serialization */
    private static final long serialVersionUID = 56789L;

    /** Request */
    private HttpServletRequest request;

    /** Response */
    private HttpServletResponse response;

    /** XML output */
    private String oaiXMLOutput;

    /**
     * A reference to the logger which writes to the HarvestOut log file
     */
    private static Logger log = Logger.getLogger(Constants.LOGGER_HARVEST_OUT);

    /**
     * It parses the OAI request parameters into an OaiRequestBean and calls the Facade class to process the request.
     * 
     * @throws ServletException
     *             if an servlet error occurred
     * @throws IOException
     *             if an IO error occurred
     */
    public String execute() throws ServletException, IOException {

        try {
            // Get servlet path from request
            String servletPath = request.getServletPath();

            // Extract the service name from servlet path
            int firstOccurrenceOfSlash = servletPath.indexOf("/");
            String serviceName = null;

            // Check if / exist in servlet path.
            if (firstOccurrenceOfSlash != -1) {
                int secondOccurrenceOfSlash = servletPath.indexOf("/", firstOccurrenceOfSlash + 1);
                int thirdOccurrenceOfSlash = servletPath.indexOf("/", secondOccurrenceOfSlash + 1);

                if ((firstOccurrenceOfSlash != -1 && secondOccurrenceOfSlash != -1 && thirdOccurrenceOfSlash != -1) && (thirdOccurrenceOfSlash > secondOccurrenceOfSlash))
                    serviceName = servletPath.substring(secondOccurrenceOfSlash + 1, thirdOccurrenceOfSlash);
                else
                    // Invalid URL
                    response.getWriter().write("Invalid URL");
            } else { // Invalid URL
                response.getWriter().write("Invalid URL");
            }

            // Get the service based on the port.
            Service service = ((ServiceDAO) MSTConfiguration.getInstance().getBean("ServiceDAO")).getByServiceName(serviceName.replace("-", " "));

            if (service == null) {
                // Write the response
                response.getWriter().write("Service with name: " + serviceName + " does not exist.");
                response.setStatus(503);
                return SUCCESS;
            }
            if (!getRepositoryDAO().ready4harvest(serviceName)) {
                // Write the response
                response.getWriter().write("Service with name: " + serviceName + " is not available at this time.  It might be in optimize mode.");
                response.setStatus(503);
                return SUCCESS;
            }

            LogWriter.addInfo(service.getHarvestOutLogFileName(), "Received the OAI request " + request.getQueryString());

            // Bean to manage data for handling the request
            OaiRequestBean oaiRequest = new OaiRequestBean();

            // Set parameters on the bean based on the OAI request's parameters
            oaiRequest.setVerb(request.getParameter("verb"));
            oaiRequest.setFrom(request.getParameter("from"));
            oaiRequest.setUntil(request.getParameter("until"));
            oaiRequest.setMetadataPrefix(request.getParameter("metadataPrefix"));
            oaiRequest.setSet(request.getParameter("set"));
            oaiRequest.setIdentifier(request.getParameter("identifier"));
            oaiRequest.setResumptionToken(request.getParameter("resumptionToken"));
            oaiRequest.setServiceId(service != null ? service.getId() : 0);
            oaiRequest.setOaiRepoBaseURL("http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + servletPath);
            oaiRequest.setRequest(request.getRequestURL().toString());

            // Create the Facade Object, which will compute the results of the request and set them on the bean
            Facade facade = (Facade) MSTConfiguration.getInstance().getBean("Facade");

            // Execute the correct request on the Facade Object
            oaiXMLOutput = facade.execute(oaiRequest);

            response.setContentType("text/xml; charset=UTF-8");

            // Write the response
            response.getWriter().write(oaiXMLOutput);

            return SUCCESS;
        } catch (DatabaseConfigException e) {
            log.error("Cannot connect to the database with the parameters from the config file.", e);

            response.getWriter().write("Do to a configuration error, this OAI repository cannot access its database.");

            return ERROR;
        }
    }

    /**
     * Get servlet request
     * 
     * @return servlet request
     */
    public HttpServletRequest getServletRequest() {
        return request;
    }

    /**
     * Set servlet request
     * 
     * @param servletRequest
     *            servlet request
     */
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.request = servletRequest;
    }

    /**
     * Get servlet response
     * 
     * @return servlet response
     */
    public HttpServletResponse getServletResponse() {
        return response;
    }

    /**
     * Set servlet response
     * 
     * @param servletResponse
     *            servlet response
     */
    public void setServletResponse(HttpServletResponse servletResponse) {
        this.response = servletResponse;
    }

    /**
     * Get output OAI PHM response xml
     * 
     * @return OAI PHM response xml
     */
    public String getOaiXMLOutput() {
        return oaiXMLOutput;
    }

    /**
     * Set output OAI PHM response xml
     * 
     * @param oaiXMLOutput
     *            OAI PHM response xml
     */
    public void setOaiXMLOutput(String oaiXMLOutput) {
        this.oaiXMLOutput = oaiXMLOutput;
    }
}
