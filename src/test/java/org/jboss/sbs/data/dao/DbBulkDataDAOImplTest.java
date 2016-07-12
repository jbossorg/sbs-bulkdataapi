/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.dao;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit test for {@link DbBulkDataDAOImpl}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DbBulkDataDAOImplTest {

	@Test
	public void prepareListUpdatedDocumentsSql() {

		Assert
				.assertEquals(
						"select a.id, max(a.d) from ( select jdv.internalDocID as id, jdv.modificationDate as d from jiveDocument as jd, jiveDocVersion as jdv where jd.internalDocID = jdv.internalDocID and jdv.state = 'published' and jd.containerID = 10 union  select jd.internalDocID as id, comment.modificationDate as d from jiveDocument as jd, jiveComment as comment where jd.internalDocID = comment.objectID and comment.objectType = 102 and jd.containerID = 10 ) as a group by a.id order by a.d",
						DbBulkDataDAOImpl.prepareListUpdatedDocumentsSql(10, null));

		Assert
				.assertEquals(
						"select a.id, max(a.d) from ( select jdv.internalDocID as id, jdv.modificationDate as d from jiveDocument as jd, jiveDocVersion as jdv where jd.internalDocID = jdv.internalDocID and jdv.state = 'published' and jd.containerID = 10 and jdv.modificationDate >= 2365 union  select jd.internalDocID as id, comment.modificationDate as d from jiveDocument as jd, jiveComment as comment where jd.internalDocID = comment.objectID and comment.objectType = 102 and jd.containerID = 10 and comment.modificationDate >= 2365 ) as a group by a.id order by a.d",
						DbBulkDataDAOImpl.prepareListUpdatedDocumentsSql(10, 2365l));

	}

	@Test
	public void prepareListForumThreadsSql() {

		Assert
				.assertEquals(
						"select jdv.threadID as id from jiveThread as jdv where jdv.status = 2 and jdv.containerID = 10 order by jdv.modificationDate asc",
						DbBulkDataDAOImpl.prepareListForumThreadsSql(10, null));

		Assert
				.assertEquals(
						"select jdv.threadID as id from jiveThread as jdv where jdv.status = 2 and jdv.containerID = 10 and jdv.modificationDate >= 2365 order by jdv.modificationDate asc",
						DbBulkDataDAOImpl.prepareListForumThreadsSql(10, 2365l));

	}

}
