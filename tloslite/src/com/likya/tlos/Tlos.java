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
package com.likya.tlos;

import com.likya.tlos.mail.TlosMailServer;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.sms.TlosSMSServer;
import com.likya.tlos.sms.helpers.TlosSMSHandler;
import com.likya.tlos.utils.ValidPlatforms;
import com.likya.tlos.utils.loaders.ConfigLoader;

public class Tlos {

	public static void main(String[] args) {

		String arg;
		int i = 0;
		while (i < args.length && args[i].startsWith("-")) { //$NON-NLS-1$

			arg = args[i++];
			
			if (arg.equals("-loadTest")) { //$NON-NLS-1$
				TlosServer.println(LocaleMessages.getString("Tlos.2")); //$NON-NLS-1$
				TlosServer.setLoadTest(true);
			}
		}

		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.5")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.6") + TlosServer.getVersion() + LocaleMessages.getString("Tlos.7")); //$NON-NLS-1$ //$NON-NLS-2$
		TlosServer.println(LocaleMessages.getString("Tlos.8")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.9") + " => " + Tlos.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		TlosServer.println();

		if(!ValidPlatforms.isOSValid()) {
			TlosServer.println(LocaleMessages.getString("Tlos.17") + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
			//System.exit(-1);
		}
		
		TlosServer.println(LocaleMessages.getString("Tlos.19")); //$NON-NLS-1$

		TlosServer.println(LocaleMessages.getString("Tlos.20")); //$NON-NLS-1$
		TlosParameters tlosParameters = new TlosParameters();
		if (!ConfigLoader.initSystemParameters(TlosServer.getConfigFileName(), tlosParameters)) {
			TlosServer.println(LocaleMessages.getString("Tlos.21")); //$NON-NLS-1$
			System.exit(-1);
		}
		TlosServer.setTlosParameters(tlosParameters);

		TlosServer.redirectLog(tlosParameters.getLogFile());

		TlosServer.println(LocaleMessages.getString("Tlos.22")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$

		// debug("Log parametreleri d�zenleniyor...");
		// if (!doLoggingIssues()) {
		// debug("Log parametreleri d�zenlenemedi ! Program sona erdi.");
		// System.exit(-1);
		// }
		// debugln("Tamam.");

		// Logger root = Logger.getRootLogger();
		// root.setLevel(Level.DEBUG);
		// schedulerLogger.fatal(Level.FATAL);
		// schedulerLogger.error(Level.ERROR);
		// schedulerLogger.warn(Level.WARN);
		// schedulerLogger.info(Level.INFO);
		// schedulerLogger.debug(Level.DEBUG);
		// schedulerLogger.trace(Level.TRACE);

		if (tlosParameters.isMail()) {
			// logger.log(Level.INFO, "E-posta ayarlar� d�zenleniyor...");
			TlosServer.println(LocaleMessages.getString("Tlos.24")); //$NON-NLS-1$
			// Eski mail arabirim
			// MailClient mailClientHandler = new MailClient(tlosParameters);
			TlosMailServer tlosMailServer = new TlosMailServer(tlosParameters);
			Thread tlosMailServerThread = new Thread(tlosMailServer);
			tlosMailServerThread.start();
			/*
			 * A�a��daki k�s�m, e-posta g�nderip g�nderemedi�ini test etmek i�in
			 * konumu�tu. �ifre bilgilerinin ve e-posta ayarlar�n� test etmek
			 * i�in e-posta atmay� denemek gerekiyor. Art�k bu i�lem, a��l��
			 * e-postas� g�nderimi s�ras�nda yap�l�yor. Serkan Ta� 29.06.2008
			 */
			// try {
			// mailClientHandler.postMail("Tlos:Haz�rlan�yor...", "Tlos e-posta
			// servisi �al��t�r�ld� !");
			// } catch (AuthenticationFailedException ae) {
			// TlosServer.println("E-posta sunucusuna ba�lanamad� ! G�venlik
			// hatas�... Program sona erdi. ");
			// System.exit(-1);
			// } catch (Exception e) {
			// TlosServer.println("E-posta ayarlar� y�klenemedi ! Program sona
			// erdi. ");
			// e.printStackTrace();
			// System.exit(-1);
			// }
			TlosServer.println(LocaleMessages.getString("Tlos.25")); //$NON-NLS-1$
			// TlosServer.setMailClientHandler(mailClientHandler);
			TlosServer.setTlosMailServer(tlosMailServer);
		}

		if (tlosParameters.isSms()) {
			
			TlosSMSHandler tlosSMSHandler = loadSMSLib(tlosParameters.getSmsClassName());
			
			if(tlosSMSHandler != null) {
				TlosServer.println(LocaleMessages.getString("Tlos.26")); //$NON-NLS-1$
				TlosSMSServer tlosSmsServer = new TlosSMSServer(tlosParameters, tlosSMSHandler);
				Thread tlosSmsServerThread = new Thread(tlosSmsServer);
				tlosSmsServerThread.start();
				TlosServer.println(LocaleMessages.getString("Tlos.27")); //$NON-NLS-1$
				TlosServer.setTlosSMSServer(tlosSmsServer);
			} else {
				TlosServer.println(LocaleMessages.getString("Tlos.28")); //$NON-NLS-1$
				tlosParameters.setSms(false);
			}
			
			if(tlosSMSHandler.getMsisdnList().size() <= 0) {
				TlosServer.println(LocaleMessages.getString("Tlos.29")); //$NON-NLS-1$
				TlosServer.getTlosParameters().setSms(false);
			}
		}
		
		// logger.log(Level.INFO, "Uygulama ba�lat�l�yor...");
		TlosServer.println(LocaleMessages.getString("Tlos.30")); //$NON-NLS-1$
		TlosServer tlosServer = new TlosServer();

		Thread tlosServerThread = new Thread(tlosServer);
		// likyaSchedulerThread.setDaemon(true);
		tlosServerThread.start();

		TlosServer.println(LocaleMessages.getString("Tlos.31")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
	}

	private static TlosSMSHandler loadSMSLib(String libName) {

		TlosSMSHandler tlosSMSHandler = null;

		try {
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.33") + libName); //$NON-NLS-1$
			@SuppressWarnings("rawtypes")
			Class handlerClass = Class.forName(libName);

			Object myObject = handlerClass.newInstance();

			if (myObject instanceof TlosSMSHandler) {
				tlosSMSHandler = (TlosSMSHandler) (myObject);
			}
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.34")); //$NON-NLS-1$
		} catch (Exception e) {
			System.out.println(LocaleMessages.getString("Tlos.35") + libName); //$NON-NLS-1$
			System.out.println(LocaleMessages.getString("Tlos.36")); //$NON-NLS-1$
			e.printStackTrace();
		}

		return tlosSMSHandler;
	}
}
