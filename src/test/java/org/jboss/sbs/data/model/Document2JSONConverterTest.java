/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import com.jivesoftware.base.UserTemplate;
import org.junit.Test;

/**
 * @author Libor Krzyzanek
 */
public class Document2JSONConverterTest {

	@Test
	public void testConvert() throws Exception {
		Document2JSONConverter converter = new Document2JSONConverter();

		UserTemplate user = new UserTemplate();
		user.setFirstName("Firstname");
		user.setLastName("Lastname");

		// TODO: Create Document via Mockito and test it

	}


}
