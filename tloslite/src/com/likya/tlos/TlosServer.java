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

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.likya.tlos.comm.TlosCommInterface;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.jobs.ManuelExternalProgram;
import com.likya.tlos.jobs.RepetitiveExternalProgram;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.mail.helpers.EndOfCycleMail;
import com.likya.tlos.mail.helpers.WelcomeMail;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.ScenarioRuntimeProperties;
import com.likya.tlos.model.TlosAuthorization;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.sms.helpers.SMSType;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.JmxManagementConsole;
import com.likya.tlos.utils.JobQueueOperations;
import com.likya.tlos.utils.ManagementConsole;
import com.likya.tlos.utils.PasswordService;
import com.likya.tlos.utils.loaders.AuthorizationLoader;
import com.likya.tlos.utils.loaders.ScenarioLoader;
import com.likya.tlos.utils.sort.SortType;
import com.likya.tlos.web.TlosWebConsole;

/**
 * @author vista
 * 
 */
public class TlosServer extends TlosServerBase {

	public TlosServer() {

		jobQueue = new HashMap<String, Job>();
		disabledJobQueue = new HashMap<String, String>();

		// Read System parameters
		if (!tlosParameters.isPersistent() || !JobQueueOperations.recoverJobQueue(tlosParameters, jobQueue)) {
			if (!initScenarioInfo(schedulerLogger, tlosParameters, jobQueue) || jobQueue.size() == 0) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.0")); //$NON-NLS-1$
				System.exit(-1);
			}
		}

		schedulerLogger.info(LocaleMessages.getString("TlosServer.1")); //$NON-NLS-1$
		try {
			authorizationList = AuthorizationLoader.readAuthorizationList();
		} catch (Exception fnf) {
			authorizationList = new HashMap<String, TlosAuthorization>();
			try {
				TlosAuthorization tlosAuthorization = new TlosAuthorization("tlos", PasswordService.encrypt("tlos")); //$NON-NLS-1$ //$NON-NLS-2$
				authorizationList.put("tlos", tlosAuthorization); //$NON-NLS-1$
			} catch (Exception e) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.3") + AuthorizationLoader.fileToPersist + LocaleMessages.getString("TlosServer.2")); //$NON-NLS-1$ //$NON-NLS-2$
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.7")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
			AuthorizationLoader.persistAuthorizationList(authorizationList);
		}
		tlosParameters.setTlosAuthorization(authorizationList.get("tlos")); //$NON-NLS-1$

		schedulerLogger.info(LocaleMessages.getString("TlosServer.9")); //$NON-NLS-1$

		schedulerLogger.info(LocaleMessages.getString("TlosServer.10")); //$NON-NLS-1$
		setTlosCommInterface(new TlosCommInterface(this));

