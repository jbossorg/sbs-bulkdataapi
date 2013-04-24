/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.dao;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit test for {@link DbBulkDataDAOImpl}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DbBulkDataDAOImplTest {

	@Test
	public void prepareSql() {

		Assert
				.assertEquals(
						"select a.id, max(a.d) from ( select jdv.internalDocID as id, jdv.modificationDate as d from jiveDocument as jd, jiveDocVersion as jdv where jd.internalDocID = jdv.internalDocID and jdv.state = 'published' and jd.containerID = 10 union  select jd.internalDocID as id, comment.modificationDate as d from jiveDocument as jd, jiveComment as comment where jd.internalDocID = comment.objectID and comment.objectType = 102 and jd.containerID = 10 ) as a group by a.id order by a.d",
						DbBulkDataDAOImpl.prepareSql(10, null));

		Assert
				.assertEquals(
						"select a.id, max(a.d) from ( select jdv.internalDocID as id, jdv.modificationDate as d from jiveDocument as jd, jiveDocVersion as jdv where jd.internalDocID = jdv.internalDocID and jdv.state = 'published' and jd.containerID = 10 and jdv.modificationDate >= 2365 union  select jd.internalDocID as id, comment.modificationDate as d from jiveDocument as jd, jiveComment as comment where jd.internalDocID = comment.objectID and comment.objectType = 102 and jd.containerID = 10 and comment.modificationDate >= 2365 ) as a group by a.id order by a.d",
						DbBulkDataDAOImpl.prepareSql(10, 2365l));

	}

}
