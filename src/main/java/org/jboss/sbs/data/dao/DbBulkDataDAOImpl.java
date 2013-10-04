/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.jboss.sbs.data.model.UpdatedDocumentInfo;
import org.springframework.jdbc.core.RowMapper;

import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.community.JiveConstants;

/**
 * Database based implementation of {@link BulkDataDAO}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DbBulkDataDAOImpl extends JiveJdbcDaoSupport implements BulkDataDAO {

	@Override
	public List<UpdatedDocumentInfo> listUpdatedDocuments(long spaceId, Long updatedAfterTimestamp) {

		String sql = prepareListUpdatedDocumentsSql(spaceId, updatedAfterTimestamp);
		logger.debug("SQL called: " + sql);
		return getSimpleJdbcTemplate().query(sql, new RowMapper<UpdatedDocumentInfo>() {

			@Override
			public UpdatedDocumentInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new UpdatedDocumentInfo(rs.getLong(1), rs.getLong(2));
			}
		});
	}

	protected static String prepareListUpdatedDocumentsSql(long spaceId, Long updatedAfterTimestamp) {
		String sqlDoc = "select jdv.internalDocID as id, jdv.modificationDate as d from jiveDocument as jd, jiveDocVersion as jdv where jd.internalDocID = jdv.internalDocID and jdv.state = 'published' and jd.containerID = "
				+ spaceId;
		String sqlComment = "select jd.internalDocID as id, comment.modificationDate as d from jiveDocument as jd, jiveComment as comment where jd.internalDocID = comment.objectID and comment.objectType = "
				+ JiveConstants.DOCUMENT + " and jd.containerID = " + spaceId;

		if (updatedAfterTimestamp != null) {
			sqlDoc = sqlDoc + " and jdv.modificationDate >= " + updatedAfterTimestamp;
			sqlComment = sqlComment + " and comment.modificationDate >= " + updatedAfterTimestamp;
		}

		String sql = "select a.id, max(a.d) from ( " + sqlDoc + " union  " + sqlComment
				+ " ) as a group by a.id order by a.d";
		return sql;
	}

	@Override
	public List<Long> listForumThreads(long spaceId, Long updatedAfterTimestamp) {
		String sql = prepareListForumThreadsSql(spaceId, updatedAfterTimestamp);
		logger.debug("SQL called: " + sql);
		return getSimpleJdbcTemplate().query(sql, new RowMapper<Long>() {

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		});
	}

	protected static String prepareListForumThreadsSql(long spaceId, Long updatedAfterTimestamp) {
		String sql = "select jdv.threadID as id from jivethread as jdv where jdv.status = 2 and jdv.containerID = "
				+ spaceId;
		if (updatedAfterTimestamp != null) {
			sql = sql + " and jdv.modificationDate >= " + updatedAfterTimestamp;
		}
		sql = sql + " order by jdv.modificationDate asc";
		return sql;
	}

}
