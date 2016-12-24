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

public class WelcomeMail extends MultipartMail {

	public WelcomeMail(HashMap<String, Job> jobQueue) {
		try {
			setMailSubject(LocaleMessages.getString("WelcomeMail.0") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("WelcomeMail.1")); //$NON-NLS-1$ //$NON-NLS-2$
			setMultipart(prepareWelcomeMail(jobQueue));
			setMAIL_TYPE(TlosMail.WELCOME);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private Multipart prepareWelcomeMail(HashMap<String, Job> jobQueue) throws MessagingException, URISyntaxException {

		// Create an "Alternative" Multipart message
		Multipart multipart = new MimeMultipart("alternative"); //$NON-NLS-1$

		MimeBodyPart mimeBodyPartPlain = new MimeBodyPart();
		// bp.setDataHandler(new DataHandler(new FileDataSource(filename)));

		// Düz metin
		String mailText = LocaleMessages.getString("WelcomeMail.3"); //$NON-NLS-1$
		mailText += LocaleMessages.getString("WelcomeMail.4"); //$NON-NLS-1$
		mailText += JobQueueOperations.getSimpleFormattedJobProperties(jobQueue);
		mailText += LocaleMessages.getString("WelcomeMail.5"); //$NON-NLS-1$
		mailText += LocaleMessages.getString("WelcomeMail.6"); //$NON-NLS-1$

		mimeBodyPartPlain.setDataHandler(new DataHandler(mailText, "text/plain; charset=UTF-8")); //$NON-NLS-1$
		multipart.addBodyPart(mimeBodyPartPlain);
		// Düz metin sonu

		MimeBodyPart mimeBodyPartHtml = new MimeBodyPart();
		String mailHtml = JobQueueOperations.getHTMLFormattedJobProperties(jobQueue);

		mimeBodyPartHtml.setContent(mailHtml, "text/html; charset=UTF-8 "); //$NON-NLS-1$

		multipart.addBodyPart(mimeBodyPartHtml);
		
		MimeBodyPart mimeBodyPartImage = new MimeBodyPart();
		String imgUrl = "/webroot/img/likya_mail.jpg"; //$NON-NLS-1$
		URL url = this.getClass().getResource(imgUrl);
		// new
		// URL("jar:file:/D:/tmp/LikyaTlosOrj.jar!/webroot/img/likya_mail.jpg")
		if (url == null) {
			TlosServer.getLogger().warn(LocaleMessages.getString("WelcomeMail.10") + imgUrl); //$NON-NLS-1$
		} else {
			try {
				mimeBodyPartImage.setDataHandler(new DataHandler(new ByteArrayDataSource(url.openStream(), "image/jpg"))); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//
			// String urlString = url.toString();
			// URI uri = new URI(urlString.replace("jar:file:/",
			// "jar:file://"));
			// try {
			// FileDataSource fds = new FileDataSource(new File(uri));
			// mimeBodyPartImage.setDataHandler(new DataHandler(fds));
			// mimeBodyPartImage.setFileName(fds.getName());
			// } catch(IllegalArgumentException il) {
			//			
			// }

			// FileDataSource fds = new FileDataSource(new
			// File(getClass().getResource("/webroot/img/likya_mail.jpg").getFile()));

			mimeBodyPartImage.setHeader("Content-ID", "<likyajpg10976@likyateknoloji.com>"); //$NON-NLS-1$ //$NON-NLS-2$
			multipart.addBodyPart(mimeBodyPartImage);
		}


		return multipart;

	}

}
