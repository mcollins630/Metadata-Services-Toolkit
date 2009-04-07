/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;

import xc.mst.bo.log.Log;
import xc.mst.dao.log.DefaultLogDAO;
import xc.mst.dao.log.LogDAO;
import xc.mst.bo.service.Service;
import xc.mst.constants.Constants;
import xc.mst.dao.service.DefaultServiceDAO;
import xc.mst.dao.service.ServiceDAO;
import xc.mst.manager.processingDirective.ConfigFileException;
import xc.mst.services.MetadataService;

/**
 * Initialize log
 *
 * @author Sharmila Ranganathan
 *
 */
public class InitializeLog  extends HttpServlet {

	/**
	 * Eclipse generated id
	 */
	private static final long serialVersionUID = 6847591197004656298L;

	/**
	 * Initialize logging
	 */
	public void init() {

	    String prefix =  getServletContext().getRealPath("/");
	    String file = getInitParameter("log4j-init-file");

	    // if the log4j-init-file is not set, then no point in trying
	    if(file != null) {
	      PropertyConfigurator.configure(prefix+file);
	    }
	    
	    // Initialize the general MST logs
	    LogDAO logDao = new DefaultLogDAO();
	    List<Log> logs = logDao.getAll();
	    for(Log log : logs)
	    	LogWriter.addInfo(log.getLogFileLocation(), "Beginning logging for " + log.getLogFileName() + ".");
	    
	    // Load the services
	    String servicesLogFileName = logDao.getById(Constants.LOG_ID_SERVICE_MANAGEMENT).getLogFileLocation();
	    ServiceDAO serviceDao = new DefaultServiceDAO();
	    List<Service> services = serviceDao.getAll();
	    for(Service service : services)
	    {
	    	// The .jar file we need to load the service from
    		File jarFile = new File(service.getServiceJar());
	    	
    		// The class loader for the MetadataService class
    		ClassLoader serviceLoader = MetadataService.class.getClassLoader();
    		
    		try 
    		{
    			// Load the class from the .jar file
    			URLClassLoader loader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, serviceLoader);
				loader.loadClass(service.getClassName());
			} 
    		catch (ClassNotFoundException e) 
    		{
    			LogWriter.addError(servicesLogFileName, "Error loading service: The class " + service.getClassName() + " could not be found in the .jar file " + service.getServiceJar());
			}
    		catch (MalformedURLException e) 
    		{
    			LogWriter.addError(servicesLogFileName, "Error loading service: " + service.getName());
			}
	    }
	  }

	  public void doGet(HttpServletRequest req, HttpServletResponse res) {
	  }

}
