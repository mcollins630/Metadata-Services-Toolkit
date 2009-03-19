/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.bo.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import xc.mst.bo.provider.Format;

public class Service
{
	/**
	 * The service's ID
	 */
	private int id = -1;

	/**
	 * The service's name
	 */
	private String serviceName = null;

	/**
	 * The service's package name
	 */
	private String packageName = null;

	/**
	 * The service's class name
	 */
	private String className = null;

	/**
	 * The whether or not the service is user defined
	 */
	private boolean isUserDefined = true;

	/**
	 * The port on which the service's OAI repository should be accessed
	 */
	private int port = -1;

	/**
	 * A list of formats which the service accepts as input
	 */
	private List<Format> inputFormats = new ArrayList<Format>();

	/**
	 * A list of formats which the service outputs
	 */
	private List<Format> outputFormats = new ArrayList<Format>();

	/**
	 * The number of warnings the service has generated
	 */
	private int warnings = 0;

	/**
	 * The number of errors the service has generated
	 */
	private int errors = 0;

	/**
	 * The number of input records for the service
	 */
	private int inputRecordCount = 0;

	/**
	 * The number of output records for the service
	 */
	private int outputRecordCount = 0;

	/**
	 * The timestamp when the service's logs were last reset
	 */
	private Date lastLogReset = null;

	/**
	 * The name of the log file for this service
	 */
	private String logFileName = null;

	/**
	 * The number of warnings harvesting the service has generated
	 */
	private int harvestOutWarnings = 0;

	/**
	 * The number of errors the harvesting service has generated
	 */
	private int harvestOutErrors = 0;

	/**
	 * The number of records available for harvest from the service
	 */
	private long harvestOutRecordsAvailable = 0;

	/**
	 * The number of records harvested from the service
	 */
	private long harvestOutRecordsHarvested = 0;

	/**
	 * The timestamp when the service's harvest out logs were last reset
	 */
	private Date harvestOutLastLogReset = null;

	/**
	 * The name of the harvest out log file for this service
	 */
	private String harvestOutLogFileName = null;

	/**
	 * Gets the service's ID
	 *
	 * @return The service's ID
	 */
	public int getId()
	{
		return id;
	} // end method getId()

	/**
	 * Sets the service's ID
	 *
	 * @param id The service's new ID
	 */
	public void setId(int id)
	{
		this.id = id;
	} // end method setId(int)

	/**
	 * Gets the service's name
	 *
	 * @return The service's name
	 */
	public String getName()
	{
		return serviceName;
	} // end method getName()

	/**
	 * Sets the service's name
	 *
	 * @param serviceName The service's new name
	 */
	public void setName(String serviceName)
	{
		this.serviceName = serviceName;
	} // end method setName(String)

	/**
	 * Gets the service's package name
	 *
	 * @return The service's package name
	 */
	public String getPackageName()
	{
		return packageName;
	} // end method getPackageName()

