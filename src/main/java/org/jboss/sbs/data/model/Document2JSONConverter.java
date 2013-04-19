/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jboss.sbs.data.action.IUserAccessor;

import com.jivesoftware.base.User;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.comments.CommentDelegator;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Converter for Document object
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
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
		appendComments(sb, document.getCommentDelegator(), userAccessor);
		sb.append("}");
	}

	/**
	 * Append comments into JSON content.
	 * 
	 * @param sb to append comments into
	 * @param commentDelegator to obtain comments from
	 * @throws TransformerException
	 * @throws IOException
	 */
	public static void appendComments(StringBuilder sb, CommentDelegator commentDelegator, IUserAccessor userAccessor)
			throws Exception {
		if (commentDelegator != null) {
			JiveIterator<Comment> comments = commentDelegator.getComments();
			if (comments.hasNext()) {
				JSONConverterHelper.appendJsonString(sb, "comments");
				sb.append(" : [");
				boolean first = true;
				for (Comment comment : comments) {
					if (first)
						first = false;
					else
						sb.append(",");
					sb.append("{");
					JSONConverterHelper.appendJSONField(sb, "content", JSONConverterHelper.toXmlString(comment), true);
					JSONConverterHelper.appendAuthors(sb, comment.getAuthors(), userAccessor);
					JSONConverterHelper.appendJSONField(sb, "published",
							JSONConverterHelper.convertDateValue(comment.getCreationDate()), false);
					sb.append("}");
				}
				sb.append("]");
			}
		}
	}

}
