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
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.TagDelegator;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.comments.CommentDelegator;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Unit test for {@link Document2JSONConverter}
 * 
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class Document2JSONConverterTest {

	@Test
	public void convert_noCommentsAndTags() throws Exception {
		Document2JSONConverter converter = new Document2JSONConverter();

		Document content = Mockito.mock(Document.class);
		Mockito.when(content.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument());
		Mockito.when(content.getID()).thenReturn(546586l);
		Mockito.when(content.getSubject()).thenReturn("my document title");
		Mockito.when(content.getCreationDate()).thenReturn(new Date(12456987));
		Mockito.when(content.getModificationDate()).thenReturn(new Date(12466987));

		User u1 = JSONConverterHelperTest.mockUser("John Doe", "john@doe.org");
		User u2 = JSONConverterHelperTest.mockUser("Jack Doe", "jack@doe.org");
		Mockito.when(content.getUser()).thenReturn(u1);
		Mockito.when(content.getLatestVersionAuthor()).thenReturn(u2);

		JSONConverterHelperTest.mockJiveUrlFactory(content);

		StringBuilder sb = new StringBuilder();
		UpdatedDocumentInfo udi = new UpdatedDocumentInfo(content, 456l);
		converter.convert(sb, udi, JSONConverterHelperTest.mockIUserAccessor());
		Assert
				.assertEquals(
						"{\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"456\",\"title\":\"my document title\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]}",
						sb.toString());

	}

	@Test
	public void convert_withCommentsAndTags() throws Exception {
		Document2JSONConverter converter = new Document2JSONConverter();

		Document content = Mockito.mock(Document.class);
		Mockito.when(content.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument());
		Mockito.when(content.getID()).thenReturn(546586l);
		Mockito.when(content.getSubject()).thenReturn("my document title");
		Mockito.when(content.getCreationDate()).thenReturn(new Date(12456987));
		Mockito.when(content.getModificationDate()).thenReturn(new Date(12466987));

		List<ContentTag> tagsList = new ArrayList<ContentTag>();
		tagsList.add(JSONConverterHelperTest.mockTag("tag_1"));
		tagsList.add(JSONConverterHelperTest.mockTag("tag_2"));
		tagsList.add(JSONConverterHelperTest.mockTag("tag_3"));
		TagDelegator tgMock = JSONConverterHelperTest.mockTagDelegator(new ListJiveIterator<ContentTag>(tagsList));
		Mockito.when(content.getTagDelegator()).thenReturn(tgMock);

		User u1 = JSONConverterHelperTest.mockUser("John Doe", "john@doe.org");
		Mockito.when(content.getUser()).thenReturn(u1);
		Mockito.when(content.getLatestVersionAuthor()).thenReturn(u1);

		List<Comment> comments = new ArrayList<Comment>();
		comments.add(mockComment("test comment text", 457895462, true));
		comments.add(mockComment("test comment text 2 ", 557895462, false));
		CommentDelegator cd = Mockito.mock(CommentDelegator.class);
		Mockito.when(cd.getComments()).thenReturn(new ListJiveIterator<Comment>(comments));
		Mockito.when(content.getCommentDelegator()).thenReturn(cd);

		JSONConverterHelperTest.mockJiveUrlFactory(content);

		StringBuilder sb = new StringBuilder();
		UpdatedDocumentInfo udi = new UpdatedDocumentInfo(content, 4567l);
		converter.convert(sb, udi, JSONConverterHelperTest.mockIUserAccessor());
		Assert
				.assertEquals(
						"{\"id\":\"546586\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"4567\",\"title\":\"my document title\""
								+ ", \"tags\" : [\"tag_1\",\"tag_2\",\"tag_3\"]"
								+ ", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"}]"
								+ ", \"comments\" : [{\"content\":\"<root>test comment text</root>\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe comment\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe comment\"}],\"published\":\"457895462\"},{\"content\":\"<root>test comment text 2 </root>\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe comment\"}],\"published\":\"557895462\"}]}",
						sb.toString());

	}

	private Comment mockComment(String text, long creationDate, boolean twoAuthors) {
		Comment ret = Mockito.mock(Comment.class);
		Mockito.when(ret.getBody()).thenReturn(JSONConverterHelperTest.getTestDOMDocument(text));
		Mockito.when(ret.getCreationDate()).thenReturn(new Date(creationDate));
		List<User> authorsList = new ArrayList<User>();
		authorsList.add(JSONConverterHelperTest.mockUser("John Doe comment", "john@doe.org"));
		if (twoAuthors)
			authorsList.add(JSONConverterHelperTest.mockUser("Jack Doe comment", "jack@doe.org"));
		JiveIterator<User> authors = new ListJiveIterator<User>(authorsList);
		Mockito.when(ret.getAuthors()).thenReturn(authors);
		return ret;
	}

}
