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
package com.likya.tlos.model;

import java.util.ArrayList;
import java.util.HashMap;
import com.likya.tlos.LocaleMessages;

public class TlosParameters {

	private boolean isPersistent = false;
	private String fileToPersist = "Tlos.recover"; //$NON-NLS-1$

	private TlosAuthorization tlosAuthorization;
	
	private ArrayList<String> emailDestinationList = new ArrayList<String>();
	private boolean useEncryption = false;
	private String emailUserName;
	private String emailPassword;
	private String smptServer;
	private int smtpPort;

	private ArrayList<Integer> statusListForMail;
	private boolean isMail = false;

	private boolean isSms = false;
	private String smsClassName;
	private ArrayList<Integer> statusListForSMS;
	
	private boolean useJobNamesForLog = false;
	private String addJobNamesForLog; 

	private boolean useGlobalLogPath = false;
	private String globalLogPath; 
	
	private String scenarioName = LocaleMessages.getString("TlosParameters.1"); //$NON-NLS-1$
	private String scenarioFile;
	private String logFile;
	private int schedulerFrequency;

	private int schedulerLowerThreshold;
	private int schedulerHigherThreshold;

	private int managementPort;

	private boolean isManagementEnabled = false;
	private boolean isJmxManagementEnabled = false;
	
	private String jmxIp;
	private int jmxPort;
	
	private String hostName;
	private int httpAccessPort;

	private boolean isNormalizable = false;

	private int[] scheduledDays;

	private HashMap<Integer, String> groupList = new HashMap<Integer, String>();
	
	private HashMap<Integer, String> dangerZoneGroup = new HashMap<Integer, String>();
	
	private String scenarioFileContent = ""; //$NON-NLS-1$
	
	private String configFileContent = ""; //$NON-NLS-1$
	
	private static String requestedFileName = ""; //$NON-NLS-1$
	
	private String[] jobCommand;
	
	private int logBufferSize;
	
	private int logPageSize;
	
	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	
	public int getHttpAccessPort() {
		return httpAccessPort;
	}

	public void setHttpAccessPort(int httpAccessPort) {
		this.httpAccessPort = httpAccessPort;
	}

	public int getManagementPort() {
		return managementPort;
	}

	public void setManagementPort(int managementPort) {
		this.managementPort = managementPort;
	}

	public int getSchedulerFrequency() {
		return schedulerFrequency;
	}

	public void setSchedulerFrequency(int schedulerFrequency) {
		this.schedulerFrequency = schedulerFrequency;
	}

	public String getScenarioFile() {
		return scenarioFile;
	}

	public void setScenarioFile(String scenarioFile) {
		this.scenarioFile = scenarioFile;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public String getEmailUserName() {
		return emailUserName;
	}

	public void setEmailUserName(String emailUserName) {
		this.emailUserName = emailUserName;
	}

	public String getEmailPassword() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getSmptServer() {
		return smptServer;
	}

	public void setSmptServer(String smptServer) {
		this.smptServer = smptServer;
	}

	public boolean isMail() {
		return isMail;
	}

	public void setMail(boolean isMail) {
		this.isMail = isMail;
	}

	public boolean isPersistent() {
		return isPersistent;
	}

	public void setPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
	}

	public String getFileToPersist() {
		return fileToPersist;
	}

