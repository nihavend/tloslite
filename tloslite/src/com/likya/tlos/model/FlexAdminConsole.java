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
import java.util.Date;
import java.util.Iterator;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tloslite.model.FlexJob;
import com.likya.tloslite.model.TlosConnection;
import com.likya.tloslite.model.TlosData;
import com.likya.tloslite.model.TlosStates;
import com.likya.tloslite.model.TlosUser;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.utils.PasswordService;
import com.likya.tlos.utils.loaders.AuthorizationLoader;
import com.likya.tlos.utils.loaders.ScenarioLoader;

public class FlexAdminConsole implements FlexAdminConsoleMBean {

	private static final String tlosStatusMessageFirstLine = LocaleMessages.getString("FlexAdminConsole.0"); //$NON-NLS-1$
	private static final String tlosStatusMessageSecondLine = LocaleMessages.getString("FlexAdminConsole.1"); //$NON-NLS-1$

	public void checkTlosConnection() {
	}

	public TlosData showLogFile(TlosConnection tlosConnection, String jobId, Integer command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		TlosData tlosData = new TlosData();
		tlosData.setTlosId(tlosConnection.getId());
		tlosData.setTlosName(tlosConnection.getTlosName());

		String viewFile = ""; //$NON-NLS-1$
		String returnText = ""; //$NON-NLS-1$

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}

		if (command == TlosData.POPUP_JOBSCRIPT) {
			viewFile = TlosServer.getJobQueue().get(jobId).getJobProperties().getJobCommand();
			returnText = HtmlPages.getHtmlPageData(viewFile, false);
		} else if (command.equals(TlosData.POPUP_JOBLOG)) {
			viewFile = TlosServer.getJobQueue().get(jobId).getJobProperties().getLogFilePath();
			returnText = HtmlPages.getHtmlPageData(viewFile, false);
		} else if (command.equals(TlosData.POPUP_TLOSLOG)) {
			viewFile = TlosServer.getTlosParameters().getLogFile();
			returnText = HtmlPages.getHtmlPageData(viewFile, false);
		} else if (command.equals(TlosData.POPUP_SYSTEMLOG)) {
			viewFile = "TlosTrace.log"; //$NON-NLS-1$
			returnText = HtmlPages.getHtmlPageData(viewFile, false);
		} else if (command.equals(TlosData.POPUP_SCENARIOS)) {
			viewFile = TlosServer.getTlosParameters().getScenarioFile();
			returnText = HtmlPages.getXMLPageData(viewFile);
		} else if (command.equals(TlosData.POPUP_SYSTEMSETTINGS)) {
			viewFile = "TlosConfig.xml"; //$NON-NLS-1$
			returnText = HtmlPages.getXMLPageData(viewFile);
		}

		tlosData.setLogOrScriptText(returnText);
		tlosData.setTlosConnection(true);

