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

import com.jivesoftware.community.JiveContentObject;

/**
 * Helper class with methods for easier implementation of {@link Content2JSONConverter}.
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JSONConverterHelper {

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

	public static StringBuilder addCommonJiveContentObjecFields(StringBuilder sb, JiveContentObject data)
			throws IOException, TransformerException {
		appendJSONField(sb, "id", data.getID() + "", true);
		appendJSONField(sb, "content", toXmlString(data), false);
		appendJSONField(sb, "published", convertDateValue(data.getCreationDate()), false);
		appendJSONField(sb, "updated", convertDateValue(data.getModificationDate()), false);
		return sb;
	}

	public static String toXmlString(JiveContentObject data) throws IOException, TransformerException {
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
}