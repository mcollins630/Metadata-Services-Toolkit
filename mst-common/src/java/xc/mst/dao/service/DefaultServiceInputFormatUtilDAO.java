/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.dao.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import xc.mst.dao.DBConnectionResetException;

public class DefaultServiceInputFormatUtilDAO extends ServiceInputFormatUtilDAO {
    /**
     * A PreparedStatement to add a format as input for a service into the database
     */
    private static PreparedStatement psInsert = null;

    /**
     * A PreparedStatement to remove a format as input for a service in the database
     */
    private static PreparedStatement psDelete = null;

    /**
     * A PreparedStatement to get the IDs of the formats that a service accepts as input
     */
    private static PreparedStatement psGetInputFormatsForService = null;

    /**
     * A PreparedStatement to remove all input format assignments for a service
     */
    private static PreparedStatement psDeleteInputFormatsForService = null;

    /**
     * Lock to prevent concurrent access of the prepared statement to
     * add a format as input for a service into the database.
     */
    private static Object psInsertLock = new Object();

    /**
     * Lock to prevent concurrent access of the prepared statement to
     * remove a format as input for a service in the database.
     */
    private static Object psDeleteLock = new Object();

    /**
     * Lock to prevent concurrent access of the prepared statement to
     * get the IDs of the formats that a service accepts as input.
     */
    private static Object psGetInputFormatsForServiceLock = new Object();

    /**
     * Lock to prevent concurrent access of the prepared statement to
     * remove all input format assignments for a service.
     */
    private static Object psDeleteInputFormatsForServiceLock = new Object();

