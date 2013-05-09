/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.jivesoftware.base.User;
import com.jivesoftware.community.ContentTag;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.TagDelegator;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Unit test for {@link ForumThread2JSONConverter}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ForumThread2JSONConverterTest {

	@Test
	public void convert_noRepliesAndTags() throws Exception {
		ForumThread2JSONConverter converter = new ForumThread2JSONConverter();

		ForumThread content = mockForumThreadSimple(546586l, "my document title");

		StringBuilder sb = new StringBuilder();
		converter.convert(sb, content, JSONConverterHelperTest.mockIUserAccessor());
		Assert
				.assertEquals(
						"{\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"12466987\",\"title\":\"my document title\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]}",
						sb.toString());
	}

	public static ForumThread mockForumThreadSimple(long id, String title) {
		ForumThread content = Mockito.mock(ForumThread.class);
		Mockito.when(content.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument());
		Mockito.when(content.getID()).thenReturn(id);
		Mockito.when(content.getName()).thenReturn(title);
		Mockito.when(content.getCreationDate()).thenReturn(new Date(12456987));
		Mockito.when(content.getModificationDate()).thenReturn(new Date(12466987));

		List<User> authorsList = new ArrayList<User>();
		authorsList.add(JSONConverterHelperTest.mockUser("John Doe", "john@doe.org"));
		authorsList.add(JSONConverterHelperTest.mockUser("Jack Doe", "jack@doe.org"));
		JiveIterator<User> authors = new ListJiveIterator<User>(authorsList);
		Mockito.when(content.getAuthors()).thenReturn(authors);

		// only main message returned
		ForumMessage mainMessage = mockForumMessage(10l, "main one", 6543216546l, false);
		Mockito.when(content.getRootMessage()).thenReturn(mainMessage);
		List<ForumMessage> messagesList = new ArrayList<ForumMessage>();
		messagesList.add(mainMessage);
		JiveIterator<ForumMessage> messages = new ListJiveIterator<ForumMessage>(messagesList);
		Mockito.when(content.getMessageCount()).thenReturn(messagesList.size());
		Mockito.when(content.getMessages()).thenReturn(messages);

		JSONConverterHelperTest.mockJiveUrlFactory(content);
		return content;
	}

	@Test
	public void convert_withRepliesAndTags() throws Exception {
		ForumThread2JSONConverter converter = new ForumThread2JSONConverter();

		ForumThread content = Mockito.mock(ForumThread.class);
		Mockito.when(content.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument());
		Mockito.when(content.getID()).thenReturn(546586l);
		Mockito.when(content.getName()).thenReturn("my document title");
		Mockito.when(content.getCreationDate()).thenReturn(new Date(12456987));
		Mockito.when(content.getModificationDate()).thenReturn(new Date(12466987));

		List<ContentTag> tagsList = new ArrayList<ContentTag>();
		tagsList.add(JSONConverterHelperTest.mockTag("tag_1"));
		tagsList.add(JSONConverterHelperTest.mockTag("tag_2"));
		tagsList.add(JSONConverterHelperTest.mockTag("tag_3"));
		TagDelegator tgMock = JSONConverterHelperTest.mockTagDelegator(new ListJiveIterator<ContentTag>(tagsList));
		Mockito.when(content.getTagDelegator()).thenReturn(tgMock);

		List<User> authorsList = new ArrayList<User>();
		authorsList.add(JSONConverterHelperTest.mockUser("John Doe", "john@doe.org"));
		authorsList.add(JSONConverterHelperTest.mockUser("Jack Doe", "jack@doe.org"));
		JiveIterator<User> authors = new ListJiveIterator<User>(authorsList);
		Mockito.when(content.getAuthors()).thenReturn(authors);

		ForumMessage mainMessage = mockForumMessage(10l, "main one", 6543216546l, false);
		Mockito.when(content.getRootMessage()).thenReturn(mainMessage);
		List<ForumMessage> messagesList = new ArrayList<ForumMessage>();
		messagesList.add(mainMessage);
		messagesList.add(mockForumMessage(20, "text 2", 56332132l, true));
		messagesList.add(mockForumMessage(20, "text 3", 63321351l, false));
		JiveIterator<ForumMessage> messages = new ListJiveIterator<ForumMessage>(messagesList);
		Mockito.when(content.getMessageCount()).thenReturn(messagesList.size());
		Mockito.when(content.getMessages()).thenReturn(messages);

		JSONConverterHelperTest.mockJiveUrlFactory(content);

		StringBuilder sb = new StringBuilder();
		converter.convert(sb, content, JSONConverterHelperTest.mockIUserAccessor());
		Assert
				.assertEquals(
						"{\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"12466987\",\"title\":\"my document title\""
								+ ", \"tags\" : [\"tag_1\",\"tag_2\",\"tag_3\"]"
								+ ", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]"
								+ ", \"comments\" : [{\"content\":\"<root>text 2</root>\", \"author\" : {\"email\":\"john@doe.org\",\"full_name\":\"John Doe comment\"},\"published\":\"56332132\"},{\"content\":\"<root>text 3</root>\", \"author\" : {\"email\":\"john@doe.org\",\"full_name\":\"John Doe comment\"},\"published\":\"63321351\"}]}",
						sb.toString());
	}

	public static ForumMessage mockForumMessage(long id, String text, long creationDate, boolean twoAuthors) {
		ForumMessage ret = Mockito.mock(ForumMessage.class);
		Mockito.when(ret.getID()).thenReturn(id);
		Mockito.when(ret.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument(text));
		Mockito.when(ret.getCreationDate()).thenReturn(new Date(creationDate));
		User user = JSONConverterHelperTest.mockUser("John Doe comment", "john@doe.org");
		Mockito.when(ret.getUser()).thenReturn(user);
		return ret;
	}

}
