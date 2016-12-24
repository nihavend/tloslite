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
package com.likya.tlos.utils.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.Tlos;
import com.likya.tlos.TlosServer;
import com.likya.tlos.encryption.LikyaEncryption;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.utils.CyclicStringBuilder;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.utils.Validation;

public class ConfigLoader {

	public static boolean initSystemParameters(String configFileName, TlosParameters tlosParameters) {
		Properties properties = new Properties();
		// TODO Bütün parametreler için geçerlilik kontrolü yapılacak.
		try {
			File configFile = new File(configFileName);

			if (configFile.getName().substring(configFile.getName().lastIndexOf('.') + 1).toLowerCase().equals("xml")) { //$NON-NLS-1$
				FileInputStream fileInputStream = new FileInputStream(configFileName);
				properties.loadFromXML(fileInputStream);
				tlosParameters.setConfigFileContent(HtmlPages.getXMLPageData(configFile.toString())); //$NON-NLS-1$
				fileInputStream.close();
			} else {
				properties.load(new FileInputStream(configFileName));
			}

			String versionNumber = properties.get("version") == null ? "" : properties.get("version").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (!TlosServer.getVersion().equals(versionNumber)) {
				System.out.println(LocaleMessages.getString("ConfigLoader.14") + versionNumber); //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("ConfigLoader.15") + TlosServer.getVersion()); //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("ConfigLoader.16")); //$NON-NLS-1$
				return false;
			}

			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.5")); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.6") + TlosServer.getVersion() + LocaleMessages.getString("Tlos.7")); //$NON-NLS-1$ //$NON-NLS-2$
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.8")); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("Tlos.9") + " => " + Tlos.class.getProtectionDomain().getCodeSource().getLocation().getFile());

			Object isPers = properties.get("ispersistent"); //$NON-NLS-1$

			if (isPers != null) {
				tlosParameters.setPersistent(Validation.getBooleanString("ispersistent", isPers.toString()));
			}

			Object isMail = properties.get("usemail"); //$NON-NLS-1$

			if (isMail != null) {
				tlosParameters.setMail(Validation.getBooleanString("usemail", isMail.toString()));
			}

			if (tlosParameters.isMail()) {
				Object setStatusListForMail = properties.get("statusListForMail"); //$NON-NLS-1$

				if (setStatusListForMail != null) {
					tlosParameters.setStatusListForMail(prepareStatusListForMail(setStatusListForMail.toString()));
				}

				Object emailListObject = properties.get("emaildestinationList"); //$NON-NLS-1$
				if (emailListObject != null) {

					StringTokenizer emailTokenizer = new StringTokenizer(emailListObject.toString(), ";"); //$NON-NLS-1$

					while (emailTokenizer.hasMoreTokens()) {
						tlosParameters.getEmailDestinationList().add(emailTokenizer.nextToken());
					}

					if (properties.get("useencryption") != null) {
						tlosParameters.setUseEncryption(Boolean.parseBoolean(properties.get("useencryption").toString()));
					}

					tlosParameters.setEmailUserName(properties.get("emailusername").toString()); //$NON-NLS-1$

					Object configEmailPassword = properties.get("emailpassword");

					if (configEmailPassword != null) {
						if (tlosParameters.isUseEncryption() && configEmailPassword.toString().length() > 0) {
							tlosParameters.setEmailPassword(LikyaEncryption.decryptPassword(configEmailPassword.toString())); //$NON-NLS-1$
						} else {
							tlosParameters.setEmailPassword(configEmailPassword.toString());
						}
					} else {
						tlosParameters.setEmailPassword(null);
					}

					Object smtpServer = properties.get("smtpserver"); //$NON-NLS-1$
					if (smtpServer == null || smtpServer == "") { //$NON-NLS-1$
						TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.10")); //$NON-NLS-1$
						return false;
					}

					tlosParameters.setSmptServer(smtpServer.toString());

					Object smtpPort = properties.get("smtpport"); //$NON-NLS-1$
					if (smtpPort != null && smtpPort != "") { //$NON-NLS-1$
						tlosParameters.setSmtpPort(Integer.parseInt(smtpPort.toString()));
					} else {
						TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.13")); //$NON-NLS-1$
						tlosParameters.setSmtpPort(25);
					}
					tlosParameters.setSmptServer(properties.get("smtpserver").toString()); //$NON-NLS-1$

					// TlosServer.getLogger().debug("Hedef E-posta Adresi : " +
					// tlosParameters.getEmailDestination());

				} else {
					tlosParameters.setMail(false);
				}

			}

			Object propertyObject = properties.get("usesms"); //$NON-NLS-1$

			if (propertyObject != null) {
				tlosParameters.setSms(Validation.getBooleanString("usesms", propertyObject.toString()));

				if (tlosParameters.isSms()) {
					propertyObject = properties.get("smsclassname"); //$NON-NLS-1$
					if (propertyObject != null) {
						tlosParameters.setSmsClassName("com.likya.tlos.sms." + propertyObject.toString()); //$NON-NLS-1$
						propertyObject = properties.get("statusListForSMS"); //$NON-NLS-1$
						if (propertyObject != null) {
							tlosParameters.setStatusListForSMS(prepareStatusListForSMS(propertyObject.toString()));
						}
					}
				}
			}