	/**
	 * Sets the service's package name
	 *
	 * @param packageName The service's new package name
	 */
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	} // end method setPackageName(String)

	/**
	 * Gets the service's class name
	 *
	 * @return The service's class name
	 */
	public String getClassName()
	{
		return className;
	} // end method getClassName()

	/**
	 * Sets the service's class name
	 *
	 * @param className The service's new class name
	 */
	public void setClassName(String className)
	{
		this.className = className;
	} // end method setClassName(String)

	/**
	 * Checks whether or not the service is user-defined
	 *
	 * @return True if the service is user-defined, false otherwise
	 */
	public boolean getIsUserDefined()
	{
		return isUserDefined;
	} // end method getIsUserDefined()

	/**
	 * Sets whether or not the service is user-defined
	 *
	 * @param isUserDefined True if the service should be marked as user-defined, false otherwise
	 */
	public void setIsUserDefined(boolean isUserDefined)
	{
		this.isUserDefined = isUserDefined;
	} // end method setIsUserDefined(boolean)

	/**
	 * Gets the port on which the service's OAI repository should be accessed
	 *
	 * @return The port on which the service's OAI repository should be accessed
	 */
	public int getPort()
	{
		return port;
	} // end method getPort()

	/**
	 * Sets the port on which the service's OAI repository should be accessed
	 *
	 * @param port The port on which the service's OAI repository should be accessed
	 */
	public void setPort(int port)
	{
		this.port = port;
	} // end method setPort(int)

	/**
	 * Gets the formats the service accepts as input
	 *
	 * @return The provider's input formats
	 */
	public List<Format> getInputFormats()
	{
		return inputFormats;
	} // end method getInputFormats()

	/**
	 * Sets the formats the service accepts as input
	 *
	 * @param formats A list of formats the service accepts as input
	 */
	public void setInputFormats(List<Format> inputFormats)
	{
		this.inputFormats = inputFormats;
	} // end method setInputFormats(List<Format>)

	/**
	 * Adds a format to the list of formats the service accepts as input
	 *
	 * @param format The format to add to the list of formats the service accepts as input
	 */
	public void addInputFormat(Format format)
	{
		if(!inputFormats.contains(format))
			inputFormats.add(format);
	} // end method addInputFormat(Format)

	/**
	 * Removes a format from the list of formats the service accepts as input
	 *
	 * @param format The format to remove from the list of formats the service accepts as input
	 */
	public void removeInputFormat(Format format)
	{
		if(inputFormats.contains(format))
			inputFormats.remove(format);
	} // end method removeInputFormat(Format)

	/**
	 * Gets the formats the service outputs
	 *
	 * @return The formats the service outputs
	 */
	public List<Format> getOutputFormats()
	{
		return outputFormats;
	} // end method getOutputFormats()

	/**
	 * Sets the formats the service outputs
	 *
	 * @param outputFormats A list of formats the service outputs
	 */
	public void setOutputFormats(List<Format> outputFormats)
	{
		this.outputFormats = outputFormats;
	} // end method setOutputFormats(List<Format>)

	/**
	 * Adds a format to the list of formats the service outputs
	 *
	 * @param format The format to add to the list of formats the service outputs
	 */
	public void addOutputFormat(Format format)
	{
		if(!outputFormats.contains(format))
			outputFormats.add(format);
	} // end method addOutputFormat(Format)

	/**
	 * Removes a format from the list of formats the service outputs
	 *
	 * @param format The format to remove from the list of formats the service outputs
	 */
	public void removeOutputFormat(Format format)
	{
		if(outputFormats.contains(format))
			outputFormats.remove(format);
	} // end method removeOutputFormat(Format)

	/**
	 * Gets the number of warnings harvesting the service
	 *
	 * @return The number of warnings harvesting the service
	 */
	public int getServicesWarnings()
	{
		return warnings;
	} // end method getWarnings()

	/**
	 * Sets the number of warnings harvesting the service
	 *
	 * @param warnings The new number of warnings harvesting the service
	 */
	public void setServicesWarnings(int warnings)
	{
		this.warnings = warnings;
	} // end method setWarnings(int)

	/**
	 * Gets the number of errors harvesting the service
	 *
	 * @return The number of errors harvesting the service
	 */
	public int getServicesErrors()
	{
		return errors;
	} // end method getErrors()

	/**
	 * Sets the number of errors generated by the serivce
	 *
	 * @param errors The new number of errors generated by the serivce
	 */
	public void setServicesErrors(int errors)
	{
		this.errors = errors;
	} // end method setErrors(int)

	/**
	 * Gets the number of records the service can use as input
	 *
	 * @return The number of records the service can use as input
	 */
	public int getInputRecordCount()
	{
		return inputRecordCount;
	} // end method getInputRecordCount()

	/**
	 * Sets the number of records the service can use as input
	 *
	 * @param inputRecordCount The new number of records the service can use as input
	 */
	public void setInputRecordCount(int inputRecordCount)
	{
		this.inputRecordCount = inputRecordCount;
	} // end method setInputRecordCount(int)

	/**
	 * Gets the number of records the service has output
	 *
	 * @return The number of records the service has output
	 */
	public int getOutputRecordCount()
	{
		return outputRecordCount;
	} // end method getOutputRecordCount()

	/**
	 * Sets the number of records the service has output
	 *
	 * @param outputRecordCount The new number of records the service has output
	 */
	public void setOutputRecordCount(int outputRecordCount)
	{
		this.outputRecordCount = outputRecordCount;
	} // end method setOutputRecordCount(int)

	/**
	 * Gets the date when the service's logs were last reset
	 *
	 * @return The date when the service's logs were last reset
	 */
	public Date getServicesLastLogReset()
	{
		return lastLogReset;
	} // end method getLastLogReset()

	/**
	 * Sets the date when the service's logs were last reset
	 *
	 * @param lastLogReset The new date when the service's logs were last reset
	 */
	public void setServicesLastLogReset(java.util.Date lastLogReset)
	{
		this.lastLogReset = (lastLogReset == null ? null : new Date(lastLogReset.getTime()));
	} // end method setLastLogReset(Date)

	/**
	 * Gets the name of the log file for the service
	 *
	 * @return The name of the log file for the service
	 */
	public String getServicesLogFileName()
	{
		return logFileName;
	} // end method getLogFileName()

	/**
	 * Sets the name of the log file for the service
	 *
	 * @param logFileName The new name of the log file for the service
	 */
	public void setServicesLogFileName(String logFileName)
	{
		this.logFileName = logFileName;
	} // end method setLogFileName(String)

	/**
	 * Gets the number of warnings harvesting the service
	 *
	 * @return The number of warnings harvesting the service
	 */
	public int getHarvestOutWarnings()
	{
		return harvestOutWarnings;
	} // end method getHarvestOutWarnings()

	/**
	 * Sets the number of warnings harvesting the service
	 *
	 * @param harvestOutWarnings The new number of warnings harvesting the service
	 */
	public void setHarvestOutWarnings(int harvestOutWarnings)
	{
		this.harvestOutWarnings = harvestOutWarnings;
	} // end method setHarvestOutWarnings(int)

	/**
	 * Gets the number of errors harvesting the service
	 *
	 * @return The number of errors harvesting the service
	 */
	public int getHarvestOutErrors()
	{
		return harvestOutErrors;
	} // end method getHarvestOutErrors()

	/**
	 * Sets the number of errors harvesting the serivce
	 *
	 * @param harvestOutErrors The new number of errors harvesting the serivce
	 */
	public void setHarvestOutErrors(int harvestOutErrors)
	{
		this.harvestOutErrors = harvestOutErrors;
	} // end method setHarvestOutErrors(int)

	/**
	 * Gets the number of records the service exposed for harvest
	 *
	 * @return The number of records the service exposed for harvest
	 */
	public long getHarvestOutRecordsAvailable()
	{
		return harvestOutRecordsAvailable;
	} // end method getHarvestOutRecordsAvailable()

	/**
	 * Sets the number of records the service exposed for harvest
	 *
	 * @param harvestOutRecordsAvailable The new number of records the service exposed for harvest
	 */
	public void setHarvestOutRecordsAvailable(long harvestOutRecordsAvailable)
	{
		this.harvestOutRecordsAvailable = harvestOutRecordsAvailable;
	} // end method setHarvestOutRecordsAvailable(long)

	/**
	 * Gets the number of records harvested from the service
	 *
	 * @return The number of records the service has output
	 */
	public long getHarvestOutRecordsHarvested()
	{
		return harvestOutRecordsHarvested;
	} // end method getHarvestOutRecordsHarvested()

	/**
	 * Sets the number of records harvested from the service
	 *
	 * @param harvestOutRecordsHarvested The new number of records harvested from the service
	 */
	public void setHarvestOutRecordsHarvested(long harvestOutRecordsHarvested)
	{
		this.harvestOutRecordsHarvested = harvestOutRecordsHarvested;
	} // end method setHarvestOutRecordsHarvested(long)

	/**
	 * Gets the date when the service's harvest out logs were last reset
	 *
	 * @return The date when the service's harvest out logs were last reset
	 */
	public Date getHarvestOutLastLogReset()
	{
		return harvestOutLastLogReset;
	} // end method getHarvestOutLastLogReset()

	/**
	 * Sets the date when the service's harvest out logs were last reset
	 *
	 * @param lastLogReset The new date when the service's harvest out logs were last reset
	 */
	public void setHarvestOutLastLogReset(java.util.Date harvestOutLastLogReset)
	{

		this.harvestOutLastLogReset = (lastLogReset == null ? null : new Date(lastLogReset.getTime())); // NOTE : ERIC I made a change here changing harvestOutLastLogReset to LastLogReset
	} // end method setHarvestOutLastLogReset(Date)

	/**
	 * Gets the name of the harvest out log file for the service
	 *
	 * @return The name of the harvest out log file for the service
	 */
	public String getHarvestOutLogFileName()
	{
		return harvestOutLogFileName;
	} // end method getLogFileName()

	/**
	 * Sets the name of the harvest out log file for the service
	 *
	 * @param logFileName The new name of the harvest out log file for the service
	 */
	public void setHarvestOutLogFileName(String harvestOutLogFileName)
	{
		this.harvestOutLogFileName = harvestOutLogFileName;
	} // end method setHarvestOutLogFileName(String)

	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof Service))
			return false;

		Service other = (Service)o;

		return other.serviceName.equals(this.serviceName);
	} // end method equals(Object)
} // end class Service