		return tlosData;
	}

	public boolean authenticate(TlosUser user) {
		if (TlosServer.getExecutionState() == TlosInfo.STATE_STOP) {
			TlosServer.getLogger().warn(LocaleMessages.getString("FlexAdminConsole.2")); //$NON-NLS-1$
			return false;
		}
		String userName = TlosServer.getTlosParameters().getTlosAuthorization().getUserName();
		String password = TlosServer.getTlosParameters().getTlosAuthorization().getPassWord();
		try {
			if (user.getUserName().equals(userName) && (PasswordService.encrypt(user.getUserPassword()).equals(password))) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/*public TlosData filterByGroupId(TlosConnection tlosConnection, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		TlosData tlosData = new TlosData();
		tlosData.setTlosId(tlosConnection.getId());
		tlosData.setTlosName(tlosConnection.getTlosName());

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}

		ArrayList<String> menuGroup = new ArrayList<String>(TlosServer.getTlosParameters().getGroupList().values());

		ArrayList<FlexJob> flexJobList = new ArrayList<FlexJob>();
		FlexJob flexJob = new FlexJob();

		Iterator<Job> localIterator = TlosServer.getJobQueue().values().iterator();
		Job job;

		while (localIterator.hasNext()) {
			job = localIterator.next();
			int id = job.getJobProperties().getGroupId();
			String groupName = TlosServer.getTlosParameters().getGroupList().get(id);
			if (command.equals(FlexJob.FILTER_ALL) || groupName.equals(command)) {
				flexJob = changeJobType(job);
				resetButtons(flexJob);
				flexJobList.add(setButtons(flexJob));
			}
		}

		tlosData.setTlosName(TlosServer.getTlosParameters().getScenarioName());
		tlosData.setNumOfTotalJobs(TlosServer.getJobQueue().size());
		tlosData.setNumOfJobListed(flexJobList.size());
		tlosData.setTlosStatus(TlosServer.getExecutionState());
		if (tlosData.getTlosStatus() == TlosStates.STATE_SUSPENDED) {
			tlosData.setTlosStatusMessageFirstLine(tlosStatusMessageFirstLine);
			tlosData.setTlosStatusMessageSecondLine(tlosStatusMessageSecondLine);
		}
		// tlosData.setTotalWorkingTime(TlosServer.getTotalWorkingTime());
		tlosData.setTlosJobs(flexJobList);
		tlosData.setTlosMenuGroup(menuGroup);
		tlosData.setTlosConnection(true);

		return tlosData;
	}*/

	public TlosData refresh(TlosConnection tlosConnection, String command) {
		return init(tlosConnection, command);
	}

	public TlosData changePassword(TlosUser user, String newPassword) {
		TlosData tlosData = new TlosData();
		tlosData.setTlosConnection(true);

		try {
			if (user.getUserName().equals(TlosServer.getTlosParameters().getTlosAuthorization().getUserName()) && PasswordService.encrypt(user.getUserPassword()).equals(TlosServer.getTlosParameters().getTlosAuthorization().getPassWord())) {

				user.setUserPassword(newPassword);

				TlosServer.getAuthorizationList().get(user.getUserName()).setPassWord(PasswordService.encrypt(newPassword)); //$NON-NLS-1$
				AuthorizationLoader.persistAuthorizationList(TlosServer.getAuthorizationList());

				tlosData.setAuthenticated(true);

				return tlosData;
			} else {
				TlosServer.getLogger().warn(LocaleMessages.getString("FlexAdminConsole.39")); //$NON-NLS-1$
				return tlosData;
			}
		} catch (Exception e) {
			TlosServer.getLogger().warn(LocaleMessages.getString("FlexAdminConsole.40")); //$NON-NLS-1$
			return tlosData;
		}
	}

	public FlexJob changeJobType(Job job) {
		FlexJob flexJob = new FlexJob();
		flexJob.setKey(job.getJobProperties().getKey());

		String jobType;
		if (job.getJobProperties().isStandart()) {
			jobType = LocaleMessages.getString("ViewHandler.880");
			flexJob.setStandart(true);
			flexJob.setManuel(false);
		} else if (job.getJobProperties().isManuel()) {
			jobType = LocaleMessages.getString("ViewHandler.881");
			flexJob.setStandart(false);
			flexJob.setManuel(true);
		} else {
			jobType = LocaleMessages.getString("ViewHandler.882");
			flexJob.setStandart(false);
			flexJob.setManuel(false);
		}

		flexJob.setJobType(jobType);

		if (!(job instanceof ExternalProgram)) {
			flexJob.setStandart(false);
		}

		int id = job.getJobProperties().getGroupId();
		String groupName = TlosServer.getTlosParameters().getGroupList().get(id);

		flexJob.setGroupId(groupName);

		String jobcommandStr = job.getJobProperties().getJobCommand();
		flexJob.setJobCommand(jobcommandStr);

		String tmp = jobcommandStr;
		if (tmp.lastIndexOf(System.getProperty("file.separator")) > 0) { //$NON-NLS-1$
			tmp = tmp.substring(tmp.lastIndexOf(System.getProperty("file.separator")) + 1); //$NON-NLS-1$
		}
		if (tmp.indexOf('.') > 0) {
			tmp = tmp.substring(0, tmp.indexOf('.'));
		}

		if (job.getJobProperties().getJobType().toLowerCase().equals(ScenarioLoader.JAVA_PROCESS.toLowerCase())) {
			tmp = "TlosDependentJob"; //$NON-NLS-1$
		}

		flexJob.setJobCommandName(tmp);

		String logFile = job.getJobProperties().getLogFilePath();
		if (FileUtils.checkFile(logFile)) {
			flexJob.setLogExist(true);
		} else {
			flexJob.setLogExist(false);
		}

		flexJob.setLogFilePath(logFile);
		ArrayList<String> dependencyList = DependencyOperations.getDependencyJobKeys(job.getJobProperties().getJobDependencyInfoList());

		String jobDependencyList = ""; //$NON-NLS-1$
		Iterator<String> dependencyIterator = dependencyList.iterator();

		while (dependencyIterator.hasNext()) {
			jobDependencyList += dependencyIterator.next() + ","; //$NON-NLS-1$
		}

		flexJob.setJobDependencyArrayList(dependencyList);
		flexJob.setJobDependencyList(jobDependencyList);
		flexJob.setSafeRestart(job.getJobProperties().isSafeRestart());
		flexJob.setState(job.getJobProperties().getStatus());
		flexJob.setStatus(FlexJob.getStatusString(flexJob.getState()));
		flexJob.setPreviousStatusList(job.getJobProperties().getPreviousStatusList());
		flexJob.setRecentWorkDuration(job.getJobProperties().getRecentWorkDuration());
		flexJob.setWorkDuration(job.getJobProperties().getWorkDuration());

		Date executionDate = job.getJobProperties().getExecutionDate();

		if (executionDate == null) {
			flexJob.setExecutionDateStr("-"); //$NON-NLS-1$
		} else {
			flexJob.setExecutionDateStr(DateUtils.getDate(executionDate));
		}

		String workDuration = null;

		if (job.getJobProperties().getStatus() == JobProperties.WORKING || job.getJobProperties().getStatus() == JobProperties.TIMEOUT) {
			workDuration = DateUtils.getDuration(job.getJobProperties().getExecutionDate());
		} else {
			workDuration = job.getJobProperties().getWorkDuration();
		}

		if (workDuration == null) {
			workDuration = "-"; //$NON-NLS-1$
		}
		flexJob.setWorkDuration(workDuration);

		String dependencyListString = job.getDependencyListString(TlosServer.getJobQueue(), job.getJobProperties().getKey().toString(), true);
		flexJob.setDependencyListString(dependencyListString);
		flexJob.setNextTime(job.getJobProperties().getTime() == null ? "-" : DateUtils.getDate(job.getJobProperties().getTime()));
		flexJob.setStandart(job.getJobProperties().isStandart());
		flexJob.setManuel(job.getJobProperties().isManuel());

		String paramText = job.getJobProperties().getJobParamList();
		if (paramText != null && !paramText.equals("")) {
			flexJob.setParamList(paramText);
		}

		return flexJob;
	}

	public void resetButtons(FlexJob flexJob) {
		flexJob.setRetryButton(false);
		flexJob.setSetSuccessButton(false);
		flexJob.setSkipButton(false);
		flexJob.setStopButton(false);
		flexJob.setPauseButton(false);
		flexJob.setResumeButton(false);
		flexJob.setPauseButton(false);
		flexJob.setEnableButton(false);
		flexJob.setDisableButton(false);
	}

	public FlexJob setButtons(FlexJob flexJob) {
		if (flexJob.getState() != FlexJob.DISABLED && flexJob.isPausable() && flexJob.isStartable() && !flexJob.isManuel()) {
			// Disable işlemleri
			flexJob.setDisableButton(true);
			flexJob.setStartButton(true);
			flexJob.setPauseButton(true);
		} else if (flexJob.getState() == FlexJob.DISABLED) {
			// Enable işlemleri
			flexJob.setEnableButton(true);
		} else if ((flexJob.getState() == FlexJob.FAIL) || (flexJob.getState() == FlexJob.STOP)) {
			if (flexJob.isSafeRestart()) {
				flexJob.setRetryButton(true);
			}
			if (flexJob.isStandart()) {
				flexJob.setSetSuccessButton(true);
				flexJob.setSkipButton(true);
			}
		} else if (flexJob.isStopable()) {
			flexJob.setStopButton(true);
		} else if (flexJob.isPausable() && !flexJob.isStartable()) {
			flexJob.setPauseButton(true);
		} else if (flexJob.getState() == JobProperties.PAUSE) {
			flexJob.setResumeButton(true);
			flexJob.setSetSuccessButton(true);
			flexJob.setSkipButton(true);
		} else if (flexJob.isPausable() && flexJob.isStartable() && !flexJob.isManuel()) {
			flexJob.setStartButton(true);
			flexJob.setPauseButton(true);
		} else if (flexJob.isStartable()) {
			// Sadece manuel işler için kullanılıyor
			flexJob.setStartButton(true);
		}

		return flexJob;
	}

	public TlosData init(TlosConnection tlosConnection, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		TlosData tlosData = new TlosData();
		tlosData.setTlosId(tlosConnection.getId());
		tlosData.setTlosName(tlosConnection.getTlosName());
		tlosData.setIpAddress(tlosConnection.getIpAddress());
		tlosData.setPort(tlosConnection.getPort());

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		tlosData = prepareTlosData(command, tlosData);

		/**
		 * bu farklı şekilde yapılacak. Merve 18.03.2011
		 */
		// calculateTotalWorkingTime();

		return tlosData;
	}

	public TlosData prepareTlosData(String command, TlosData tlosData) {

		ArrayList<String> menuGroup = new ArrayList<String>(TlosServer.getTlosParameters().getGroupList().values());

		ArrayList<FlexJob> flexJobList = new ArrayList<FlexJob>();
		FlexJob flexJob = new FlexJob();

		Iterator<Job> localIterator = TlosServer.getJobQueue().values().iterator();

		if (command.equals(FlexJob.FILTER_ALL)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				resetButtons(flexJob);
				flexJobList.add(setButtons(flexJob));
			}
		} else if (command.equals(FlexJob.FILTER_READY)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.READY) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_WORKING)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.WORKING) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_SUCCESSFUL)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.SUCCESS) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_TIMEOUT)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.TIMEOUT) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_WAITING)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.WAITING) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_PAUSED)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.PAUSE) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_FAILED)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.FAIL) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_SKIPPED)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.SKIP) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_STOPPED)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.STOP) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		} else if (command.equals(FlexJob.FILTER_DISABLED)) {

			while (localIterator.hasNext()) {
				flexJob = changeJobType(localIterator.next());
				if (flexJob.getState() == JobProperties.DISABLED) {
					resetButtons(flexJob);
					flexJobList.add(setButtons(flexJob));
				}
			}
		}

		tlosData.setTlosName(TlosServer.getTlosParameters().getScenarioName());
		tlosData.setTlosVersion(TlosServer.getVersion());
		tlosData.setNumOfTotalJobs(TlosServer.getJobQueue().size());
		tlosData.setNumOfJobListed(flexJobList.size());
		tlosData.setTlosStatus(TlosServer.getExecutionState());
		if (tlosData.getTlosStatus() == TlosStates.STATE_SUSPENDED) {
			tlosData.setTlosStatusMessageFirstLine(tlosStatusMessageFirstLine);
			tlosData.setTlosStatusMessageSecondLine(tlosStatusMessageSecondLine);
		}
		// tlosData.setTotalWorkingTime(TlosServer.getTotalWorkingTime());
		tlosData.setTlosJobs(flexJobList);
		tlosData.setTlosMenuGroup(menuGroup);
		tlosData.setTlosConnection(true);

		return tlosData;
	}

	public void skipJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().skipJob(jobName);
		}
	}

	public void setSuccess(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().setSuccess(jobName);
		}
	}

	public void retryJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().retryExecution(jobName);
		}
	}

	public void resumeJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().resumeJob(jobName);
		}
	}

	public void pauseJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().pauseJob(jobName);
		}
	}

	public void stopJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().stopJob(jobName);
		}
	}

	public void startJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().startJob(jobName);
		}
	}

	public void enableJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().enableJob(jobName);
		}
	}

	public void disableJob(TlosConnection tlosConnection, String jobName, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		if (authenticate(user)) {
			TlosServer.getTlosCommInterface().disableJob(jobName);
		}
	}

	public TlosData resumeTlos(TlosUser user, String command) {
		TlosData tlosData = new TlosData();

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		TlosServer.getTlosCommInterface().resumeTlos();

		tlosData = prepareTlosData(command, tlosData);

		return tlosData;
	}

	public TlosData suspendTlos(TlosUser user, String command) {
		TlosData tlosData = new TlosData();

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		TlosServer.getTlosCommInterface().suspendTlos();

		tlosData = prepareTlosData(command, tlosData);

		return tlosData;
	}

	public TlosData forcefulShutDown(TlosUser user) {
		TlosData tlosData = new TlosData();

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		TlosServer.getTlosCommInterface().forceFullShutDown();

		tlosData.setTlosConnection(true);

		return tlosData;
	}

	public TlosData gracefulShutDown(TlosUser user) {
		TlosData tlosData = new TlosData();

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		TlosServer.getTlosCommInterface().gracefulShutDown();

		tlosData.setTlosConnection(true);

		return tlosData;
	}

	public TlosData setJobInputParam(TlosConnection tlosConnection, String jobName, String parameterList, String command) {
		TlosUser user = new TlosUser();
		user.setUserName(tlosConnection.getTlosUserName());
		user.setUserPassword(tlosConnection.getTlosPassword());

		TlosData tlosData = new TlosData();
		tlosData.setTlosId(tlosConnection.getId());
		tlosData.setTlosName(tlosConnection.getTlosName());

		if (authenticate(user)) {
			tlosData.setAuthenticated(true);
		} else {
			tlosData.setAuthenticated(false);
			return tlosData;
		}
		String popupText = TlosServer.getTlosCommInterface().setJobInputParam(jobName, parameterList);

		tlosData = prepareTlosData(command, tlosData);
		tlosData.setTlosConnection(true);
		tlosData.setLogOrScriptText(popupText);

		return tlosData;
	}

	/**
	 * Getter: get the "State" attribute of the "Test" standard MBean.
	 * 
	 * @return the current value of the "State" attribute.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Setter: set the "State" attribute of the "Test" standard MBean.
	 * 
	 * @param <VAR>s</VAR> the new value of the "State" attribute.
	 */
	public void setState(String s) {
		state = s;
		nbChanges++;
	}

	/**
	 * Getter: get the "NbChanges" attribute of the "Test" standard MBean.
	 * 
	 * @return the current value of the "NbChanges" attribute.
	 */
	public int getNbChanges() {
		return nbChanges;
	}

	/**
	 * Operation: reset to their initial values the "State" and "NbChanges"
	 * attributes of the "Test" standard MBean.
	 */
	public void reset() {
		// TODO Auto-generated method stub
		/*
		 * AttributeChangeNotification acn = new
		 * AttributeChangeNotification(this, 0, 0, "NbChanges reset",
		 * "NbChanges", "Integer", new Integer(nbChanges), new Integer(0));
		 * state = "initial state"; nbChanges = 0; nbResets++;
		 * sendNotification(acn);
		 */
	}

	/*
	 * ----------------------------------------------------- METHOD NOT EXPOSED
	 * FOR MANAGEMENT BY A JMX AGENT
	 * -----------------------------------------------------
	 */

	/**
	 * Return the "NbResets" property. This method is not a Getter in the JMX
	 * sense because it is not exposed in the "TestMBean" interface.
	 * 
	 * @return the current value of the "NbResets" property.
	 */
	public int getNbResets() {
		return nbResets;
	}

	/*
	 * ----------------------------------------------------- ATTRIBUTES
	 * ACCESSIBLE FOR MANAGEMENT BY A JMX AGENT
	 * -----------------------------------------------------
	 */

	private String state = "initial state"; //$NON-NLS-1$
	private int nbChanges = 0;

	/*
	 * ----------------------------------------------------- PROPERTY NOT
	 * ACCESSIBLE FOR MANAGEMENT BY A JMX AGENT
	 * -----------------------------------------------------
	 */

	private int nbResets = 0;
}
