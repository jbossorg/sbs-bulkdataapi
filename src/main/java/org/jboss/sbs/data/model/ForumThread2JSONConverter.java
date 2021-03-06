/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.sbs.data.model;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.Question;
import com.jivesoftware.community.web.GlobalResourceResolver;
import org.jboss.sbs.data.action.IUserAccessor;

/**
 * ForumThread converter
 *
 * @author Libor Krzyzanek
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ForumThread2JSONConverter implements Content2JSONConverter<ForumThread> {

	@Override
	public void convert(StringBuilder sb, ForumThread thread, IUserAccessor userAccessor,
						GlobalResourceResolver resourceResolver) throws Exception {
		sb.append("{");
		JSONConverterHelper.appendCommonJiveContentObjecFields(sb, thread, null, resourceResolver);
		JSONConverterHelper.appendJSONField(sb, "title", thread.getName(), false);
		JSONConverterHelper.appendTags(sb, thread.getTagDelegator());
		JSONConverterHelper.appendThreadInfo(sb, thread);
		JSONConverterHelper.appendAuthors(sb, thread.getAuthors(), userAccessor);
		appendComments(sb, thread, userAccessor);
		sb.append("}");
	}

	/**
	 * Append 'comments' based on thread replies into JSON content.
	 *
	 * @param sb           to append comments into
	 * @param thread       to obtain comments from
	 * @param userAccessor user to access
	 * @throws TransformerException
	 * @throws IOException
	 */
	protected static void appendComments(StringBuilder sb, ForumThread thread, IUserAccessor userAccessor)
			throws Exception {
		if (thread.getMessageCount() > 1) {
			long rootMessageId = thread.getRootMessage().getID();
			Iterable<ForumMessage> messages = thread.getMessages();
			Question question = thread.getQuestion();
			sb.append(", ");
			JSONConverterHelper.appendJsonString(sb, "comments");
			sb.append(" : [");
			boolean first = true;
			for (ForumMessage message : messages) {
				if (message.getID() == rootMessageId)
					continue;
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append("{");
				JSONConverterHelper.appendJSONField(sb, "content", JSONConverterHelper.bodyToXmlString(message), true);
				JSONConverterHelper.appendAuthor(sb, message.getUser(), userAccessor);
				JSONConverterHelper.appendJSONField(sb, "published",
						JSONConverterHelper.convertDateValue(message.getCreationDate()), false);
				if (question != null) {
					JSONConverterHelper.appendJSONField(sb, "correct_answer", question.isCorrectAnswer(message), false);
					JSONConverterHelper.appendJSONField(sb, "helpful_answer", question.isHelpfulAnswer(message), false);
				}
				sb.append("}");
			}
			sb.append("]");
		}
	}

}
