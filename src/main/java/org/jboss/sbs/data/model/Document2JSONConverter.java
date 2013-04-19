/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import com.jivesoftware.community.Document;

/**
 * Converter for Document object
 * 
 * @author Libor Krzyzanek
 */
public class Document2JSONConverter implements Content2JSONConverter<Document> {

	@Override
	public void convert(StringBuilder sb, Document document) throws Exception {
		sb.append("{");
		JSONConverterHelper.addCommonJiveContentObjecFields(sb, document);
		sb.append("}");
	}

}
