/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import java.util.concurrent.Callable;

import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.aaa.authz.SystemExecutor;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Base for Struts Actions within this API. Has methods to handle authentication etc.
 */
public abstract class ActionBase extends JiveActionSupport implements IUserAccessor {

	/**
	 * Name of security group used to authorize for this API. If Group doesn't exists then all logged in users are
	 * authorized.
	 */
	public static final String SECURITY_GROUP_NAME = "Bulk Data API Users";

	/**
	 * Little hack to make unit tests possible.
	 */
	protected IUserAccessor userAccessor = this;

	private GroupManager groupManager;

	/**
	 * Check if user is authorized to use this API.
	 * 
	 * @return null if authorized, struts outcome in other cases.
	 */
	protected String isAuthorizedForBulkDataAPIUse() {
		if (isGuest())
			return UNAUTHORIZED;

		try {
			Group gr = groupManager.getGroup(SECURITY_GROUP_NAME);
			if (gr != null && !gr.isMember(getUser()))
				return UNAUTHORIZED;
		} catch (GroupNotFoundException e) {
			// OK, we allow access in this case
		}
		return null;
	}

	@Override
	public User getTargetUser(final User origUser) throws Exception {
		SystemExecutor<User> exec = new SystemExecutor<User>(this.authProvider);

		Callable<User> callable = new Callable<User>() {
			public User call() throws UserNotFoundException {
				return userManager.getUser(origUser.getID());
			}
		};

		return exec.executeCallable(callable);
	}

	public void setGroupManager(GroupManager groupManager) {
		this.groupManager = groupManager;
	}

}