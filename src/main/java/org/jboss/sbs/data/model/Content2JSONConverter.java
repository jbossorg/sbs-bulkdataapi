/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import org.jboss.sbs.data.action.IUserAccessor;

/**
 * Interface to convert content to JSON
 * 
 * @param T type of content to convert from
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface Content2JSONConverter<T> {

	/**
	 * Converts content object into JSOW and write it to the output string builder
	 * 
	 * @param sb builder to write object to
	 * @param t content object to write into JSON
	 * @param userAccessor service to access user data with necessary informations over security boundaries
	 * @throws exception in case of problems
	 */
	public void convert(StringBuilder sb, T t, IUserAccessor userAccessor) throws Exception;

}
