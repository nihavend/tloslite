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
package com.likya.tloslite.model;

import java.io.Serializable;
import java.util.ArrayList;

public class FlexJob implements Serializable {

	private static final long serialVersionUID = -8705875000563448780L;

	public static final String FILTER_ALL = "List All";
	public static final String FILTER_READY = "Ready";
	public static final String FILTER_WORKING = "Working";
	public static final String FILTER_SUCCESSFUL = "Successful";
	public static final String FILTER_TIMEOUT = "Time-out";
	public static final String FILTER_WAITING = "Waiting";
	public static final String FILTER_PAUSED = "Paused";
	public static final String FILTER_FAILED = "Failed";
	public static final String FILTER_SKIPPED = "Skipped";
	public static final String FILTER_STOPPED = "Stopped";
	public static final String FILTER_DISABLED = "Disabled";

	private Object key;
	private String groupId;
	private String jobCommand;
	private String jobCommandName;
	private String jobType;
	private String logFilePath;
	private boolean logExist;
	private String jobDependencyList;
	private ArrayList<String> jobDependencyArrayList;
	private String dependencyListString;
	private boolean safeRestart;
	private String paramList;

	private int state;
	private String status;
	private ArrayList<Integer> previousStatusList = new ArrayList<Integer>();

	public String executionDateStr = "-";
	public String nextTime = "-";
	public String workDuration = "-";
	public String recentWorkDuration = "-";

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

	private boolean retryButton = false;
	private boolean setSuccessButton = false;
	private boolean skipButton = false;
	private boolean stopButton = false;
	private boolean pauseButton = false;
	private boolean resumeButton = false;
	private boolean startButton = false;
	private boolean enableButton = false;
	private boolean disableButton = false;

	private boolean standart = true;
	private boolean manuel = false;

	public static final String UNDEFINED_VALUE = "yok";

	public static String getStatusString(int state) {

		String retStr = "-";

		switch (state) {
		case READY:
			retStr = "READY";
			break;
		case WORKING:
			retStr = "WORKING";
			break;
		case WAITING:
			retStr = "WAITING";
			break;
		case SUCCESS:
			retStr = "SUCCESS";
			break;
		case FAIL:
			retStr = "FAIL";
			break;
		case TIMEOUT:
			retStr = "TIMEOUT";
			break;
		case SKIP:
			retStr = "SKIP";
			break;
		case STOP:
			retStr = "STOP";
			break;
		case PAUSE:
			retStr = "PAUSE";
			break;
		case RESUME:
			retStr = "RESUME";
			break;
		}

		return retStr;
	}

	public boolean isStopable() {
		if (getState() == FlexJob.WORKING || getState() == FlexJob.TIMEOUT) {
			return true;
		}
		return false;
	}

	public boolean isPausable() {
		if ((getState() == FlexJob.READY) || (getState() == FlexJob.WAITING)) {
			return true;
		}
		return false;
	}

	public boolean isStartable() {
		if ((getState() == FlexJob.READY || getState() == FlexJob.SUCCESS) && (isManuel() || getJobDependencyArrayList().size() > 0 && (getJobDependencyArrayList().get(0).equals(FlexJob.UNDEFINED_VALUE)))) {
			return true;
		} else if (isManuel() && getState() == FlexJob.SUCCESS) {
			return true;
		}
		return false;
	}

	public boolean isRetryButton() {
		return retryButton;
	}

	public void setRetryButton(boolean retryButton) {
		this.retryButton = retryButton;
	}

	public boolean isSetSuccessButton() {
		return setSuccessButton;
	}

	public void setSetSuccessButton(boolean setSuccessButton) {
		this.setSuccessButton = setSuccessButton;
	}

	public boolean isSkipButton() {
		return skipButton;
	}

	public void setSkipButton(boolean skipButton) {
		this.skipButton = skipButton;
	}

	public boolean isStopButton() {
		return stopButton;
	}

	public void setStopButton(boolean stopButton) {
		this.stopButton = stopButton;
	}

	public boolean isPauseButton() {
		return pauseButton;
	}

	public void setPauseButton(boolean pauseButton) {
		this.pauseButton = pauseButton;
	}

	public boolean isResumeButton() {
		return resumeButton;
	}

	public void setResumeButton(boolean resumeButton) {
		this.resumeButton = resumeButton;
	}

	public boolean isStartButton() {
		return startButton;
	}

	public void setStartButton(boolean startButton) {
		this.startButton = startButton;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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

	public String getJobDependencyList() {
		return jobDependencyList;
	}

	public void setJobDependencyList(String jobDependencyList) {
		this.jobDependencyList = jobDependencyList;
	}

	public boolean isSafeRestart() {
		return safeRestart;
	}

	public void setSafeRestart(boolean safeRestart) {
		this.safeRestart = safeRestart;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getWorkDuration() {
		return workDuration;
	}

	public void setWorkDuration(String workDuration) {
		this.workDuration = workDuration;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExecutionDateStr() {
		return executionDateStr;
	}

	public void setExecutionDateStr(String executionDateStr) {
		this.executionDateStr = executionDateStr;
	}

	public void setDependencyListString(String dependencyListString) {
		this.dependencyListString = dependencyListString;
	}

	public String getDependencyListString() {
		return dependencyListString;
	}

	public String getNextTime() {
		return nextTime;
	}

	public void setNextTime(String nextTime) {
		this.nextTime = nextTime;
	}

	public void setLogExist(boolean logExist) {
		this.logExist = logExist;
	}

	public boolean isLogExist() {
		return logExist;
	}

	public void setJobCommandName(String jobCommandName) {
		this.jobCommandName = jobCommandName;
	}

	public String getJobCommandName() {
		return jobCommandName;
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

	public String getRecentWorkDuration() {
		return recentWorkDuration;
	}

	public void setRecentWorkDuration(String recentWorkDuration) {
		this.recentWorkDuration = recentWorkDuration;
	}

	public boolean isEnableButton() {
		return enableButton;
	}

	public void setEnableButton(boolean enableButton) {
		this.enableButton = enableButton;
	}

	public boolean isDisableButton() {
		return disableButton;
	}

	public void setDisableButton(boolean disableButton) {
		this.disableButton = disableButton;
	}

	public String getParamList() {
		return paramList;
	}

	public void setParamList(String paramList) {
		this.paramList = paramList;
	}

	public ArrayList<String> getJobDependencyArrayList() {
		return jobDependencyArrayList;
	}

	public void setJobDependencyArrayList(ArrayList<String> jobDependencyArrayList) {
		this.jobDependencyArrayList = jobDependencyArrayList;
	}

	public ArrayList<Integer> getPreviousStatusList() {
		return previousStatusList;
	}

	public void setPreviousStatusList(ArrayList<Integer> previousStatusList) {
		this.previousStatusList = previousStatusList;
	}

}
