/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/.
  *
  */

package xc.mst.action.user;

import java.util.List;

import org.apache.log4j.Logger;

import xc.mst.bo.user.Server;
import xc.mst.bo.user.User;
import xc.mst.constants.Constants;
import xc.mst.dao.DataException;
import xc.mst.email.Emailer;
import xc.mst.manager.user.DefaultServerService;
import xc.mst.manager.user.DefaultUserService;
import xc.mst.manager.user.ServerService;
import xc.mst.manager.user.UserService;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Action to register a user
 *
 * @author Sharmila Ranganathan
 *
 */
public class UserRegistration extends ActionSupport {

	/** Generated id  */
	private static final long serialVersionUID = 5946519493525167816L;

	/** A reference to the logger for this class */
	static Logger log = Logger.getLogger(Constants.LOGGER_GENERAL);

	/** New user registering with the system */
	private User newUser;

	/** List of servers */
	private List<Server> servers;

	/** Server service */
	private ServerService serverService =  new DefaultServerService();

	/** User service */
	private UserService userService =  new DefaultUserService();

	/** Comments entered by user */
	private String comments;

	/** Name of server selected */
	private String serverName;

	/**
	 * To register the user
	 */
	public String execute() throws DataException {
		servers =  serverService.getAll();

		return SUCCESS;
	}

	/**
	 * Registers the new user
	 *
	 * @return
	 */
	public String registerUser() {

		log.debug(UserRegistration.class + ":" + "registerUser()" );
		Server server = serverService.getServerByName(serverName);
		User otherUser = userService.getUserByUserName(newUser.getUsername().trim(), server);

		try {
			if (otherUser == null) {
				User otherUserWithSameEmail = userService.getUserByEmail(newUser.getEmail().trim(), server);

				if (otherUserWithSameEmail == null) {

					newUser.setServer(server);

					boolean emailSent = false;

					// Email the user
					Emailer emailer = new Emailer();
					StringBuffer messageBody = new StringBuffer();
					messageBody.append("Account has been created successfully in Metadata Services Toolkit.\n");
					messageBody.append("You will be able to login once System admin assigns permissions for your account.");
					String subject = "Your New Metadata Services Toolkit account";

					emailSent = emailer.sendEmail(newUser.getEmail().trim(), subject, messageBody.toString());

					if (!emailSent) {
						servers =  serverService.getAll();
						StringBuffer errorMessage = new StringBuffer();
						errorMessage.append("Email verification failed. Either the Email address doesnot exist or some problem with the mail server.\n");
						errorMessage.append("So user registartion Failed. Please enter valid email address or try again later.");
						addFieldError("emailError",  errorMessage.toString());
						return INPUT;
					}

					// Insert only after email verification is sent
					userService.insertUser(newUser);

					// Email the admin to assign permissions for new user
					StringBuffer adminMessageBody = new StringBuffer();
					adminMessageBody.append("New account created in Metadata Services Toolkit with user name : " + newUser.getUsername().trim());
					adminMessageBody.append("\nComments from the user : " + comments);
					adminMessageBody.append("\nPlease login into the system and assign appropriate permissions for the user.");
					String adminSubject = "Assign permission to new User";

					// TODO Change to get all admin of system and email to their ids
					emailer.sendEmail(userService.getUserByUserName("admin", serverService.getServerByName("Local")).getEmail(), adminSubject, adminMessageBody.toString());
				} else {
					servers =  serverService.getAll();
					addFieldError("userEmailExist", "This email address already exist in the system.- " + newUser.getEmail().trim());
					return INPUT;
				}

			} else {
				servers =  serverService.getAll();
				addFieldError("userNameExist", "User name already exist - " + newUser.getUsername().trim());
				return INPUT;
			}
		} catch (Exception e) {
			addFieldError("dataError", e.getMessage());
			return INPUT;
		}
		return SUCCESS;
	}

	public User getNewUser() {
		return newUser;
	}

	public void setNewUser(User newUser) {
		this.newUser = newUser;
	}

	public List<Server> getServers() {
		return servers;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
