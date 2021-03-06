/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.ForumManager;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.ForumThreadNotFoundException;
import com.jivesoftware.community.JiveContentObject.Status;
import com.jivesoftware.community.aaa.authz.SystemExecutor;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.sbs.data.dao.BulkDataDAO;
import org.jboss.sbs.data.model.Document2JSONConverter;
import org.jboss.sbs.data.model.ForumThread2JSONConverter;
import org.jboss.sbs.data.model.JSONConverterHelper;
import org.jboss.sbs.data.model.UpdatedDocumentInfo;

/**
 * Struts Action with bulk data content access handler implementation.
 */
@Decorate(false)
public class ContentAction extends ActionBase {

	/**
	 * Name of security group used to authorize for this API. If Group doesn't exists then all logged in users are
	 * authorized.
	 */
	public static final String SECURITY_GROUP_NAME = "Bulk Data API Users";

	protected static final Logger log = LogManager.getLogger(ContentAction.class);

	public enum ContentType {
		DOCUMENT("document"), FORUM("forum");

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

	private Long spaceId;

	private String type;

	private String errorMessage = "";

	// Injected
	private DocumentManager documentManager;

	// Injected
	private ForumManager forumManager;

	// Injected
	private BulkDataDAO bulkDataDAO;

	public void validateFields() {
		if (StringUtils.isBlank(type)) {
			errorMessage += "parameter 'type' is required";
		} else if (!(ContentType.DOCUMENT.equalsIgnoreCase(type) || ContentType.FORUM.equalsIgnoreCase(type))) {
			errorMessage = "parameter 'type' must be either '" + ContentType.DOCUMENT.toString() + "' or '"
					+ ContentType.FORUM.toString() + "'";
		}
		if (spaceId == null) {
			errorMessage += "\nparameter 'spaceId' is required";
		}

		if (maxSize == null || maxSize <= 0)
			maxSize = 20;
		if (maxSize > 100)
			maxSize = 100;
	}

	@Override
	public String execute() {
		if (log.isDebugEnabled()) {
			log.debug("Get content, parameters: {type: " + type + ", spaceId: " + spaceId + ", updatedAfter: " + updatedAfter
					+ ", maxSize: " + maxSize + "}");
		}

		String ret = isAuthorizedForBulkDataAPIUse();
		if (ret != null)
			return ret;

		validateFields();
		if (errorMessage != null && errorMessage.length() > 0) {
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

		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		JSONConverterHelper.appendJsonString(sb, "container_info");
		sb.append(" : { ");
		boolean devSpace = space.getDisplayName().equalsIgnoreCase("dev");
		JSONConverterHelper.appendJSONField(sb, "dev", devSpace, true);
		sb.append("}, ");

		JSONConverterHelper.appendJsonString(sb, "items");
		sb.append(": [");

		if (ContentType.DOCUMENT.equalsIgnoreCase(type)) {
			List<UpdatedDocumentInfo> list = getDocuments(space);
			Document2JSONConverter converter = new Document2JSONConverter();
			boolean first = true;
			for (UpdatedDocumentInfo d : list) {
				try {
					if (first)
						first = false;
					else
						sb.append(",");
					converter.convert(sb, d, userAccessor, globalResourceResolver);
				} catch (Exception e) {
					throw new RuntimeException("Cannot parse document, id: " + d.getDocumentId() + " due: " + e.getMessage(), e);
				}
			}
		} else if (ContentType.FORUM.equalsIgnoreCase(type)) {
			Iterable<ForumThread> iterator = getThreads(space);
			ForumThread2JSONConverter converter = new ForumThread2JSONConverter();
			boolean first = true;
			for (ForumThread thread : iterator) {
				try {
					if (first)
						first = false;
					else
						sb.append(",");
					converter.convert(sb, thread, userAccessor, globalResourceResolver);
				} catch (Exception e) {
					throw new RuntimeException("Cannot parse forum thread, id: " + thread.getID() + " due: " + e.getMessage(), e);
				}

			}
		}

		sb.append("]}");

		try {
			dataInputStream = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return SUCCESS;
	}

	protected List<UpdatedDocumentInfo> getDocuments(Community space) {
		List<UpdatedDocumentInfo> rawDocsInfo = bulkDataDAO.listUpdatedDocuments(space.getID(), updatedAfter);
		List<UpdatedDocumentInfo> ret = new ArrayList<UpdatedDocumentInfo>();
		for (UpdatedDocumentInfo updateDocInfo : rawDocsInfo) {
			try {
				Document doc = documentManager.getDocument(updateDocInfo.getDocumentId());
				if (doc != null && Status.PUBLISHED.equals(doc.getStatus())) {
					updateDocInfo.setDocument(doc);
					ret.add(updateDocInfo);
				}
			} catch (UnauthorizedException e) {
				// nothing to do, we skip this document
			} catch (DocumentObjectNotFoundException e) {
				// nothing to do, ignore it
			}
			// check if we have enough documents and break loop if yes
			if (ret.size() >= maxSize)
				break;
		}
		return ret;
	}

	protected List<ForumThread> getThreads(Community space) {
		List<Long> rawThreadIds = bulkDataDAO.listForumThreads(space.getID(), updatedAfter);
		List<ForumThread> ret = new ArrayList<ForumThread>();
		for (Long threadId : rawThreadIds) {
			try {
				ForumThread thread = forumManager.getForumThread(threadId);
				if (thread != null && Status.PUBLISHED.equals(thread.getStatus())) {
					ret.add(thread);
				}
			} catch (UnauthorizedException e) {
				// nothing to do, we skip this thread
			} catch (ForumThreadNotFoundException e) {
				// nothing to do, ignore it
			}
			// check if we have enough documents and break loop if yes
			if (ret.size() >= maxSize)
				break;
		}
		return ret;
	}

	@Override
	public User getTargetUser(final User origUser) throws Exception {
		SystemExecutor<User> exec = new SystemExecutor<User>(this.authProvider);

		Callable<User> callable = new Callable<User>() {
			public User call() throws UserNotFoundException {
				return userManager.getUser(origUser.getID());
			}
		};

		return exec.executeCallable(callable);
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

	public Long getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(Long spaceId) {
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

	public void setBulkDataDAO(BulkDataDAO bulkDataDAO) {
		this.bulkDataDAO = bulkDataDAO;
	}

}