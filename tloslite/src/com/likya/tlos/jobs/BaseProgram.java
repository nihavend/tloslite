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
package com.likya.tlos.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.jvnet.winp.WinProcess;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.StreamGrabber;
import com.likya.tlos.utils.ValidPlatforms;
import com.likya.tlos.utils.WatchDogTimer;
import com.likya.tlos.utils.loaders.ScenarioLoader;

public abstract class BaseProgram extends Job {

	private static final long serialVersionUID = 1L;

	transient private Process process;

	private boolean retryFlag = true;
	private int retryCounter = 1;

	transient private WatchDogTimer watchDogTimer = null;
	private int wdtCounter = 0;
	
	public BaseProgram(HashMap<String, Job> jobQueue, JobProperties jobProperties, boolean isMail, boolean isSms) {
		super(jobQueue, jobProperties, isMail, isSms);
	}

	public void stopMyDogBarking() {
		if(watchDogTimer != null) {
			watchDogTimer.interrupt();
			watchDogTimer = null;
		}
	}
	
	public void run() {

		Date startTime = Calendar.getInstance().getTime();

		String startLog = getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.0") + DateUtils.getDate(startTime); //$NON-NLS-1$
		getJobProperties().setExecutionDate(startTime);
		getJobProperties().setTime(startTime);
		
		sendEmail();
		sendSms();
		
		TlosServer.getLogger().info(startLog);
		
		

		while (true) {
			try {
				
				getJobProperties().setRecentWorkDuration(getJobProperties().getWorkDuration());
				getJobProperties().setRecentWorkDurationNumeric(getJobProperties().getWorkDurationNumeric());

				if (!(getJobProperties().isAutoRetry() && wdtCounter > 0)) {
					watchDogTimer = new WatchDogTimer(this, getJobProperties().getKey().toString(), Thread.currentThread(), getJobProperties().getTimeout());
					watchDogTimer.setName(getJobProperties().getKey().toString() + ".WatchDogTimer.id." + watchDogTimer.getId()); //$NON-NLS-1$
					watchDogTimer.start();
					
					wdtCounter++;
				}

				String[] cmd = null; 

				if(getJobProperties().getJobType().toUpperCase().equals(ScenarioLoader.JAVA_PROCESS)) {
					process = Runtime.getRuntime().exec(getJobProperties().getJobCommand());
				} else {
					cmd = parseParameter();
					process = Runtime.getRuntime().exec(cmd);
				}
				
				getJobProperties().getMessageBuffer().delete(0, getJobProperties().getMessageBuffer().capacity());
				// any error message?
				StreamGrabber errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR", TlosServer.getLogger(), TlosServer.getTlosParameters().getLogBufferSize()); //$NON-NLS-1$
				errorGobbler.setName(getJobProperties().getKey().toString() + ".ErrorGobbler.id." + errorGobbler.getId()); //$NON-NLS-1$

				// any output?
				StreamGrabber outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT", TlosServer.getLogger(), TlosServer.getTlosParameters().getLogBufferSize()); //$NON-NLS-1$
				outputGobbler.setName(getJobProperties().getKey().toString() + ".OutputGobbler.id." + outputGobbler.getId()); //$NON-NLS-1$

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				try {

					process.waitFor();

					int processExitValue = process.exitValue();
					TlosServer.getLogger().info(getJobProperties().getKey().toString() + LocaleMessages.getString("ExternalProgram.6") + processExitValue); //$NON-NLS-1$
					int successRetValue = JobProperties.PROCESS_EXIT_RC_SUCCESS;

					String errStr = getJobProperties().getLogAnalyzeString();
					boolean hasErrorInLog = false;
					if (!getJobProperties().getLogFilePath().equals(ScenarioLoader.UNDEFINED_VALUE)) {
						if (errStr != null) {
							hasErrorInLog = FileUtils.analyzeFileForString(getJobProperties().getLogFilePath(), errStr);
						}
					} else if(errStr != null) {
						TlosServer.getLogger().error("jobFailString: \"" + errStr + "\" " + LocaleMessages.getString("ExternalProgram.1") + " !"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
					/**
					 * 17.11.2008 Aşağıdaki kısım yerine, artık warning
					 * değerleri bir diziden okunacak.
					 * 
					 */

					if(((processExitValue == successRetValue) || getJobProperties().inDiscardList(processExitValue)) && !((errStr != null) && hasErrorInLog)) {
						getJobProperties().setStatus(JobProperties.SUCCESS); 	
					} else {
						getJobProperties().setStatus(JobProperties.FAIL); 
					}
					getJobProperties().setProcessExitValue(processExitValue);

					if (watchDogTimer != null) {
						watchDogTimer.interrupt();
						watchDogTimer = null;
					}
					
					cleanUpFastEndings(errorGobbler, outputGobbler);
					
					StringBuilder stringBufferForERROR = errorGobbler.getOutputBuffer();
					StringBuilder stringBufferForOUTPUT = outputGobbler.getOutputBuffer();
					updateDescStr(getJobProperties().getMessageBuffer(), stringBufferForOUTPUT, stringBufferForERROR);

				} catch (InterruptedException e) {

					errorGobbler.interrupt();
					outputGobbler.interrupt();
					if (ValidPlatforms.getOSName() != null && ValidPlatforms.getOSName().contains(ValidPlatforms.OS_WINDOWS)) {
						try {
							// System.out.println("Killing windows process tree...");
							WinProcess winProcess = new WinProcess(process);
							winProcess.killRecursively();
							// System.out.println("Killed.");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					// Stop the process from running
					TlosServer.getLogger().warn(LocaleMessages.getString("ExternalProgram.8") + getJobProperties().getKey()); //$NON-NLS-1$

					// process.waitFor() komutu thread'in interrupt statusunu temizlemedigi icin 
					// asagidaki sekilde temizliyoruz
					Thread.interrupted();

					process.destroy();
					getJobProperties().setStatus(JobProperties.FAIL);

				}

				errorGobbler.stopStreamGobbler();
				outputGobbler.stopStreamGobbler();
				errorGobbler = null;
				outputGobbler = null;
				watchDogTimer = null;

			} catch (Exception err) {
				if(watchDogTimer != null) {
					watchDogTimer.interrupt();
					watchDogTimer = null;
				}
				getJobProperties().setStatus(JobProperties.FAIL);
				err.printStackTrace();
			}

			if (getJobProperties().getStatus() == JobProperties.SUCCESS || (getJobProperties().getStatus() == JobProperties.READY && getJobProperties().getPreviousStatusList().get(getJobProperties().getPreviousStatusList().size() - 1) == JobProperties.SUCCESS)) {

				setWorkDurations(this, startTime);

				if(this instanceof ManuelExternalProgram) {
					getJobProperties().setStatus(JobProperties.READY);
				} else {
					if(Calendar.getInstance().getTime().before(getJobProperties().getJobPlannedStartTime())) {
						getJobProperties().setTime(getJobProperties().getJobPlannedStartTime());
					} else {
						DateUtils.iterateNextDate(getJobProperties());
					}
					
					// Eğer elle çalıştırılan standart bir iş ise, ve bağımlılık listesinde değilse
					// Normal çalışma zamanı geldiğinde tekrar çalışsın.
					if(getJobProperties().getSubStatus() == JobProperties.MSTART && DependencyOperations.getDependencyList(getJobQueue(), getJobProperties().getKey()) == null) {
						if(getJobProperties().getStatusBeforeMstart() == JobProperties.READY) {
							getJobProperties().setStatus(JobProperties.READY);
							getJobProperties().setSubStatus(JobProperties.READY);
						} else {
							getJobProperties().setStatus(JobProperties.SUCCESS);
							getJobProperties().setSubStatus(JobProperties.SUCCESS);
						}
					}
				}
				
				TlosServer.getLogger().info(LocaleMessages.getString("ExternalProgram.9") + getJobProperties().getKey() + " => " + ObjectUtils.getStatusAsString(getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
				
			} else {

				setWorkDurations(this, startTime);

				// is elle stop edildiginde otomatik olarak calismaya baslamasin diye bir onceki statu kontrolu eklendi
				int onePreviousStatus = getJobProperties().getPreviousStatusList().get(getJobProperties().getPreviousStatusList().size() - 1);
				
				if (getJobProperties().isAutoRetry() && retryFlag && onePreviousStatus != JobProperties.STOP) {
					TlosServer.getLogger().info(LocaleMessages.getString("ExternalProgram.11") + getJobProperties().getKey()); //$NON-NLS-1$

					if(retryCounter < getJobProperties().getAutoRetryCount()) {
						retryCounter++;
						try {
							Thread.sleep(getJobProperties().getAutoRetryDelay());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						startTime = Calendar.getInstance().getTime();
						getJobProperties().setExecutionDate(startTime);
						getJobProperties().setTime(startTime);
						
						getJobProperties().setStatus(JobProperties.WORKING);
						
						continue;
						
					} 
					
				} 
				
				if(this instanceof ExternalProgram && !getJobProperties().isBlocker() && getJobProperties().getStatus() == JobProperties.FAIL) {
					DateUtils.iterateNextDate(getJobProperties());	
				}
				
				TlosServer.getLogger().info(getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.12")); //$NON-NLS-1$
				TlosServer.getLogger().debug(getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.13")); //$NON-NLS-1$
				
			}
			
			// restore to the value derived from sernayobilgileri file.
			getJobProperties().setJobParamList(getJobProperties().getJobParamListPerm());

			retryFlag = false;
			
			sendEmail();
			sendSms();
			break;
		}

		setMyExecuter(null);
		process = null;
		
//		retryFlag = true;
		wdtCounter = 0;
	}

	public boolean isRetryFlag() {
		return retryFlag;
	}
	
}
