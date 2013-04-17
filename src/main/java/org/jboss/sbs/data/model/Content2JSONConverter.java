/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

/**
 * Interface to convert content to JSON
 *
 * @author Libor Krzyzanek
 */
public interface Content2JSONConverter<T> {

	/**
	 * Converts content to string
	 *
	 * @param t
	 * @return
	 * @throws
	 */
	public String convert(T t) throws Exception;

}
