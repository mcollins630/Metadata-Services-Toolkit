/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.action.harvest;

import java.util.List;

import org.apache.log4j.Logger;

import xc.mst.action.BaseActionSupport;
import xc.mst.bo.harvest.HarvestSchedule;
import xc.mst.constants.Constants;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;
import xc.mst.dao.harvest.HarvestScheduleDAO;

/**
 * Action to view all schedules
 * 
 * @author Sharmila Ranganathan
 */
public class AllSchedules extends BaseActionSupport {

    /** determines the column by which the rows are to be sorted */
    private String columnSorted = "ScheduleName";

    /** Determines if rows are to be sorted in ascending or descending order */
    private boolean isAscendingOrder = true;

    /** Eclipse generated id */
    private static final long serialVersionUID = 4699309117473144076L;

    /** A reference to the logger for this class */
    static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

    /** All schedules */
    private List<HarvestSchedule> schedules;

    /** Id of schedule to be deleted. */
    private int scheduleId;

    /** Error type */
    private String errorType;

    /**
     * Get all schedules.
     */
    public String getAllSchedules() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("In All schedules Execute()");
            }
            if ((columnSorted.equalsIgnoreCase("ScheduleName")) || (columnSorted.equalsIgnoreCase("Recurrence")) || (columnSorted.equalsIgnoreCase("Status"))) {
                if (columnSorted.equalsIgnoreCase("ScheduleName")) {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_SCHEDULE_NAME);
                } else if (columnSorted.equalsIgnoreCase("Recurrence")) {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_RECURRENCE);
                } else if (columnSorted.equalsIgnoreCase("Status")) {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_STATUS);
                } else {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, "ProviderName");
                }
            } else {
                schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_SCHEDULE_NAME);
            }
            return SUCCESS;
        } catch (DatabaseConfigException dce) {
            log.error("Exception occured while getting Harvest schedule information.", dce);
            return SUCCESS;
        }
    }

    /**
     * Delete a schedule
     * 
     * @return
     */
    public String deleteSchedule() {

        if (log.isDebugEnabled()) {
            log.debug("AllSchedules::deleteSchedule() scheduleId = " + scheduleId);
        }

        String returnType = SUCCESS;

        HarvestSchedule schedule = null;
        try {
            schedule = getScheduleService().getScheduleById(scheduleId);
        } catch (DatabaseConfigException dce) {
            log.error(dce.getMessage(), dce);
            addFieldError("scheduleDeleteFailed", "Problems with retrieving and deleting the schedule.");
            errorType = "error";
            returnType = INPUT;
        }

        if (schedule != null) {
            try {
                getScheduleService().deleteSchedule(schedule);
            } catch (DataException e) {
                log.error("Deleting the schedule failed", e);
                addFieldError("scheduleDeleteFailed", "Problems with deleting the schedule :" + schedule.getScheduleName());
                errorType = "error";
                returnType = INPUT;
            }
        }

        try {
            if ((columnSorted.equalsIgnoreCase("ScheduleName")) || (columnSorted.equalsIgnoreCase("Recurrence"))) {
                if (columnSorted.equalsIgnoreCase("ScheduleName")) {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_SCHEDULE_NAME);
                } else {
                    schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_RECURRENCE);
                }

            } else {
                this.addFieldError("allSchedulesError", "The column " + columnSorted + " cannot be matched");
                schedules = getScheduleService().getAllSchedulesSorted(isAscendingOrder, HarvestScheduleDAO.COL_SCHEDULE_NAME);
            }
            setIsAscendingOrder(isAscendingOrder);

        } catch (DataException e) {
            log.error("Deleting the schedule failed" + e);
            addFieldError("scheduleDIsplayError", "Problems with displaying all the schedules.");
            errorType = "error";
            returnType = INPUT;
        }

        return returnType;
    }

    /**
     * Get all schedules
     * 
     * @return
     */
    public List<HarvestSchedule> getSchedules() {
        return schedules;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * sets the boolean value which determines if the rows are to be sorted in ascending order
     * 
     * @param isAscendingOrder
     */
    public void setIsAscendingOrder(boolean isAscendingOrder) {
        this.isAscendingOrder = isAscendingOrder;
    }

    /**
     * sgets the boolean value which determines if the rows are to be sorted in ascending order
     * 
     * @param isAscendingOrder
     */
    public boolean getIsAscendingOrder() {
        return this.isAscendingOrder;
    }

    /**
     * sets the name of the column on which the sorting should be performed
     * 
     * @param columnSorted
     *            name of the column
     */
    public void setColumnSorted(String columnSorted) {
        this.columnSorted = columnSorted;
    }

    /**
     * returns the name of the column on which sorting should be performed
     * 
     * @return column name
     */
    public String getColumnSorted() {
        return this.columnSorted;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

}
