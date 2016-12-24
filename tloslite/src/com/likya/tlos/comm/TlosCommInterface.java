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
package com.likya.tlos.comm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.TlosServerBase;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.jobs.ManuelExternalProgram;
import com.likya.tlos.jobs.RepetitiveExternalProgram;
import com.likya.tlos.mail.helpers.SimpleMail;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.sms.helpers.SMSType;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.ObjectUtils;

public class TlosCommInterface {

	private TlosServer tlosServer;

	public TlosCommInterface(TlosServer tlosServer) {
		this.tlosServer = tlosServer;
	}

	public void retryExecution(String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.0") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			if (myJob.getJobProperties().getStatus() == JobProperties.FAIL) {

				if (myJob instanceof ManuelExternalProgram) {
					myJob.getJobProperties().setStatus(JobProperties.MSTART);
				} else {
					if(!myJob.getJobProperties().getTime().before(Calendar.getInstance().getTime())) {
						TlosServer.getLogger().info("Fail eden job normalize edilmiş, bu nedenle retry öncesi çalışma saati son çalıştığa saate getirildi !");
						myJob.getJobProperties().setTime(myJob.getJobProperties().getExecutionDate());
					}
					myJob.getJobProperties().setStatus(JobProperties.READY);
				}

				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.1") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$

				if (myJob instanceof RepetitiveExternalProgram) {
					Thread starterThread = new Thread(myJob);
					myJob.setMyExecuter(starterThread);
					myJob.getMyExecuter().start();
				}
			}
		}
	}

	public void setSuccess(String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.3") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			if (myJob.getJobProperties().getStatus() == JobProperties.FAIL || myJob.getJobProperties().getStatus() == JobProperties.PAUSE) {
				if (myJob instanceof ExternalProgram) {
					DateUtils.iterateNextDate(myJob.getJobProperties());
				}
				myJob.getJobProperties().setStatus(JobProperties.SUCCESS);
				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.4") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void skipJob(String jobName) {
		skipJob(false, jobName);
	}

	public void skipJob(boolean isForced, String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.6") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();
			if (isForced || jobProperties.getStatus() == JobProperties.FAIL || jobProperties.getStatus() == JobProperties.PAUSE) {
				if (myJob instanceof ExternalProgram) {
					DateUtils.iterateNextDate(jobProperties);
				}
				myJob.getJobProperties().setStatus(JobProperties.SKIP);
				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.7") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void stopJob(String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.9") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();
			if (jobProperties.isStopable()) {
				myJob.stopMyDogBarking();
				myJob.getJobProperties().setStatus(JobProperties.STOP);
				Thread executerThread = myJob.getMyExecuter();
				if (executerThread != null) {
					myJob.getMyExecuter().interrupt();
					myJob.setMyExecuter(null);
				}
				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.10") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void pauseJob(String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.12") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();
			if (jobProperties.isPausable()) {
				myJob.getJobProperties().setStatus(JobProperties.PAUSE);
				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.13") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void resumeJob(String jobName) {
		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.15") + jobName); //$NON-NLS-1$
		if (TlosServer.getJobQueue().containsKey(jobName)) {
			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();
			if (jobProperties.getStatus() == JobProperties.PAUSE) {
				int previousArraySize = myJob.getJobProperties().getPreviousStatusList().size();
				int history = 0;
				if (previousArraySize > 1) {
					history = 1;
				}
				if (previousArraySize == 1) {
					myJob.getJobProperties().setStatus(myJob.getJobProperties().getPreviousStatusList().get(0));
				} else if (previousArraySize > 1) {
					myJob.getJobProperties().setStatus(myJob.getJobProperties().getPreviousStatusList().get(previousArraySize - history));
				}
				TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.16") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	public void startJob(String jobName) {

		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.18") + jobName); //$NON-NLS-1$

		if (TlosServer.getJobQueue().containsKey(jobName)) {

			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();

			if (myJob instanceof ExternalProgram) {
				if (jobProperties.isStartable()) {
					Date justNow = Calendar.getInstance().getTime();
					updateStartConditions(jobName, justNow);
					jobProperties.setTime(justNow);
					jobProperties.setStatusBeforeMstart(jobProperties.getStatus());
					jobProperties.setStatus(JobProperties.READY);
					jobProperties.setSubStatus(JobProperties.MSTART);
				}
			} else if (myJob instanceof ManuelExternalProgram) {
				if (jobProperties.isStartable()) {
					Date justNow = Calendar.getInstance().getTime();
					updateStartConditions(jobName, justNow);
					jobProperties.setTime(justNow);
					jobProperties.setStatus(JobProperties.MSTART);
				}
			} else {
				((RepetitiveExternalProgram) myJob).setForced(true);
			}

			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.19") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void disableJob(String jobName) {

		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.35") + jobName); //$NON-NLS-1$

		if (TlosServer.getJobQueue().containsKey(jobName)) {

			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();

			jobProperties.setStatus(JobProperties.DISABLED);

			synchronized (TlosServer.getDisabledJobQueue()) {
				TlosServer.getDisabledJobQueue().put(jobName, jobName);
			}
			
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.19") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void enableJob(String jobName) {

		TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.36") + jobName); //$NON-NLS-1$

		if (TlosServer.getJobQueue().containsKey(jobName)) {

			Job myJob = TlosServer.getJobQueue().get(jobName);
			JobProperties jobProperties = myJob.getJobProperties();

			if (myJob instanceof ExternalProgram && myJob.getJobProperties().getTime().before(Calendar.getInstance().getTime())) {
				DateUtils.iterateNextDate(myJob.getJobProperties());
			}

			jobProperties.setStatus(JobProperties.READY);

			synchronized (TlosServer.getDisabledJobQueue()) {
				TlosServer.getDisabledJobQueue().remove(jobName);
			}
			
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.36") + jobName + " : " + ObjectUtils.getStatusAsString(myJob.getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void updateStartConditions(String jobName, Date myDate) {
		ArrayList<Job> dependencyList = DependencyOperations.getDependencyList(TlosServer.getJobQueue(), jobName);
		if (dependencyList == null) {
			return;
		}
		Iterator<Job> dependencyListIterator = dependencyList.iterator();
		while (dependencyListIterator.hasNext()) {
			Job scheduledJob = dependencyListIterator.next();
			String tmpJobKey = scheduledJob.getJobProperties().getKey().toString();
			ArrayList<Job> tempJobList = DependencyOperations.getDependencyList(TlosServer.getJobQueue(), tmpJobKey);
			if ((tempJobList != null) && (tempJobList.size() > 0)) {
				updateStartConditions(tmpJobKey, myDate);
			}
			scheduledJob.getJobProperties().setTime(myDate);
		}
	}

	public void suspendTlos() {
		if (TlosServer.getExecutionState() == TlosInfo.STATE_WORKING) {
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.21")); //$NON-NLS-1$
			TlosServer.setExecutionState(TlosInfo.STATE_SUSPENDED);
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.22")); //$NON-NLS-1$
		}
	}

	public void resumeTlos() {
		if (TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) {
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.23")); //$NON-NLS-1$
			TlosServer.setExecutionState(TlosInfo.STATE_WORKING);
			TlosServer.getLogger().info(LocaleMessages.getString("TlosCommInterface.24")); //$NON-NLS-1$
		}
	}

	public void gracefulShutDown() {
		shutDown(false);
	}

	private void shutDown(boolean isForced) {
		if (TlosServer.getTlosParameters().isMail()) {
			if (isForced) {
				TlosServerBase.getTlosMailServer().sendMail(new SimpleMail(LocaleMessages.getString("TlosCommInterface.25") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosCommInterface.26"), LocaleMessages.getString("TlosCommInterface.27"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				TlosServerBase.getTlosMailServer().sendMail(new SimpleMail(LocaleMessages.getString("TlosCommInterface.28") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosCommInterface.29"), LocaleMessages.getString("TlosCommInterface.30"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			TlosServer.getTlosMailServer().terminate(isForced);
		}
		if (TlosServer.getTlosParameters().isSms()) {
			if (isForced) {
				TlosServerBase.getTlosSMSServer().sendSMS(new SMSType(LocaleMessages.getString("TlosCommInterface.31") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosCommInterface.32"))); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				TlosServerBase.getTlosSMSServer().sendSMS(new SMSType(LocaleMessages.getString("TlosCommInterface.33") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosCommInterface.34"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			TlosServer.getTlosSMSServer().terminate(isForced);
		}

		cleanUpRepeatatives();

		tlosServer.setExecutionPermission(false);

		TlosServer.setExecutionState(TlosInfo.STATE_STOP);
	}

	public void forceFullShutDown() {

		Iterator<Job> jobsIterator = TlosServer.getJobQueue().values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			Thread executerThread = scheduledJob.getMyExecuter();
			if (executerThread != null) {
				scheduledJob.getMyExecuter().interrupt();
			}
		}

		shutDown(true);

		return;

	}

	public void cleanUpRepeatatives() {

		Iterator<Job> jobsIterator = TlosServer.getJobQueue().values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (scheduledJob instanceof RepetitiveExternalProgram) {
				((RepetitiveExternalProgram) scheduledJob).setPeriodPermission(false);
				Thread executerThread = scheduledJob.getMyExecuter();
				if (executerThread != null) {
					scheduledJob.getMyExecuter().interrupt();
				}
			}
		}

		return;
	}

	public String setJobInputParam(String jobName, String parameterList) {
		String returnValue;

		Job myJob = TlosServer.getJobQueue().get(jobName);

		if (myJob == null || myJob.getJobProperties().getStatus() != JobProperties.READY) {
			TlosServer.print(LocaleMessages.getString("ViewHandler.874"));
			returnValue = LocaleMessages.getString("ViewHandler.874");
		} else {
			myJob.getJobProperties().setJobParamList(parameterList);
			returnValue = parameterList + " " + LocaleMessages.getString("ViewHandler.873")  + " " + jobName;
		}
		return returnValue;
	}
}
