/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.dao.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import xc.mst.bo.log.Log;
import xc.mst.bo.user.Server;
import xc.mst.constants.Constants;
import xc.mst.dao.DBConnectionResetException;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.utils.LogWriter;

public class DefaultServerDAO extends ServerDAO {

    /**
     * The repository management log file name
     */
    private static Log logObj = null;

    /**
     * A PreparedStatement to get all servers in the database
     */
    private static PreparedStatement psGetAll = null;

    /**
     * A PreparedStatement to get a server from the database by its ID
     */
    private static PreparedStatement psGetById = null;

    /**
     * A PreparedStatement to get a server from the database by its name
     */
    private static PreparedStatement psGetByName = null;

    /**
     * A PreparedStatement to insert a server into the database
     */
    private static PreparedStatement psInsert = null;

    /**
     * A PreparedStatement to update a server in the database
     */
    private static PreparedStatement psUpdate = null;

    /**
     * A PreparedStatement to delete a server from the database
     */
    private static PreparedStatement psDelete = null;

    /**
     * Lock to synchronize access to the get all PreparedStatement
     */
    private static Object psGetAllLock = new Object();

    /**
     * Lock to synchronize access to the get by ID PreparedStatement
     */
    private static Object psGetByIdLock = new Object();

    /**
     * Lock to synchronize access to the get by Name PreparedStatement
     */
    private static Object psGetByNameLock = new Object();

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

    public void init() {
        try {
            logObj = getLogDAO().getById(Constants.LOG_ID_AUTHENTICATION_SERVER_MANAGEMENT);
        } catch (DatabaseConfigException e) {
            log.error("Unable to connect to the database using the parameters from the configuration file.", e);
        }
    }

