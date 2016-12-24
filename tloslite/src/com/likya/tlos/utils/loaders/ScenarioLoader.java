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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.jobs.ManuelExternalProgram;
import com.likya.tlos.jobs.RepetitiveExternalProgram;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.model.AutoRetryInfo;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.ScenarioLoaderUtil;
import com.likya.tlos.utils.Validation;

public class ScenarioLoader {

	public static final String UNDEFINED_VALUE = "yok"; //$NON-NLS-1$
	public static final String SYSTEM_PROCESS = "SYSTEM"; //$NON-NLS-1$
	public static final String JAVA_PROCESS = "JAVA"; //$NON-NLS-1$
	public static final String JOB_BASE_TYPE_STANDART = "S"; //$NON-NLS-1$
	public static final String JOB_BASE_TYPE_MANUEL = "M"; //$NON-NLS-1$
	private static final int MAX_INSTANCE = 50;

	private static final int JOB_GROUP = 0;
	private static final int COMMAND = 1;
	private static final int JOB_TYPE = 2;
	private static final int LOG_FILE_NAME = 3;
	private static final int DEPENDENCY_LIST = 4;
	private static final int JOB_BASE_TYPE = 5;
	private static final int EXECUTION_TIME = 6;
	private static final int TIME_OUT = 7;
	private static final int AUOT_RETRY = 8;
	private static final int EXCEPTION_RESULT_LIST = 9;
	private static final int SAFE_RESTART = 10;
	private static final int LOG_ANALYZE_STRING = 11;

	public static boolean readScenario(Logger schedulerLogger, TlosParameters tlosParameters, HashMap<String, Job> jobQueue) {
		return readScenario(schedulerLogger, tlosParameters, jobQueue, true);
	}

