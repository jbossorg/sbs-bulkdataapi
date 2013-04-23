/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.sbs.data.model.JSONConverterHelper;

import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;

/**
 * Struts Action with spaces operation handler implementation.
 */
@Decorate(false)
public class SpacesAction extends JiveActionSupport {

	protected static final Logger log = LogManager.getLogger(SpacesAction.class);

	private InputStream dataInputStream;

	protected CommunityManager communityManager;

	@Override
	public String execute() {
		if (log.isDebugEnabled()) {
			log.debug("Get spaces");
		}

		JiveIterator<Community> spaces = communityManager.getCommunities(communityManager.getRootCommunity());

		StringBuilder sb = new StringBuilder();
		sb.append("{ \"spaces\": [");
		boolean first = true;
		if (spaces != null) {
			for (Community space : spaces) {
				if (first)
					first = false;
				else
					sb.append(",");
				JSONConverterHelper.appendJsonString(sb, space.getID() + "");
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

	public InputStream getDataInputStream() {
		return dataInputStream;
	}

	public void setCommunityManager(CommunityManager communityManager) {
		this.communityManager = communityManager;
	}
}