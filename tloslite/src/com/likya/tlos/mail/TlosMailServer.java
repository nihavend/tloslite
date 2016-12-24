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
package com.likya.tlos.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.mail.helpers.MultipartMail;
import com.likya.tlos.mail.helpers.SimpleMail;
import com.likya.tlos.mail.helpers.TlosMail;
import com.likya.tlos.model.TlosParameters;

/**
 * @author vista
 * 
 */
public class TlosMailServer implements Runnable {

	private final int timeout = 10000;
	private boolean executePermission = true;
	private ArrayList<TlosMail> mailQueue = new ArrayList<TlosMail>();

	private Properties props;
	private Authenticator authenticator;

	private String userName;
	private String password;
	private String from;
	// private String to;

	TlosParameters tlosParameters;

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(userName, password);
		}

	}

	public TlosMailServer(TlosParameters tlosParameters) {

		this.tlosParameters = tlosParameters;

		this.props = System.getProperties();
		authenticator = new SMTPAuthenticator();

		this.userName = tlosParameters.getEmailUserName();
		this.password = tlosParameters.getEmailPassword();

		this.from = this.userName;
		// this.to = tlosParameters.getEmailDestination();

		// Setup mail server
		
		props.put("mail.smtp.host", tlosParameters.getSmptServer()); //$NON-NLS-1$
		props.put("mail.smtp.auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		props.put("mail.smtp.port", Integer.toString(tlosParameters.getSmtpPort())); //$NON-NLS-1$
		 
	}

	public void terminate(boolean forcedTerminate) {
		synchronized (this) {
			if (forcedTerminate) {
				mailQueue.clear();
			}
			this.executePermission = false;
		}
	}

	public void run() {
		while (executePermission || mailQueue.size() > 0) {
			while (mailQueue.size() > 0 && tlosParameters.getEmailDestinationList().size() > 0) {
				TlosMail tlosMail = (TlosMail) mailQueue.get(0);
				TlosServer.getLogger().debug(LocaleMessages.getString("TlosMailServer.4")); //$NON-NLS-1$

				Iterator<String> emailIterator = tlosParameters.getEmailDestinationList().iterator();
				while (emailIterator.hasNext()) {

					try {
						switch (tlosMail.getMAIL_TYPE()) {

						case TlosMail.SIMPLE:
							postMail(emailIterator.next(), ((SimpleMail) tlosMail).getMailSubject(), ((SimpleMail) tlosMail).getMailText());
							break;
						case TlosMail.WELCOME:
							postMultiPartMail(emailIterator.next(), ((MultipartMail) tlosMail).getMailSubject(), ((MultipartMail) tlosMail).getMultipart());
							break;
						case TlosMail.ENDOFCYCLE:
							postMultiPartMail(emailIterator.next(), ((MultipartMail) tlosMail).getMailSubject(), ((MultipartMail) tlosMail).getMultipart());
							break;
						}
					} catch (Exception e) {
						TlosServer.getLogger().info(LocaleMessages.getString("TlosMailServer.5") + e.getMessage() + LocaleMessages.getString("TlosMailServer.6")); //$NON-NLS-1$ //$NON-NLS-2$
						e.printStackTrace();
					}
				}
				if(!mailQueue.isEmpty()) {
					mailQueue.remove(0);
				}
			}
			try {
				// TlosServer.getLogger().debug("Mail server sleeping !");
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		TlosServer.getLogger().info(LocaleMessages.getString("TlosMailServer.7")); //$NON-NLS-1$
		TlosServer.getLogger().info(LocaleMessages.getString("TlosMailServer.8") + mailQueue.size()); //$NON-NLS-1$
	}

	private void postMultiPartMail(String to, String subject, Multipart multipart) throws MessagingException {

		// Get session
		Session session = Session.getDefaultInstance(props, authenticator);

		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);

		message.setSentDate(new Date());

		// Set the content for the message and transmit
		message.setContent(multipart);

		try {
		// Send message
			Transport.send(message);
		} catch(AuthenticationFailedException a) {
			TlosServer.getLogger().info("Hatalı kullanıcı adı veya şifre nedeni ile e-posta sunucusuna bağlanamadı !");
			TlosServer.getLogger().info(LocaleMessages.getString("TlosMailServer.5") + LocaleMessages.getString("TlosMailServer.6")); 
		} catch(MessagingException me) {
			TlosServer.getLogger().info(me.getLocalizedMessage());
			TlosServer.getLogger().info(LocaleMessages.getString("TlosMailServer.5") + LocaleMessages.getString("TlosMailServer.6")); 
		}
	}

	private void postMail(String to, String subject, String messageText) throws AddressException, MessagingException {

		// Get session
		Session session = Session.getDefaultInstance(props, authenticator);

		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(messageText);

		// Send message
		Transport.send(message);
	}

	private synchronized void addMail(TlosMail tlosMail) {
		mailQueue.add(tlosMail);
	}

	public void sendMail(TlosMail tlosMail) {
		addMail(tlosMail);
	}

	public int getQueueSize() {
		return mailQueue.size();
	}
}
