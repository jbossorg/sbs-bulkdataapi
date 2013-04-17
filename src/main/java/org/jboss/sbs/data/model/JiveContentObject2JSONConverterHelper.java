/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import com.jivesoftware.community.JiveContentObject;

/**
 * @author Libor Krzyzanek
 */
public class JiveContentObject2JSONConverterHelper {

	public static StringBuffer addCommonFields(StringBuffer sb, JiveContentObject data) throws IOException, TransformerException {
		sb.append("\"id\":" + data.getID());
		sb.append(",\"content\":\"" + toXmlString(data).replace("\"", "\\\"") + "\"");
		sb.append(",\"published\":" + data.getCreationDate().getTime());
		sb.append(",\"updated\":" + data.getModificationDate().getTime());

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