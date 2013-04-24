/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.sbs.data.dao.BulkDataDAO;
import org.jboss.sbs.data.model.ForumThread2JSONConverterTest;
import org.jboss.sbs.data.model.JSONConverterHelperTest;
import org.jboss.sbs.data.model.UpdatedDocumentInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.CommunityNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.ForumManager;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContentObject.Status;
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Unit test for {@link ContentAction}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ContentActionTest {

	@Test
	public void validateFields() {
		{
			ContentAction tested = new ContentAction();
			tested.validateFields();
			Assert.assertEquals("parameter 'type' is required\nparameter 'spaceId' is required", tested.getErrorMessage());
		}

		{
			ContentAction tested = new ContentAction();
			tested.setType("bad");
			tested.validateFields();
			Assert.assertEquals("parameter 'type' must be either 'document' or 'forum'\nparameter 'spaceId' is required",
					tested.getErrorMessage());
		}

		// case - default maxSize handling
		{
			ContentAction tested = new ContentAction();
			tested.setType("forum");
			tested.setSpaceId(123l);
			tested.validateFields();
			Assert.assertEquals("", tested.getErrorMessage());
			Assert.assertEquals(new Integer(20), tested.getMaxSize());
		}

		// case - maxSize setter
		{
			ContentAction tested = new ContentAction();
			tested.setType("document");
			tested.setSpaceId(123l);
			tested.setMaxSize(50);
			tested.validateFields();
			Assert.assertEquals("", tested.getErrorMessage());
			Assert.assertEquals(new Integer(50), tested.getMaxSize());
		}

		// case - maximal maxSize handling
		{
			ContentAction tested = new ContentAction();
			tested.setType("forum");
			tested.setSpaceId(123l);
			tested.setMaxSize(101);
			tested.validateFields();
			Assert.assertEquals("", tested.getErrorMessage());
			Assert.assertEquals(new Integer(100), tested.getMaxSize());
		}
	}

	@Test
	public void execute_validateError() {
		ContentAction tested = new ContentAction();
		Assert.assertEquals("badrequest", tested.execute());
		Assert.assertEquals("parameter 'type' is required\nparameter 'spaceId' is required", tested.getErrorMessage());
	}

	@Test
	public void execute_unknownCommunity() throws Exception {
		ContentAction tested = new ContentAction();
		tested.setType("forum");
		tested.setSpaceId(123l);
		CommunityManager communityManagerMock = Mockito.mock(CommunityManager.class);
		Mockito.when(communityManagerMock.getCommunity(123l)).thenThrow(new CommunityNotFoundException());
		tested.setCommunityManager(communityManagerMock);

		Assert.assertEquals("badrequest", tested.execute());
		Assert.assertEquals("Cannot find space with id: 123", tested.getErrorMessage());
	}

	@Test
	public void execute_forum_noresult() throws Exception {
		ContentAction tested = new ContentAction();
		final List<ForumThread> threadsList = new ArrayList<ForumThread>();
		performForumTest(tested, threadsList, 100l);
		assertOutputContent("{ \"items\": []}", tested);
	}

	@Test
	public void execute_forum_withresults() throws Exception {
		ContentAction tested = new ContentAction();
		tested.userAccessor = JSONConverterHelperTest.mockIUserAccessor();
		final List<ForumThread> threadsList = new ArrayList<ForumThread>();
		threadsList.add(ForumThread2JSONConverterTest.mockForumThreadSimple(10, "thread 1"));
		threadsList.add(ForumThread2JSONConverterTest.mockForumThreadSimple(20, "thread 2"));
		performForumTest(tested, threadsList, 100l);
		assertOutputContent(
				"{ \"items\": [{\"id\":\"10\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"12466987\",\"title\":\"thread 1\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]},{\"id\":\"20\",\"url\":\"http://my.test.org/myobject\",\"content\":\"<root>test &gt; text \\\" content</root>\",\"published\":\"12456987\",\"updated\":\"12466987\",\"title\":\"thread 2\", \"authors\" : [{\"email\":\"john@doe.org\",\"full_name\":\"John Doe\"},{\"email\":\"jack@doe.org\",\"full_name\":\"Jack Doe\"}]}]}",
				tested);
	}

	private void performForumTest(ContentAction tested, final List<ForumThread> threadsList,
			final Long modificationDateExpected) throws CommunityNotFoundException {
		CommunityManager communityManagerMock = Mockito.mock(CommunityManager.class);
		Community secondSpaceMock = Mockito.mock(Community.class);
		Mockito.when(communityManagerMock.getCommunity(123l)).thenReturn(secondSpaceMock);
		tested.setCommunityManager(communityManagerMock);
		ForumManager forumManagerMock = Mockito.mock(ForumManager.class);
		tested.setForumManager(forumManagerMock);
		Mockito.when(forumManagerMock.getThreads(Mockito.eq(secondSpaceMock), Mockito.notNull(ThreadResultFilter.class)))
				.thenAnswer(new Answer<JiveIterator<ForumThread>>() {

					@Override
					public JiveIterator<ForumThread> answer(InvocationOnMock invocation) throws Throwable {
						ThreadResultFilter filter = (ThreadResultFilter) invocation.getArguments()[1];
						if (modificationDateExpected != null)
							Assert.assertEquals(modificationDateExpected.longValue(), filter.getModificationDateRangeMin().getTime());
						else
							Assert.assertNull(filter.getModificationDateRangeMin());
						Assert.assertEquals(10, filter.getNumResults());
						Assert.assertEquals(JiveConstants.MODIFICATION_DATE, filter.getSortField());
						Assert.assertEquals(ResultFilter.ASCENDING, filter.getSortOrder());
						return new ListJiveIterator<ForumThread>(threadsList);
					}
				});

		tested.setType("forum");
		tested.setSpaceId(123l);
		tested.setUpdatedAfter(100l);
		tested.setMaxSize(10);
		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
	}

	private void assertOutputContent(String expected, ContentAction tested) throws IOException {
		Assert.assertNotNull(tested.getDataInputStream());
		Assert.assertEquals(expected, new String(IOUtils.toByteArray(tested.getDataInputStream()), "UTF-8"));
	}

	@Test
	public void getDocuments() throws Exception {
		ContentAction tested = new ContentAction();
		tested.setMaxSize(3);
		tested.setUpdatedAfter(123l);

		Community space = Mockito.mock(Community.class);
		Mockito.when(space.getID()).thenReturn(1l);

		BulkDataDAO bulkDataDAO = Mockito.mock(BulkDataDAO.class);
		tested.setBulkDataDAO(bulkDataDAO);

		DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		tested.setDocumentManager(documentManager);

		// case - emply list from DAO
		{
			List<UpdatedDocumentInfo> list = new ArrayList<UpdatedDocumentInfo>();
			Mockito.when(bulkDataDAO.listUpdatedDocuments(1l, 123l)).thenReturn(list);
			Assert.assertEquals(0, tested.getDocuments(space).size());
			Mockito.verifyZeroInteractions(documentManager);
		}

		// case - something returned from dao, exception handling from document details reading
		{
			Mockito.reset(bulkDataDAO, documentManager);
			List<UpdatedDocumentInfo> list = new ArrayList<UpdatedDocumentInfo>();
			list.add(new UpdatedDocumentInfo(10, 100));
			list.add(new UpdatedDocumentInfo(11, 100));
			list.add(new UpdatedDocumentInfo(12, 100));
			list.add(new UpdatedDocumentInfo(13, 100));
			list.add(new UpdatedDocumentInfo(14, 100));
			list.add(new UpdatedDocumentInfo(15, 100));
			list.add(new UpdatedDocumentInfo(16, 100));
			// next will be skipped because are after maxSize
			list.add(new UpdatedDocumentInfo(17, 100));
			list.add(new UpdatedDocumentInfo(18, 100));
			Mockito.when(bulkDataDAO.listUpdatedDocuments(1l, 123l)).thenReturn(list);

			Document doc1 = mockDocument(10, Status.PUBLISHED);
			Mockito.when(documentManager.getDocument(10)).thenReturn(doc1);

			// this will be skipped
			Document doc2 = mockDocument(11, Status.DELETED);
			Mockito.when(documentManager.getDocument(11)).thenReturn(doc2);

			Document doc3 = mockDocument(12, Status.PUBLISHED);
			Mockito.when(documentManager.getDocument(12)).thenReturn(doc3);

			// these will be skipped
			Mockito.when(documentManager.getDocument(13)).thenThrow(new UnauthorizedException());
			Mockito.when(documentManager.getDocument(14)).thenThrow(new DocumentObjectNotFoundException());
			Mockito.when(documentManager.getDocument(15)).thenReturn(null);

			Document doc4 = mockDocument(16, Status.PUBLISHED);
			Mockito.when(documentManager.getDocument(16)).thenReturn(doc4);

			Assert.assertEquals(3, tested.getDocuments(space).size());
			Assert.assertEquals(doc1, list.get(0).getDocument());
			Assert.assertEquals(doc3, list.get(2).getDocument());
			Assert.assertEquals(doc4, list.get(6).getDocument());
			Mockito.verify(documentManager, Mockito.times(7)).getDocument(Mockito.anyLong());
			Mockito.verifyNoMoreInteractions(documentManager);
		}
	}

	private Document mockDocument(long id, Status status) {
		Document doc = Mockito.mock(Document.class);
		Mockito.when(doc.getID()).thenReturn(id);
		Mockito.when(doc.getStatus()).thenReturn(status);
		return doc;
	}

	@Test
	public void execute_document_noresult() throws Exception {
		ContentAction tested = new ContentAction();
		tested.setType("document");
		tested.setMaxSize(3);
		tested.setUpdatedAfter(123l);
		tested.setSpaceId(1l);

		CommunityManager communityManagerMock = Mockito.mock(CommunityManager.class);
		Community secondSpaceMock = Mockito.mock(Community.class);
		Mockito.when(secondSpaceMock.getID()).thenReturn(1l);
		Mockito.when(communityManagerMock.getCommunity(1l)).thenReturn(secondSpaceMock);
		tested.setCommunityManager(communityManagerMock);

		BulkDataDAO bulkDataDAO = Mockito.mock(BulkDataDAO.class);
		tested.setBulkDataDAO(bulkDataDAO);
		List<UpdatedDocumentInfo> list = new ArrayList<UpdatedDocumentInfo>();
		Mockito.when(bulkDataDAO.listUpdatedDocuments(1l, 123l)).thenReturn(list);

		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
		assertOutputContent("{ \"items\": []}", tested);
	}

	@Test
	public void execute_document_withresults() throws Exception {
		ContentAction tested = new ContentAction();
		tested.setType("document");
		tested.setMaxSize(3);
		tested.setSpaceId(1l);

		CommunityManager communityManagerMock = Mockito.mock(CommunityManager.class);
		Community secondSpaceMock = Mockito.mock(Community.class);
		Mockito.when(secondSpaceMock.getID()).thenReturn(1l);
		Mockito.when(communityManagerMock.getCommunity(1l)).thenReturn(secondSpaceMock);
		tested.setCommunityManager(communityManagerMock);

		BulkDataDAO bulkDataDAO = Mockito.mock(BulkDataDAO.class);
		tested.setBulkDataDAO(bulkDataDAO);
		List<UpdatedDocumentInfo> list = new ArrayList<UpdatedDocumentInfo>();
		list.add(new UpdatedDocumentInfo(10, 100));
		list.add(new UpdatedDocumentInfo(11, 110));
		Mockito.when(bulkDataDAO.listUpdatedDocuments(1l, null)).thenReturn(list);

		DocumentManager documentManager = Mockito.mock(DocumentManager.class);
		tested.setDocumentManager(documentManager);
		Document doc1 = mockDocument(10, Status.PUBLISHED);
		Mockito.when(documentManager.getDocument(10)).thenReturn(doc1);
		Document doc2 = mockDocument(11, Status.PUBLISHED);
		Mockito.when(documentManager.getDocument(11)).thenReturn(doc2);

		JSONConverterHelperTest.mockJiveUrlFactory(doc1);

		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
		assertOutputContent(
				"{ \"items\": [{\"id\":\"10\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\",\"updated\":\"100\"},{\"id\":\"11\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\",\"updated\":\"110\"}]}",
				tested);
	}

}
