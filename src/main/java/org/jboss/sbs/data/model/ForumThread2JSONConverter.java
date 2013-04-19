/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import com.jivesoftware.community.ForumThread;

/**
 * ForumThread converter
 * 
 * @author Libor Krzyzanek
 */
public class ForumThread2JSONConverter implements Content2JSONConverter<ForumThread> {

	@Override
	public void convert(StringBuilder sb, ForumThread thread) throws Exception {
		sb.append("{");
		JSONConverterHelper.addCommonJiveContentObjecFields(sb, thread);
		sb.append("}");
	}
}
