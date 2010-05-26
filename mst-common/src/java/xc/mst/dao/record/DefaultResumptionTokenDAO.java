/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.dao.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import xc.mst.bo.record.ResumptionToken;
import xc.mst.dao.DBConnectionResetException;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;

/**
 * MySQL implementation of the Data Access Object for the resumption tokens table
 *
 * @author Eric Osisek
 */
public class DefaultResumptionTokenDAO extends ResumptionTokenDAO
{
	/**
	 * A PreparedStatement to get all resumption tokens in the database
	 */
	private static PreparedStatement psGetAll = null;

	/**
	 * A PreparedStatement to get a resumption token from the database by its ID
	 */
	private static PreparedStatement psGetById = null;
	
	/**
	 * A PreparedStatement to get a resumption token from the database by its token
	 */
	private static PreparedStatement psGetByToken = null;

	/**
	 * A PreparedStatement to insert a resumption token into the database
	 */
	private static PreparedStatement psInsert = null;

	/**
	 * A PreparedStatement to update a resumption token in the database
	 */
	private static PreparedStatement psUpdate = null;

	/**
	 * A PreparedStatement to delete a resumption token from the database
	 */
	private static PreparedStatement psDelete = null;

	/**
	 * Lock to synchronize access to the PreparedStatement to get all resumption tokens in the database
	 */
	private static Object psGetAllLock = new Object();

	/**
	 * Lock to synchronize access to the PreparedStatement to get a resumption token from the database by its ID
	 */
	private static Object psGetByIdLock = new Object();
	
	/**
	 * Lock to synchronize access to the PreparedStatement to get a resumption token from the database by its token
	 */
	private static Object psGetByTokenLock = new Object();

	/**
	 * Lock to synchronize access to the PreparedStatement to insert a resumption token into the database
	 */
	private static Object psInsertLock = new Object();

	/**
	 * Lock to synchronize access to the PreparedStatement to update a resumption token in the database
	 */
	private static Object psUpdateLock = new Object();

	/**
	 * Lock to synchronize access to the PreparedStatement to delete a resumption token from the database
	 */
	private static Object psDeleteLock = new Object();