    @Override
    public boolean insert(int serviceId, int formatId) {
        synchronized (psInsertLock) {
            if (log.isDebugEnabled())
                log.debug("Adding the format with ID " + formatId + " as input for the service with ID " + serviceId + ".");

            // The ResultSet returned by the query
            ResultSet rs = null;

            try {
                // If the PreparedStatement to add an input format to a service is not defined, create it
                if (psInsert == null || dbConnectionManager.isClosed(psInsert)) {
                    // SQL to insert the new row
                    String insertSql = "INSERT INTO " + SERVICES_TO_INPUT_FORMATS_TABLE_NAME +
                                                        " (" + COL_SERVICE_ID + ", " +
                                                               COL_FORMAT_ID + ") " +
                                       "VALUES (?, ?)";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"add input format for a service\" PreparedStatement from the SQL " + insertSql);

                    // A prepared statement to run the insert SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psInsert = dbConnectionManager.prepareStatement(insertSql, psInsert);
                } // end if (insert prepared statement is null)

                // Set the parameters on the insert statement
                psInsert.setInt(1, serviceId);
                psInsert.setInt(2, formatId);

                // Execute the insert statement and return the result
                return dbConnectionManager.executeUpdate(psInsert) > 0;
            } // end try (insert the input format to service assignment)
            catch (SQLException e) {
                log.error("A SQLException occurred while adding the format with ID " + formatId + " as input for the service with ID " + serviceId + ".", e);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return insert(serviceId, formatId);
            } finally {
                dbConnectionManager.closeResultSet(rs);
            } // end finally
        } // end synchronized
    } // end method insert(int, int)

    @Override
    public boolean delete(int serviceId, int formatId) {
        synchronized (psDeleteLock) {
            if (log.isDebugEnabled())
                log.debug("Removing the format with ID " + formatId + " as input for the service with ID " + serviceId + ".");

            // The ResultSet returned by the query
            ResultSet rs = null;

            try {
                // If the PreparedStatement to remove an input format from a service is not defined, create it
                if (psDelete == null || dbConnectionManager.isClosed(psDelete)) {
                    // SQL to insert the new row
                    String deleteSql = "DELETE FROM " + SERVICES_TO_INPUT_FORMATS_TABLE_NAME + " " +
                                       "WHERE " + COL_SERVICE_ID + "=? " +
                                       "AND " + COL_FORMAT_ID + "=? ";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"remove input format from a service\" PreparedStatement from the SQL " + deleteSql);

                    // A prepared statement to run the insert SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psDelete = dbConnectionManager.prepareStatement(deleteSql, psDelete);
                } // end if (insert prepared statement is null)

                // Set the parameters on the insert statement
                psDelete.setInt(1, serviceId);
                psDelete.setInt(2, formatId);

                // Execute the delete statement and return the result
                return dbConnectionManager.executeUpdate(psDelete) > 0;
            } // end try (delete the input format to service assignment)
            catch (SQLException e) {
                log.error("A SQLException occurred while removing the format with ID " + formatId + " as input for the service with ID " + serviceId + ".", e);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return delete(serviceId, formatId);
            } finally {
                dbConnectionManager.closeResultSet(rs);
            } // end finally
        } // end synchronized
    } // end method delete(int, int)

    @Override
    public List<Integer> getInputFormatsForService(int serviceId) {
        synchronized (psGetInputFormatsForServiceLock) {
            if (log.isDebugEnabled())
                log.debug("Getting the input formats for the service with service ID " + serviceId);

            // The ResultSet from the SQL query
            ResultSet results = null;

            // The list of format IDs for the input formats for the service with the passed ID
            List<Integer> formatIds = new ArrayList<Integer>();

            try {
                // If the PreparedStatement to get input format IDs by service ID wasn't defined, create it
                if (psGetInputFormatsForService == null || dbConnectionManager.isClosed(psGetInputFormatsForService)) {
                    // SQL to get the rows
                    String selectSql = "SELECT " + COL_FORMAT_ID + " " +
                                       "FROM " + SERVICES_TO_INPUT_FORMATS_TABLE_NAME + " " +
                                       "WHERE " + COL_SERVICE_ID + "=?";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"get input formats for service\" PreparedStatement from the SQL " + selectSql);

                    // A prepared statement to run the select SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psGetInputFormatsForService = dbConnectionManager.prepareStatement(selectSql, psGetInputFormatsForService);
                }

                // Set the parameters on the select statement
                psGetInputFormatsForService.setInt(1, serviceId);

                // Get the result of the SELECT statement

                // Execute the query
                results = dbConnectionManager.executeQuery(psGetInputFormatsForService);

                // For each result returned, add the format ID to the list with the returned data
                while (results.next())
                    formatIds.add(new Integer(results.getInt(1)));

                if (log.isDebugEnabled())
                    log.debug("Found " + formatIds.size() + " format IDs that the service with service ID " + serviceId + " accepts as input.");

                return formatIds;
            } // end try (get and return the format IDs which the service accepts as input)
            catch (SQLException e) {
                log.error("A SQLException occurred while getting the input format IDs for the service with service ID " + serviceId, e);

                return formatIds;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return getInputFormatsForService(serviceId);
            } finally {
                dbConnectionManager.closeResultSet(results);
            } // end finally
        } // end synchronized
    } // end method getInputFormatsForService(int)

    @Override
    public boolean deleteInputFormatsForService(int serviceId) {
        synchronized (psDeleteInputFormatsForServiceLock) {
            if (log.isDebugEnabled())
                log.debug("Removing the input format assignments for the service with service ID " + serviceId);

            try {
                // If the PreparedStatement to delete input format assignments by service ID wasn't defined, create it
                if (psDeleteInputFormatsForService == null || dbConnectionManager.isClosed(psDeleteInputFormatsForService)) {
                    // SQL to get the rows
                    String selectSql = "DELETE FROM " + SERVICES_TO_INPUT_FORMATS_TABLE_NAME + " " +
                                       "WHERE " + COL_SERVICE_ID + "=? ";

                    if (log.isDebugEnabled())
                        log.debug("Creating the \"remove input formats for service\" PreparedStatement from the SQL " + selectSql);

                    // A prepared statement to run the select SQL
                    // This should sanitize the SQL and prevent SQL injection
                    psDeleteInputFormatsForService = dbConnectionManager.prepareStatement(selectSql, psDeleteInputFormatsForService);
                }

                // Set the parameters on the select statement
                psDeleteInputFormatsForService.setInt(1, serviceId);

                // Get the result of the SELECT statement

                // Execute the insert statement and return the result
                return dbConnectionManager.executeUpdate(psDeleteInputFormatsForService) > 0;
            } // end try (remove all input format assignments from the service)
            catch (SQLException e) {
                log.error("A SQLException occurred while deleting the input format assignments for the service with service ID " + serviceId, e);

                return false;
            } // end catch(SQLException)
            catch (DBConnectionResetException e) {
                log.info("Re executing the query that failed ");
                return deleteInputFormatsForService(serviceId);
            }
        } // end synchronized
    } // end method deleteInputFormatsForService(int)
} // end class DefaultServiceInputFormatUtilDAO
