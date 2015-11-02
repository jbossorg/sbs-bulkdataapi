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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.jivesoftware.community.Community;
import com.jivesoftware.community.CommunityManager;

/**
 * Unit test for {@link SpacesAction}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SpacesActionTest {

	@Test
	public void execute_authentication() {
		Community rootCommunityMock = mockCommunity(10l);
		// case - unauthorized due anonymous
		{
			SpacesAction tested = new SpacesAction();
			ContentActionTest.mockAuthenticationProvider(tested, true, false, false);
			mockCommunityManager(tested, rootCommunityMock, null);
			Assert.assertEquals("unauthorized", tested.execute());
		}

		// case - authorized, not anonymous and not group exists
		{
			SpacesAction tested = new SpacesAction();
			ContentActionTest.mockAuthenticationProvider(tested, false, false, false);
			mockCommunityManager(tested, rootCommunityMock, null);
			Assert.assertEquals("success", tested.execute());
		}

		// case - unauthorized, not anonymous but group exists and not in group
		{
			SpacesAction tested = new SpacesAction();
			ContentActionTest.mockAuthenticationProvider(tested, true, true, false);
			mockCommunityManager(tested, rootCommunityMock, null);
			Assert.assertEquals("unauthorized", tested.execute());
		}

		// case - authorized, not anonymous and is in group
		{
			SpacesAction tested = new SpacesAction();
			ContentActionTest.mockAuthenticationProvider(tested, false, true, true);
			mockCommunityManager(tested, rootCommunityMock, null);
			Assert.assertEquals("success", tested.execute());
		}
	}

	@Test
	public void execute() throws IOException {
		SpacesAction tested = new SpacesAction();
		ContentActionTest.mockAuthenticationProviderAndGlobalResourceResolver(tested);

		Community rootCommunityMock = mockCommunity(10l);

		// case - null communities list returned - not NPE
		{
			mockCommunityManager(tested, rootCommunityMock, null);
			tested.execute();
			assertOutputContent("{ \"spaces\": []}", tested);
		}

		// case - empty communities list returned
		{
			List<Community> communityList = new ArrayList<Community>();
			mockCommunityManager(tested, rootCommunityMock, communityList);
			tested.execute();
			assertOutputContent("{ \"spaces\": []}", tested);
		}

		// case - one community returned
		{
			List<Community> communityList = new ArrayList<Community>();
			communityList.add(mockCommunity(20l));
			mockCommunityManager(tested, rootCommunityMock, communityList);
			tested.execute();
			assertOutputContent("{ \"spaces\": [\"20\"]}", tested);
		}

		// case - more communities returned
		{
			List<Community> communityList = new ArrayList<Community>();
			communityList.add(mockCommunity(20l));
			communityList.add(mockCommunity(30l));
			communityList.add(mockCommunity(256l));
			communityList.add(mockCommunity(20565l));
			mockCommunityManager(tested, rootCommunityMock, communityList);
			tested.execute();
			assertOutputContent("{ \"spaces\": [\"20\",\"30\",\"256\",\"20565\"]}", tested);
		}

	}

	private void mockCommunityManager(SpacesAction tested, Community rootCommunityMock, List<Community> communityList) {
		CommunityManager cm = Mockito.mock(CommunityManager.class);
		tested.setCommunityManager(cm);
		Mockito.when(cm.getRootCommunity()).thenReturn(rootCommunityMock);
		Mockito.when(cm.getRecursiveCommunities(rootCommunityMock)).thenReturn(communityList);
	}

	private Community mockCommunity(long id) {
		Community communityMock = Mockito.mock(Community.class);
		Mockito.when(communityMock.getID()).thenReturn(id);
		return communityMock;
	}

	private void assertOutputContent(String expected, SpacesAction tested) throws IOException {
		Assert.assertNotNull(tested.getDataInputStream());
		Assert.assertEquals(expected, new String(IOUtils.toByteArray(tested.getDataInputStream()), "UTF-8"));
	}

}
