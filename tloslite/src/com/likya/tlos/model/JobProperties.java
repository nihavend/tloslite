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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.likya.tlos.LocaleMessages;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.loaders.ScenarioLoader;

public class JobProperties implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int PROCESS_EXIT_RC_SUCCESS = 0;
	
	public static final int PREVIOUS_STATUS_HISTORY_DEPTH = 2; // in terms of status changes

	public static final int SUCSFAIL = -1;
	public static final int READY = 0;
	public static final int WAITING = 1;
	public static final int WORKING = 2;
	public static final int SUCCESS = 3;
	public static final int FAIL = 4;
	public static final int TIMEOUT = 5;
	public static final int SKIP = 6;
	public static final int STOP = 7;
	public static final int PAUSE = 8;
	public static final int RESUME = 9;
	/** Manually started */
	public static final int MSTART = 10;
	/** Disabled */
	public static final int DISABLED = 11;
	
	private int priority = 99;
	private String jobBaseType;
	private boolean blocker;
	private Object key;
	private int groupId;
	private String groupName;
	private int dangerZoneGroupId;
	private String dangerZoneGroup;
	private String jobCommand;
	private String jobParamListPerm = null;
	private String jobParamList = null;
	private String jobType;
	private String logFilePath;
	private ArrayList<DependencyInfo> dependencyInfoList;
	
	private Date jobPlannedStartTime;
	private Date jobPlannedEndTime;
	private Date time;
	private Date scenarioTime;
	private int timeout;
	private boolean autoRetry;
	private int autoRetryCount;
	private int autoRetryDelay;
	private ArrayList<Integer> discardList;
	private boolean safeRestart;
	private String logAnalyzeString;
	
	long periodTime;
	int processExitValue;
	public String previousTime = "-"; //$NON-NLS-1$

	public Date executionDate = null;
	public String completionDate = "-"; //$NON-NLS-1$
	public Date completionDateTime;
	public String workDuration = "-"; //$NON-NLS-1$
	public long workDurationNumeric = 0;

	public String recentWorkDuration = "-"; //$NON-NLS-1$
	public long recentWorkDurationNumeric = 0;
	
	private boolean standart = true;
	private boolean manuel = false;
	
	private StringBuffer messageBuffer = new StringBuffer();
	
	int status;
	private int subStatus;
	private int statusBeforeMstart;
	
	private ArrayList<Integer> previousStatusList = new ArrayList<Integer>();
	
	public JobProperties() {
	}

	/**
	 * Yeni kullanım public String getStatusString(int status) 
	 * Örnek : getStatusString(JobProperties.SUCCESS);
	 * 
	 * @return
	 */
	@Deprecated
	public String getStatusString() {
		return ObjectUtils.getStatusAsString(status);
	}

	/**
	 * 17.11.2008 Aşağıdaki kısım yerine, artık warning değerleri bir diziden
	 * okunacak.
	 * 
	 */

	public String getStatusString(int status, int returnCode) {
		String retValue = ObjectUtils.getStatusAsString(status);
		if ((status == SUCCESS) && (returnCode != JobProperties.PROCESS_EXIT_RC_SUCCESS) && inDiscardList(returnCode)) {
			retValue = retValue + LocaleMessages.getString("JobProperties.11") + returnCode + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return retValue;
	}

	public String getJobCommand() {
		return jobCommand;
	}

	public void setJobCommand(String jobCommand) {
		this.jobCommand = jobCommand;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getLogFilePath() {
		return logFilePath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	public ArrayList<DependencyInfo> getJobDependencyInfoList() {
		return dependencyInfoList;
	}

	public void setJobDependencyInfoList(ArrayList<DependencyInfo> dependencyInfoList) {
		this.dependencyInfoList = dependencyInfoList;
	}

	public int getStatus() {
		return status;
	}

	public synchronized void setStatus(int status) {
		if(previousStatusList.size() > PREVIOUS_STATUS_HISTORY_DEPTH) {
			this.previousStatusList.remove(0);
		}
		this.previousStatusList.add(new Integer(this.status));
		this.status = status;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {

		String dumpString = ""; //$NON-NLS-1$
		dumpString += LocaleMessages.getString("JobProperties.29") + key + " * "; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.31") + jobCommand + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.33") + jobType + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.35") + logFilePath + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.37") + dependencyInfoList + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.39") + status + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.41") + time + "]["; //$NON-NLS-1$ //$NON-NLS-2$
		dumpString += LocaleMessages.getString("JobProperties.43") + timeout + "]["; //$NON-NLS-1$ //$NON-NLS-2$

		return dumpString;
	}

	public Object getKey() {
		return key;
	}

	public boolean isAutoRetry() {
		return autoRetry;
	}

	public ArrayList<Integer> getPreviousStatusList() {
		return previousStatusList;
	}

	public String getExecutionDateStr() {
		// return executionDateStr;
		
		return this.executionDate == null ? "-" : DateUtils.getDate(this.executionDate); //$NON-NLS-1$
	}

	public String getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(String completionDate) {
		this.completionDate = completionDate;
	}

	public String getWorkDuration() {
		return workDuration;
	}

	public void setWorkDuration(String workDuration) {
		this.workDuration = workDuration;
	}

	public int getProcessExitValue() {
		return processExitValue;
	}

	public void setProcessExitValue(int processExitValue) {
		this.processExitValue = processExitValue;
	}

	public String getPreviousTime() {
		return previousTime;
	}

	public void setPreviousTime(String previousTime) {
		this.previousTime = previousTime;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public void setAutoRetry(boolean autoRetry) {
		this.autoRetry = autoRetry;
	}

	public ArrayList<Integer> getDiscardList() {
		return discardList;
	}

	public void setDiscardList(ArrayList<Integer> discardList) {
		this.discardList = discardList;
	}

	public boolean inDiscardList(int rc) {
		if (discardList == null || discardList.indexOf(rc) >= 0) {
			return true;
		}
		return false;
	}

	public Date getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public boolean isSafeRestart() {
		return safeRestart;
	}

	public void setSafeRestart(boolean safeRestart) {
		this.safeRestart = safeRestart;
	}

	public boolean isStopable() {
		if(getStatus() == JobProperties.WORKING || getStatus() == JobProperties.TIMEOUT) {
			return true;
		} 
		return false;
	}

	public boolean isPausable() {
		if((getStatus() == JobProperties.READY) || (getStatus() == JobProperties.WAITING)) {
			return true;
		} 		
		return false;
	}

	public boolean isStartable() {
		if((getStatus() == JobProperties.READY || getStatus() == JobProperties.SUCCESS) && (isManuel() || dependencyInfoList.size() > 0 && ((DependencyInfo)dependencyInfoList.get(0)).getJobKey().equals(ScenarioLoader.UNDEFINED_VALUE))) {
			return true;
		} else if(isManuel() && getStatus() == JobProperties.SUCCESS) {
			return true;
		}
		return false;
	}

	public Date getScenarioTime() {
		return scenarioTime;
	}

	public void setScenarioTime(Date scenarioTime) {
		this.scenarioTime = scenarioTime;
	}

	public String getLogAnalyzeString() {
		return logAnalyzeString;
	}

	public void setLogAnalyzeString(String logAnalyzeString) {
		this.logAnalyzeString = logAnalyzeString;
	}

	public long getWorkDurationNumeric() {
		return workDurationNumeric;
	}

	public void setWorkDurationNumeric(long workDurationNumeric) {
		this.workDurationNumeric = workDurationNumeric;
	}

	public Date getCompletionDateTime() {
		return completionDateTime;
	}

	public void setCompletionDateTime(Date completionDateTime) {
		this.completionDateTime = completionDateTime;
	}

	public long getPeriodTime() {
		return periodTime;
	}

	public void setPeriodTime(long periodTime) {
		this.periodTime = periodTime;
	}

	public String getJobBaseType() {
		return jobBaseType;
	}

	public void setJobBaseType(String jobBaseType) {
		this.jobBaseType = jobBaseType;
	}

	public Date getJobPlannedStartTime() {
		return jobPlannedStartTime;
	}

	public void setJobPlannedStartTime(Date jobPlannedStartTime) {
		this.jobPlannedStartTime = jobPlannedStartTime;
	}

	public Date getJobPlannedEndTime() {
		return jobPlannedEndTime;
	}

	public void setJobPlannedEndTime(Date jobPlannedEndTime) {
		this.jobPlannedEndTime = jobPlannedEndTime;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}

	public boolean isStandart() {
		return standart;
	}

	public void setStandart(boolean standart) {
		this.standart = standart;
	}

	public boolean isManuel() {
		return manuel;
	}

	public void setManuel(boolean manuel) {
		this.manuel = manuel;
	}

	public int getAutoRetryCount() {
		return autoRetryCount;
	}

	public void setAutoRetryCount(int autoRetryCount) {
		this.autoRetryCount = autoRetryCount;
	}

	public int getAutoRetryDelay() {
		return autoRetryDelay;
	}

	public void setAutoRetryDelay(int autoRetryDelay) {
		this.autoRetryDelay = autoRetryDelay;
	}

	public boolean isBlocker() {
		return blocker;
	}

	public void setBlocker(boolean blocker) {
		this.blocker = blocker;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getRecentWorkDuration() {
		return recentWorkDuration;
	}

	public void setRecentWorkDuration(String recentWorkDuration) {
		this.recentWorkDuration = recentWorkDuration;
	}

	public long getRecentWorkDurationNumeric() {
		return recentWorkDurationNumeric;
	}

	public void setRecentWorkDurationNumeric(long recentWorkDurationNumeric) {
		this.recentWorkDurationNumeric = recentWorkDurationNumeric;
	}

	public synchronized String getJobParamList() {
		return jobParamList;
	}

	public synchronized void setJobParamList(String jobParamList) {
		this.jobParamList = jobParamList;
	}

	public synchronized String getJobParamListPerm() {
		return jobParamListPerm;
	}

	public synchronized void setJobParamListPerm(String jobParamListPerm) {
		this.jobParamListPerm = jobParamListPerm;
	}

	public StringBuffer getMessageBuffer() {
		return messageBuffer;
	}

	public void setMessageBuffer(StringBuffer messageBuffer) {
		this.messageBuffer = messageBuffer;
	}

	public String getDangerZoneGroup() {
		return dangerZoneGroup;
	}

	public void setDangerZoneGroup(String dangerZoneGroup) {
		this.dangerZoneGroup = dangerZoneGroup;
	}

	public int getDangerZoneGroupId() {
		return dangerZoneGroupId;
	}

	public void setDangerZoneGroupId(int dangerZoneGroupId) {
		this.dangerZoneGroupId = dangerZoneGroupId;
	}

	public int getSubStatus() {
		return subStatus;
	}

	public void setSubStatus(int subStatus) {
		this.subStatus = subStatus;
	}

	public int getStatusBeforeMstart() {
		return statusBeforeMstart;
	}

	public void setStatusBeforeMstart(int statusBeforeMstart) {
		this.statusBeforeMstart = statusBeforeMstart;
	}

}