	public void setFileToPersist(String fileToPersist) {
		this.fileToPersist = fileToPersist;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public ArrayList<Integer> getStatusListForMail() {
		return statusListForMail;
	}

	public void setStatusListForMail(ArrayList<Integer> statusListForMail) {
		this.statusListForMail = statusListForMail;
	}

	public int getSchedulerLowerThreshold() {
		return schedulerLowerThreshold;
	}

	public void setSchedulerLowerThreshold(int schedulerLowerThreshold) {
		this.schedulerLowerThreshold = schedulerLowerThreshold;
	}

	public int getSchedulerHigherThreshold() {
		return schedulerHigherThreshold;
	}

	public void setSchedulerHigherThreshold(int schedulerHigherThreshold) {
		this.schedulerHigherThreshold = schedulerHigherThreshold;
	}

	public boolean isNormalizable() {
		return isNormalizable;
	}

	public void setNormalizable(boolean isNormalizable) {
		this.isNormalizable = isNormalizable;
	}

	public int[] getScheduledDays() {
		return scheduledDays;
	}

	public void setScheduledDays(int[] scheduledDays) {
		this.scheduledDays = scheduledDays;
	}

	public boolean isSms() {
		return isSms;
	}

	public void setSms(boolean isSms) {
		this.isSms = isSms;
	}

	public String getSmsClassName() {
		return smsClassName;
	}

	public void setSmsClassName(String smsClassName) {
		this.smsClassName = smsClassName;
	}

	public ArrayList<Integer> getStatusListForSMS() {
		return statusListForSMS;
	}

	public void setStatusListForSMS(ArrayList<Integer> statusListForSMS) {
		this.statusListForSMS = statusListForSMS;
	}

	public ArrayList<String> getEmailDestinationList() {
		return emailDestinationList;
	}

	public void setEmailDestinationList(ArrayList<String> emailDestinationList) {
		this.emailDestinationList = emailDestinationList;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public boolean isUseJobNamesForLog() {
		return useJobNamesForLog;
	}

	public void setUseJobNamesForLog(boolean useJobNamesForLog) {
		this.useJobNamesForLog = useJobNamesForLog;
	}

	public String getAddJobNamesForLog() {
		return addJobNamesForLog;
	}

	public void setAddJobNamesForLog(String addJobNamesForLog) {
		this.addJobNamesForLog = addJobNamesForLog;
	}

	public boolean isUseGlobalLogPath() {
		return useGlobalLogPath;
	}

	public void setUseGlobalLogPath(boolean useGlobalLogPath) {
		this.useGlobalLogPath = useGlobalLogPath;
	}

	public String getGlobalLogPath() {
		return globalLogPath;
	}

	public void setGlobalLogPath(String globalLogPath) {
		this.globalLogPath = globalLogPath;
	}

	public boolean isManagementEnabled() {
		return isManagementEnabled;
	}

	public void setManagementEnabled(boolean isManagementEnabled) {
		this.isManagementEnabled = isManagementEnabled;
	}

	public boolean isJmxManagementEnabled() {
		return isJmxManagementEnabled;
	}

	public void setJmxManagementEnabled(boolean isJmxManagementEnabled) {
		this.isJmxManagementEnabled = isJmxManagementEnabled;
	}

	public TlosAuthorization getTlosAuthorization() {
		return tlosAuthorization;
	}

	public void setTlosAuthorization(TlosAuthorization tlosAuthorization) {
		this.tlosAuthorization = tlosAuthorization;
	}

	public String getJmxIp() {
		return jmxIp;
	}

	public void setJmxIp(String jmxIp) {
		this.jmxIp = jmxIp;
	}

	public int getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}

	public void setGroupList(HashMap<Integer, String> groupList) {
		this.groupList = groupList;
	}

	public HashMap<Integer, String> getGroupList() {
		return groupList;
	}

	public String getScenarioFileContent() {
		return scenarioFileContent;
	}

	public void setScenarioFileContent(String scenarioFileContent) {
		this.scenarioFileContent = scenarioFileContent;
	}

	public String getConfigFileContent() {
		return configFileContent;
	}

	public void setConfigFileContent(String configFileContent) {
		this.configFileContent = configFileContent;
	}

	public static void setRequestedFileName(String requestedFileName) {
		TlosParameters.requestedFileName = requestedFileName;
	}

	public static String getRequestedFileName() {
		return requestedFileName;
	}

	public boolean isUseEncryption() {
		return useEncryption;
	}

	public void setUseEncryption(boolean useEncryption) {
		this.useEncryption = useEncryption;
	}

	public synchronized String[] getJobCommand() {
		return jobCommand;
	}

	public synchronized void setJobCommand(String[] jobCommand) {
		this.jobCommand = jobCommand;
	}

	public int getLogBufferSize() {
		return logBufferSize;
	}

	public void setLogBufferSize(int logBufferSize) {
		this.logBufferSize = logBufferSize;
	}

	public int getLogPageSize() {
		return logPageSize;
	}

	public void setLogPageSize(int logPageSize) {
		if(logPageSize > 100) {
			this.logPageSize = 100;
		} else if(logPageSize < 20) { 
			this.logPageSize = 20;
		} else {
			this.logPageSize = logPageSize;
		}
	}

	public HashMap<Integer, String> getDangerZoneGroup() {
		return dangerZoneGroup;
	}

	public void setDangerZoneGroup(HashMap<Integer, String> dangerZoneGroup) {
		this.dangerZoneGroup = dangerZoneGroup;
	}

}