	public static boolean readScenario(Logger schedulerLogger, TlosParameters tlosParameters, HashMap<String, Job> jobQueue, boolean enableValidation) {
		// Read properties file.
		String strstr = LocaleMessages.getString("ScenarioLoader.3"); //$NON-NLS-1$
		schedulerLogger.info(strstr);
		schedulerLogger.info(LocaleMessages.getString("ScenarioLoader.4")); //$NON-NLS-1$
		String fileName = tlosParameters.getScenarioFile();

		Properties properties = new Properties();

		try {
			if (fileName == null) {
				System.out.println(LocaleMessages.getString("ScenarioLoader.5")); //$NON-NLS-1$
				return false;
			}
			File scenarioFile = new File(fileName);

			FileInputStream fileInputStream = new FileInputStream(scenarioFile);
			if (scenarioFile.getName().substring(scenarioFile.getName().lastIndexOf('.') + 1).toLowerCase().equals("xml")) { //$NON-NLS-1$
				properties.loadFromXML(fileInputStream);
				tlosParameters.setScenarioFileContent(HtmlPages.getXMLPageData(scenarioFile.toString()));
				fileInputStream.close();
			} else {
				properties.load(new FileInputStream(fileName));
			}

			String versionNumber = properties.get("version") == null ? "" : properties.get("version").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (!TlosServer.getVersion().equals(versionNumber)) {
				System.out.println(LocaleMessages.getString("ScenarioLoader.10") + versionNumber); //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("ScenarioLoader.11") + TlosServer.getVersion()); //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("ScenarioLoader.12")); //$NON-NLS-1$
				return false;
			}

			properties.remove("version"); //$NON-NLS-1$

			Enumeration<Object> keyEnums = properties.keys();

			int groupId = 1;
			int dangerZoneGroupId = 1;
			String groupName = ""; //$NON-NLS-1$

			while (keyEnums.hasMoreElements()) {

				JobProperties jobProperties = new JobProperties();
				boolean isStandart = true;
				boolean isManuel = false;

				String mixedKey = keyEnums.nextElement().toString();

				int openBraceIdx = mixedKey.indexOf('(');
				int closeBraceIdx = mixedKey.indexOf(')');
				int priorityIdx = mixedKey.indexOf('|');

				int countOfMine = -1;
				if ((openBraceIdx < 0 && closeBraceIdx < 0)) {
					// 
				} else if (openBraceIdx > 0 && closeBraceIdx > 0 && openBraceIdx < closeBraceIdx && mixedKey.substring(openBraceIdx + 1, closeBraceIdx).length() > 0) {
					countOfMine = Integer.parseInt(mixedKey.substring(openBraceIdx + 1, closeBraceIdx));
					if (countOfMine > MAX_INSTANCE) {
						System.err.println("instance can not be higher than " + MAX_INSTANCE + " but found " + countOfMine + " ! ");
						return false;
					}
				} else {
					System.err.println("(n) is required ! => " + mixedKey);
					return false;
				}

				if (countOfMine > 0 && priorityIdx < 0 && mixedKey.length() != closeBraceIdx + 1) {
					System.err.println("nothing required after ) but | => " + mixedKey);
					return false;
				}

				if (priorityIdx > 0 && closeBraceIdx > priorityIdx) {
					System.err.println("| is required after (n) ! => " + mixedKey);
					return false;
				}

				StringTokenizer keyString = new StringTokenizer(mixedKey, "|"); //$NON-NLS-1$

				ArrayList<String> keyList = new ArrayList<String>();
				while (keyString.hasMoreTokens()) {
					String currentToken = keyString.nextToken();
					keyList.add(currentToken);
				}

				int jobPriority = 99;

				if (keyList.size() == 2 && keyList.get(1) != null && (Integer.parseInt(keyList.get(1).toString()) > 0) && (Integer.parseInt(keyList.get(1).toString()) <= jobPriority)) {
					jobPriority = Integer.parseInt(keyList.get(1).toString());
				}

				String key = keyList.get(0);

				String jobPropertiesKey = properties.get(mixedKey).toString();

				// StringTokenizer st = new StringTokenizer(jobPropertiesKey.toString(), ","); //$NON-NLS-1$

				ArrayList<String> propertiesList = new ArrayList<String>();
				String tmpBuff = "[" + key + ":"; //$NON-NLS-1$ //$NON-NLS-2$

				String[] result = jobPropertiesKey.split("\\,");
				for (int paramIdx = 0; paramIdx < result.length; paramIdx++) {

					if (result[paramIdx].length() == 0) {
						schedulerLogger.error(key + ": eksik parametre girisi, " + (paramIdx + 1) + " numaralı parametre hatalı => {" + result[paramIdx] + "}");
						return false;
					}
					propertiesList.add(result[paramIdx]);
					tmpBuff += ", " + result[paramIdx]; //$NON-NLS-1$
				}

				StringTokenizer jobBaseTypeString = new StringTokenizer(propertiesList.get(JOB_BASE_TYPE), "|"); //$NON-NLS-1$

				ArrayList<String> jobBaseTypeList = new ArrayList<String>();
				while (jobBaseTypeString.hasMoreTokens()) {
					String currentToken = jobBaseTypeString.nextToken();
					jobBaseTypeList.add(currentToken);
				}

				String jobBaseTypeStr = jobBaseTypeList.get(0);
				if (jobBaseTypeStr == null) {
					schedulerLogger.error(key + ": eksik parametre girisi => " + jobBaseTypeStr);
					return false;
				}

				boolean isBlocker = true;

				if (isStandart && jobBaseTypeList.size() == 2 && jobBaseTypeList.get(1) != null) {
					isBlocker = Boolean.parseBoolean(jobBaseTypeList.get(1));
				}

				try {

					if (jobBaseTypeStr.equals(JOB_BASE_TYPE_STANDART)) {
						//
					} else if (jobBaseTypeStr.equals(JOB_BASE_TYPE_MANUEL)) {
						isManuel = true;
						isStandart = false;
					} else {
						long periodTime = Long.parseLong(jobBaseTypeStr);
						jobProperties.setPeriodTime(periodTime);
						isStandart = false;
					}

				} catch (Exception e) {
					// e.printStackTrace();
					schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.2")); //$NON-NLS-1$
					return false;
				}

				jobProperties.setStandart(isStandart);
				jobProperties.setManuel(isManuel);

				// Parse timer parameters
				if (!isManuel && (propertiesList.get(EXECUTION_TIME) == null || !ScenarioLoaderUtil.parseTimer(schedulerLogger, propertiesList.get(EXECUTION_TIME), jobProperties))) {
					schedulerLogger.error(key + ": eksik ya da hatalı parametre girisi, parametre  => {" + EXECUTION_TIME + "}");
					return false;
				}

				// Parse dependency list
				ArrayList<DependencyInfo> jobDependencyInfoList = ScenarioLoaderUtil.getDependencyList(propertiesList.get(DEPENDENCY_LIST), isManuel, isStandart);

				schedulerLogger.info(" " + tmpBuff + "]"); //$NON-NLS-1$ //$NON-NLS-2$

				String jobType = propertiesList.get(JOB_TYPE);

				String jobCommand = propertiesList.get(COMMAND);

				String jobCommandParameterStr = null;
				if (jobCommand != null && jobType.toUpperCase().equals(SYSTEM_PROCESS)) {

					int firstIdxOfQuota = jobCommand.indexOf('\"');
					int lastIdxOfQuota = jobCommand.lastIndexOf('\"');

					if (firstIdxOfQuota == -1) {
						if (jobCommand.split(" ").length > 1) {
							int idxOfPrm = jobCommand.indexOf(" ");
							jobCommandParameterStr = jobCommand.substring(idxOfPrm + 1);
							jobCommand = jobCommand.split(" ")[0];
						}
					} else if (firstIdxOfQuota != lastIdxOfQuota) {
						jobCommandParameterStr = jobCommand.substring(lastIdxOfQuota + 1).trim();
						jobCommand = jobCommand.substring(0, lastIdxOfQuota).replaceAll("\"", "");
					} else {
						schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.27") + key + LocaleMessages.getString("ScenarioLoader.28") + jobCommand); //$NON-NLS-1$ //$NON-NLS-2$
						schedulerLogger.fatal("Missing quota !"); //$NON-NLS-1$ //$NON-NLS-2$
						return false;
					}
					jobProperties.setJobParamList(jobCommandParameterStr);
					jobProperties.setJobParamListPerm(jobCommandParameterStr);
				}

				if (jobCommand != null && jobType.toUpperCase().equals(SYSTEM_PROCESS) && !FileUtils.checkFile(jobCommand) && enableValidation) {
					schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.27") + key + LocaleMessages.getString("ScenarioLoader.28") + jobCommand); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}

				String logFileName = null;
				if (tlosParameters.isUseJobNamesForLog()) {
					File f = new File(jobCommand);
					String fName = f.getName();
					if (fName != null) {
						logFileName = fName.substring(0, fName.lastIndexOf("."));
					} else {
						schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.27") + key + LocaleMessages.getString("ScenarioLoader.28") + jobCommand);
						return false;
					}
					if (!tlosParameters.getAddJobNamesForLog().equals("\\null")) { //$NON-NLS-1$
						logFileName += tlosParameters.getAddJobNamesForLog();
					}
				} else {
					logFileName = propertiesList.get(LOG_FILE_NAME);
				}

				if (!logFileName.equals(ScenarioLoader.UNDEFINED_VALUE) && tlosParameters.isUseGlobalLogPath()) {
					logFileName = tlosParameters.getGlobalLogPath() + File.separator + logFileName; //$NON-NLS-1$
				}

				int timeoutValue = 1000 * Integer.parseInt(propertiesList.get(TIME_OUT));

				// Parse autoRetryInfo
				AutoRetryInfo autoRetryInfo = ScenarioLoaderUtil.getAutoRetryInfo(propertiesList.get(AUOT_RETRY), isManuel, isStandart);

				String str = propertiesList.get(EXCEPTION_RESULT_LIST);
				if(UNDEFINED_VALUE.equals(str)) {
					// any return value means success
					jobProperties.setDiscardList(null);
				} else if (str != null) {
					jobProperties.setDiscardList(extractDiscardList(str));
				}

				jobProperties.setKey(key);
				
				StringTokenizer groupNameString = new StringTokenizer(propertiesList.get(JOB_GROUP), "|"); //$NON-NLS-1$

				ArrayList<String> groupNameList = new ArrayList<String>();
				while (groupNameString.hasMoreTokens()) {
					String currentToken = groupNameString.nextToken();
					groupNameList.add(currentToken);
				}

				groupName = groupNameList.get(0);
				
				if (groupName == null) {
					schedulerLogger.error(key + ": eksik parametre girisi => " + jobBaseTypeStr);
					return false;
				}
				
				if (!tlosParameters.getGroupList().containsValue(groupName)) {
					tlosParameters.getGroupList().put(groupId, groupName);
					groupId++;
				}
				
				if (groupNameList.size() == 2 && groupNameList.get(1) != null) {
					String dangerZoneGroup = groupNameList.get(1);
					if (!tlosParameters.getDangerZoneGroup().containsValue(dangerZoneGroup)) {
						tlosParameters.getDangerZoneGroup().put(dangerZoneGroupId, dangerZoneGroup);
						dangerZoneGroupId++; 
					}
					
					jobProperties.setDangerZoneGroup(dangerZoneGroup);
					jobProperties.setDangerZoneGroupId(getJobGroupId(tlosParameters.getDangerZoneGroup(), dangerZoneGroup));
				}

				jobProperties.setGroupName(groupName);
				jobProperties.setGroupId(getJobGroupId(tlosParameters.getGroupList(), groupName));
				jobProperties.setJobCommand(jobCommand);
				jobProperties.setJobType(jobType);
				jobProperties.setLogFilePath(logFileName);
				jobProperties.setJobDependencyInfoList(jobDependencyInfoList);
				jobProperties.setTimeout(timeoutValue);
				jobProperties.setAutoRetry(autoRetryInfo.isAutoRetry());
				jobProperties.setAutoRetryDelay(autoRetryInfo.getAutorRetryDelay());
				jobProperties.setAutoRetryCount(autoRetryInfo.getAutoRetryCount());
				jobProperties.setJobBaseType(jobBaseTypeStr);
				jobProperties.setBlocker(isBlocker);
				jobProperties.setPriority(jobPriority);

				jobProperties.setSafeRestart(Validation.getBooleanString("restartable", propertiesList.get(ScenarioLoader.SAFE_RESTART)));

				if (propertiesList.size() > LOG_ANALYZE_STRING && propertiesList.get(LOG_ANALYZE_STRING) != null) {
					if (!propertiesList.get(LOG_ANALYZE_STRING).toLowerCase().equals("\\null")) { //$NON-NLS-1$
						jobProperties.setLogAnalyzeString(propertiesList.get(LOG_ANALYZE_STRING));
					} else {
						jobProperties.setLogAnalyzeString(null);
					}
				} else {
					schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.41")); //$NON-NLS-1$
					return false;
				}

				int instanceCount = 1;

				if (countOfMine > 0) {
					instanceCount = countOfMine;
				}

				for (int i = 0; i < instanceCount; i++) {

					Job myJob = null;

					String tmpKey = key;

					if (instanceCount > 1) {
						tmpKey = "(" + i + ")";
						tmpKey = key.replace("(" + instanceCount + ")", tmpKey);
					}

					JobProperties cloneProperties = ObjectUtils.cloneJobProperties(jobProperties);

					cloneProperties.setKey(tmpKey);

					if (instanceCount > 1) {
						// set dynamic log names
						String tmpPath = null;
						String tmpExt = null;

						if (cloneProperties.getLogFilePath() != null && !cloneProperties.getLogFilePath().equals(ScenarioLoader.UNDEFINED_VALUE)) {
							if (cloneProperties.getLogFilePath().lastIndexOf(File.separator) > 0) {
								tmpPath = cloneProperties.getLogFilePath().substring(0, cloneProperties.getLogFilePath().lastIndexOf(File.separator) + 1);
							} else {
								tmpPath = "";
							}
							if (cloneProperties.getLogFilePath().lastIndexOf(".") > 0) {
								tmpExt = cloneProperties.getLogFilePath().substring(cloneProperties.getLogFilePath().lastIndexOf("."));
							} else {
								tmpExt = ".log";
							}
						} else {
							tmpPath = "";
							tmpExt = ".log";
						}

						tmpPath = tmpPath + key.replace('(', '-').replace(')', '-') + i + tmpExt;
						cloneProperties.setLogFilePath(tmpPath);
						cloneProperties.setJobParamList(key.replace('(', '-').replace(')', '-') + i + " " + (cloneProperties.getJobParamList() == null ? "" : cloneProperties.getJobParamList()));
						cloneProperties.setJobParamListPerm(cloneProperties.getJobParamList());
						// System.out.println(cloneProperties.getLogFilePath());
						// System.out.println(cloneProperties.getJobCommand());
					}

					if (isStandart) {
						myJob = new ExternalProgram(jobQueue, cloneProperties, tlosParameters.isMail(), tlosParameters.isSms());
					} else if (isManuel) {
						myJob = new ManuelExternalProgram(jobQueue, cloneProperties, tlosParameters.isMail(), tlosParameters.isSms());
					} else {
						if (jobDependencyInfoList.size() > 0) {
							cloneProperties.setBlocker(true);
						}
						myJob = new RepetitiveExternalProgram(jobQueue, cloneProperties, tlosParameters.isMail(), tlosParameters.isSms());
					}

					jobQueue.put(tmpKey, myJob);

				}

			}

			if (!DependencyOperations.validateDependencyList(schedulerLogger, jobQueue)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		schedulerLogger.info(LocaleMessages.getString("ScenarioLoader.42")); //$NON-NLS-1$
		schedulerLogger.info(LocaleMessages.getString("ScenarioLoader.3")); //$NON-NLS-1$
		return true;
	}

	public static int getJobGroupId(HashMap<Integer, String> group, String groupName) {

		Set<Integer> groupKeys = group.keySet();

		Iterator<Integer> groupIterator = groupKeys.iterator();
		int groupId;

		while (groupIterator.hasNext()) {
			groupId = groupIterator.next();
			if (group.get(groupId).equals(groupName)) {
				return groupId;
			}
		}

		return -1;
	}

	private static ArrayList<Integer> extractDiscardList(String discardString) {

		StringTokenizer discardStringTokens = new StringTokenizer(discardString, "|"); //$NON-NLS-1$

		ArrayList<Integer> discardList = new ArrayList<Integer>();

		while (discardStringTokens.hasMoreTokens()) {
			Integer currentToken = new Integer(discardStringTokens.nextToken());
			discardList.add(currentToken);
		}

		return discardList;
	}
}