	@Override
	public List<ResumptionToken> getAll() throws DatabaseConfigException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		synchronized(psGetAllLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting all resumption tokens");

			// The ResultSet from the SQL query
			ResultSet results = null;

			// The list of all resumption tokens
			List<ResumptionToken> resumptionTokens = new ArrayList<ResumptionToken>();

			try
			{
				// If the PreparedStatement to get all resumption tokens was not defined, create it
				if(psGetAll == null || dbConnectionManager.isClosed(psGetAll))
				{
					// SQL to get the rows
					String selectSql = "SELECT " + COL_RESUMPTION_TOKEN_ID + ", " +
				                                   COL_SET_SPEC + ", " +
				                                   COL_METADATA_FORMAT + ", " +
				                                   COL_FROM + ", " +
				                                   COL_UNTIL + ", " +
				                                   COL_OFFSET + ", " +
				                                   COL_TOKEN + " " +
	                                   "FROM " + RESUMPTION_TOKENS_TABLE_NAME;

					if(log.isDebugEnabled())
						log.debug("Creating the \"get all resumption tokens\" PreparedStatement from the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetAll = dbConnectionManager.prepareStatement(selectSql, psGetAll);
				} // end if(get all PreparedStatement not defined)

				// Get the result of the SELECT statement

				// Execute the query
				results = dbConnectionManager.executeQuery(psGetAll);

				// For each result returned, add a ResumptionToken object to the list with the returned data
				while(results.next())
				{
					// The Object which will contain data on the resumption token
					ResumptionToken resumptionToken = new ResumptionToken();

					// Set the fields on the resumption token
					resumptionToken.setId(results.getLong(1));
					resumptionToken.setSetSpec(results.getString(2));
					resumptionToken.setMetadataFormat(results.getString(3));
					resumptionToken.setFrom(results.getTimestamp(4));
					resumptionToken.setUntil(results.getTimestamp(5));
					resumptionToken.setOffset(results.getInt(6));
					resumptionToken.setToken(results.getString(7));

					// Add the resumption tokens to the list
					resumptionTokens.add(resumptionToken);
				} // end loop over results

				if(log.isDebugEnabled())
					log.debug("Found " + resumptionTokens.size() + " resumption tokens in the database.");

				return resumptionTokens;
			} // end try(get the records
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the resumption tokens.", e);

				return resumptionTokens;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return getAll();
			}
			finally
			{
				dbConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getAll()

	@Override
	public ResumptionToken getById(long id) throws DatabaseConfigException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		synchronized(psGetByIdLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting the resumption token with ID " + id);

			// The ResultSet from the SQL query
			ResultSet results = null;

			try
			{
				// If the PreparedStatement to get a resumption token by ID was not defined, create it
				if(psGetById == null || dbConnectionManager.isClosed(psGetById))
				{
					// SQL to get the row
					String selectSql = "SELECT " + COL_RESUMPTION_TOKEN_ID + ", " +
	            	    						   COL_SET_SPEC + ", " +
	            	    						   COL_METADATA_FORMAT + ", " +
	            	    						   COL_FROM + ", " +
	            	    						   COL_UNTIL + ", " +
	            	    						   COL_OFFSET + ", " +
	            	    						   COL_TOKEN + " " +
	                                   "FROM " + RESUMPTION_TOKENS_TABLE_NAME + " " +
	                                   "WHERE " + COL_RESUMPTION_TOKEN_ID + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"get resumption token by ID\" PreparedStatement from the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetById = dbConnectionManager.prepareStatement(selectSql, psGetById);
				} // end if(get by ID PreparedStatement not defined)

				// Set the parameters on the update statement
				psGetById.setLong(1, id);

				// Get the result of the SELECT statement

				// Execute the query
				results = dbConnectionManager.executeQuery(psGetById);

				// If any results were returned
				if(results.next())
				{
					// The Object which will contain data on the resumption token
					ResumptionToken resumptionToken = new ResumptionToken();

					// Set the fields on the resumption token
					resumptionToken.setId(results.getLong(1));
					resumptionToken.setSetSpec(results.getString(2));
					resumptionToken.setMetadataFormat(results.getString(3));
					resumptionToken.setFrom(results.getTimestamp(4));
					resumptionToken.setUntil(results.getTimestamp(5));
					resumptionToken.setOffset(results.getInt(6));
					resumptionToken.setToken(results.getString(7));

					if(log.isDebugEnabled())
						log.debug("Found the resumption token with ID " + id + " in the database.");

					// Return the resumption token
					return resumptionToken;
				} // end if(result found)

				if(log.isDebugEnabled())
					log.debug("The resumption token with ID " + id + " was not found in the database.");

				return null;
			} // end try(get the resumption token)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the resumption token with ID " + id, e);

				return null;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return getById(id);
			}
			finally
			{
				dbConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getById(long)
	
	@Override
	public ResumptionToken getByToken(String token) throws DatabaseConfigException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		synchronized(psGetByTokenLock)
		{
			if(log.isDebugEnabled())
				log.debug("Getting the resumption token with token " + token);

			// The ResultSet from the SQL query
			ResultSet results = null;

			try
			{
				// If the PreparedStatement to get a resumption token by token was not defined, create it
				if(psGetByToken == null || dbConnectionManager.isClosed(psGetByToken))
				{
					// SQL to get the row
					String selectSql = "SELECT " + COL_RESUMPTION_TOKEN_ID + ", " +
	            	    						   COL_SET_SPEC + ", " +
	            	    						   COL_METADATA_FORMAT + ", " +
	            	    						   COL_FROM + ", " +
	            	    						   COL_UNTIL + ", " +
	            	    						   COL_OFFSET + ", " +
	            	    						   COL_TOKEN + " " +
	                                   "FROM " + RESUMPTION_TOKENS_TABLE_NAME + " " +
	                                   "WHERE " + COL_TOKEN + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"get resumption token by toeken\" PreparedStatement from the SQL " + selectSql);

					// A prepared statement to run the select SQL
					// This should sanitize the SQL and prevent SQL injection
					psGetByToken = dbConnectionManager.prepareStatement(selectSql, psGetByToken);
				} // end if(get by ID PreparedStatement not defined)

				// Set the parameters on the update statement
				psGetByToken.setString(1, token);

				// Get the result of the SELECT statement

				// Execute the query
				results = dbConnectionManager.executeQuery(psGetByToken);

				// If any results were returned
				if(results.next())
				{
					// The Object which will contain data on the resumption token
					ResumptionToken resumptionToken = new ResumptionToken();

					// Set the fields on the resumption token
					resumptionToken.setId(results.getLong(1));
					resumptionToken.setSetSpec(results.getString(2));
					resumptionToken.setMetadataFormat(results.getString(3));
					resumptionToken.setFrom(results.getTimestamp(4));
					resumptionToken.setUntil(results.getTimestamp(5));
					resumptionToken.setOffset(results.getInt(6));
					resumptionToken.setToken(results.getString(7));

					if(log.isDebugEnabled())
						log.debug("Found the resumption token with token " + token + " in the database.");

					// Return the resumption token
					return resumptionToken;
				} // end if(result found)

				if(log.isDebugEnabled())
					log.debug("The resumption token with token " + token + " was not found in the database.");

				return null;
			} // end try(get the resumption token)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while getting the resumption token with token " + token, e);

				return null;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return getByToken(token);
			}
			finally
			{
				dbConnectionManager.closeResultSet(results);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method getById(long)

	@Override
	public boolean insert(ResumptionToken resumptionToken) throws DataException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		// Check that the non-ID fields on the resumption token are valid
		validateFields(resumptionToken, false, true);

		synchronized(psInsertLock)
		{
			if(log.isDebugEnabled())
				log.debug("Inserting a new resumption token with offset " + resumptionToken.getOffset());

			// The ResultSet returned by the query
			ResultSet rs = null;
			try
			{
				// If the PreparedStatement to insert a resumption token was not defined, create it
				if(psInsert == null || dbConnectionManager.isClosed(psInsert))
				{
					// SQL to insert the new row
					String insertSql = "INSERT INTO " + RESUMPTION_TOKENS_TABLE_NAME + " (" + COL_SET_SPEC + ", " +
				                                                            COL_METADATA_FORMAT + ", " +
				                                                            COL_FROM + ", " +
				                                                            COL_UNTIL + ", " +
				                                                            COL_OFFSET + ", " +
				                                                            COL_TOKEN + ") " +
	            				       "VALUES (?, ?, ?, ?, ?, ?)";

					if(log.isDebugEnabled())
						log.debug("Creating the \"insert resumption token\" PreparedStatement from the SQL " + insertSql);

					// A prepared statement to run the insert SQL
					// This should sanitize the SQL and prevent SQL injection
					psInsert = dbConnectionManager.prepareStatement(insertSql, psInsert);
				} // end if(insert PreparedStatement not defined)

				// Set the parameters on the insert statement
				psInsert.setString(1, resumptionToken.getSetSpec());
				psInsert.setString(2, resumptionToken.getMetadataFormat());
				psInsert.setTimestamp(3, resumptionToken.getFrom());
				psInsert.setTimestamp(4, resumptionToken.getUntil());
				psInsert.setInt(5, resumptionToken.getOffset());
				psInsert.setString(6, resumptionToken.getToken());

				// Execute the insert statement and return the result
				if(dbConnectionManager.executeUpdate(psInsert) > 0)
				{
					// Get the auto-generated resource identifier ID and set it correctly on this ResumptionToken Object
					rs = dbConnectionManager.createStatement().executeQuery("SELECT LAST_INSERT_ID()");

				    if (rs.next())
				        resumptionToken.setId(rs.getLong(1));

					return true;
				} // end if(insert succeeded)

				return false;
			} // end try(insert the resumption token)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while inserting a new resumption token with offset " + resumptionToken.getOffset(), e);

				return false;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return insert(resumptionToken);
			}
			finally
			{
				dbConnectionManager.closeResultSet(rs);
			} // end finally(close ResultSet)
		} // end synchronized
	} // end method insert(ResumptionToken)

	@Override
	public boolean update(ResumptionToken resumptionToken) throws DataException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		// Check that the fields on the resumption token are valid
		validateFields(resumptionToken, true, true);

		synchronized(psUpdateLock)
		{
			if(log.isDebugEnabled())
				log.debug("Updating the resumption token with ID " + resumptionToken.getId());

			try
			{
				// If the PreparedStatement to update a resumption token was not defined, create it
				if(psUpdate == null || dbConnectionManager.isClosed(psUpdate))
				{
					// SQL to update new row
					String updateSql = "UPDATE " + RESUMPTION_TOKENS_TABLE_NAME + " SET " + COL_SET_SPEC + "=?, " +
				                                                          COL_METADATA_FORMAT + "=?, " +
				                                                          COL_FROM + "=?, " +
				                                                          COL_UNTIL + "=?, " +
				                                                          COL_OFFSET + "=?, " +
				                                                          COL_TOKEN + "=? " +
	                                   "WHERE " + COL_RESUMPTION_TOKEN_ID + "=?";

					if(log.isDebugEnabled())
						log.debug("Creating the \"update resumption token\" PreparedStatement from the SQL " + updateSql);

					// A prepared statement to run the update SQL
					// This should sanitize the SQL and prevent SQL injection
					psUpdate = dbConnectionManager.prepareStatement(updateSql, psUpdate);
				} // end if(update PreparedStatement was not defined)

				// Set the parameters on the update statement
				psUpdate.setString(1, resumptionToken.getSetSpec());
				psUpdate.setString(2, resumptionToken.getMetadataFormat());
				psUpdate.setTimestamp(3, resumptionToken.getFrom());
				psUpdate.setTimestamp(4, resumptionToken.getUntil());
				psUpdate.setInt(5, resumptionToken.getOffset());
				psUpdate.setString(6, resumptionToken.getToken());
				psUpdate.setLong(7, resumptionToken.getId());

				// Execute the update statement and return the result
				return dbConnectionManager.executeUpdate(psUpdate) > 0;
			} // end try(update the record)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while updating the resumption token with ID " + resumptionToken.getId(), e);

				return false;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return update(resumptionToken);
			}
		} // end synchronized
	} // end method update(ResumptionToken)

	@Override
	public boolean delete(ResumptionToken resumptionToken) throws DataException
	{
		// Throw an exception if the connection is null.  This means the configuration file was bad.
		if(dbConnectionManager.getDbConnection() == null)
			throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");
		
		// Check that the ID field on the resumption token are valid
		validateFields(resumptionToken, true, false);

		synchronized(psDeleteLock)
		{
			if(log.isDebugEnabled())
				log.debug("Deleting the resumption token with ID " + resumptionToken.getId());

			try
			{
				// If the PreparedStatement to delete a resumption token was not defined, create it
				if(psDelete == null || dbConnectionManager.isClosed(psDelete))
				{
					// SQL to delete the row from the table
					String deleteSql = "DELETE FROM "+ RESUMPTION_TOKENS_TABLE_NAME + " " +
									   "WHERE " + COL_RESUMPTION_TOKEN_ID + " = ? ";

					if(log.isDebugEnabled())
						log.debug("Creating the \"delete resumption token\" PreparedStatement from the SQL " + deleteSql);

					// A prepared statement to run the delete SQL
					// This should sanitize the SQL and prevent SQL injection
					psDelete = dbConnectionManager.prepareStatement(deleteSql, psDelete);
				} // end if(delete PreparedStatement not defined)

				// Set the parameters on the delete statement
				psDelete.setLong(1, resumptionToken.getId());

				// Execute the delete statement and return the result
				return dbConnectionManager.execute(psDelete);
			} // end try(delete the record)
			catch(SQLException e)
			{
				log.error("A SQLException occurred while deleting the resumption token with ID " + resumptionToken.getId(), e);

				return false;
			} // end catch(SQLException)
			catch (DBConnectionResetException e){
				log.info("Re executing the query that failed ");
				return delete(resumptionToken);
			}
		} // end synchronized
	} // end method delete(ResumptionToken)
} // end class DefaultResumptionTokenDAO