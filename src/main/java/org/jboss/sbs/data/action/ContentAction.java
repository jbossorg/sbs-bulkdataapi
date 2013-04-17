/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.jivesoftware.community.*;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Action for bulk data content
 */
@Decorate(false)
public class ContentAction extends JiveActionSupport {

	protected static final Logger log = LogManager.getLogger(ContentAction.class);

	public enum ContentType {
		DOCUMENT("document"),
		FORUM("forum");

		private String value;

		private ContentType(String value) {
			this.value = value;
		}

		public boolean equalsIgnoreCase(String equalsTo) {
			return value.equalsIgnoreCase(equalsTo);
		}

		@Override
		public String toString() {
			return value;
		}
	}


	private InputStream dataInputStream;

	private Long updatedAfter;

	private Integer maxSize;

	private Integer spaceId;

	private String type;

	private String errorMessage = "";

	private CommunityManager communityManager;

	private DocumentManager documentManager;

	private ForumManager forumManager;

	public void validateFields() {
		if (StringUtils.isBlank(type)) {
			errorMessage += "\nparameter 'type' is required";
		}
		if (!(ContentType.DOCUMENT.equalsIgnoreCase(type) || ContentType.FORUM.equalsIgnoreCase(type))) {
			errorMessage = "type must be either '" + ContentType.DOCUMENT.toString() + "' or '"
					+ ContentType.FORUM.toString() + "'";
		}
		if (spaceId == null) {
			errorMessage += "\nparameter 'spaceId' is required";
		}
		if (updatedAfter == null) {
			errorMessage += "\nparameter 'updatedAfter' is required";
		}
	}

	@Override
	public String execute() {
		if (log.isDebugEnabled()) {
			log.debug("Get content, parameters: {type: " + type + ", spaceId: " + spaceId + ", updatedAfter: "
					+ updatedAfter + ", maxSize: " + maxSize + "}");
		}

		validateFields();
		if (errorMessage.length() > 0) {
			if (log.isDebugEnabled()) {
				log.debug("Bad request error message: " + errorMessage);
			}
			return "badrequest";
		}

		Community space;
		try {
			space = communityManager.getCommunity(spaceId);
		} catch (CommunityNotFoundException e) {
			errorMessage = "Cannot find space with id: " + spaceId;
			return "badrequest";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{");

		if (ContentType.DOCUMENT.equalsIgnoreCase(type)) {
			JiveIterator<Document> iterator = getDocuments(space);
			for (Document d : iterator) {
				appendData(sb, d.getID());
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
		} else if (ContentType.FORUM.equalsIgnoreCase(type)) {
			JiveIterator<ForumThread> iterator = getThreads(space);
			for (ForumThread t : iterator) {
				appendData(sb, t.getID());
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
		}

		sb.append("}");

		try {
			dataInputStream = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return SUCCESS;
	}

	protected void appendData(StringBuffer sb, Long id) {
		sb.append("{");
		sb.append("id:" + id);
		sb.append("}");
	}

	protected JiveIterator<Document> getDocuments(Community space) {
		return documentManager.getDocuments(space);
	}

	protected JiveIterator<ForumThread> getThreads(Community space) {
		return forumManager.getThreads(space);
	}

	public InputStream getDataInputStream() {
		return dataInputStream;
	}

	public Long getUpdatedAfter() {
		return updatedAfter;
	}

	public void setUpdatedAfter(Long updatedAfter) {
		this.updatedAfter = updatedAfter;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public Integer getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Integer spaceId) {
		this.spaceId = spaceId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	public void setForumManager(ForumManager forumManager) {
		this.forumManager = forumManager;
	}

	public void setCommunityManager(CommunityManager communityManager) {
		this.communityManager = communityManager;
	}
}