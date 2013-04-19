/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link JSONConverterHelper}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JSONConverterHelperTest {

	@Test
	public void jsonEscape() {
		Assert.assertNull(JSONConverterHelper.jsonEscape(null));
		Assert.assertEquals("", JSONConverterHelper.jsonEscape(""));
		Assert.assertEquals("/", JSONConverterHelper.jsonEscape("/"));

		Assert.assertEquals("\\\\", JSONConverterHelper.jsonEscape("\\"));
		Assert.assertEquals("\\\"", JSONConverterHelper.jsonEscape("\""));
		Assert.assertEquals("ah\\noj\\n", JSONConverterHelper.jsonEscape("ah\noj\n"));
		Assert.assertEquals("ah\\roj\\t", JSONConverterHelper.jsonEscape("ah\roj\t"));
		Assert.assertEquals("ah\\boj\\f", JSONConverterHelper.jsonEscape("ah\boj\f"));
		Assert.assertEquals("ah\\\"oj", JSONConverterHelper.jsonEscape("ah\"oj"));
	}

	@Test
	public void convertDateValue() {
		Assert.assertNull(JSONConverterHelper.convertDateValue(null));
		Assert.assertEquals("12354", JSONConverterHelper.convertDateValue(new Date(12354l)));
	}

	@Test
	public void appendJSONField() {
		StringBuilder sb = new StringBuilder();

		Assert.assertFalse(JSONConverterHelper.appendJSONField(sb, "filed", null, true));
		Assert.assertFalse(JSONConverterHelper.appendJSONField(sb, "filed", null, false));
		Assert.assertEquals(0, sb.length());

		sb.append("{");
		Assert.assertTrue(JSONConverterHelper.appendJSONField(sb, "filed", "val", true));
		Assert.assertTrue(JSONConverterHelper.appendJSONField(sb, "filed2", "val\"ue", false));
		sb.append("}");

		Assert.assertEquals("{\"filed\":\"val\",\"filed2\":\"val\\\"ue\"}", sb.toString());

	}

}