			if (properties.get("usejobnamesforlog") != null) {
				tlosParameters.setUseJobNamesForLog(Validation.getBooleanString("usejobnamesforlog", properties.get("usejobnamesforlog").toString()));

				if (tlosParameters.isUseJobNamesForLog()) {
					if (properties.get("addjobnamesforlog") != null || !properties.get("addjobnamesforlog").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						tlosParameters.setAddJobNamesForLog(properties.get("addjobnamesforlog").toString()); //$NON-NLS-1$
					}
				}
			}

			if (properties.get("usegloballogpath") != null) {
				tlosParameters.setUseGlobalLogPath(Validation.getBooleanString("usegloballogpath", properties.get("usegloballogpath").toString()));

				if (tlosParameters.isUseGlobalLogPath()) {
					if (properties.get("globallogpath") != null) { //$NON-NLS-1$
						tlosParameters.setUseGlobalLogPath(true);
						tlosParameters.setGlobalLogPath(properties.get("globallogpath").toString()); //$NON-NLS-1$
					}
				}
			}

			if (properties.get("scenarioName") != null) { //$NON-NLS-1$
				tlosParameters.setScenarioName(properties.get("scenarioName").toString()); //$NON-NLS-1$
			}
			tlosParameters.setScenarioFile(properties.get("scenarioFile").toString()); //$NON-NLS-1$
			String fileNameAndPath = properties.get("logfile").toString(); //$NON-NLS-1$
			tlosParameters.setLogFile(fileNameAndPath);

			tlosParameters.setSchedulerFrequency(1000 * Integer.parseInt(properties.get("schedulerFrequency").toString())); //$NON-NLS-1$

			int schedulerHigherThreshold = Integer.parseInt(properties.get("schedulerHigherThreshold").toString()); //$NON-NLS-1$
			int schedulerLowerThreshold = Integer.parseInt(properties.get("schedulerLowerThreshold").toString()); //$NON-NLS-1$

