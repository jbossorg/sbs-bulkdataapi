/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.dao;

import java.util.List;

import org.jboss.sbs.data.model.UpdatedDocumentInfo;

/**
 * DAO interface.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public interface BulkDataDAO {

	/**
	 * Get list of documents matching criteria. Comments are took into consideration in 'date of last update' evaluation.
	 * Returned documents are ordered by 'date of last update' as required by Bulk data API. Returned list may contain
	 * documents not available for current user, security check must be done on upper level!!!
	 * 
	 * @param spaceId identifier of space to get documents for
	 * @param updatedAfterTimestamp return only documents updated after this date. If null then whole history is used.
	 * @return last update ordered list with info about documents matching criteria
	 */
	public List<UpdatedDocumentInfo> listUpdatedDocuments(long spaceId, Long updatedAfterTimestamp);

}
