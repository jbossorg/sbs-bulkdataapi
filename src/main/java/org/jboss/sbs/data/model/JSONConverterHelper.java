/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.sbs.data.action.IUserAccessor;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.ContentTag;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.TagDelegator;
import com.jivesoftware.community.web.JiveResourceResolver;

/**
 * Helper class with methods for easier implementation of {@link Content2JSONConverter}.
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JSONConverterHelper {

	private static final String AUTHOR_FULL_NAME = "full_name";
	private static final String AUTHOR_EMAIL = "email";

	/**
	 * Append JSON string into builder. So get value, JSON escape it, and wrap it up in quotation marks.
	 * 
	 * @param sb to append value into
	 * @param value to append
	 */
	public static void appendJsonString(StringBuilder sb, String value) {
		if (value == null)
			sb.append("null");
		else
			sb.append("\"").append(jsonEscape(value)).append("\"");

	}

	/**
	 * Append JSON field into builder. Field is not created if passed in value is null.
	 * 
	 * @param sb to append field into
	 * @param name of field
	 * @param value of field
	 * @param first if true then field is treated as first, so no comma before it
	 * @return true if field was really appended (so value was not null)
	 */
	public static boolean appendJSONField(StringBuilder sb, String name, String value, boolean first) {
		if (value != null) {
			if (!first) {
				sb.append(",");
			}
			sb.append("\"").append(name).append("\":\"").append(jsonEscape(value)).append("\"");
			return true;
		}
		return false;
	}

	/**
	 * Convert Date value into JSON.
	 * 
	 * @param date to convert
	 * @return converted string representation of Date.
	 */
	public static String convertDateValue(Date date) {
		if (date == null)
			return null;
		return date.getTime() + "";
	}

	/**
	 * Escape value to be used in JSON.
	 * 
	 * @param in value to escape
	 * @return JSON escaped value
	 */
	public static String jsonEscape(String in) {
		if (in == null || in.isEmpty())
			return in;
		return in.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace("\r", "\\r")
				.replace("\t", "\\t").replace("\b", "\\b").replace("\f", "\\f");
	}

	/**
	 * Appends these fields into JSON string for passed {@link JiveContentObject}.
	 * <ul>
	 * <li><code>id</code>
	 * <li><code>url</code>
	 * <li><code>content</code>
	 * <li><code>published</code>
	 * <li><code>updated</code>
	 * </ul>
	 * 
	 * @param sb to append content into
	 * @param data to append values from
	 * @param updatedTimespamp timestamp of update to be used. if null then JiveContentObject#getModificationDate() is
	 *          used
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static void appendCommonJiveContentObjecFields(StringBuilder sb, JiveContentObject data, Long updatedTimespamp)
			throws IOException, TransformerException {
		appendJSONField(sb, "id", data.getID() + "", true);
		appendJSONField(sb, "url", JiveResourceResolver.getJiveObjectURL(data, true), false);
		appendJSONField(sb, "content", bodyToXmlString(data), false);
		appendJSONField(sb, "published", convertDateValue(data.getCreationDate()), false);
		appendJSONField(sb, "updated",
				updatedTimespamp != null ? updatedTimespamp + "" : convertDateValue(data.getModificationDate()), false);
	}

	/**
	 * Get rendered body from JiveContentObject and convert it into String.
	 * 
	 * @param data to get body from
	 * @return body converted to the String with XML
	 * @throws IOException
	 * @throws TransformerException
	 * @see JiveContentObject#getBody()
	 */
	public static String bodyToXmlString(JiveContentObject data) throws IOException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer trans = transformerFactory.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "no");

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(data.getBody());
		trans.transform(source, result);
		sw.flush();
		sw.close();
		return sw.toString();
	}

	/**
	 * Append tags into JSON content.
	 * 
	 * @param sb to append tags into
	 * @param tagDelegator to obtain tags from
	 */
	public static void appendTags(StringBuilder sb, TagDelegator tagDelegator) {
		if (tagDelegator != null) {
			JiveIterator<ContentTag> tags = tagDelegator.getTags();
			if (tags.hasNext()) {
				sb.append(", ");
				JSONConverterHelper.appendJsonString(sb, "tags");
				sb.append(" : [");
				boolean first = true;
				for (ContentTag tag : tags) {
					if (first)
						first = false;
					else
						sb.append(",");
					JSONConverterHelper.appendJsonString(sb, tag.getName());
				}
				sb.append("]");
			}
		}
	}

	/**
	 * Append authors into JSON content.
	 * 
	 * @param sb to append into
	 * @param authors to append
	 * @param userAccessor service to access user data with necessary informations over security boundaries
	 * @throws Exception
	 * @throws UserNotFoundException
	 */
	public static void appendAuthors(StringBuilder sb, JiveIterator<User> authors, IUserAccessor userAccessor)
			throws Exception {
		if (authors != null && authors.hasNext()) {
			sb.append(", ");
			JSONConverterHelper.appendJsonString(sb, "authors");
			sb.append(" : [");
			boolean first = true;
			for (User author : authors) {
				if (first)
					first = false;
				else
					sb.append(",");
				sb.append("{");
				author = userAccessor.getTargetUser(author);
				appendJSONField(sb, AUTHOR_EMAIL, author.getEmail(), true);
				appendJSONField(sb, AUTHOR_FULL_NAME, author.getName(), false);
				sb.append("}");
			}
			sb.append("]");
		}
	}

	/**
	 * Append author into JSON content.
	 * 
	 * @param sb to append into
	 * @param author to append
	 * @param userAccessor service to access user data with necessary informations over security boundaries
	 * @throws Exception
	 * @throws UserNotFoundException
	 */
	public static void appendAuthor(StringBuilder sb, User author, IUserAccessor userAccessor) throws Exception {
		if (author != null && !author.isAnonymous()) {
			sb.append(", ");
			JSONConverterHelper.appendJsonString(sb, "author");
			sb.append(" : {");
			author = userAccessor.getTargetUser(author);
			appendJSONField(sb, AUTHOR_EMAIL, author.getEmail(), true);
			appendJSONField(sb, AUTHOR_FULL_NAME, author.getName(), false);
			sb.append("}");
		}
	}
}