			if (schedulerHigherThreshold < 0 || schedulerLowerThreshold < 0) {
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.42") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.43") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				schedulerHigherThreshold = 9;
				schedulerLowerThreshold = 1;
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.45") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.46") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (schedulerHigherThreshold == schedulerLowerThreshold) {
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.42") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.43") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (schedulerLowerThreshold >= 1) {
					--schedulerLowerThreshold;
				} else {
					++schedulerLowerThreshold;
				}
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.45") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.46") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (schedulerHigherThreshold < schedulerLowerThreshold) {
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.42") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.43") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				schedulerHigherThreshold = schedulerLowerThreshold + 1;
				TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.45") + schedulerLowerThreshold + LocaleMessages.getString("ConfigLoader.46") + schedulerHigherThreshold + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			tlosParameters.setSchedulerLowerThreshold(schedulerLowerThreshold);
			tlosParameters.setSchedulerHigherThreshold(schedulerHigherThreshold);

			Object isJmxManagementEnabled = properties.get("useJmxManagement"); //$NON-NLS-1$

			if (isJmxManagementEnabled != null) {
				tlosParameters.setJmxManagementEnabled(Validation.getBooleanString("useJmxManagement", isJmxManagementEnabled.toString()));
			}

			if (tlosParameters.isJmxManagementEnabled()) {
				Object jmxPort = properties.get("jmxPort"); //$NON-NLS-1$

				if (jmxPort != null) {
					try {
						tlosParameters.setJmxPort(Integer.parseInt(jmxPort.toString()));
					} catch (NumberFormatException ne) {
						TlosServer.getLogger().info("\"jmxPort\" parametresi sadece tam sayi degerleri alabilir !"); //$NON-NLS-1$
						System.exit(2);
					}

				}

				Object jmxIp = properties.get("jmxIp"); //$NON-NLS-1$

				if (jmxIp != null) {
					tlosParameters.setJmxIp(jmxIp.toString());
				}
			}

			Object isManagementEnabled = properties.get("useManagement"); //$NON-NLS-1$

			if (isManagementEnabled != null) {
				tlosParameters.setManagementEnabled(Validation.getBooleanString("useManagement", isManagementEnabled.toString()));
			}

			if (tlosParameters.isManagementEnabled()) {
				Object managementPort = properties.get("managementPort"); //$NON-NLS-1$

				if (managementPort != null) {

					try {
						tlosParameters.setManagementPort(Integer.parseInt(managementPort.toString()));
					} catch (NumberFormatException ne) {
						TlosServer.getLogger().info("\"managementPort\" parametresi sadece tam sayi degerleri alabilir !"); //$NON-NLS-1$
						System.exit(2);
					}
				}
			}

			tlosParameters.setHostName(properties.get("httpHostName").toString()); //$NON-NLS-1$

			try {
				tlosParameters.setHttpAccessPort(Integer.parseInt(properties.get("httpAccessPort").toString())); //$NON-NLS-1$
			} catch (NumberFormatException ne) {
				TlosServer.getLogger().info("\"httpAccessPort\" parametresi sadece tam sayi degerleri alabilir !"); //$NON-NLS-1$
				System.exit(2);
			}

			Object normalizeStr = properties.get("normalize"); //$NON-NLS-1$
			if (normalizeStr != null) {
				tlosParameters.setNormalizable(Validation.getBooleanString("normalize", normalizeStr.toString()));
			}

			Object scheduledDays = properties.get("scheduledDays"); //$NON-NLS-1$

			if (scheduledDays != null) {
				tlosParameters.setScheduledDays(prepareScheduledDays(scheduledDays.toString()));
			} else {
				tlosParameters.setScheduledDays(prepareScheduledDays("1,2,3,4,5,6,7")); //$NON-NLS-1$
			}

			Object jobCommand = properties.get("jobCommand");
			if (jobCommand != null && jobCommand != "") {
				tlosParameters.setJobCommand(jobCommand.toString().split(" "));
			} else {
				tlosParameters.setJobCommand(null);
			}

			Object logBufferSize = properties.get("logbuffersize"); //$NON-NLS-1$
			if (logBufferSize != null && logBufferSize != "") { //$NON-NLS-1$
				tlosParameters.setLogBufferSize(Integer.parseInt(logBufferSize.toString()));
			} else {
				tlosParameters.setLogBufferSize(CyclicStringBuilder.getMAX_LENGTH());
			}

			Object logPageSize = properties.get("logpagesize"); //$NON-NLS-1$
			if (logPageSize != null && logPageSize != "") { //$NON-NLS-1$
				tlosParameters.setLogPageSize(Integer.parseInt(logPageSize.toString()));
			} else {
				tlosParameters.setLogPageSize(20);
			}

			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.1") + tlosParameters.getScenarioName()); //$NON-NLS-1$

			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.59") + tlosParameters.getScenarioFile()); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.60") + tlosParameters.getLogFile()); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.61") + new SimpleDateFormat("ss").format(tlosParameters.getSchedulerFrequency()) + LocaleMessages.getString("ConfigLoader.63")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.0") + tlosParameters.getManagementPort()); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.65") + tlosParameters.getHostName()); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.66") + tlosParameters.getHttpAccessPort()); //$NON-NLS-1$

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private static ArrayList<Integer> prepareStatusListForSMS(String sendStatusReportMailFor) {
		return prepareStatusList(sendStatusReportMailFor);
	}

	private static ArrayList<Integer> prepareStatusListForMail(String sendStatusReportMailFor) {
		return prepareStatusList(sendStatusReportMailFor);
	}

	private static ArrayList<Integer> prepareStatusList(String sendStatusReportMailFor) {

		ArrayList<Integer> statusList = new ArrayList<Integer>();
		StringTokenizer statusTokenizer = new StringTokenizer(sendStatusReportMailFor, ","); //$NON-NLS-1$

		while (statusTokenizer.hasMoreTokens()) {
			String currentToken = statusTokenizer.nextToken();
			try {
				int status = Integer.parseInt(currentToken.trim());
				if (status > 1 && status < 6) {
					statusList.add(new Integer(status));
				} else {
					System.out.println(status + LocaleMessages.getString("ConfigLoader.2")); //$NON-NLS-1$
					System.out.println(LocaleMessages.getString("ConfigLoader.3")); //$NON-NLS-1$
					System.out.println(LocaleMessages.getString("ConfigLoader.4")); //$NON-NLS-1$
					System.out.println(LocaleMessages.getString("ConfigLoader.5")); //$NON-NLS-1$
					System.out.println(LocaleMessages.getString("ConfigLoader.6")); //$NON-NLS-1$
					System.exit(2);
				}
			} catch (NumberFormatException ne) {
				System.out.println(LocaleMessages.getString("ConfigLoader.7")); //$NON-NLS-1$
				System.exit(2);
			}
		}

		return statusList.size() == 0 ? null : statusList;
	}

	private static int[] prepareScheduledDays(String scheduledDaysList) {

		StringTokenizer scheduledDaysTokenizer = new StringTokenizer(scheduledDaysList, ","); //$NON-NLS-1$

		int[] tmp = new int[scheduledDaysTokenizer.countTokens()];
		int counter = 0;
		while (counter < tmp.length) {

			try {
				String currentToken = scheduledDaysTokenizer.nextToken();
				int dayOfWeek = Integer.parseInt(currentToken);
				if (dayOfWeek > 0 && dayOfWeek < 8) {
					tmp[counter++] = dayOfWeek;
				} else {
					TlosServer.getLogger().info(LocaleMessages.getString("ConfigLoader.9")); //$NON-NLS-1$
				}
			} catch (NumberFormatException ne) {
				TlosServer.getLogger().warn(LocaleMessages.getString("ConfigLoader.11")); //$NON-NLS-1$
			}
		}

		if (tmp.length == 0) {
			TlosServer.getLogger().error(LocaleMessages.getString("ConfigLoader.9")); //$NON-NLS-1$
			System.exit(2);
		}
		return tmp;
	}
}
