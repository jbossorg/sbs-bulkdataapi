/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import org.jboss.sbs.data.action.IUserAccessor;

import com.jivesoftware.community.ForumThread;

/**
 * ForumThread converter
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ForumThread2JSONConverter implements Content2JSONConverter<ForumThread> {

	@Override
	public void convert(StringBuilder sb, ForumThread thread, IUserAccessor userAccessor) throws Exception {
		sb.append("{");
		JSONConverterHelper.appendCommonJiveContentObjecFields(sb, thread);
		JSONConverterHelper.appendJSONField(sb, "title", thread.getName(), false);
		JSONConverterHelper.appendTags(sb, thread.getTagDelegator());
		JSONConverterHelper.appendAuthors(sb, thread.getAuthors(), userAccessor);
		// TODO add "comments" field
		sb.append("}");
	}
}
