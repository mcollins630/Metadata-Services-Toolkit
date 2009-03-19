/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.dao.harvest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import xc.mst.bo.harvest.HarvestScheduleStep;
import xc.mst.dao.DataException;
import xc.mst.dao.MySqlConnectionManager;
import xc.mst.dao.provider.DefaultFormatDAO;
import xc.mst.dao.provider.DefaultSetDAO;
import xc.mst.dao.provider.FormatDAO;
import xc.mst.dao.provider.SetDAO;

public class DefaultHarvestScheduleStepDAO extends HarvestScheduleStepDAO
{
	/**
	 * The DAO for getting and inserting sets
	 */
	private static SetDAO setDao = new DefaultSetDAO();

	/**
	 * The DAO for getting and inserting formats
	 */
	private static FormatDAO formatDao = new DefaultFormatDAO();

	/**
	 * The DAO for getting and inserting harvest schedules
	 */
	private static HarvestScheduleDAO scheduleDao = new DefaultHarvestScheduleDAO();

	/**
	 * A PreparedStatement to get all harvest schedule steps in the database
	 */
	private static PreparedStatement psGetAll = null;

	/**
	 * A PreparedStatement to get a harvest schedule step from the database by its ID
	 */
	private static PreparedStatement psGetById = null;

	/**
	 * A PreparedStatement to get a harvest schedule step from the database by its harvest schedule ID
	 */
	private static PreparedStatement psGetByHarvestScheduleId = null;

	/**
	 * A PreparedStatement to insert a harvest schedule step into the database
	 */
	private static PreparedStatement psInsert = null;

	/**
	 * A PreparedStatement to update a harvest schedule step in the database
	 */
	private static PreparedStatement psUpdate = null;

	/**
	 * A PreparedStatement to delete a harvest schedule step from the database
	 */
	private static PreparedStatement psDelete = null;

	/**
	 * A PreparedStatement to delete all harvest schedule steps for a given schedule from the database
	 */
	private static PreparedStatement psDeleteStepsForSchedule = null;

	/**
	 * Lock to synchronize access to the get all PreparedStatement
	 */
	private static Object psGetAllLock = new Object();

	/**
	 * Lock to synchronize access to the get by ID PreparedStatement
	 */
	private static Object psGetByIdLock = new Object();

	/**
	 * Lock to synchronize access to the get by harvest schedule ID PreparedStatement
	 */
	private static Object psGetByHarvestScheduleIdLock = new Object();

	/**
	 * Lock to synchronize access to the insert PreparedStatement
	 */
	private static Object psInsertLock = new Object();

	/**
	 * Lock to synchronize access to the update PreparedStatement
	 */
	private static Object psUpdateLock = new Object();

	/**
	 * Lock to synchronize access to the delete PreparedStatement
	 */
	private static Object psDeleteLock = new Object();

	/**
	 * Lock to synchronize access to the delete steps for schedule PreparedStatement
	 */
	private static Object psDeleteStepsForScheduleLock = new Object();

