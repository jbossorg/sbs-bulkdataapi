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
import com.jivesoftware.community.JiveIterator;
import com.jivesoftware.community.impl.ListJiveIterator;

/**
 * Unit test for {@link SpacesAction}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SpacesActionTest {

	@Test
	public void execute() throws IOException {
		SpacesAction tested = new SpacesAction();

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
		tested.setCommunityManager(Mockito.mock(CommunityManager.class));
		Mockito.when(tested.communityManager.getRootCommunity()).thenReturn(rootCommunityMock);
		JiveIterator<Community> spaces = null;
		if (communityList != null) {
			spaces = new ListJiveIterator<Community>(communityList);
		}
		Mockito.when(tested.communityManager.getCommunities(rootCommunityMock)).thenReturn(spaces);
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
