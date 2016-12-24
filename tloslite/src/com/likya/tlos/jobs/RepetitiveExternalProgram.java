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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.jvnet.winp.WinProcess;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.JobQueueOperations;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.StreamGrabber;
import com.likya.tlos.utils.ValidPlatforms;
import com.likya.tlos.utils.WatchDogTimer;
import com.likya.tlos.utils.loaders.ScenarioLoader;

public class RepetitiveExternalProgram extends Job {

	private static final long serialVersionUID = 1L;

	transient protected Process process;
	transient protected StreamGrabber errorGobbler;
	transient protected StreamGrabber outputGobbler;

	private static boolean someoneBusy = false;
	
	private boolean periodPermission = true;

	private boolean isForced = false;
	
	private boolean isFirst = true;

	transient private WatchDogTimer watchDogTimer = null;
	private int wdtCounter = 0;

	public RepetitiveExternalProgram(HashMap<String, Job> jobQueue, JobProperties jobProperties, boolean isMail, boolean isSms) {
		super(jobQueue, jobProperties, isMail, isSms);
	}

	public void run() {

		Long periodTime = getJobProperties().getPeriodTime();

		TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.0") + periodTime); //$NON-NLS-1$

		String jobKey = null;
		Date startTime = null;

		// System.out.print("periodPermission : " + periodPermission); //$NON-NLS-1$ //$NON-NLS-1$

		if (TlosServer.getTlosParameters().isNormalizable()) {
			TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.2") + periodTime); //$NON-NLS-1$
			Date nextTime = DateUtils.findRangedNextPeriod(getJobProperties());
			getJobProperties().setTime(nextTime);
		}
		
		while (periodPermission) {
			try {
				
				if (getJobProperties().getTime().before(Calendar.getInstance().getTime())) {
					Date nextPeriodTime = DateUtils.findRangedNextPeriod(getJobProperties());
					getJobProperties().setTime(nextPeriodTime);
					TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.3") + nextPeriodTime); //$NON-NLS-1$
				}

				TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.4") + Calendar.getInstance().getTime()); //$NON-NLS-1$
				TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.5") + getJobProperties().getTime()); //$NON-NLS-1$

				getJobProperties().setStatus(JobProperties.READY);
				
				try {
					while (true) {
						if (isForced) {
							break;
						} else if (getJobProperties().getStatus() == JobProperties.READY) {
							if (!Calendar.getInstance().getTime().before(getJobProperties().getTime())) {
								if ((TlosServer.getExecutionState() != TlosInfo.STATE_SUSPENDED) && !TlosServer.checkThresholdOverflow()) {
									if((TlosServer.deltaToOverflow() > 1) || (TlosServer.deltaToOverflow() == 1 && !isSomeoneBusy(getJobProperties().getPriority()))) {
										break;
									}	
								}
							}
						} else if (getJobProperties().getStatus() != JobProperties.DISABLED && getJobProperties().getStatus() != JobProperties.PAUSE) {
							break;
						}
						Thread.sleep(1000);

					}
				} catch (Exception e) {
					TlosServer.getLogger().error(e.getLocalizedMessage());
					periodPermission = false;
					continue;
				}
				
				if(getJobProperties().getStatus() == JobProperties.SUCCESS || getJobProperties().getStatus() == JobProperties.SKIP) {
					sendEmail();
					sendSms();
					setSomeoneBusy(false);
					continue;
				}

				if(isFirst) {
					ArrayList<DependencyInfo> dependentJobList = getJobProperties().getJobDependencyInfoList();
					if(dependentJobList.size() > 0) {
						getJobProperties().setStatus(JobProperties.WAITING);
					}
					
					
					if (!dependentJobList.get(0).getJobKey().equals(ScenarioLoader.UNDEFINED_VALUE)) {
						while (!TlosServer.checkDependency(this, dependentJobList)) {
							Thread.sleep(1000);
						}
					}
					isFirst = false;
					getJobProperties().setBlocker(false);
				}
				
				getJobProperties().setRecentWorkDuration(getJobProperties().getWorkDuration());
				getJobProperties().setRecentWorkDurationNumeric(getJobProperties().getWorkDurationNumeric());

				if (isForced) {
					isForced = false;
				}

				getJobProperties().setStatus(JobProperties.WORKING);

				sendEmail();
				sendSms();
				
				jobKey = getJobProperties().getKey().toString();

				if (!(getJobProperties().isAutoRetry() && wdtCounter > 0)) {
					watchDogTimer = new WatchDogTimer(this, getJobProperties().getKey().toString(), Thread.currentThread(), getJobProperties().getTimeout());
					watchDogTimer.setName(jobKey + ".WatchDogTimer.id." + watchDogTimer.getId()); //$NON-NLS-1$
					watchDogTimer.start();
	
					wdtCounter++;
				}

				String[] cmd = null;

				startTime = Calendar.getInstance().getTime();
				getJobProperties().setExecutionDate(startTime);

				String startLog = jobKey + LocaleMessages.getString("RepetitiveExternalProgram.7") + DateUtils.getDate(startTime); //$NON-NLS-1$
				TlosServer.getLogger().info(startLog);

				if (getJobProperties().getJobType().toUpperCase().equals(ScenarioLoader.JAVA_PROCESS)) {
					process = Runtime.getRuntime().exec(getJobProperties().getJobCommand());
				} else {
					cmd = parseParameter();
					process = Runtime.getRuntime().exec(cmd);
				}

				System.out.print(LocaleMessages.getString("RepetitiveExternalProgram.8")); //$NON-NLS-1$
				Thread.sleep(100);
				TlosServer.getLogger().warn(LocaleMessages.getString("RepetitiveExternalProgram.9")); //$NON-NLS-1$

				getJobProperties().getMessageBuffer().delete(0, getJobProperties().getMessageBuffer().capacity());
				errorGobbler = new StreamGrabber(process.getErrorStream(), "ERROR", TlosServer.getLogger(), TlosServer.getTlosParameters().getLogBufferSize()); //$NON-NLS-1$
				errorGobbler.setName(jobKey + ".ErrorGobbler.id." + errorGobbler.getId()); //$NON-NLS-1$

				outputGobbler = new StreamGrabber(process.getInputStream(), "OUTPUT", TlosServer.getLogger(), TlosServer.getTlosParameters().getLogBufferSize()); //$NON-NLS-1$
				outputGobbler.setName(jobKey + ".OutputGobbler.id." + outputGobbler.getId()); //$NON-NLS-1$

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
						TlosServer.getLogger().warn("jobFailString: \"" + errStr + "\" " + LocaleMessages.getString("ExternalProgram.1") + " !"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}

					if (((processExitValue == successRetValue) || getJobProperties().inDiscardList(processExitValue)) && !((errStr != null) && hasErrorInLog)) {
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

			} catch (InterruptedException e) {
				if (watchDogTimer != null) {
					watchDogTimer.interrupt();
					watchDogTimer = null;
				}
				getJobProperties().setStatus(JobProperties.FAIL);
				e.printStackTrace();
			} catch (Exception err) {
				err.printStackTrace();
			}

			sendEmail();
			sendSms();

			// Hangi koşulda biterse bitsin geçen zaman saklanıyor
			Date endTime = Calendar.getInstance().getTime();
			long timeDiff = endTime.getTime() - startTime.getTime();
			
			getJobProperties().setWorkDuration(DateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
			getJobProperties().setWorkDurationNumeric(timeDiff);
			
			// restore to the value derived from sernayobilgileri file.
			getJobProperties().setJobParamList(getJobProperties().getJobParamListPerm());
			
			boolean runEvenFailed = getJobProperties().isSafeRestart(); 
			
			if ((runEvenFailed && getJobProperties().getStatus() == JobProperties.FAIL) || getJobProperties().getStatus() == JobProperties.SUCCESS || getJobProperties().getStatus() == JobProperties.TIMEOUT) {
				setWorkDurations(this, startTime);
				TlosServer.getLogger().info(LocaleMessages.getString("ExternalProgram.9") + getJobProperties().getKey() + " => " + ObjectUtils.getStatusAsString(getJobProperties().getStatus())); //$NON-NLS-1$ //$NON-NLS-2$
				getJobProperties().setStatus(JobProperties.READY);
				setSomeoneBusy(false);
				continue;
			} else {
				setWorkDurations(this, startTime);

				TlosServer.getLogger().info(getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.12")); //$NON-NLS-1$
				TlosServer.getLogger().debug(getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.13")); //$NON-NLS-1$

				wdtCounter = 0;

				// autoretry parametresi true ise job fail ettikten sonra 
				// bir sonraki periyodda calismasi icin asagidaki kisim eklendi
				if (getJobProperties().isAutoRetry()) {
					continue;
				}
			}
		
			process = null;

			break;

		}

		// end of period
	}

	public void stopMyDogBarking() {
		if (watchDogTimer != null) {
			watchDogTimer.interrupt();
			watchDogTimer = null;
		}
	}

	protected void stopErrorGobbler() {
		if (errorGobbler != null && errorGobbler.isAlive()) {
			errorGobbler.stopStreamGobbler();
			errorGobbler.interrupt();
			TlosServer.getLogger().warn("PeriodicExternalProgram -> errorGobbler.isAlive ->" + errorGobbler.isAlive()); //$NON-NLS-1$
			errorGobbler = null;
		}
	}

	protected void stopOutputGobbler() {
		if (outputGobbler != null && outputGobbler.isAlive()) {
			outputGobbler.stopStreamGobbler();
			outputGobbler.interrupt();
			TlosServer.getLogger().warn("PeriodicExternalProgram -> outputGobbler.isAlive ->" + outputGobbler.isAlive()); //$NON-NLS-1$
			outputGobbler = null;
		}
	}

	public boolean isForced() {
		return isForced;
	}

	public void setForced(boolean isForced) {
		this.isForced = isForced;
	}

	public static synchronized boolean isSomeoneBusy(int proirity) {
		// System.out.println("This is NOT busy : " + Thread.currentThread().getId());
		if(!someoneBusy) {
			if(JobQueueOperations.hasAnyBodyUpperThanMe(proirity)) {
				return true;
			}
			someoneBusy = true;
			return false;
		}
		//System.out.println("This is busy : " + Thread.currentThread().getId());
		return someoneBusy;
	}

	public static synchronized void setSomeoneBusy(boolean someoneBusy) {
		RepetitiveExternalProgram.someoneBusy = someoneBusy;
	}

	public synchronized boolean isPeriodPermission() {
		return periodPermission;
	}

	public synchronized void setPeriodPermission(boolean periodPermission) {
		this.periodPermission = periodPermission;
	}

}
