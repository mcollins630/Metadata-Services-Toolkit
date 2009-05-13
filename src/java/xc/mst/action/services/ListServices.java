/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.action.services;

import com.opensymphony.xwork2.ActionSupport;

import java.util.List;
import org.apache.log4j.Logger;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import xc.mst.bo.service.Service;
import xc.mst.constants.Constants;
import xc.mst.dao.service.ServiceDAO;
import xc.mst.manager.processingDirective.DefaultServicesService;
import xc.mst.manager.processingDirective.ServicesService;

/**
 * This action is used to display all the services that are part of the MST
 *
 * @author Tejaswi Haramurali
 */

public class ListServices extends ActionSupport 
{
       
    /** Determines whether the rows are to be sorted in ascending or descending order*/
    private boolean isAscendingOrder = true;

    /** The coumn on which the rows are sorted*/
    private String columnSorted = "ServiceName";
    
    /** The list of services that are part of the MST */
    private List<Service> ServicesList;

    /** Base URL of the system **/
    private String baseURL;

    /** A reference to the logger for this class */
    static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

    /**
     * Overrides default implementation to list all services
     *
     * @return {@link #SUCCESS}
     */
    @Override
    public String execute()
    {
        try
        {
           String address = java.net.InetAddress.getLocalHost().getHostAddress();
           baseURL = "htpp://"+address+":8080/MetadataServicesToolkit/oaiRepositoryServlet";
           ServicesService servService = new DefaultServicesService();
           if(columnSorted.equalsIgnoreCase("ServiceName")||(columnSorted.equalsIgnoreCase("Port")))
            {
                if(columnSorted.equalsIgnoreCase("ServiceName"))
                {
                    ServicesList = servService.getAllServicesSorted(isAscendingOrder, ServiceDAO.COL_SERVICE_NAME);
                }
                else
                {
                    ServicesList = servService.getAllServicesSorted(isAscendingOrder, ServiceDAO.COL_PORT);
                }
                setServices(ServicesList);
                setIsAscendingOrder(isAscendingOrder);
                setColumnSorted(columnSorted);
                return SUCCESS;
            }
            else
            {
                this.addFieldError("listServicesError", "The specified column does not exist");
                return INPUT;
            }
           
           
        }
        catch(Exception e)
        {
            log.error("The list of services could not be displayed",e);
            return INPUT;
        }
    }

    /**
     * Sets the List of services that are part of the MST
     *
     * @param ServicesList list of services
     */
    public void setServices(List<Service> ServicesList)
    {
        this.ServicesList = ServicesList;
    }

    /**
     * Returns the list of services that are part of the MST
     *
     * @return list of services
     */
    public List<Service> getServices()
    {
        return ServicesList;
    }

     /**
     * Sets the boolean value which determines if the rows are to be sorted in ascending order
     *
     * @param isAscendingOrder
     */
    public void setIsAscendingOrder(boolean isAscendingOrder)
    {
        this.isAscendingOrder = isAscendingOrder;
    }

    /**
     * Gets the boolean value which determines if the rows are to be sorted in ascending order
     *
     * @param isAscendingOrder
     */
    public boolean getIsAscendingOrder()
    {
        return this.isAscendingOrder;
    }

    /**
     * Sets the name of the column on which the sorting should be performed
     *
     * @param columnSorted name of the column
     */
    public void setColumnSorted(String columnSorted)
    {
        this.columnSorted = columnSorted;
    }

    /**
     * Returns the name of the column on which sorting should be performed
     * 
     * @return column name
     */
    public String getColumnSorted()
    {
        return this.columnSorted;
    }

    /**
     * Sets the base URL
     *
     * @param Base URL
     */
    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    /**
     * Returns base URL
     *
     * @return base URL
     */
    public String getBaseURL()
    {
        return this.baseURL;
    }
    
}
