/*******************************************************************************
 * Copyright 2014 Likya Teknoloji
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.likya.tlos.mail.helpers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.utils.JobQueueOperations;

public class EndOfCycleMail extends MultipartMail {

	public EndOfCycleMail(HashMap<String, Job> jobQueue) {
		try {
			setMailSubject(LocaleMessages.getString("EndOfCycleMail.0") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("EndOfCycleMail.1")); //$NON-NLS-1$ //$NON-NLS-2$
			setMultipart(prepareEndOfCycleMail(jobQueue));
			setMAIL_TYPE(TlosMail.ENDOFCYCLE);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private Multipart prepareEndOfCycleMail(HashMap<String, Job> jobQueue) throws MessagingException, URISyntaxException {

		// mp.setSubType("related");

		// Create an "Alternative" Multipart message
		Multipart multipart = new MimeMultipart("alternative"); //$NON-NLS-1$

		MimeBodyPart mimeBodyPartPlain = new MimeBodyPart();
		// bp.setDataHandler(new DataHandler(new FileDataSource(filename)));

		// Düz metin
		String mailText = LocaleMessages.getString("EndOfCycleMail.2"); //$NON-NLS-1$
		mailText += LocaleMessages.getString("EndOfCycleMail.3"); //$NON-NLS-1$
		mailText += JobQueueOperations.getSimpleFormattedJobProperties(jobQueue);
		mailText += LocaleMessages.getString("EndOfCycleMail.4"); //$NON-NLS-1$
		mailText += LocaleMessages.getString("EndOfCycleMail.6"); //$NON-NLS-1$

		mimeBodyPartPlain.setDataHandler(new DataHandler(mailText, "text/plain; charset=UTF-8 ")); //$NON-NLS-1$
		multipart.addBodyPart(mimeBodyPartPlain);
		// Düz metin sonu

		MimeBodyPart mimeBodyPartHtml = new MimeBodyPart();
		String localizedMessage = LocaleMessages.getString("EndOfCycleMail.8"); //$NON-NLS-1$
		String mailHtml = JobQueueOperations.getHTMLFormattedJobProperties(jobQueue, localizedMessage);

		mimeBodyPartHtml.setContent(mailHtml, "text/html; charset=UTF-8 "); //$NON-NLS-1$

		MimeBodyPart mimeBodyPartImage = new MimeBodyPart();
		URL url = this.getClass().getResource("/webroot/img/likya_mail.jpg"); //$NON-NLS-1$
		
		try {
			mimeBodyPartImage.setDataHandler(new DataHandler(new ByteArrayDataSource(url.openStream(), "image/jpg"))); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		FileDataSource fds = new FileDataSource(new File(url.toURI()));
//
//		mimeBodyPartImage.setDataHandler(new DataHandler(fds));
//		mimeBodyPartImage.setFileName(fds.getName());
		mimeBodyPartImage.setHeader("Content-ID", "<likyajpg10976@likyateknoloji.com>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		multipart.addBodyPart(mimeBodyPartHtml);
		multipart.addBodyPart(mimeBodyPartImage);

		return multipart;

	}

}