    @Override
    public List<Server> getAll() throws DatabaseConfigException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        synchronized (psGetAllLock) {
            if (log.isDebugEnabled())
                log.debug("Getting all servers");

            // The ResultSet from the SQL query
            ResultSet results = null;

            // A list to hold the results of the query
            List<Server> servers = new ArrayList<Server>();

            try {
                // If the PreparedStatement to get all servers was not defined, create it
                if (psGetAll == null || dbConnectionManager.isClosed(psGetAll)) {
                    // SQL to get the rows
                    String selectSql = "SELECT " + COL_SERVER_ID + ", " +
                                                   COL_URL + ", " +
                                                   COL_NAME + ", " +
                                                   COL_TYPE + ", " +
                                                   COL_PORT + ", " +
                                                   COL_USERNAME_ATTRIBUTE + ", " +
                                                   COL_START_LOCATION + ", " +
                                                   COL_INSTITUION + ", " +
                                                   COL_FORGOT_PASSWORD_URL + ", " +
                                                   COL_FORGOT_PASSWORD_LABEL + ", " +
                                                   COL_SHOW_FORGOT_PASSWORD_LINK + " " +
                                       "FROM " + SERVERS_TABLE_NAME;

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"get all servers\" PreparedStatement from the SQL " + selectSql);

                    // A prepared statement to run the select SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psGetAll = dbConnectionManager.prepareStatement(selectSql, psGetAll);
                } // end if(get all PreparedStatement undefined)

                // Get the results of the SELECT statement

                // Execute the query
                results = dbConnectionManager.executeQuery(psGetAll);

                // If any results were returned
                while (results.next()) {
                    // The Object which will contain data on the server
                    Server server = new Server();

                    // Set the fields on the server
                    server.setId(results.getInt(1));
                    server.setUrl(results.getString(2));
                    server.setName(results.getString(3));

                    try {
                        server.setType(Server.ServerType.class.getEnumConstants()[results.getInt(4)]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        server.setType(Server.ServerType.UNDEFINED);
                    }

                    server.setPort(results.getInt(5));
                    server.setUserNameAttribute(results.getString(6));
                    server.setStartLocation(results.getString(7));
                    server.setInstitution(results.getString(8));
                    server.setForgotPasswordUrl(results.getString(9));
                    server.setForgotPasswordLabel(results.getString(10));
                    server.setShowForgotPasswordLink(results.getBoolean(11));

                    // Add the server to the list of servers to return
                    servers.add(server);
                } // end loop over results

                if (log.isDebugEnabled())
                    log.debug("Found " + servers.size() + " servers in the database.");

                return servers;
            } // end try(get and return all servers)
            catch (SQLException e) {
                log.error("A SQLException occurred while getting the servers", e);

                return servers;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return getAll();
            } finally {
                dbConnectionManager.closeResultSet(results);
            } // end finally (close ResultSet)
        } // end synchronized
    } // end method getAll()

    @Override
    public Server getById(int serverId) throws DatabaseConfigException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        synchronized (psGetByIdLock) {
            if (log.isDebugEnabled())
                log.debug("Getting the server with ID " + serverId);

            // The ResultSet from the SQL query
            ResultSet results = null;

            try {
                // If the PreparedStatement to get a server by ID was not defined, create it
                if (psGetById == null || dbConnectionManager.isClosed(psGetById)) {
                    // SQL to get the row
                    String selectSql = "SELECT " + COL_SERVER_ID + ", " +
                                                   COL_URL + ", " +
                                                   COL_NAME + ", " +
                                                   COL_TYPE + ", " +
                                                   COL_PORT + ", " +
                                                   COL_USERNAME_ATTRIBUTE + ", " +
                                                   COL_START_LOCATION + ", " +
                                                   COL_INSTITUION + ", " +
                                                   COL_FORGOT_PASSWORD_URL + ", " +
                                                   COL_FORGOT_PASSWORD_LABEL + ", " +
                                                   COL_SHOW_FORGOT_PASSWORD_LINK + " " +
                                       "FROM " + SERVERS_TABLE_NAME + " " +
                                       "WHERE " + COL_SERVER_ID + "=?";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"get server by ID\" PreparedSatement from the SQL " + selectSql);

                    // A prepared statement to run the select SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psGetById = dbConnectionManager.prepareStatement(selectSql, psGetById);
                } // end if (get by ID PreparedStatement not defined)

                // Set the parameters on the update statement
                psGetById.setInt(1, serverId);

                // Get the result of the SELECT statement

                // Execute the query
                results = dbConnectionManager.executeQuery(psGetById);

                // If any results were returned
                if (results.next()) {
                    // The Object which will contain data on the server
                    Server server = new Server();

                    // Set the fields on the user
                    server.setId(results.getInt(1));
                    server.setUrl(results.getString(2));
                    server.setName(results.getString(3));

                    try {
                        server.setType(Server.ServerType.class.getEnumConstants()[results.getInt(4)]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        server.setType(Server.ServerType.UNDEFINED);
                    }

                    server.setPort(results.getInt(5));
                    server.setUserNameAttribute(results.getString(6));
                    server.setStartLocation(results.getString(7));
                    server.setInstitution(results.getString(8));
                    server.setForgotPasswordUrl(results.getString(9));
                    server.setForgotPasswordLabel(results.getString(10));
                    server.setShowForgotPasswordLink(results.getBoolean(11));

                    if (log.isDebugEnabled())
                        log.debug("Found the server with ID " + serverId + " in the database.");

                    // Return the page
                    return server;
                } // end if (there was a result)
                else // There were no rows in the database, the server could not be found
                {
                    if (log.isDebugEnabled())
                        log.debug("The server with ID " + serverId + " was not found in the database.");

                    return null;
                } // end else
            } // end try (get and return server)
            catch (SQLException e) {
                log.error("A SQLException occurred while getting the server with ID " + serverId, e);

                return null;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return getById(serverId);
            } finally {
                dbConnectionManager.closeResultSet(results);
            } // end finally (close ResultSet)
        } // end synchronized
    } // end method getById(int)

    @Override
    public Server getByName(String name) throws DatabaseConfigException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        synchronized (psGetByNameLock) {
            if (log.isDebugEnabled())
                log.debug("Getting the server with the name " + name);

            // The ResultSet from the SQL query
            ResultSet results = null;

            try {
                // If the PreparedStatement to get a server by ID was not defined, create it
                if (psGetByName == null || dbConnectionManager.isClosed(psGetByName)) {
                    // SQL to get the row
                    String selectSql = "SELECT " + COL_SERVER_ID + ", " +
                                                   COL_URL + ", " +
                                                   COL_NAME + ", " +
                                                   COL_TYPE + ", " +
                                                   COL_PORT + ", " +
                                                   COL_USERNAME_ATTRIBUTE + ", " +
                                                   COL_START_LOCATION + ", " +
                                                   COL_INSTITUION + ", " +
                                                   COL_FORGOT_PASSWORD_URL + ", " +
                                                   COL_FORGOT_PASSWORD_LABEL + ", " +
                                                   COL_SHOW_FORGOT_PASSWORD_LINK + " " +
                                       "FROM " + SERVERS_TABLE_NAME + " " +
                                       "WHERE " + COL_NAME + "=?";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"get server by name\" PreparedSatement from the SQL " + selectSql);

                    // A prepared statement to run the select SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psGetByName = dbConnectionManager.prepareStatement(selectSql, psGetByName);
                } // end if (get by ID PreparedStatement not defined)

                // Set the parameters on the update statement
                psGetByName.setString(1, name);

                // Get the result of the SELECT statement

                // Execute the query
                results = dbConnectionManager.executeQuery(psGetByName);

                // If any results were returned
                if (results.next()) {
                    // The Object which will contain data on the server
                    Server server = new Server();

                    // Set the fields on the user
                    server.setId(results.getInt(1));
                    server.setUrl(results.getString(2));
                    server.setName(results.getString(3));

                    try {
                        server.setType(Server.ServerType.class.getEnumConstants()[results.getInt(4)]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        server.setType(Server.ServerType.UNDEFINED);
                    }

                    server.setPort(results.getInt(5));
                    server.setUserNameAttribute(results.getString(6));
                    server.setStartLocation(results.getString(7));
                    server.setInstitution(results.getString(8));
                    server.setForgotPasswordUrl(results.getString(9));
                    server.setForgotPasswordLabel(results.getString(10));
                    server.setShowForgotPasswordLink(results.getBoolean(11));

                    if (log.isDebugEnabled())
                        log.debug("Found the server with the name " + name + " in the database.");

                    // Return the page
                    return server;
                } // end if (there was a result)
                else // There were no rows in the database, the server could not be found
                {
                    if (log.isDebugEnabled())
                        log.debug("The server with the name " + name + " was not found in the database.");

                    return null;
                } // end else
            } // end try (get and return server)
            catch (SQLException e) {
                log.error("A SQLException occurred while getting the server with the name " + name, e);

                return null;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return getByName(name);
            } finally {
                dbConnectionManager.closeResultSet(results);
            } // end finally (close ResultSet)
        } // end synchronized
    } // end method getByName(String)

    @Override
    public boolean insert(Server server) throws DataException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        // Check that the non-ID fields on the server are valid
        validateFields(server, false, true);

        synchronized (psInsertLock) {
            if (log.isDebugEnabled())
                log.debug("Inserting a new sever with the url " + server.getUrl());

            // The ResultSet returned by the query
            ResultSet rs = null;

            try {
                // If the PreparedStatement to insert a server was not defined, create it
                if (psInsert == null || dbConnectionManager.isClosed(psInsert)) {
                    // SQL to insert the new row
                    String insertSql = "INSERT INTO " + SERVERS_TABLE_NAME + " (" + COL_URL + ", " +
                                                                                    COL_NAME + ", " +
                                                                                    COL_TYPE + ", " +
                                                                                    COL_PORT + ", " +
                                                                                    COL_USERNAME_ATTRIBUTE + ", " +
                                                                                    COL_START_LOCATION + ", " +
                                                                                    COL_INSTITUION + ", " +
                                                                                    COL_FORGOT_PASSWORD_URL + ", " +
                                                                                    COL_FORGOT_PASSWORD_LABEL + ", " +
                                                                                    COL_SHOW_FORGOT_PASSWORD_LINK + ") " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"insert server\" PreparedStatement from the SQL " + insertSql);

                    // A prepared statement to run the insert SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psInsert = dbConnectionManager.prepareStatement(insertSql, psInsert);
                } // end if(insert PreparedStatement not defined)

                // Set the parameters on the insert statement
                psInsert.setString(1, server.getUrl());
                psInsert.setString(2, server.getName());
                psInsert.setInt(3, server.getType().ordinal());
                psInsert.setInt(4, server.getPort());
                psInsert.setString(5, server.getUserNameAttribute());
                psInsert.setString(6, server.getStartLocation());
                psInsert.setString(7, server.getInstitution());
                psInsert.setString(8, server.getForgotPasswordUrl());
                psInsert.setString(9, server.getForgotPasswordLabel());
                psInsert.setBoolean(10, server.getShowForgotPasswordLink());

                // Execute the insert statement and return the result
                if (dbConnectionManager.executeUpdate(psInsert) > 0) {
                    // Get the auto-generated user ID and set it correctly on this Page Object
                    rs = dbConnectionManager.createStatement().executeQuery("SELECT LAST_INSERT_ID()");

                    if (rs.next())
                        server.setId(rs.getInt(1));

                    LogWriter.addInfo(logObj.getLogFileLocation(), "Added a new authentication server with the name " + server.getName());

                    return true;
                } else {
                    LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while adding a new authentication server with the name " + server.getName());

                    logObj.setErrors(logObj.getErrors() + 1);
                    getLogDAO().update(logObj);

                    return false;
                }
            } // end try(insert the server)
            catch (SQLException e) {
                log.error("A SQLException occurred while inserting a new server with the URL " + server.getUrl(), e);

                LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while adding a new authentication server with the name " + server.getName());

                logObj.setErrors(logObj.getErrors() + 1);
                getLogDAO().update(logObj);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return insert(server);
            } finally {
                dbConnectionManager.closeResultSet(rs);
            } // end finally (close ResultSet)
        } // end synchronized
    } // end method insert(Server)

    @Override
    public boolean update(Server server) throws DataException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        // Check that the fields on the server are valid
        validateFields(server, true, true);

        synchronized (psUpdateLock) {
            if (log.isDebugEnabled())
                log.debug("Updating the server with ID " + server.getId());

            try {
                // If the PreparedStatement to update a server was not defined, create it
                if (psUpdate == null || dbConnectionManager.isClosed(psUpdate)) {
                    // SQL to update new row
                    String updateSql = "UPDATE " + SERVERS_TABLE_NAME + " SET " + COL_URL + "=?, " +
                                                                                  COL_NAME + "=?, " +
                                                                                  COL_TYPE + "=?, " +
                                                                                  COL_PORT + "=?, " +
                                                                                  COL_USERNAME_ATTRIBUTE + "=?, " +
                                                                                  COL_START_LOCATION + "=?, " +
                                                                                  COL_INSTITUION + "=?, " +
                                                                                  COL_FORGOT_PASSWORD_URL + "=?, " +
                                                                                  COL_FORGOT_PASSWORD_LABEL + "=?, " +
                                                                                  COL_SHOW_FORGOT_PASSWORD_LINK + "=? " +
                                       "WHERE " + COL_SERVER_ID + "=?";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"update server\" PreparedStatement from the SQL " + updateSql);

                    // A prepared statement to run the update SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psUpdate = dbConnectionManager.prepareStatement(updateSql, psUpdate);
                } // end if (update PreparedStatement not defined)

                // Set the parameters on the update statement
                psUpdate.setString(1, server.getUrl());
                psUpdate.setString(2, server.getName());
                psUpdate.setInt(3, server.getType().ordinal());
                psUpdate.setInt(4, server.getPort());
                psUpdate.setString(5, server.getUserNameAttribute());
                psUpdate.setString(6, server.getStartLocation());
                psUpdate.setString(7, server.getInstitution());
                psUpdate.setString(8, server.getForgotPasswordUrl());
                psUpdate.setString(9, server.getForgotPasswordLabel());
                psUpdate.setBoolean(10, server.getShowForgotPasswordLink());
                psUpdate.setInt(11, server.getId());

                // Execute the update statement and return the result
                boolean success = dbConnectionManager.executeUpdate(psUpdate) > 0;

                if (success)
                    LogWriter.addInfo(logObj.getLogFileLocation(), "Updated the authentication server with the name " + server.getName());
                else {
                    LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while updating the authentication server with the name " + server.getName());

                    logObj.setErrors(logObj.getErrors() + 1);
                    getLogDAO().update(logObj);
                }
                return success;
            } // end try
            catch (SQLException e) {
                log.error("A SQLException occurred while updating the server with ID " + server.getId(), e);

                LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while updating the authentication server with the name " + server.getName());

                logObj.setErrors(logObj.getErrors() + 1);
                getLogDAO().update(logObj);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return update(server);
            }
        } // end synchronized
    } // end method update(Server)

    @Override
    public boolean delete(Server server) throws DataException {
        // Throw an exception if the connection is null. This means the configuration file was bad.
        if (dbConnectionManager.getDbConnection() == null)
            throw new DatabaseConfigException("Unable to connect to the database using the parameters from the configuration file.");

        // Check that the ID field on the server are valid
        validateFields(server, true, false);

        synchronized (psDeleteLock) {
            if (log.isDebugEnabled())
                log.debug("Deleting the server with ID " + server.getId());

            try {
                // If the PreparedStatement to delete a server was not defined, create it
                if (psDelete == null || dbConnectionManager.isClosed(psDelete)) {
                    // SQL to delete the row from the table
                    String deleteSql = "DELETE FROM " + SERVERS_TABLE_NAME + " " +
                                       "WHERE " + COL_SERVER_ID + " = ? ";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"delete server\" PreparedStatement from the SQL " + deleteSql);

                    // A prepared statement to run the delete SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psDelete = dbConnectionManager.prepareStatement(deleteSql, psDelete);
                } // end if(delete PreparedStatemnt not defined)

                // Set the parameters on the delete statement
                psDelete.setInt(1, server.getId());

                // Execute the delete statement and return the result
                boolean success = dbConnectionManager.execute(psDelete);

                if (success)
                    LogWriter.addInfo(logObj.getLogFileLocation(), "Deleted the authentication server with the name " + server.getName());
                else {
                    LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while deleting the authentication server with the name " + server.getName());

                    logObj.setErrors(logObj.getErrors() + 1);
                    getLogDAO().update(logObj);
                }

                return success;
            } // end try
            catch (SQLException e) {
                log.error("A SQLException occurred while deleting the server with ID " + server.getId(), e);

                LogWriter.addError(logObj.getLogFileLocation(), "An error occurrred while deleting the authentication server with the name " + server.getName());

                logObj.setErrors(logObj.getErrors() + 1);
                getLogDAO().update(logObj);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return delete(server);
            }
        } // end synchronized
    } // end method delete(Server)
} // end class DefaultServerDAO
