/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.jboss.sbs.data.action.IUserAccessor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.ContentTag;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.JiveObjectURLFactory;
import com.jivesoftware.community.TagDelegator;
import com.jivesoftware.community.impl.EmptyJiveIterator;
import com.jivesoftware.community.impl.ListJiveIterator;
import com.jivesoftware.community.web.JiveResourceResolver;

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
	public void appendJsonString() {

		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendJsonString(sb, null);
			Assert.assertEquals("null", sb.toString());
		}

		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendJsonString(sb, "my value");
			Assert.assertEquals("\"my value\"", sb.toString());
		}

		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendJsonString(sb, "my val\tue");
			Assert.assertEquals("\"my val\\tue\"", sb.toString());
		}

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

	@Test
	public void appendTags() {
		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendTags(sb, null);
			Assert.assertEquals(0, sb.length());
		}

		{
			StringBuilder sb = new StringBuilder();
			JiveIterator<ContentTag> tags = EmptyJiveIterator.getInstance();
			TagDelegator tagDelegator = mockTagDelegator(tags);
			JSONConverterHelper.appendTags(sb, tagDelegator);
			Assert.assertEquals(0, sb.length());
		}

		{
			StringBuilder sb = new StringBuilder();
			List<ContentTag> tagsList = new ArrayList<ContentTag>();
			tagsList.add(mockTag("tag_1"));
			JSONConverterHelper.appendTags(sb, mockTagDelegator(new ListJiveIterator<ContentTag>(tagsList)));
			Assert.assertEquals(", \"tags\" : [\"tag_1\"]", sb.toString());
		}

		{
			StringBuilder sb = new StringBuilder();
			List<ContentTag> tagsList = new ArrayList<ContentTag>();
			tagsList.add(mockTag("tag_1"));
			tagsList.add(mockTag("tag_2"));
			tagsList.add(mockTag("tag_3"));
			JSONConverterHelper.appendTags(sb, mockTagDelegator(new ListJiveIterator<ContentTag>(tagsList)));
			Assert.assertEquals(", \"tags\" : [\"tag_1\",\"tag_2\",\"tag_3\"]", sb.toString());
		}

	}

	@Test
	public void bodyToXmlString() throws IOException, TransformerException {
		JiveContentObject data = mockJiveContentObject();
		Assert.assertEquals("<root>test &gt; text \" content</root>", JSONConverterHelper.bodyToXmlString(data));
	}

	@Test
	public void appendCommonJiveContentObjecFields() throws IOException, TransformerException {
		JiveContentObject data = mockJiveContentObject();
		mockJiveUrlFactory(data);
		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendCommonJiveContentObjecFields(sb, data, null);
			Assert
					.assertEquals(
							"\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"12466987\"",
							sb.toString());
		}

		{
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendCommonJiveContentObjecFields(sb, data, 123l);
			Assert
					.assertEquals(
							"\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"123\"",
							sb.toString());
		}
	}

	public static void mockJiveUrlFactory(JiveContentObject data) {
		JiveObjectURLFactory urlFactoryMock = Mockito.mock(JiveObjectURLFactory.class);
		Mockito.when(urlFactoryMock.createURL(Mockito.any(JiveObject.class), Mockito.eq(true))).thenReturn(
				"http://my.test.org/myobject");
		JiveResourceResolver.addURLFactory(data.getObjectType(), urlFactoryMock);
	}

	protected static JiveContentObject mockJiveContentObject() {
		JiveContentObject content = Mockito.mock(JiveContentObject.class);
		Mockito.when(content.getBody()).thenReturn(getTestDOMDocument());
		Mockito.when(content.getID()).thenReturn(546586l);
		Mockito.when(content.getCreationDate()).thenReturn(new Date(12456987));
		Mockito.when(content.getModificationDate()).thenReturn(new Date(12466987));

		return content;
	}

	protected static Document getTestDOMDocument() {
		return getTestDOMDocument("test > text \" content");
	}

	protected static Document getTestDOMDocument(String text) {
		Document doc = DOMImplementationImpl.getDOMImplementation().createDocument(null, null, null);
		Element elRoot = doc.createElement("root");
		elRoot.setTextContent(text);
		doc.appendChild(elRoot);
		return doc;
	}

	protected static TagDelegator mockTagDelegator(JiveIterator<ContentTag> tags) {
		TagDelegator ret = Mockito.mock(TagDelegator.class);
		Mockito.when(ret.getTags()).thenReturn(tags);
		return ret;
	}

	protected static ContentTag mockTag(final String name) {
		ContentTag user = Mockito.mock(ContentTag.class);
		Mockito.when(user.getName()).thenReturn(name);
		return user;
	}

	@Test
	public void appendAuthors() throws Exception {

		{
			IUserAccessor userAccessor = mockIUserAccessor();
			StringBuilder sb = new StringBuilder();
			JSONConverterHelper.appendAuthors(sb, null, userAccessor);
			Assert.assertEquals(0, sb.length());
		}

		{
			IUserAccessor userAccessor = mockIUserAccessor();
			StringBuilder sb = new StringBuilder();
			JiveIterator<User> authors = EmptyJiveIterator.getInstance();
			JSONConverterHelper.appendAuthors(sb, authors, userAccessor);
			Assert.assertEquals(0, sb.length());
		}

		{
			IUserAccessor userAccessor = mockIUserAccessor();
			StringBuilder sb = new StringBuilder();
			List<User> authorsList = new ArrayList<User>();
			authorsList.add(mockUser("John Doe", "john@doe.org"));
			JiveIterator<User> authors = new ListJiveIterator<User>(authorsList);
			JSONConverterHelper.appendAuthors(sb, authors, userAccessor);
			Assert.assertEquals(", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"}]", sb.toString());
			Mockito.verify(userAccessor).getTargetUser(authorsList.get(0));
		}

		{
			IUserAccessor userAccessor = mockIUserAccessor();
			StringBuilder sb = new StringBuilder();
			List<User> authorsList = new ArrayList<User>();
			authorsList.add(mockUser("John Doe", "john@doe.org"));
			authorsList.add(mockUser("Jack Doe", "jack@doe.org"));
			JiveIterator<User> authors = new ListJiveIterator<User>(authorsList);
			JSONConverterHelper.appendAuthors(sb, authors, userAccessor);
			Assert
					.assertEquals(
							", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]",
							sb.toString());
			Mockito.verify(userAccessor).getTargetUser(authorsList.get(0));
			Mockito.verify(userAccessor).getTargetUser(authorsList.get(1));
		}

	}

	protected static User mockUser(final String name, final String email) {
		User user = Mockito.mock(User.class);
		Mockito.when(user.getEmail()).thenReturn(email);
		Mockito.when(user.getName()).thenReturn(name);
		return user;
	}

	public static IUserAccessor mockIUserAccessor() throws UserNotFoundException, Exception {
		IUserAccessor userAccessor = Mockito.mock(IUserAccessor.class);
		Mockito.when(userAccessor.getTargetUser(Mockito.any(User.class))).thenAnswer(new Answer<User>() {

			@Override
			public User answer(InvocationOnMock invocation) throws Throwable {
				return (User) invocation.getArguments()[0];
			}
		});
		return userAccessor;
	}

}