		schedulerLogger.info(LocaleMessages.getString("TlosServer.11")); //$NON-NLS-1$
		TlosWebConsole tlosWebConsole = new TlosWebConsole(this);
		schedulerLogger.info(LocaleMessages.getString("TlosServer.12") + tlosWebConsole.getHostName() + " " + LocaleMessages.getString("TlosServer.14") + tlosWebConsole.getHttpPort()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try {
			tlosWebConsole.initServer();
		} catch (Exception e) {
			schedulerLogger.fatal(LocaleMessages.getString("TlosServer.15")); //$NON-NLS-1$
			schedulerLogger.fatal(LocaleMessages.getString("TlosServer.16")); //$NON-NLS-1$
			e.printStackTrace();
			System.exit(-1);
		}
		schedulerLogger.info(LocaleMessages.getString("TlosServer.17")); //$NON-NLS-1$

		if (TlosServer.getTlosParameters().isManagementEnabled()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.18")); //$NON-NLS-1$
			try {
				managementConsoleHandler = ManagementConsole.initComm(getTlosCommInterface(), tlosParameters.getManagementPort(), tlosParameters.getHostName());
				new Thread(managementConsoleHandler).start();
			} catch (SocketException e) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.19")); //$NON-NLS-1$
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.20")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.21")); //$NON-NLS-1$
		}

		if (TlosServer.getTlosParameters().isJmxManagementEnabled()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.22")); //$NON-NLS-1$
			try {
				JmxManagementConsole.initialize(getTlosCommInterface(), tlosParameters.getJmxPort(), tlosParameters.getJmxIp());
			} catch (Exception e) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.23")); //$NON-NLS-1$
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.24")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.25")); //$NON-NLS-1$
		}

		if (tlosParameters.isMail()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.26")); //$NON-NLS-1$

			try {
				tlosMailServer.sendMail(new WelcomeMail(jobQueue));
			} catch (Exception e) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.27")); //$NON-NLS-1$
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.28")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.29")); //$NON-NLS-1$
		}
		if (tlosParameters.isSms()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.30")); //$NON-NLS-1$

			try {
				tlosSMSServer.sendSMS(new SMSType(LocaleMessages.getString("TlosServer.31") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosServer.32") + LocaleMessages.getString("TlosServer.33"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (Exception e) {
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.34")); //$NON-NLS-1$
				schedulerLogger.fatal(LocaleMessages.getString("TlosServer.35")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.36")); //$NON-NLS-1$
		}
	}

	private boolean initScenarioInfo(Logger s, TlosParameters t, HashMap<String, Job> j) {
		return ScenarioLoader.readScenario(s, t, j);
	}

	public ArrayList<SortType> createProrityIndex(HashMap<String, Job> jobQueue) {

		ArrayList<SortType> jobQueueArray = new ArrayList<SortType>();

		Job job = null;

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			job = jobsIterator.next();
			SortType mySortType = new SortType(job.getJobProperties().getKey().toString(), job.getJobProperties().getPriority());
			jobQueueArray.add(mySortType);
		}

		return jobQueueArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Thread.currentThread().setName("LikyaTlos"); //$NON-NLS-1$

		schedulerLogger.debug(LocaleMessages.getString("TlosServer.38")); //$NON-NLS-1$
		schedulerLogger.info(LocaleMessages.getString("TlosServer.39") + jobQueue.size()); //$NON-NLS-1$
		if (tlosParameters.isNormalizable() && !TlosServer.isRecovered()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.40")); //$NON-NLS-1$
			JobQueueOperations.normalizeJobQueueForStartup(jobQueue);
			schedulerLogger.info(LocaleMessages.getString("TlosServer.41")); //$NON-NLS-1$
		}

		ArrayList<SortType> jobIndex = createProrityIndex(jobQueue);
		Collections.sort(jobIndex);

		if (tlosParameters.isPersistent()) {
			JobQueueOperations.recoverDisabledJobQueue(tlosParameters, disabledJobQueue, jobQueue);
		}

		while (executionPermission) {

			try {

				// Iterator<Job> jobsIterator = jobQueue.values().iterator();
				Iterator<SortType> indexIterator = jobIndex.iterator();

				// while (jobsIterator.hasNext()) {

				while (indexIterator.hasNext()) {

					if ((TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) || checkThresholdOverflow()) {
						TlosServer.print("."); //$NON-NLS-1$
						break;
					}

					// Job scheduledJob = jobsIterator.next();
					SortType mySortType = indexIterator.next();
					Job scheduledJob = jobQueue.get(mySortType.getJobKey());

					// System.err.println("Key : " + mySortType.getJobKey() + " >>>>>>>>>>>>> " + scheduledJob.getJobProperties().getStatusString());

					ArrayList<DependencyInfo> dependentJobList = scheduledJob.getJobProperties().getJobDependencyInfoList();

					if (scheduledJob instanceof ExternalProgram) {
						if (scheduledJob.getJobProperties().getStatus() == JobProperties.READY) {

							Date scheduledTime = scheduledJob.getJobProperties().getTime();
							Date currentTime = Calendar.getInstance().getTime();

							if (scheduledTime.before(currentTime)) {
								if (dependentJobList.get(0).getJobKey().equals(ScenarioLoader.UNDEFINED_VALUE)) {
									executeJob(scheduledJob);
								} else {
									if (checkDependency(scheduledJob, dependentJobList)) {
										executeJob(scheduledJob);
									} else {
										// set waiting
										if (scheduledJob.getJobProperties().getStatus() != JobProperties.SKIP) {
											scheduledJob.getJobProperties().setStatus(JobProperties.WAITING);
										}
									}

								}
							}

						} else if (scheduledJob.getJobProperties().getStatus() == JobProperties.WAITING) {

							if (checkDependency(scheduledJob, dependentJobList)) {
								executeJob(scheduledJob);
							} else {
								// set waiting
								if (scheduledJob.getJobProperties().getStatus() != JobProperties.SKIP) {
									scheduledJob.getJobProperties().setStatus(JobProperties.WAITING);
								}
							}

						}
					} else if (scheduledJob instanceof RepetitiveExternalProgram) {
						executeRepetitiveJob(scheduledJob);
					} else if (scheduledJob instanceof ManuelExternalProgram) {
						executeManuelJob(scheduledJob);
					}
				}
				if (tlosParameters.isPersistent()) {
					JobQueueOperations.persistJobQueue(tlosParameters, jobQueue);
					JobQueueOperations.persistDisabledJobQueue(tlosParameters, disabledJobQueue);
				}

				cleanUpQueueIssues();

				TlosServer.print("."); //$NON-NLS-1$
				Thread.sleep(tlosParameters.getSchedulerFrequency());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		schedulerLogger.info(LocaleMessages.getString("TlosServer.49")); //$NON-NLS-1$

		while (isActiveThreads()) {
			try {
				print("."); //$NON-NLS-1$
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (TlosServer.getTlosParameters().isMail()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.51")); //$NON-NLS-1$
			schedulerLogger.info(LocaleMessages.getString("TlosServer.52")); //$NON-NLS-1$
			schedulerLogger.info(LocaleMessages.getString("TlosServer.53") + getTlosMailServer().getQueueSize()); //$NON-NLS-1$

			while (getTlosMailServer().getQueueSize() > 0) {
				print(getTlosMailServer().getQueueSize() + "-"); //$NON-NLS-1$
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.55")); //$NON-NLS-1$
		}

		if (TlosServer.getTlosParameters().isSms()) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.56")); //$NON-NLS-1$
			schedulerLogger.info(LocaleMessages.getString("TlosServer.57")); //$NON-NLS-1$
			schedulerLogger.info(LocaleMessages.getString("TlosServer.58") + getTlosSMSServer().getQueueSize()); //$NON-NLS-1$

			while (getTlosSMSServer().getQueueSize() > 0) {
				print(getTlosSMSServer().getQueueSize() + "-"); //$NON-NLS-1$
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			schedulerLogger.info(LocaleMessages.getString("TlosServer.60")); //$NON-NLS-1$
		}
		if (tlosParameters.isPersistent()) {
			JobQueueOperations.persistJobQueue(tlosParameters, jobQueue);
			JobQueueOperations.persistDisabledJobQueue(tlosParameters, disabledJobQueue);
		}

		if (TlosServer.getTlosParameters().isJmxManagementEnabled()) {
			TlosServer.getLogger().info(LocaleMessages.getString("TlosServer.61")); //$NON-NLS-1$
			JmxManagementConsole.disconnect();
		}

		schedulerLogger.info(LocaleMessages.getString("TlosServer.62")); //$NON-NLS-1$

		System.exit(0);
	}

	private void setUpForTest() {

		schedulerLogger.info(LocaleMessages.getString("TlosServer.63")); //$NON-NLS-1$

		Calendar calendar = Calendar.getInstance();
		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			Date executeDate = scheduledJob.getJobProperties().getTime();
			calendar.setTime(executeDate);
			calendar.add(Calendar.DATE, -1);
			executeDate = calendar.getTime();
			schedulerLogger.debug(executeDate);
			scheduledJob.getJobProperties().setTime(executeDate);
		}

		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long memoryInUse = totalMemory - freeMemory;

		schedulerLogger.info(LocaleMessages.getString("TlosServer.64") + memoryInUse); //$NON-NLS-1$
		schedulerLogger.info(LocaleMessages.getString("TlosServer.65")); //$NON-NLS-1$
		return;
	}

	//	public static boolean checkDependency(ArrayList<DependencyInfo> jobDependencyInfoList) {
	//		return checkDependency(null, jobDependencyInfoList);
	//	}

	public static boolean checkDependency(Job meJob, ArrayList<DependencyInfo> jobDependencyInfoList) {

		int i = 0;

		while (i < jobDependencyInfoList.size()) {
			Job selectedJob = jobQueue.get(jobDependencyInfoList.get(i).getJobKey());
			if (!(selectedJob instanceof RepetitiveExternalProgram)) {
				JobProperties jobProperties = selectedJob.getJobProperties();
				if ((jobProperties.getStatus() != JobProperties.DISABLED)) {

					if (jobDependencyInfoList.get(i).getStatus() == JobProperties.SUCCESS && jobProperties.getStatus() != JobProperties.SUCCESS && jobProperties.getStatus() != JobProperties.SKIP) {
						cleanCyclecDeps(meJob, jobProperties, JobProperties.FAIL);
						return false;
					} else if (jobDependencyInfoList.get(i).getStatus() == JobProperties.FAIL && jobProperties.getStatus() != JobProperties.FAIL) {
						cleanCyclecDeps(meJob, jobProperties, JobProperties.SUCCESS);
						return false;
					} else if (jobDependencyInfoList.get(i).getStatus() == JobProperties.SUCSFAIL && jobProperties.getStatus() != JobProperties.FAIL && jobProperties.getStatus() != JobProperties.SUCCESS) {
						cleanCyclecDeps(meJob, jobProperties, JobProperties.FAIL);
						cleanCyclecDeps(meJob, jobProperties, JobProperties.SUCCESS);
						return false;
					}
				}
			}
			i++;
		}

		return true;
	}

	private static void cleanCyclecDeps(Job meJob, JobProperties jobProperties, int status) {
		if (meJob != null && DependencyOperations.hasDependentWithStatus(jobProperties.getKey().toString(), status) && jobProperties.getStatus() == status) {
			// Bu kod muhtemelen cyclic baüımlılık ayıklaması yapıyor
			// Benim beklediklerimden beni bekleyen var ise, onları skip yapıp ben devam ediyorum
			TlosServer.getTlosCommInterface().skipJob(true, meJob.getJobProperties().getKey().toString());
		}
	}

	public static boolean checkDangerGroupZoneIntrusion(Job currentJob) {

		JobProperties currentJobProperties = currentJob.getJobProperties();

		for (Job myJob : jobQueue.values()) {
			JobProperties myJobProperties = myJob.getJobProperties();
			if (currentJobProperties.getDangerZoneGroup() != null && currentJobProperties.getDangerZoneGroup().equals(myJobProperties.getDangerZoneGroup()) && (myJobProperties.getStatus() == JobProperties.WORKING)) {
				return false;
			}
		}
		return true;
	}

	private void executeJob(Job scheduledJob) throws InterruptedException {

		if (!checkDangerGroupZoneIntrusion(scheduledJob)) {
			// schedulerLogger.debug("Grup kısıtı nedeni ile çalışmıyor ! ==> " + scheduledJob.getJobProperties().getKey());
			return;
		}

		if (getScenarioRuntimeProperties().getCurrentState() == ScenarioRuntimeProperties.STATE_WAITING) {
			getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_RUNNING);
			getScenarioRuntimeProperties().setStartTime(Calendar.getInstance().getTime());
		}
		schedulerLogger.debug(LocaleMessages.getString("TlosServer.66")); //$NON-NLS-1$
		schedulerLogger.debug(scheduledJob.getJobProperties().toString());
		scheduledJob.getJobProperties().setStatus(JobProperties.WORKING);
		Thread starterThread = new Thread(scheduledJob);
		if (scheduledJob.getJobProperties().isManuel()) {
			starterThread.setName("TlosLite-M-" + scheduledJob.getJobProperties().getKey());
		} else {
			starterThread.setName("TlosLite-S-" + scheduledJob.getJobProperties().getKey());
		}
		scheduledJob.setMyExecuter(starterThread);
		// // starterThread.setDaemon(true);
		// starterThread.start();
		scheduledJob.getMyExecuter().start();

		return;
	}

	private void executeRepetitiveJob(Job scheduledJob) throws InterruptedException {

		/**
		 * Tekrarlı işlerin yönetimi kendi Thread'leri içinden olacak. Başlama ve bitiş koşulları
		 * kendi içinden yönetilecek.
		 * 
		 * @author serkan taş
		 * @date 26.03.2011
		 */

		int jobStatus = scheduledJob.getJobProperties().getStatus();

		if (scheduledJob.getMyExecuter() == null && (jobStatus != JobProperties.FAIL && jobStatus != JobProperties.DISABLED)) {
			Thread starterThread = new Thread(scheduledJob);
			starterThread.setName("TlosLite-T-" + scheduledJob.getJobProperties().getKey());
			scheduledJob.setMyExecuter(starterThread);
			scheduledJob.getMyExecuter().start();
		}
		return;
	}

	private void executeManuelJob(Job scheduledJob) throws InterruptedException {

		/**
		 * Manuel iş başlatılması hk.
		 * 
		 * @author serkan taş
		 * @date 28.02.2013
		 */

		if (scheduledJob.getJobProperties().getStatus() == JobProperties.MSTART) {
			executeJob(scheduledJob);
		}

		return;
	}

	public boolean isActiveThreads() {

		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			Thread myExecuter = scheduledJob.getMyExecuter();
			if ((myExecuter != null) && myExecuter.isAlive()) {
				return true;
			}
		}

		return false;
	}

	protected static void redirectLog(String fileNameAndPath) {
		if (fileNameAndPath != null) {
			RollingFileAppender appndr = (RollingFileAppender) Logger.getRootLogger().getAppender("dosya"); //$NON-NLS-1$
			appndr.setFile(fileNameAndPath);
			appndr.activateOptions();
		}

	}

	public synchronized static boolean checkThresholdOverflow() {

		int lowerLimit = tlosParameters.getSchedulerLowerThreshold();
		int higherLimit = tlosParameters.getSchedulerHigherThreshold();

		int numOfActiveJobs = getNumOfActiveJobs();

		if ((!thresholdOverflow) && (numOfActiveJobs >= higherLimit)) {
			schedulerLogger.info(LocaleMessages.getString("TlosServer.68") + numOfActiveJobs + LocaleMessages.getString("TlosServer.69") + lowerLimit); //$NON-NLS-1$ //$NON-NLS-2$
			thresholdOverflow = true;
		} else if (thresholdOverflow && (numOfActiveJobs <= lowerLimit)) {
			thresholdOverflow = false;
			schedulerLogger.info(LocaleMessages.getString("TlosServer.70") + numOfActiveJobs); //$NON-NLS-1$
		}

		// System.out.println("lowerLimit : " + lowerLimit + " higherLimit : " +
		// higherLimit + " numOfActiveJobs : " + numOfActiveJobs +
		// " thresholdOverflow : " + thresholdOverflow);

		return thresholdOverflow;

	}

	public synchronized static int deltaToOverflow() {

		int higherLimit = tlosParameters.getSchedulerHigherThreshold();

		int numOfActiveJobs = getNumOfActiveJobs();

		return (higherLimit - numOfActiveJobs);

	}

	public static int getNumOfActiveJobs() {

		int numOfWorkingJobs = getNumOfJobs(JobProperties.WORKING);
		int numOfTimeoutJobs = getNumOfJobs(JobProperties.TIMEOUT);

		return numOfWorkingJobs + numOfTimeoutJobs;
	}

	public static int getNumOfJobs(int status) {

		int counter = 0;

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (scheduledJob.getJobProperties().getStatus() == status) {
				counter += 1;
			}

		}

		return counter;
	}

	public static boolean isEXTERNSmsPermit() {
		return true;
	}

	private void cleanUpQueueIssues() {

		// Hepsi T-tekrarlı ise ve blocker değilse check yapma direk çık
		// Hepsi S-standart veya S-T Karışık ise
		// 1. T-ler için blocker değilse hiç bir şey yapma
		// 2. S-ler için if(Hepsi Non-Blocker ise succ-skip-fail ise reset) else
		// (succ-skip reset)

		boolean resetQueue = false;

		boolean isAllRepetitive = JobQueueOperations.isAllRepetitive(jobQueue);
		boolean isAllDisabled = JobQueueOperations.isAllDisabled(jobQueue);

		boolean isAllStandart = JobQueueOperations.isAllStandart(jobQueue);
		// boolean isAllStandartBlocker =
		// JobQueueOperations.isAllStandartBlocker(jobQueue);
		boolean isAllStandartNonBlocker = JobQueueOperations.isAllStandartNonBlocker(jobQueue);

		boolean isAllSuccessOrSkip = JobQueueOperations.isAllSuccessOrSkip(jobQueue);
		boolean isAllSuccessOrSkipOrFail = JobQueueOperations.isAllSuccessOrSkipOrFail(jobQueue);

		if (isAllRepetitive || isAllDisabled) {
			// Do nothing
		} else if (isAllStandart) {
			// S-ler için if(Hepsi Non-Blocker ise succ-skip-fail ise reset)
			// else (succ-skip reset)
			if (isAllStandartNonBlocker) {
				if (isAllSuccessOrSkipOrFail) {
					// reset queue
					resetQueue = true;
				}
			} else {
				// blocker ise için success fail ise true
				// non-blocker ise success fail skip ise true
				if (isAllSuccessOrSkip) {
					// reset
					resetQueue = true;
				}
			}
		} else {
			Iterator<Job> jobsIterator = jobQueue.values().iterator();

			while (jobsIterator.hasNext()) {
				Job scheduledJob = jobsIterator.next();

				int myJobsStatus = scheduledJob.getJobProperties().getStatus();

				if (myJobsStatus != JobProperties.DISABLED) {

					if (scheduledJob instanceof RepetitiveExternalProgram) {
						if (!scheduledJob.getJobProperties().isBlocker()) {
							// do nothing
						} else {
							if (isAllSuccessOrSkip) {
								// reset queue
								resetQueue = resetQueue || true;
							} else {
								resetQueue = resetQueue && false;
							}
						}
					} else if (scheduledJob instanceof ExternalProgram) {
						if (isAllStandartNonBlocker) {
							if (isAllSuccessOrSkip) {
								// reset queue
								resetQueue = resetQueue || true;
							} else {
								resetQueue = resetQueue && false;
							}
						} else {
							// blocker ise için success fail ise true
							// non-blocker ise success fail skip ise true
							if (isAllSuccessOrSkip) {
								// reset
								resetQueue = resetQueue || true;
							} else {
								resetQueue = resetQueue && false;
							}
						}
					}

				}
			}
		}

		if (resetQueue) {
			JobQueueOperations.resetJobQueue(jobQueue);
			getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_WAITING);
			getScenarioRuntimeProperties().setEndTime(Calendar.getInstance().getTime());
			if (tlosParameters.isNormalizable()) {
				schedulerLogger.info(LocaleMessages.getString("TlosServer.43")); //$NON-NLS-1$
				JobQueueOperations.normalizeJobQueue(jobQueue);
				schedulerLogger.info(LocaleMessages.getString("TlosServer.44")); //$NON-NLS-1$
			}
			if (tlosParameters.isMail()) {
				tlosMailServer.sendMail(new EndOfCycleMail(jobQueue));
			}
			if (tlosParameters.isSms()) {
				tlosSMSServer.sendSMS(new SMSType(LocaleMessages.getString("TlosServer.45") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosServer.46"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (loadTest) {
				++loadTestTurnCount;
				schedulerLogger.info(LocaleMessages.getString("TlosServer.47") + loadTestTurnCount); //$NON-NLS-1$
				setUpForTest();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// if(!isAllRepetitiveJob) {
		// if (JobQueueOperations.checkJobQueueAllSuccessORSkip(jobQueue)) {
		// JobQueueOperations.resetJobQueue(jobQueue);
		// getScenarioRuntimeProperties().setCurrentState(ScenarioRuntimeProperties.STATE_WAITING);
		// getScenarioRuntimeProperties().setEndTime(Calendar.getInstance().getTime());
		// if (tlosParameters.isNormalizable()) {
		//					schedulerLogger.info(LocaleMessages.getString("TlosServer.43")); //$NON-NLS-1$
		// JobQueueOperations.normalizeJobQueue(jobQueue);
		//					schedulerLogger.info(LocaleMessages.getString("TlosServer.44")); //$NON-NLS-1$
		// }
		// if (tlosParameters.isMail()) {
		// tlosMailServer.sendMail(new EndOfCycleMail(jobQueue));
		// }
		// if (tlosParameters.isSms()) {
		//					tlosSMSServer.sendSMS(new SMSType(LocaleMessages.getString("TlosServer.45") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("TlosServer.46"))); //$NON-NLS-1$ //$NON-NLS-2$
		// }
		// if (loadTest) {
		// ++loadTestTurnCount;
		//					schedulerLogger.info(LocaleMessages.getString("TlosServer.47") + loadTestTurnCount); //$NON-NLS-1$
		// setUpForTest();
		// Thread.sleep(3000);
		// }
		// }
		// }
	}
}
