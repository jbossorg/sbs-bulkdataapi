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
import org.jboss.sbs.data.model.JSONConverterHelperTest;
import org.jboss.sbs.data.model.UpdatedDocumentInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.CommunityNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.ForumManager;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.ForumThreadNotFoundException;
import com.jivesoftware.community.JiveContentObject.Status;

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
	public void execute_authentication() {
		// case - unauthorized due anonymous
		{
			ContentAction tested = new ContentAction();
			mockAuthenticationProvider(tested, true, false, false);
			Assert.assertEquals("unauthorized", tested.execute());
		}

		// case - authorized, not anonymous and not group exists
		{
			ContentAction tested = new ContentAction();
			mockAuthenticationProvider(tested, false, false, false);
			Assert.assertEquals("badrequest", tested.execute());
		}

		// case - unauthorized, not anonymous but group exists and not in group
		{
			ContentAction tested = new ContentAction();
			mockAuthenticationProvider(tested, true, true, false);
			Assert.assertEquals("unauthorized", tested.execute());
		}

		// case - authorized, not anonymous and is in group
		{
			ContentAction tested = new ContentAction();
			mockAuthenticationProvider(tested, false, true, true);
			Assert.assertEquals("badrequest", tested.execute());
		}
	}

	@Test
	public void execute_validateError() {
		ContentAction tested = new ContentAction();
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
		Assert.assertEquals("badrequest", tested.execute());
		Assert.assertEquals("parameter 'type' is required\nparameter 'spaceId' is required", tested.getErrorMessage());
	}

	@Test
	public void execute_unknownCommunity() throws Exception {
		ContentAction tested = new ContentAction();
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
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
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
		tested.setType("forum");
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
		List<Long> list = new ArrayList<Long>();
		Mockito.when(bulkDataDAO.listForumThreads(1l, 123l)).thenReturn(list);

		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
		assertOutputContent("{ \"items\": []}", tested);
		Mockito.verify(bulkDataDAO).listForumThreads(1l, 123l);
		Mockito.verifyNoMoreInteractions(bulkDataDAO);
	}

	@Test
	public void execute_forum_withresults() throws Exception {
		ContentAction tested = new ContentAction();
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
		tested.setType("forum");
		tested.setMaxSize(3);
		tested.setSpaceId(1l);

		CommunityManager communityManagerMock = Mockito.mock(CommunityManager.class);
		Community secondSpaceMock = Mockito.mock(Community.class);
		Mockito.when(secondSpaceMock.getID()).thenReturn(1l);
		Mockito.when(communityManagerMock.getCommunity(1l)).thenReturn(secondSpaceMock);
		tested.setCommunityManager(communityManagerMock);

		BulkDataDAO bulkDataDAO = Mockito.mock(BulkDataDAO.class);
		tested.setBulkDataDAO(bulkDataDAO);
		List<Long> list = new ArrayList<Long>();
		list.add(10l);
		list.add(11l);
		Mockito.when(bulkDataDAO.listForumThreads(1l, null)).thenReturn(list);

		ForumManager documentManager = Mockito.mock(ForumManager.class);
		tested.setForumManager(documentManager);
		ForumThread doc1 = mockForumThread(10, Status.PUBLISHED);
		Mockito.when(documentManager.getForumThread(10l)).thenReturn(doc1);
		ForumThread doc2 = mockForumThread(11, Status.PUBLISHED);
		Mockito.when(documentManager.getForumThread(11l)).thenReturn(doc2);

		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
		assertOutputContent(
				"{ \"items\": [{\"id\":\"10\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\"},{\"id\":\"11\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\"}]}",
				tested);
	}

	private void assertOutputContent(String expected, ContentAction tested) throws IOException {
		Assert.assertNotNull(tested.getDataInputStream());
		Assert.assertEquals(expected, new String(IOUtils.toByteArray(tested.getDataInputStream()), "UTF-8"));
	}

	@Test
	public void getThreads() throws Exception {
		ContentAction tested = new ContentAction();
		tested.setMaxSize(3);
		tested.setUpdatedAfter(123l);

		Community space = Mockito.mock(Community.class);
		Mockito.when(space.getID()).thenReturn(1l);

		BulkDataDAO bulkDataDAO = Mockito.mock(BulkDataDAO.class);
		tested.setBulkDataDAO(bulkDataDAO);

		ForumManager documentManager = Mockito.mock(ForumManager.class);
		tested.setForumManager(documentManager);

		// case - emply list from DAO
		{
			List<Long> list = new ArrayList<Long>();
			Mockito.when(bulkDataDAO.listForumThreads(1l, 123l)).thenReturn(list);
			Assert.assertFalse(tested.getThreads(space).iterator().hasNext());
			Mockito.verifyZeroInteractions(documentManager);
		}

		// case - something returned from dao, exception handling from document details reading
		{
			Mockito.reset(bulkDataDAO, documentManager);
			List<Long> list = new ArrayList<Long>();
			list.add(10l);
			list.add(11l);
			list.add(12l);
			list.add(13l);
			list.add(14l);
			list.add(15l);
			list.add(16l);
			// next will be skipped because are after maxSize
			list.add(17l);
			list.add(18l);
			Mockito.when(bulkDataDAO.listForumThreads(1l, 123l)).thenReturn(list);

			ForumThread doc1 = mockForumThread(10, Status.PUBLISHED);
			Mockito.when(documentManager.getForumThread(10)).thenReturn(doc1);

			// this will be skipped
			ForumThread doc2 = mockForumThread(11, Status.DELETED);
			Mockito.when(documentManager.getForumThread(11)).thenReturn(doc2);

			ForumThread doc3 = mockForumThread(12, Status.PUBLISHED);
			Mockito.when(documentManager.getForumThread(12)).thenReturn(doc3);

			// these will be skipped
			Mockito.when(documentManager.getForumThread(13)).thenThrow(new UnauthorizedException());
			Mockito.when(documentManager.getForumThread(14)).thenThrow(new ForumThreadNotFoundException());
			Mockito.when(documentManager.getForumThread(15)).thenReturn(null);

			ForumThread doc4 = mockForumThread(16, Status.PUBLISHED);
			Mockito.when(documentManager.getForumThread(16)).thenReturn(doc4);

			List<ForumThread> ret = tested.getThreads(space);
			Assert.assertEquals(3, ret.size());
			Assert.assertEquals(doc1, ret.get(0));
			Assert.assertEquals(doc3, ret.get(1));
			Assert.assertEquals(doc4, ret.get(2));
			Mockito.verify(documentManager, Mockito.times(7)).getForumThread(Mockito.anyLong());
			Mockito.verifyNoMoreInteractions(documentManager);
		}
	}

	private ForumThread mockForumThread(long id, Status status) {
		ForumThread doc = Mockito.mock(ForumThread.class);
		Mockito.when(doc.getID()).thenReturn(id);
		Mockito.when(doc.getStatus()).thenReturn(status);
		return doc;
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

			List<UpdatedDocumentInfo> ret = tested.getDocuments(space);
			Assert.assertEquals(3, ret.size());
			Assert.assertEquals(doc1, ret.get(0).getDocument());
			Assert.assertEquals(doc3, ret.get(1).getDocument());
			Assert.assertEquals(doc4, ret.get(2).getDocument());
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
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
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
		mockAuthenticationProviderAndGlobalResourceResolver(tested);
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

		Assert.assertEquals("success", tested.execute());
		Assert.assertEquals("", tested.getErrorMessage());
		assertOutputContent(
				"{ \"items\": [{\"id\":\"10\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\",\"updated\":\"100\"},{\"id\":\"11\",\"url\":\"http://my.test.org/myobject\",\"content\":\"\",\"updated\":\"110\"}]}",
				tested);
	}

	public static void mockAuthenticationProviderAndGlobalResourceResolver(ActionBase tested) {
		mockAuthenticationProvider(tested, false, false, false);
		tested.setGlobalResourceResolver(JSONConverterHelperTest.mockGlobalResourceResolver());
	}

	public static void mockAuthenticationProvider(ActionBase tested, boolean anonymous, boolean groupExists,
			boolean isUserInGroup) {
		AuthenticationProvider ap = Mockito.mock(AuthenticationProvider.class);
		JiveAuthentication ja = Mockito.mock(JiveAuthentication.class);
		Mockito.when(ja.isAnonymous()).thenReturn(anonymous);
		Mockito.when(ap.getAuthentication()).thenReturn(ja);
		User user = Mockito.mock(User.class);
		Mockito.when(ap.getJiveUser()).thenReturn(user);
		tested.setAuthenticationProvider(ap);

		GroupManager groupManager = Mockito.mock(GroupManager.class);
		try {
			if (groupExists) {
				Group group = Mockito.mock(Group.class);
				if (isUserInGroup) {
					Mockito.when(group.isMember(Mockito.any(User.class))).thenReturn(true);
				} else {
					Mockito.when(group.isMember(Mockito.any(User.class))).thenReturn(false);
				}

				Mockito.when(groupManager.getGroup(ActionBase.SECURITY_GROUP_NAME)).thenReturn(group);
			} else {
				Mockito.when(groupManager.getGroup(ActionBase.SECURITY_GROUP_NAME)).thenThrow(new GroupNotFoundException());
			}
			tested.setGroupManager(groupManager);
		} catch (GroupNotFoundException e) {

		}
	}

}
