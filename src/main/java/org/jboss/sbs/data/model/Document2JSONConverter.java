/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.util.ArrayList;
import java.util.List;

import org.jboss.sbs.data.action.IUserAccessor;

import com.jivesoftware.base.User;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Converter for Document object
 * 
 * @author Libor Krzyzanek
 */
public class Document2JSONConverter implements Content2JSONConverter<Document> {

	@Override
	public void convert(StringBuilder sb, Document document, IUserAccessor userAccessor) throws Exception {
		sb.append("{");
		JSONConverterHelper.appendCommonJiveContentObjecFields(sb, document);
		JSONConverterHelper.appendJSONField(sb, "title", document.getSubject(), false);
		JSONConverterHelper.appendTags(sb, document.getTagDelegator());
		if (document.getLatestVersionAuthor() != null || document.getUser() != null) {
			List<User> al = new ArrayList<User>();
			if (document.getLatestVersionAuthor() != null)
				al.add(document.getLatestVersionAuthor());
			if (document.getUser() != null)
				al.add(document.getUser());
			JiveIterator<User> authors = new ListJiveIterator<User>(al);
			JSONConverterHelper.appendAuthors(sb, authors, userAccessor);
		}
		// TODO add "comments" field
		sb.append("}");
	}

}
