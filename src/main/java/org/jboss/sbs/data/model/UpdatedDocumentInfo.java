/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import com.jivesoftware.community.Document;

/**
 * Info about updated Document.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class UpdatedDocumentInfo {

	/**
	 * @param documentId
	 * @param lastUpdated
	 */
	public UpdatedDocumentInfo(long documentId, long lastUpdated) {
		super();
		this.documentId = documentId;
		this.lastUpdated = lastUpdated;
	}

	/**
	 * Constructor for unit tests
	 * 
	 * @param document
	 * @param lastUpdated
	 */
	protected UpdatedDocumentInfo(Document document, long lastUpdated) {
		super();
		this.lastUpdated = lastUpdated;
		this.document = document;
		documentId = document.getID();
	}

	/**
	 * Identifier of document.
	 */
	private long documentId;

	/**
	 * Date of last document update (comments are taken into account here)
	 */
	private long lastUpdated;

	/**
	 * Document for given ID. added later in service layer.
	 */
	private Document document;

	public long getDocumentId() {
		return documentId;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
