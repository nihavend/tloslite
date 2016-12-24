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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.mail.helpers.SimpleMail;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.sms.helpers.SMSType;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.DependencyOperations;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.StreamGrabber;
import com.likya.tlos.utils.ValidPlatforms;

public abstract class Job implements Runnable, Serializable {

	private static final long serialVersionUID = 2540934879831919506L;

	transient private HashMap<String, Job> jobQueue;

	transient private Thread myExecuter;

	private JobProperties jobProperties;

	private boolean isMail;
	
	private boolean isSms;

	public abstract void stopMyDogBarking();
	
	public Job(HashMap<String, Job> jobQueue, JobProperties jobProperties, boolean isMail, boolean isSms) {
		this.jobQueue = jobQueue;
		this.jobProperties = jobProperties;
		this.isMail = isMail;
		this.isSms = isSms;
	}

	public void sendEmail() {
		if (isMail) {
			int jobStatus = jobProperties.getStatus();
			int jobResultCode = jobProperties.getProcessExitValue();

			ArrayList<Integer> statusListForMail = TlosServer.getTlosParameters().getStatusListForMail();

			if ((statusListForMail != null) && (statusListForMail.indexOf(new Integer(jobStatus)) >= 0)) {
				String dependencyListString = ""; //$NON-NLS-1$
				if (jobStatus == JobProperties.FAIL) {
					dependencyListString = getDependencyListString(jobQueue, jobProperties.getKey());
				}
				String statusString = jobProperties.getStatusString(jobStatus, jobResultCode);
				String subject = LocaleMessages.getString("Job.2") + getJobProperties().getKey().toString() + " : " + statusString + " : " + getJobProperties().getJobCommand() + " " + LocaleMessages.getString("Job.1") + TlosServer.getTlosParameters().getScenarioName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				String messageText = subject + dependencyListString + " Tlos Server : http://" + TlosServer.getTlosParameters().getHostName() + ":" + TlosServer.getTlosParameters().getHttpAccessPort();

				try {
					sendEmail(subject, messageText);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendSms() {
		if (isSms) {
			int jobStatus = jobProperties.getStatus();
			int jobResultCode = jobProperties.getProcessExitValue();

			ArrayList<Integer> statusListForSms = TlosServer.getTlosParameters().getStatusListForSMS();

			if ((statusListForSms != null) && (statusListForSms.indexOf(new Integer(jobStatus)) >= 0)) {
				String dependencyListString = ""; //$NON-NLS-1$
				if (jobStatus == JobProperties.FAIL) {
					dependencyListString = getDependencyListString(jobQueue, jobProperties.getKey());
				}
				String statusString = jobProperties.getStatusString(jobStatus, jobResultCode);
				String subject = LocaleMessages.getString("Job.6") + TlosServer.getTlosParameters().getScenarioName() + LocaleMessages.getString("Job.7") + getJobProperties().getKey().toString() + " : " + statusString + ":" + getJobProperties().getJobCommand(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				String messageText = subject + dependencyListString;

				try {
					sendSms(messageText);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void sendEmail(String subject, String messageText) throws Exception {

		if (isMail) {
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.10")); //$NON-NLS-1$
//			TlosServer.getLogger().debug("E-posta adresi : " + TlosServer.getTlosParameters().getEmailDestination());
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.11") + subject); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.12") + messageText); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.13") + getJobProperties().getLogFilePath()); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.14")); //$NON-NLS-1$
			TlosServer.getTlosMailServer().sendMail(new SimpleMail(subject, messageText));
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.15")); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.10")); //$NON-NLS-1$
		} else {
			// LikyaScheduler.getLogger().log(Level.INFO,
			// "-------------------------------------------");
			// LikyaScheduler.getLogger().log(Level.INFO, "Ba�l�k : " +
			// subject);
			// LikyaScheduler.getLogger().log(Level.INFO, "Mesaj ��eri�i : " +
			// messageText);
			// LikyaScheduler.getLogger().log(Level.INFO,
			// "-------------------------------------------");
			// System.out.println("-------------------------------------------");
			// System.out.println("E-posta adresi : " +
			// TlosServer.getLikyaSchedulerParameters().getEmailAddress());
			// System.out.println("Ba�l�k : " + subject);
			// System.out.println("Mesaj ��eri�i : " + messageText);
			// System.out.println("Program Log adresi : " +
			// getJobProperties().getJobProgramPath());
			// System.out.println("-------------------------------------------");
		}
	}

	private void sendSms(String messageText) throws Exception {

		if (isSms) {
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.10")); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.18") + messageText); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.19")); //$NON-NLS-1$
			TlosServer.getTlosSMSServer().sendSMS(new SMSType(messageText));
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.20")); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("Job.10")); //$NON-NLS-1$
		}
	}
	// Not 1 den dolay� gerek kalmad�
	// public abstract void terminate();

	public JobProperties getJobProperties() {
		return jobProperties;
	}

	public void setJobProperties(JobProperties jobProperties) {
		this.jobProperties = jobProperties;
	}

	public HashMap<String, Job> getJobQueue() {
		return jobQueue;
	}

	public Thread getMyExecuter() {
		return myExecuter;
	}

	public void setMyExecuter(Thread myExecuter) {
		this.myExecuter = myExecuter;
	}

	public String getJobInfo() {
		return jobProperties.getKey() + ":" + jobProperties.getStatusString(jobProperties.getStatus(), jobProperties.getProcessExitValue()); //$NON-NLS-1$
	}

	public String getDependencyListString(HashMap<String, Job> jobQueue, Object jobKey, boolean simple) {

		ArrayList<Job> jobList = DependencyOperations.getDependencyList(jobQueue, jobKey);

		String jobListString = ""; //$NON-NLS-1$

		if (simple) {
			// jobListString = "NA";
			jobListString = "-"; //$NON-NLS-1$
		}

		if (jobList != null) {

			if (!simple) {
				jobListString = LocaleMessages.getString("Job.25"); //$NON-NLS-1$
			} else {
				jobListString = "("; //$NON-NLS-1$
			}
			int i = 0;
			while (i < jobList.size()) {
				if (!simple) {
					jobListString += LocaleMessages.getString("Job.27") + jobList.get(i).getJobProperties().getKey() + " (" + jobList.get(i).getJobProperties().getJobCommand() + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					BigInteger checkValue = BigInteger.valueOf(i + 1).mod(BigInteger.valueOf(5));
					if((checkValue.intValue() == 0) && i > 0) {
						jobListString += jobList.get(i).getJobProperties().getKey() + ",<br> "; //$NON-NLS-1$
					} else {
						jobListString += jobList.get(i).getJobProperties().getKey() + ","; //$NON-NLS-1$
					}
				}
				i++;
			}
			if (simple) {
				if (jobListString.charAt(jobListString.length() - 1) == ',') {
					jobListString = jobListString.substring(0, jobListString.length() - 1);
				}
				jobListString += ")"; //$NON-NLS-1$
			}
		}

		return jobListString;
	}
	
	public String getDependencyListStringForTooltip(HashMap<String, Job> jobQueue, Object jobKey, boolean simple) {

		ArrayList<Job> jobList = DependencyOperations.getDependencyList(jobQueue, jobKey);

		String jobListString = ""; //$NON-NLS-1$

		if (simple) {
			// jobListString = "NA";
			jobListString = "-"; //$NON-NLS-1$
		}

		if (jobList != null) {

			if (!simple) {
				jobListString = LocaleMessages.getString("Job.25"); //$NON-NLS-1$
			} else {
				jobListString = ""; //$NON-NLS-1$
				//jobListString = "("; //$NON-NLS-1$
			}
			int i = 0, lineSize = 0;
			while (i < jobList.size()) {
				if (!simple) {
					jobListString += LocaleMessages.getString("Job.27") + jobList.get(i).getJobProperties().getKey() + " (" + jobList.get(i).getJobProperties().getJobCommand() + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					lineSize += jobList.get(i).getJobProperties().getKey().toString().length();
					
					if (lineSize > 60) {
						lineSize = jobList.get(i).getJobProperties().getKey().toString().length();
						jobListString += "\n ";
					}
					jobListString += jobList.get(i).getJobProperties().getKey() + ", "; //$NON-NLS-1$
				}
				i++;
			}
			if (simple) {
				if (jobListString.charAt(jobListString.length() - 1) == ',') {
					jobListString = jobListString.substring(0, jobListString.length() - 1);
				}
				//jobListString += ")"; //$NON-NLS-1$
			}
		}

		return jobListString;
	}
	
	public String getTruncatedDependencyListString(HashMap<String, Job> jobQueue, Object jobKey) {

		ArrayList<Job> jobList = DependencyOperations.getDependencyList(jobQueue, jobKey);

		String jobListString = "-"; //$NON-NLS-1$

		if (jobList != null) {

			jobListString = "("; //$NON-NLS-1$
			
			if(jobList.size() == 1) {
				jobListString += getTruncatedString(jobList.get(0).getJobProperties().getKey().toString(), 76);
			} else if(jobList.size() == 2) {
				if(jobList.get(0).getJobProperties().getKey().toString().length() + jobList.get(1).getJobProperties().getKey().toString().length() > 76) {
					jobListString += getTruncatedString(jobList.get(0).getJobProperties().getKey().toString(), 37) + "," + getTruncatedString(jobList.get(1).getJobProperties().getKey().toString(), 37);
				} else {
					jobListString += jobList.get(0).getJobProperties().getKey().toString() + "," + jobList.get(1).getJobProperties().getKey().toString();
				}
			} else if(jobList.size() == 3) {
				if(jobList.get(0).getJobProperties().getKey().toString().length() + jobList.get(1).getJobProperties().getKey().toString().length() + jobList.get(2).getJobProperties().getKey().toString().length() > 76) {
					jobListString += getTruncatedString(jobList.get(0).getJobProperties().getKey().toString(), 24) + "," + getTruncatedString(jobList.get(1).getJobProperties().getKey().toString(), 24) + "," + getTruncatedString(jobList.get(2).getJobProperties().getKey().toString(), 24);
				} else {
					jobListString += jobList.get(0).getJobProperties().getKey().toString() + "," + jobList.get(1).getJobProperties().getKey().toString() + "," + jobList.get(2).getJobProperties().getKey().toString();
				}
			} else {
				int i = 0;
				int jobSizeList[] = new int[jobList.size()];
				String jobGroupList[] = new String[jobList.size()/4];
				
				while (i < jobList.size()) {
					jobSizeList[i] = jobList.get(i).getJobProperties().getKey().toString().length();
					i++;
				}
				
				i = 0;
				
				while (i < jobGroupList.length) {
					int lineSize = jobSizeList[i*4] + jobSizeList[1 + i*4] + jobSizeList[2 + i*4] + jobSizeList[3 + i*4];
					
					if(lineSize > 85) {
						jobListString += getTruncatedString(jobList.get(i*4).getJobProperties().getKey().toString(), 19) + ",";
						jobListString += getTruncatedString(jobList.get(1 + i*4).getJobProperties().getKey().toString(), 19) + ",";
						jobListString += getTruncatedString(jobList.get(2 + i*4).getJobProperties().getKey().toString(), 19) + ",";
						jobListString += getTruncatedString(jobList.get(3 + i*4).getJobProperties().getKey().toString(), 19) + ",<br>";
					} else {
						jobListString += jobList.get(i*4).getJobProperties().getKey().toString() + ",";
						jobListString += jobList.get(1 + i*4).getJobProperties().getKey().toString() + ",";
						jobListString += jobList.get(2 + i*4).getJobProperties().getKey().toString() + ",";
						jobListString += jobList.get(3 + i*4).getJobProperties().getKey().toString() + ",<br>";
					}
					i++;
				}
				i = 4 * jobGroupList.length;
				int lineSize = 0;
				
				while(i < jobList.size()) {
					lineSize += jobSizeList[i];
					i++;
				}
				
				i = 4 * jobGroupList.length;
				while(i < jobList.size()) {
					if(lineSize > 76) {
						jobListString += getTruncatedString(jobList.get(i).getJobProperties().getKey().toString(), 24) + ",";
					} else {
						jobListString += jobList.get(i).getJobProperties().getKey().toString() + ",";
					}
					i++;
				}
			}
			
			if (jobListString.charAt(jobListString.length() - 1) == ',') {
				jobListString = jobListString.substring(0, jobListString.length() - 1);
			}
			jobListString += ")"; //$NON-NLS-1$
		}

		return jobListString;
	}
	
	public static String getTruncatedString(String name, int maxLength) {
		if(name.length() > maxLength) {
			return name.substring(0, maxLength - 2) + "...";
		} else {
			return name;
		}
	}

	public String getDependencyListString(HashMap<String, Job> jobQueue, Object jobKey) {
		boolean simple = false;
		return getDependencyListString(jobQueue, jobKey, simple);
	}

	public void setJobQueue(HashMap<String, Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public String getPreviousStatusListString() {
		ArrayList<Integer> statusList = this.getJobProperties().getPreviousStatusList();
		String statusListStr = "("; //$NON-NLS-1$
		if (statusList != null) {
			int i = 0;
			while (i < statusList.size()) {
				int jobStatus = statusList.get(i).intValue();

				BigInteger checkValue = BigInteger.valueOf(i + 1).mod(BigInteger.valueOf(3));
				if((checkValue.intValue() == 0) && (i > 0 && i + 1 < statusList.size())) {
					statusListStr += ObjectUtils.getStatusAsString(jobStatus) + ",<br> "; //$NON-NLS-1$
				} else {
					statusListStr += ObjectUtils.getStatusAsString(jobStatus) + ","; //$NON-NLS-1$
				}
				
				i++;
			}
		}

		char lastChar = statusListStr.charAt(statusListStr.length() - 1);
		if (lastChar == ',') {
			statusListStr = statusListStr.substring(0, statusListStr.length() - 1);
		}

		statusListStr += ")"; //$NON-NLS-1$
		
		return statusListStr;
	}
	
	public void setWorkDurations(Job jobClassName, Date startTime) {
		
		Date endTime = Calendar.getInstance().getTime();
		long timeDiff = endTime.getTime() - startTime.getTime();

		String endLog = getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.14") + DateUtils.getDate(endTime); //$NON-NLS-1$
		String duration = getJobProperties().getKey() + LocaleMessages.getString("ExternalProgram.15") + DateUtils.getFormattedElapsedTime((int) timeDiff / 1000); //$NON-NLS-1$
		
		getJobProperties().setCompletionDate(DateUtils.getDate(endTime));
		getJobProperties().setCompletionDateTime(endTime);

		getJobProperties().setWorkDuration(DateUtils.getUnFormattedElapsedTime((int) timeDiff / 1000));
		getJobProperties().setWorkDurationNumeric(timeDiff);
		
		TlosServer.getLogger().info(endLog);
		TlosServer.getLogger().info(duration);
		
		reportLog(jobClassName, startTime, endTime);
		
	}
	
	public void reportLog(Job jobClass, Date startTime, Date endTime) {
		
		String jobClassName = "JOBSTATS|";
		
		if(jobClass instanceof ExternalProgram) {
			jobClassName = jobClassName.concat("STANDART"); 
		} else if(jobClass instanceof ManuelExternalProgram) {
			jobClassName = jobClassName.concat("MANUEL");
		} else if(jobClass instanceof RepetitiveExternalProgram) {
			jobClassName = jobClassName.concat("TEKRARLI");
		}
		
		TlosServer.getLogger().info(
				jobClassName + "|"	
				+ TlosServer.getTlosParameters().getScenarioName().toString() + "|" 
				+ getJobProperties().getGroupName().toString()+ "|" 
				+ getJobProperties().getKey().toString() + "|"
				+ DateUtils.getDate(startTime)+ "|"
				+ DateUtils.getDate(endTime)+ "|"
				+ getJobProperties().getStatusString(getJobProperties().getStatus(), getJobProperties().getProcessExitValue()).toString()
				); //$NON-NLS-1$
	}
	
	public String[] parseParameter() {
		
		String[] cmd = null;
		
		if(getJobProperties().getJobParamList() != null && !getJobProperties().getJobParamList().equals("")) {
			String tmpCmd [] = ValidPlatforms.getCommand(getJobProperties().getJobCommand());
			String tmpPrm [] = getJobProperties().getJobParamList().split(" ").clone();
			cmd = new String[tmpCmd.length + tmpPrm.length];
			System.arraycopy(tmpCmd, 0, cmd, 0, tmpCmd.length);
			System.arraycopy(tmpPrm, 0, cmd, tmpCmd.length, tmpPrm.length);
		} else {
			cmd = ValidPlatforms.getCommand(getJobProperties().getJobCommand());
		}
		
		// String joinedString = StringUtils.join(cmd, " ");
		
		// TlosServer.getLogger().info("Executing : " + joinedString);
		
		return cmd;
	}
	
	protected void updateDescStr(StringBuffer descStr, StringBuilder stringBufferForOUTPUT, StringBuilder stringBufferForERROR) {

		if (stringBufferForOUTPUT != null && stringBufferForOUTPUT.length() > 1) {
			descStr.append("OUTPUT : " + stringBufferForOUTPUT);
		}

		if (stringBufferForERROR != null && stringBufferForERROR.length() > 1) {
			descStr.append("\nERROR : " + stringBufferForERROR);
		}

		return;
	}
	
	protected void cleanUpFastEndings(StreamGrabber errorGobbler, StreamGrabber outputGobbler) throws InterruptedException {
		if(errorGobbler.isAlive()) {
			errorGobbler.stopStreamGobbler();
			while (errorGobbler.isAlive()) {
				Thread.sleep(10);
			}
		}
		if (outputGobbler.isAlive()) {
			outputGobbler.stopStreamGobbler();
			while (outputGobbler.isAlive()) {
				Thread.sleep(10);
			}
		}
	}
}