	@Override
	public List<HarvestScheduleStep> getAll()
	{
		synchronized(psGetAllLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting all harvest schedule steps");

			// The ResultSet from the SQL query
			ResultSet results = null;

			// The list of all harvest schedule steps
			ArrayList<HarvestScheduleStep> harvestScheduleSteps = new ArrayList<HarvestScheduleStep>();

			try
			{
				// If the PreparedStatement to get all harvest schedule steps was not already defined, create it
				if(psGetAll == null)
				{
					// SQL to get the rows
					String selectSql = "SELECT " + COL_HARVEST_SCHEDULE_STEP_ID + ", " +
				                                   COL_HARVEST_SCHEDULE_ID + ", " +
				                                   COL_FORMAT_ID + ", " +
				                                   COL_SET_ID + ", " +
				                                   COL_LAST_RAN + " " +
	                                   "FROM " + HARVEST_SCHEDULE_STEP_TABLE_NAME;

					if(log.isDebugEnabled())
						log.debug("Creating the \"get all harvest schedule steps\" PreparedStatement from the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetAll = dbConnection.prepareStatement(selectSql);
				} // end if(get all PreparedStatement not defined)

				// Get the result of the SELECT statement

				// Execute the query
				results = psGetAll.executeQuery();

				// For each result returned, add a HarvestScheduleStep object to the list with the returned data
				while(results.next())
				{
					// The Object which will contain data on the harvest schedule step
					HarvestScheduleStep harvestScheduleStep = new HarvestScheduleStep();

					// Set the fields on the harvest schedule step
					harvestScheduleStep.setId(results.getInt(1));
					harvestScheduleStep.setSchedule(scheduleDao.loadWithoutSteps(results.getInt(2)));
					harvestScheduleStep.setFormat(formatDao.getById(results.getInt(3)));
					harvestScheduleStep.setSet(results.getInt(4) == 0 ? null : setDao.loadBasicSet(results.getInt(4)));
					harvestScheduleStep.setLastRan(results.getDate(5));

					// Add the harvest schedule step to the list
					harvestScheduleSteps.add(harvestScheduleStep);
				} // end loop over results

				if(log.isDebugEnabled())
					log.debug("Found " + harvestScheduleSteps.size() + " harvest schedule steps in the database.");

				return harvestScheduleSteps;
			} // end try(get harvest schedule steps)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the harvest schedule steps.", e);

				return harvestScheduleSteps;
			} // end catch(SQLException)
			finally
			{
				MySqlConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getAll()

	@Override
	public HarvestScheduleStep getById(int harvestScheduleStepId)
	{
		synchronized(psGetByIdLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting the harvest schedule step with ID " + harvestScheduleStepId);

			// The ResultSet from the SQL query
			ResultSet results = null;

			try
			{
				// Create the PreparedStatement to get a harvest schedule step by ID if it was not already created
				if(psGetById == null)
				{
					// SQL to get the row
					String selectSql = "SELECT " + COL_HARVEST_SCHEDULE_STEP_ID + ", " +
				                                   COL_HARVEST_SCHEDULE_ID + ", " +
				                                   COL_FORMAT_ID + ", " +
				                                   COL_SET_ID + ", " +
				                                   COL_LAST_RAN + " " +
	                                   "FROM " + HARVEST_SCHEDULE_STEP_TABLE_NAME + " " +
	                                   "WHERE " + COL_HARVEST_SCHEDULE_STEP_ID + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"get harvest schedule step by ID\" PreparedStatement the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetById = dbConnection.prepareStatement(selectSql);
				} // end if(get by ID PreparedStatement not defined)

				// Set the parameters on the SELECT statement
				psGetById.setInt(1, harvestScheduleStepId);

				// Get the result of the SELECT statement

				// Execute the query
				results = psGetById.executeQuery();

				// If any results were returned
				if(results.next())
				{
					// The Object which will contain data on the harvest schedule step
					HarvestScheduleStep harvestScheduleStep = new HarvestScheduleStep();

					// Set the fields on the harvest schedule step
					harvestScheduleStep.setId(results.getInt(1));
					harvestScheduleStep.setSchedule(scheduleDao.loadWithoutSteps(results.getInt(2)));
					harvestScheduleStep.setFormat(formatDao.getById(results.getInt(3)));
					harvestScheduleStep.setSet(results.getInt(4) == 0 ? null : setDao.loadBasicSet(results.getInt(4)));
					harvestScheduleStep.setLastRan(results.getDate(5));

					if(log.isDebugEnabled())
						log.debug("Found the harvest schedule step with ID " + harvestScheduleStepId + " in the database.");

					// Return the harvest schedule
					return harvestScheduleStep;
				} // end if(harvest schedule step was found

				if(log.isDebugEnabled())
					log.debug("The harvest schedule step with ID " + harvestScheduleStepId + " was not found in the database.");

				return null;
			} // end try(get the harvest schedule step)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the harvest schedule Step with ID " + harvestScheduleStepId, e);

				return null;
			} // end catch(SQLException)
			finally
			{
				MySqlConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getById(int)

	@Override
	public List<HarvestScheduleStep> getStepsForSchedule(int harvestSchedlueId)
	{
		synchronized(psGetByHarvestScheduleIdLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting all harvest schedule steps with harvest schedule ID " + harvestSchedlueId);

			// The ResultSet from the SQL query
			ResultSet results = null;

			// The list of all harvest schedule steps
			ArrayList<HarvestScheduleStep> harvestScheduleSteps = new ArrayList<HarvestScheduleStep>();

			try
			{
				// If the PreparedStatement to get all harvest schedule steps was not already defined, create it
				if(psGetByHarvestScheduleId == null)
				{
					// SQL to get the rows
					String selectSql = "SELECT " + COL_HARVEST_SCHEDULE_STEP_ID + ", " +
				                                   COL_HARVEST_SCHEDULE_ID + ", " +
				                                   COL_FORMAT_ID + ", " +
				                                   COL_SET_ID + ", " +
				                                   COL_LAST_RAN + " " +
	                                   "FROM " + HARVEST_SCHEDULE_STEP_TABLE_NAME + " " +
	                                   "WHERE " + COL_HARVEST_SCHEDULE_ID + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"get harvest schedule steps by harvest schedule ID\" PreparedStatement from the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetByHarvestScheduleId = dbConnection.prepareStatement(selectSql);
				} // end if(get by harvest schedule ID PreparedStatement not defined)

				// Set the parameters on the PreparedStatement
				psGetByHarvestScheduleId.setInt(1, harvestSchedlueId);

				// Get the result of the SELECT statement

				// Execute the query
				results = psGetByHarvestScheduleId.executeQuery();

				// For each result returned, add a HarvestScheduleStep object to the list with the returned data
				while(results.next())
				{
					// The Object which will contain data on the harvest schedule step
					HarvestScheduleStep harvestScheduleStep = new HarvestScheduleStep();

					// Set the fields on the harvest schedule step
					harvestScheduleStep.setId(results.getInt(1));
					harvestScheduleStep.setSchedule(scheduleDao.loadWithoutSteps(results.getInt(2)));
					harvestScheduleStep.setFormat(formatDao.getById(results.getInt(3)));
					harvestScheduleStep.setSet(results.getInt(4) == 0 ? null : setDao.loadBasicSet(results.getInt(4)));
					harvestScheduleStep.setLastRan(results.getDate(5));

					// Add the harvest schedule step to the list
					harvestScheduleSteps.add(harvestScheduleStep);
				} // end loop over results

				if(log.isDebugEnabled())
					log.debug("Found " + harvestScheduleSteps.size() + " harvest schedule steps in the database.");

				return harvestScheduleSteps;
			} // end try(get harvest schedule steps)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the harvest schedule steps.", e);

				return harvestScheduleSteps;
			} // end catch(SQLException)
			finally
			{
				MySqlConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getStepsForSchedule(int)

	@Override
	public boolean insert(HarvestScheduleStep harvestScheduleStep, int harvestScheduleId) throws DataException
	{
		// Check that the non-ID fields on the user are valid
		validateFields(harvestScheduleStep, false, true);

		// Insert the format if it hasn't already been inserted
		if(harvestScheduleStep.getFormat().getId() <= 0)
			if(!formatDao.insert(harvestScheduleStep.getFormat()))
				return false;

		// Insert the set if it hasn't already been inserted
		if(harvestScheduleStep.getSet() != null && harvestScheduleStep.getSet().getId() <= 0)
			if(!setDao.insert(harvestScheduleStep.getSet()))
				return false;

		synchronized(psInsertLock)
		{
			if(log.isDebugEnabled())
				log.debug("Inserting a new harvest schedule step with harvest schedule ID " + harvestScheduleId);

			// The ResultSet returned by the query
			ResultSet rs = null;

			try
			{
				// If the PreparedStatement to insert a harvest schedule step was not defined, create it
				if(psInsert == null)
				{
					// SQL to insert the new row
					String insertSql = "INSERT INTO " + HARVEST_SCHEDULE_STEP_TABLE_NAME + " (" + COL_HARVEST_SCHEDULE_ID + ", " +
				                                                            COL_FORMAT_ID + ", " +
				                                                            COL_SET_ID + ", " +
				                                                            COL_LAST_RAN + ") " +
	            				       "VALUES (?, ?, ?, ?)";

					if(log.isDebugEnabled())
						log.debug("Creating the PreparedStatement to insert a harvest schedule step from the SQL " + insertSql);

					// A prepared statement to run the insert SQL
					// This should sanitize the SQL and prevent SQL injection
					psInsert = dbConnection.prepareStatement(insertSql);
				} // end if(insert PreparedStatement not defined)

				// Set the parameters on the insert statement
				psInsert.setInt(1, harvestScheduleId);
				psInsert.setInt(2, harvestScheduleStep.getFormat().getId());
				psInsert.setInt(3, (harvestScheduleStep.getSet() == null ? 0 : harvestScheduleStep.getSet().getId()));
				psInsert.setDate(4, harvestScheduleStep.getLastRan());

				// Execute the insert statement and return the result
				if(psInsert.executeUpdate() > 0)
				{
					// Get the auto-generated resource identifier ID and set it correctly on this HarvestSchedule Object
					rs = dbConnection.createStatement().executeQuery("SELECT LAST_INSERT_ID()");

				    if (rs.next())
				        harvestScheduleStep.setId(rs.getInt(1));

					return true;
				} // end if(insert succeeded)

				return false;
			} // end try(insert the row)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while inserting a new harvest schedule step with harvest schedule ID " + harvestScheduleId, e);

				return false;
			} // end catch(SQLException)
			finally
			{
				MySqlConnectionManager.closeResultSet(rs);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method insert(HarvestScheduleStep)

	@Override
	public boolean update(HarvestScheduleStep harvestScheduleStep, int harvestScheduleId) throws DataException
	{
		// Check that the fields on the user are valid
		validateFields(harvestScheduleStep, true, true);

		// Insert the format if it hasn't already been inserted
		if(harvestScheduleStep.getFormat().getId() <= 0)
			if(!formatDao.insert(harvestScheduleStep.getFormat()))
				return false;

		// Insert the set if it hasn't already been inserted
		if(harvestScheduleStep.getSet() != null && harvestScheduleStep.getSet().getId() <= 0)
			if(!setDao.insert(harvestScheduleStep.getSet()))
				return false;

		synchronized(psUpdateLock)
		{
			if(log.isDebugEnabled())
				log.debug("Updating the harvest schedule step with ID " + harvestScheduleStep.getId());

			try
			{
				if(psUpdate == null)
				{
					// SQL to update new row
					String updateSql = "UPDATE " + HARVEST_SCHEDULE_STEP_TABLE_NAME + " SET " + COL_HARVEST_SCHEDULE_ID + "=?, " +
				                                                          COL_FORMAT_ID + "=?, " +
				                                                          COL_SET_ID + "=?, " +
				                                                          COL_LAST_RAN + "=? " +
	                                   "WHERE " + COL_HARVEST_SCHEDULE_STEP_ID + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"update harvest schedule step\" PreparedStatement from the SQL " + updateSql);

					// A prepared statement to run the update SQL
					// This should sanitize the SQL and prevent SQL injection
					psUpdate = dbConnection.prepareStatement(updateSql);
				} // end if(update PreparedStatement not defined)

				// Set the parameters on the update statement
				psUpdate.setInt(1, harvestScheduleId);
				psUpdate.setInt(2, harvestScheduleStep.getFormat().getId());
				psUpdate.setInt(3, (harvestScheduleStep.getSet() == null ? 0 : harvestScheduleStep.getSet().getId()));
				psUpdate.setDate(4, harvestScheduleStep.getLastRan());
				psUpdate.setInt(5, harvestScheduleStep.getId());

				// Execute the update statement and return the result
				return psUpdate.executeUpdate() > 0;
			} // end try(update the row)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while updating the harvest schedule step with ID " + harvestScheduleStep.getId(), e);

				return false;
			} // end catch(SQLException)
		} // end synchronized
	} // end method update(HarvestScheduleStep)

	@Override
	public boolean delete(HarvestScheduleStep harvestScheduleStep) throws DataException
	{
		// Check that the ID field on the user are valid
		validateFields(harvestScheduleStep, true, false);

		synchronized(psDeleteLock)
		{
			if(log.isDebugEnabled())
				log.debug("Deleting the harvest schedule step with ID " + harvestScheduleStep.getId());

			try
			{
				if(psDelete == null)
				{
					// SQL to delete the row from the table
					String deleteSql = "DELETE FROM "+ HARVEST_SCHEDULE_STEP_TABLE_NAME + " " +
									   "WHERE " + COL_HARVEST_SCHEDULE_STEP_ID + " = ? ";

					if(log.isDebugEnabled())
						log.debug("Creating the \"delete harvest step schedule\" PrepareStatement from the SQL " + deleteSql);

					// A prepared statement to run the delete SQL
					// This should sanitize the SQL and prevent SQL injection
					psDelete = dbConnection.prepareStatement(deleteSql);
				} // end if(delete PreparedStatement not defined)

				// Set the parameters on the delete statement
				psDelete.setInt(1, harvestScheduleStep.getId());

				// Execute the delete statement and return the result
				return psDelete.execute();
			} // end try(delete the row)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while deleting the harvest schedule step with ID " + harvestScheduleStep.getId(), e);

				return false;
			} // end catch(SQLException)
		} // end synchronized
	} // end method delete(HarvestScheduleStep)

	@Override
	public boolean deleteStepsForSchedule(int harvestScheduleId)
	{
		synchronized(psDeleteStepsForScheduleLock)
		{
			if(log.isDebugEnabled())
				log.debug("Deleting the harvest schedule steps for the harvest schedule with ID " + harvestScheduleId);

			try
			{
				if(psDeleteStepsForSchedule == null)
				{
					// SQL to delete the row from the table
					String deleteSql = "DELETE FROM "+ HARVEST_SCHEDULE_STEP_TABLE_NAME + " " +
									   "WHERE " + COL_HARVEST_SCHEDULE_ID + " = ? ";

					if(log.isDebugEnabled())
						log.debug("Creating the \"delete harvest schedule steps for harvest schedule\" PrepareStatement from the SQL " + deleteSql);

					// A prepared statement to run the delete SQL
					// This should sanitize the SQL and prevent SQL injection
					psDeleteStepsForSchedule = dbConnection.prepareStatement(deleteSql);
				} // end if(delete PreparedStatement not defined)

				// Set the parameters on the delete statement
				psDeleteStepsForSchedule.setInt(1, harvestScheduleId);

				// Execute the delete statement and return the result
				return psDeleteStepsForSchedule.execute();
			} // end try(delete the rows)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while deleting the harvest schedule step for the harvest schedule with ID " + harvestScheduleId, e);

				return false;
			} // end catch(SQLException)
		} // end synchronized
	} // end method deleteStepsForSchedule(int)
} // end class DefaultHarvestScheduleStepDAO
