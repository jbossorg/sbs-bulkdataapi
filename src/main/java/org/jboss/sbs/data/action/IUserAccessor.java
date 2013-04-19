/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;

/**
 * Interface for user acessor implementation. Allows to obtain info about user over security restrictions.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface IUserAccessor {
	public User getTargetUser(final User origUser) throws UserNotFoundException, Exception;
}
