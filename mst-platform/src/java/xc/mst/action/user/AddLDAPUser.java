/**
 * Copyright (c) 2009 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
 * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
 * website http://www.extensiblecatalog.org/.
 *
 */

package xc.mst.action.user;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import xc.mst.action.BaseActionSupport;
import xc.mst.bo.user.Group;
import xc.mst.bo.user.Server;
import xc.mst.bo.user.User;
import xc.mst.constants.Constants;
import xc.mst.dao.DataException;
import xc.mst.dao.DatabaseConfigException;

/**
 * This action method is used to add an LDAP User to the system
 * 
 * @author Tejaswi Haramurali
 */
public class AddLDAPUser extends BaseActionSupport {
    /** Serial id */
    private static final long serialVersionUID = 4411174825770792605L;

    /** The username of the user */
    private String userName;

    /** The email ID of the user */
    private String email;

    /** The first Name of the user */
    private String firstName;

    /** The Last Name of the user */
    private String lastName;

    /** The groups that have been assigned to the new user */
    private String[] groupsSelected;

    /** The list of all groups in the system */
    private List<Group> groupList;

    /** List of servers in the system **/
    private List<Server> serversList;

    /** The list of groups that are already associated with the user **/
    private String[] selectedGroups;

    /** The temporary user object that is used to populate JSP form fields **/
    private User temporaryUser;

    /** A reference to the logger for this class */
    static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

    /** Error type */
    private String errorType;

    /**
     * Overrides default implementation to view the 'add NCIP user' page.
     * 
     * @return {@link #SUCCESS}
     */
    @Override
    public String execute() {
        try {
            setGroupList(getGroupService().getAllGroups());
            return SUCCESS;
        } catch (DatabaseConfigException dce) {
            log.error(dce.getMessage(), dce);
            this.addFieldError("addLDAPUserError", "Unable to connect to the database. Database Configuration may be incorrect");
            errorType = "error";
            return SUCCESS;
        }
    }

    /**
     * This is the action method that actually inserts a new NCIP user into the system
     * 
     * @return {@link #SUCCESS}
     */
    public String addLDAPUser() {
        try {

            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(null);
            user.setAccountCreated(new Date());
            user.setFailedLoginAttempts(0);
            user.setUsername(userName);

            List<Server> serverList = getServerService().getAll();
            boolean serverExists = false;
            Server tempServer = null;
            Iterator<Server> iter = serverList.iterator();
            while (iter.hasNext()) {
                tempServer = (Server) iter.next();
                if (tempServer.getType() != Server.ServerType.LOCAL) {
                    serverExists = true;
                    break;
                }
            }

            if (serverExists == false) {
                this.addFieldError("addLDAPUserError", "NO LDAP Server has been configured");
                errorType = "error";
                setGroupList(getGroupService().getAllGroups());
                setTemporaryUser(user);
                setSelectedGroups(groupsSelected);
                return INPUT;
            }
            user.setServer(tempServer);
            user.setLastLogin(new Date());

            User similarUserName = getUserService().getUserByUserName(user.getUsername(), tempServer);
            User similarEmail = getUserService().getUserByEmail(email, tempServer);
            if (similarUserName != null) {
                if (!similarUserName.getServer().getName().equalsIgnoreCase("Local")) {
                    this.addFieldError("addLDAPUserError", "Username already exists");
                    errorType = "error";
                    setGroupList(getGroupService().getAllGroups());
                    setTemporaryUser(user);
                    setSelectedGroups(groupsSelected);
                    return INPUT;
                }
            }
            if (similarEmail != null) {
                if (!similarEmail.getServer().getName().equalsIgnoreCase("Local")) {
                    this.addFieldError("addLDAPUserError", "Email ID already exists");
                    errorType = "error";
                    setGroupList(getGroupService().getAllGroups());
                    setTemporaryUser(user);
                    setSelectedGroups(groupsSelected);
                    return INPUT;
                }
            }

            for (int i = 0; i < groupsSelected.length; i++) {
                Group tempGroup = getGroupService().getGroupById(Integer.parseInt(groupsSelected[i]));
                user.addGroup(tempGroup);
            }
            getUserService().insertUser(user);

            return SUCCESS;
        } catch (DatabaseConfigException dce) {
            log.error(dce.getMessage(), dce);
            this.addFieldError("addLDAPUserError", "Unable to connect to the database. Database Configuration may be incorrect");
            errorType = "error";
            return ERROR;
        } catch (DataException de) {
            log.error(de.getMessage(), de);
            this.addFieldError("addLDAPUserError", "Error in adding LDAP user. An email has been sent to the administrator");
            getUserService().sendEmailErrorReport();
            errorType = "error";
            return ERROR;
        }
    }

    /**
     * Returns error type
     * 
     * @return error type
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Sets error type
     * 
     * @param errorType
     *            error type
     */
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    /**
     * Returns the first name of the user
     * 
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user
     * 
     * @param firstName
     *            first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    /**
     * Returns the last name of the user
     * 
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user
     * 
     * @param lastName
     *            last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    /**
     * Sets list of all servers
     * 
     * @param serversList
     *            server list
     */
    public void setServersList(List<Server> serversList) {
        this.serversList = serversList;
    }

    /**
     * Returns the list of all servers
     * 
     * @return server list
     */
    public List<Server> getServersList() {
        return serversList;
    }

    /**
     * Assigns the list of groups that a user can belong to
     * 
     * @param groupList
     *            list of groups
     */
    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    /**
     * Returns a list of groups that a user can belong to
     * 
     * @return list of groups
     */
    public List<Group> getGroupList() {
        return groupList;
    }

    /**
     * Sets the email ID of the user
     * 
     * @param email
     *            email ID
     */
    public void setEmail(String email) {
        this.email = email.trim();
    }

    /**
     * Returns the email ID of the user
     * 
     * @return email ID
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the username of the LDAP user
     * 
     * @param userName
     *            user name
     */
    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    /**
     * Returns the user name of the user
     * 
     * @return user Name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the list of groups that the user has been assigned
     * 
     * @param selectedGroupList
     *            list of selected groups
     */
    public void setGroupsSelected(String[] groupsSelected) {
        this.groupsSelected = groupsSelected;
    }

    /**
     * Returns the list of groups that have been assigned to the user
     * 
     * @return list of selected groups
     */
    public String[] getGroupsSelected() {
        return groupsSelected;
    }

    /**
     * Sets the list of groups that the user has been assigned (used to pre-fill JSP form fields)
     * 
     * @param selectedGroupList
     *            list of selected groups
     */
    public void setSelectedGroups(String[] selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    /**
     * Returns the list of groups that have been assigned to the user (used to pre-fill JSP form fields)
     * 
     * @return list of selected groups
     */
    public String[] getSelectedGroups() {
        return selectedGroups;
    }

    /**
     * Sets the temporary user object which is used to populate JSP form fields
     * 
     * @param user
     *            LDAP user object
     */
    public void setTemporaryUser(User user) {
        this.temporaryUser = user;
    }

    /**
     * Returns the temporary user object
     * 
     * @return user object
     */
    public User getTemporaryUser() {
        return temporaryUser;
    }

}
