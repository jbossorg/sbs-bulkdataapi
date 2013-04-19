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
	 * Converts content object into JSOW and write it to the output string builder
	 * 
	 * @param sb builder to write object to
	 * @param t object to write into JSON
	 * @throws
	 */
	public void convert(StringBuilder sb, T t) throws Exception;

}
