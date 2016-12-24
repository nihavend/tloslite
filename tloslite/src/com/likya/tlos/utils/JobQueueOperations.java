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
package com.likya.tlos.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.jobs.RepetitiveExternalProgram;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.PersistObject;
import com.likya.tlos.model.TlosParameters;

public class JobQueueOperations {

	/**
	 * İş kuyruğunda bitmeyen bir iş var mı yok mu ona bakıyor. Eğer yok ise,
	 * bütün işler başarı ile bitmiş sayılıyor, ve true değeri dönüyor.
	 * 
	 * Sonradan blocker ve/veya disabled durumları eklendi. Yukarıdaki açıklama
	 * eksik kaldı.
	 * 
	 * @author serkan taş 03.03.2013
	 * 
	 * @param jobQueue
	 * @return true, false
	 */
	public static boolean isAllSuccessOrSkip(HashMap<String, Job> jobQueue) {

		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			// sadece periyodik olmayan islerin bitip bitmedigi kontrol ediliyor
			if (scheduledJob instanceof ExternalProgram) {

				int myJobsStatus = scheduledJob.getJobProperties().getStatus();

				if (myJobsStatus != JobProperties.DISABLED) {

					if (scheduledJob.getJobProperties().isBlocker()) {
						if (myJobsStatus != JobProperties.SUCCESS && myJobsStatus != JobProperties.SKIP) {
							return false;
						}
					} else {
						if ((myJobsStatus == JobProperties.FAIL && ((ExternalProgram) scheduledJob).isRetryFlag()) || (myJobsStatus != JobProperties.FAIL && myJobsStatus != JobProperties.SUCCESS && myJobsStatus != JobProperties.SKIP)) {
							return false;
						}
					}
				}

			}
		}

		return true;
	}

	public static void dumpJobQueue(HashMap<String, Job> jobQueue, boolean doNotCheckStatusModification) {
		String queueDumpInfo = ""; //$NON-NLS-1$
		String queueDumpDebug = ""; //$NON-NLS-1$

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			String currentStatus = scheduledJob.getJobInfo();

			if (scheduledJob.getJobProperties().getStatus() == JobProperties.WORKING) {
				queueDumpInfo += '\n';
				queueDumpDebug += '\n';
			}
			queueDumpInfo += "[" + currentStatus + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			queueDumpDebug += "[" + currentStatus + ":" + DateUtils.getDate(scheduledJob.getJobProperties().getTime()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		if (doNotCheckStatusModification || !queueDumpInfo.equals(TlosServer.getQueueStat())) {
			TlosServer.setQueueStat(queueDumpInfo);
			TlosServer.getLogger().info(LocaleMessages.getString("JobQueueOperations.7") + queueDumpInfo); //$NON-NLS-1$
			TlosServer.getLogger().debug(LocaleMessages.getString("JobQueueOperations.8") + queueDumpDebug); //$NON-NLS-1$
		} else {

			TlosServer.print("."); //$NON-NLS-1$
		}

		return;

	}

	public static void dumpJobQueue(HashMap<String, Job> jobQueue) {
		dumpJobQueue(jobQueue, false);
		return;
	}
	
	public static boolean persistDisabledJobQueue(TlosParameters tlosParameters, HashMap<String, String> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			return true;
		}
		
		try {

			File fileTemp = new File(tlosParameters.getFileToPersist() + "_disabled.temp"); //$NON-NLS-1$
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			out.writeObject(jobQueue);
			out.close();

			File file = new File(tlosParameters.getFileToPersist() + "_disabled");

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}
	
	public static boolean persistJobQueue(TlosParameters tlosParameters, HashMap<String, Job> jobQueue) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if (jobQueue.size() == 0) {
			TlosServer.getLogger().fatal(LocaleMessages.getString("JobQueueOperations.10")); //$NON-NLS-1$
			TlosServer.getLogger().fatal(LocaleMessages.getString("JobQueueOperations.11")); //$NON-NLS-1$
			System.exit(-1);
		}
		try {

			File fileTemp = new File(tlosParameters.getFileToPersist() + ".temp"); //$NON-NLS-1$
			fos = new FileOutputStream(fileTemp);

			out = new ObjectOutputStream(fos);

			PersistObject persistObject = new PersistObject();

			persistObject.setJobQueue(jobQueue);
			persistObject.setTlosVersion(TlosServer.getVersion());
			persistObject.setGroupList(TlosServer.getTlosParameters().getGroupList());

			out.writeObject(persistObject);
			out.close();

			File file = new File(tlosParameters.getFileToPersist());

			if (file.exists()) {
				file.delete();
			}

			fileTemp.renameTo(file);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}
	
	public static boolean recoverDisabledJobQueue(TlosParameters tlosParameters, HashMap<String, String> disabledJobQueue, HashMap<String, Job> jobQueue) {

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(tlosParameters.getFileToPersist() + "_disabled");
			in = new ObjectInputStream(fis);
			Object input = in.readObject();

			@SuppressWarnings("unchecked")
			HashMap<String, String> persistObject = (HashMap<String, String>) input;

			disabledJobQueue.putAll(persistObject);

			in.close();

			resetDisabledJobQueueForRecover(JobProperties.DISABLED, disabledJobQueue, jobQueue);

		} catch (FileNotFoundException fnf) {
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		return true;
	}

	public static boolean recoverJobQueue(TlosParameters tlosParameters, HashMap<String, Job> jobQueue) {

		TlosServer.getLogger().info(LocaleMessages.getString("JobQueueOperations.12")); //$NON-NLS-1$

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(tlosParameters.getFileToPersist());
			in = new ObjectInputStream(fis);
			Object input = in.readObject();

			PersistObject persistObject = (PersistObject) input;

			if (!persistObject.getTlosVersion().equals(TlosServer.getVersion())) {
				TlosServer.getLogger().error(LocaleMessages.getString("JobQueueOperations.13")); //$NON-NLS-1$
				TlosServer.getLogger().error(LocaleMessages.getString("JobQueueOperations.14") + TlosServer.getVersion() + LocaleMessages.getString("JobQueueOperations.15") + persistObject.getTlosVersion() + LocaleMessages.getString("JobQueueOperations.16")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				in.close();
				return false;
			}

			jobQueue.putAll(persistObject.getJobQueue());

			// grup listesi de recover dosyasindan okunuyor
			tlosParameters.setGroupList(persistObject.getGroupList());

			in.close();

			resetJobQueueForRecover(JobProperties.SUCCESS, jobQueue);

		} catch (FileNotFoundException fnf) {
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		dumpJobQueue(jobQueue);
		TlosServer.getLogger().info(LocaleMessages.getString("JobQueueOperations.17")); //$NON-NLS-1$

		TlosServer.setRecovered(true);

		return true;
	}

	public static void resetJobQueue(HashMap<String, Job> jobQueue) {
		resetJobQueue(JobProperties.READY, jobQueue);
	}

	public static void resetJobQueue(int exceptionStatus, HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			if (scheduledJob instanceof ExternalProgram) {
				int status = scheduledJob.getJobProperties().getStatus();
				if (status != exceptionStatus && status != JobProperties.DISABLED) {
					scheduledJob.getJobProperties().setStatus(JobProperties.READY);
					if(scheduledJob.getJobProperties().getTime().before(Calendar.getInstance().getTime())) {
						DateUtils.iterateNextDate(scheduledJob.getJobProperties());
					}
				}
			}
		}

		return;
	}
	
	public static void resetDisabledJobQueueForRecover(int exceptionStatus, HashMap<String, String> disabledJobQueue, HashMap<String, Job> jobQueue) {
		
		Iterator<String> jobsIterator = disabledJobQueue.values().iterator();
		
		while (jobsIterator.hasNext()) {
			
			String jobKey = jobsIterator.next();
			
			Job myJob = jobQueue.get(jobKey);

			if (myJob != null) {
				myJob.getJobProperties().setStatus(JobProperties.DISABLED);
			}
			// System.err.println("resetDisabledJobQueueForRecover : Key : " + jobKey + " >>>>>>>>>>>>> " + myJob.getJobProperties().getStatusString());
		}

		return;
	}

	public static void resetJobQueueForRecover(int exceptionStatus, HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			int status = scheduledJob.getJobProperties().getStatus();
			if (status != exceptionStatus) {
				scheduledJob.getJobProperties().setStatus(JobProperties.READY);
			}

			if (scheduledJob.getJobQueue() == null) {
				/**
				 * jobQueue transient olduğunudun, serialize etmiyor Recover
				 * ederken, bu alan null geliyor. Bu nedenle null ise yeninde
				 * okumak gerekiyor.
				 */
				scheduledJob.setJobQueue(jobQueue);
			}

		}

		return;
	}

	public static void normalizeJobQueueForStartup(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		Date now = Calendar.getInstance().getTime();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			if (scheduledJob instanceof ExternalProgram) {
				if (scheduledJob.getJobProperties().getTime().before(now) || Arrays.binarySearch(TlosServer.getTlosParameters().getScheduledDays(), Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) < 0) {
					DateUtils.iterateNextDate(scheduledJob.getJobProperties());
					if(scheduledJob.getJobProperties().getStatus() != JobProperties.READY) {
						TlosServer.getLogger().info("Job " + scheduledJob.getJobProperties().getKey() + " is not READY, setting to SKIP !");
						scheduledJob.getJobProperties().setStatus(JobProperties.SKIP);
					}
				}
			}
		}

		return;
	}
	
	public static void normalizeJobQueue(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		Date now = Calendar.getInstance().getTime();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			if (scheduledJob instanceof ExternalProgram) {
				if (scheduledJob.getJobProperties().getTime().before(now) || Arrays.binarySearch(TlosServer.getTlosParameters().getScheduledDays(), Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) < 0) {
					DateUtils.iterateNextDate(scheduledJob.getJobProperties());
				}
			}
		}

		return;
	}
	
	public static String getSimpleFormattedJobProperties(HashMap<String, Job> jobQueue) {

		StringBuilder sb = new StringBuilder();

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			Job scheduledJob = jobsIterator.next();
			JobProperties jobProperties = scheduledJob.getJobProperties();

			// TR
			sb.append(LocaleMessages.getString("JobQueueOperations.18") + jobProperties.getKey() + " => "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LocaleMessages.getString("JobQueueOperations.20") + jobProperties.getJobCommand() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LocaleMessages.getString("JobQueueOperations.22") + jobProperties.getLogFilePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LocaleMessages.getString("JobQueueOperations.24") + jobProperties.getPreviousTime()); //$NON-NLS-1$
			sb.append(LocaleMessages.getString("JobQueueOperations.25") + jobProperties.getExecutionDateStr()); //$NON-NLS-1$
			sb.append(LocaleMessages.getString("JobQueueOperations.26") + jobProperties.getCompletionDate()); //$NON-NLS-1$
			sb.append(LocaleMessages.getString("JobQueueOperations.27") + (jobProperties.getTime() == null ? "-" : DateUtils.getDate(jobProperties.getTime())) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LocaleMessages.getString("JobQueueOperations.29") + jobProperties.getWorkDuration()); //$NON-NLS-1$
			sb.append(LocaleMessages.getString("JobQueueOperations.30") + (jobProperties.getTime() == null ? "-" : DateUtils.getDate(jobProperties.getTime()))); //$NON-NLS-1$
			sb.append(LocaleMessages.getString("JobQueueOperations.31") + LocaleMessages.getString("JobQueueOperations.32")); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(LocaleMessages.getString("JobQueueOperations.33") + extractList(jobProperties.getJobDependencyInfoList()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		}

		return sb.toString();
	}

	public static String getHTMLFormattedJobProperties(HashMap<String, Job> jobQueue) {
		return getHTMLFormattedJobProperties(jobQueue, null);
	}

	public static String getHTMLFormattedJobProperties(HashMap<String, Job> jobQueue, String localizedMessage) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\"http://www.w3.org/tr/html4/loose.dtd\">"); //$NON-NLS-1$
		stringBuilder.append("<html>"); //$NON-NLS-1$
		stringBuilder.append("    <head>"); //$NON-NLS-1$
		stringBuilder.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>"); //$NON-NLS-1$
		stringBuilder.append("        <title>Tlos Scheduler</title>"); //$NON-NLS-1$
		stringBuilder.append("    </head>"); //$NON-NLS-1$
		stringBuilder.append("    <body>"); //$NON-NLS-1$
		stringBuilder.append("        <table border=\"0\" summary=\"" + LocaleMessages.getString("JobQueueOperations.44") + "\" align=\"center\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("            <tr>"); //$NON-NLS-1$
		stringBuilder.append("               <td>"); //$NON-NLS-1$
		stringBuilder.append("                    <img src=\"cid:likyajpg10976@likyateknoloji.com\" align=\"right\" width=\"100\" height=\"35\" alt=\"\"/>"); //$NON-NLS-1$
		stringBuilder.append("                    <tr align=\"right\">"); //$NON-NLS-1$
		stringBuilder.append("                        <td>"); //$NON-NLS-1$
		stringBuilder.append("                            <a href=\"http://" + TlosServer.getTlosParameters().getHostName() + ":" + TlosServer.getTlosParameters().getHttpAccessPort() + "/\" target=\"_blank\" title=\"" + LocaleMessages.getString("JobQueueOperations.54") + "\">" + LocaleMessages.getString("JobQueueOperations.56") + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		stringBuilder.append("                        </td>"); //$NON-NLS-1$
		stringBuilder.append("                    </tr>"); //$NON-NLS-1$
		stringBuilder.append("                </td>"); //$NON-NLS-1$
		stringBuilder.append("            </tr>"); //$NON-NLS-1$
		stringBuilder.append("            <tr>"); //$NON-NLS-1$
		stringBuilder.append("                <td align=\"center\">"); //$NON-NLS-1$

		if (localizedMessage == null) {
			stringBuilder.append("                    <h1>TLOS Scheduler</h1>" + LocaleMessages.getString("JobQueueOperations.64")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			stringBuilder.append("                    <h1>TLOS Scheduler</h1>" + localizedMessage);
		}

		stringBuilder.append("                </td>"); //$NON-NLS-1$
		stringBuilder.append("            </tr>"); //$NON-NLS-1$
		stringBuilder.append("            <tr>"); //$NON-NLS-1$
		stringBuilder.append("                <td colspan=\"2\">"); //$NON-NLS-1$
		stringBuilder.append("                <hr/>"); //$NON-NLS-1$
		stringBuilder.append("                </td>"); //$NON-NLS-1$
		stringBuilder.append("            </tr>"); //$NON-NLS-1$
		stringBuilder.append("            <tr>"); //$NON-NLS-1$
		stringBuilder.append("                <td colspan=\"2\">"); //$NON-NLS-1$
		stringBuilder.append("                    <table border=\"1\" summary=\"" + LocaleMessages.getString("JobQueueOperations.75") + "\" align=\"center\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("                        <caption>"); //$NON-NLS-1$
		stringBuilder.append("                            <em>" + LocaleMessages.getString("JobQueueOperations.78") + "</em>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("                        </caption>"); //$NON-NLS-1$
		stringBuilder.append("				        <tr>"); //$NON-NLS-1$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.82")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.84")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.86")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.88")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.90")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.92")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.94")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.96")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.98")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("				        <th>" + LocaleMessages.getString("JobQueueOperations.100")); //$NON-NLS-1$ //$NON-NLS-2$

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			Job scheduledJob = jobsIterator.next();
			JobProperties jobProperties = scheduledJob.getJobProperties();

			stringBuilder.append("				        <tr>"); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getKey()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getJobCommand()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getLogFilePath()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getPreviousTime()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getExecutionDateStr()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getCompletionDate()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + jobProperties.getWorkDuration()); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + (jobProperties.getTime() == null ? "-" : DateUtils.getDate(jobProperties.getTime()))); //$NON-NLS-1$
			stringBuilder.append("				        <td>" + LocaleMessages.getString("JobQueueOperations.111")); //$NON-NLS-1$ //$NON-NLS-2$
			stringBuilder.append("				        <td>" + extractList(jobProperties.getJobDependencyInfoList())); //$NON-NLS-1$
		}

		stringBuilder.append("                    </table>"); //$NON-NLS-1$
		stringBuilder.append("                </td>"); //$NON-NLS-1$
		stringBuilder.append("            </tr>"); //$NON-NLS-1$
		stringBuilder.append("        </table>"); //$NON-NLS-1$

		stringBuilder.append(pageFooter());

		stringBuilder.append("    </body>"); //$NON-NLS-1$
		stringBuilder.append("</html>"); //$NON-NLS-1$

		return stringBuilder.toString();

	}
	
	private static String extractList(ArrayList<DependencyInfo> depList) {
		
		Iterator<DependencyInfo> it = depList.iterator();
		
		if (!it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;) {
			DependencyInfo dependencyInfo = it.next();
			
			sb.append(dependencyInfo.getJobKey());
			
			if (!it.hasNext())
				return sb.append(']').toString();
			
			sb.append(',').append(' ');
		}
	}

	public static String getHTMLFormattedJobPropertiesOld(HashMap<String, Job> jobQueue, String localizedMessage) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""); //$NON-NLS-1$
		stringBuilder.append("\"http://www.w3.org/TR/html4/loose.dtd\">"); //$NON-NLS-1$
		stringBuilder.append("<html>"); //$NON-NLS-1$
		stringBuilder.append("<head>"); //$NON-NLS-1$
		stringBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-9\">"); //$NON-NLS-1$
		stringBuilder.append("<title>Tlos Scheduler</title>"); //$NON-NLS-1$
		stringBuilder.append("</head>"); //$NON-NLS-1$
		stringBuilder.append("<body>"); //$NON-NLS-1$

		stringBuilder.append("<TABLE border=\"0\" summary=\"" + LocaleMessages.getString("JobQueueOperations.44") + "\" align=\"center\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("<tr>"); //$NON-NLS-1$
		stringBuilder.append("<td>"); //$NON-NLS-1$
		stringBuilder.append("<img src=cid:likyajpg10976@likyateknoloji.com align=\"right\" width=100 height=35>"); //$NON-NLS-1$
		stringBuilder.append("<tr align=\"right\"><td>"); //$NON-NLS-1$
		stringBuilder.append("<a href=\"http://" + TlosServer.getTlosParameters().getHostName() + ":" + TlosServer.getTlosParameters().getHttpAccessPort() + "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("\" target=\"_blank\" title=\"" + LocaleMessages.getString("JobQueueOperations.54") + "\">" + LocaleMessages.getString("JobQueueOperations.56") + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		stringBuilder.append("</td></tr>"); //$NON-NLS-1$
		stringBuilder.append("</td>"); //$NON-NLS-1$
		stringBuilder.append("</tr>"); //$NON-NLS-1$

		stringBuilder.append("<tr>"); //$NON-NLS-1$
		stringBuilder.append("<td align=\"center\">"); //$NON-NLS-1$
		stringBuilder.append("<h1>TLOS Scheduler</h1>"); //$NON-NLS-1$

		if (localizedMessage == null) {
			stringBuilder.append(LocaleMessages.getString("JobQueueOperations.64")); //$NON-NLS-1$
		} else {
			stringBuilder.append(localizedMessage);
		}

		stringBuilder.append("</td>"); //$NON-NLS-1$
		stringBuilder.append("</tr>"); //$NON-NLS-1$
		stringBuilder.append("<tr>"); //$NON-NLS-1$
		stringBuilder.append("<td colspan=\"2\">"); //$NON-NLS-1$
		stringBuilder.append("<hr/>"); //$NON-NLS-1$
		stringBuilder.append("</td>"); //$NON-NLS-1$
		stringBuilder.append("</tr>"); //$NON-NLS-1$
		stringBuilder.append("<tr>"); //$NON-NLS-1$
		stringBuilder.append("<td colspan=\"2\">"); //$NON-NLS-1$

		stringBuilder.append("<TABLE border=\"1\" summary=\"" + LocaleMessages.getString("JobQueueOperations.75") + "\" align=\"center\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("<CAPTION><EM>" + LocaleMessages.getString("JobQueueOperations.78") + "</EM></CAPTION>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuilder.append("<TR>"); //$NON-NLS-1$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.82")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.84")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.86")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.88")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.90")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.92")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.94")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.96")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.98")); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuilder.append("<TH>" + LocaleMessages.getString("JobQueueOperations.100")); //$NON-NLS-1$ //$NON-NLS-2$

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {

			Job scheduledJob = jobsIterator.next();
			stringBuilder.append("<TR>"); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getKey()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getJobCommand()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getLogFilePath()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getPreviousTime()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getExecutionDateStr()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getCompletionDate()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + scheduledJob.getJobProperties().getWorkDuration()); //$NON-NLS-1$
			stringBuilder.append("<TD>" + DateUtils.getDate(scheduledJob.getJobProperties().getTime())); //$NON-NLS-1$
			stringBuilder.append("<TD>" + LocaleMessages.getString("JobQueueOperations.111")); //$NON-NLS-1$ //$NON-NLS-2$
			stringBuilder.append("<TD>" + extractList(scheduledJob.getJobProperties().getJobDependencyInfoList())); //$NON-NLS-1$
		}

		stringBuilder.append("</TABLE>"); //$NON-NLS-1$
		stringBuilder.append("</td>"); //$NON-NLS-1$
		stringBuilder.append("</tr>"); //$NON-NLS-1$
		stringBuilder.append("</TABLE>"); //$NON-NLS-1$
		stringBuilder.append(pageFooter());
		stringBuilder.append("</body>"); //$NON-NLS-1$
		stringBuilder.append("</html>"); //$NON-NLS-1$

		return stringBuilder.toString();

	}

	private static StringBuilder pageFooter() {

		StringBuilder footerValue = new StringBuilder();

		footerValue.append("        <br/>"); //$NON-NLS-1$
		footerValue.append("        <br/>"); //$NON-NLS-1$

		if (!TlosServer.isLicensed()) {
			footerValue.append("        <h4 align=\"center\">" + LocaleMessages.getString("JobQueueOperations.121") + "</h4>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		footerValue.append("        <hr size=\"1\" color=\"black\" width=\"80%\"/>"); //$NON-NLS-1$
		footerValue.append("        <h6 align=\"center\">" + TlosServer.getVersion()); //$NON-NLS-1$
		footerValue.append("            <br/>" + LocaleMessages.getString("JobQueueOperations.126")); //$NON-NLS-1$ //$NON-NLS-2$
		footerValue.append("            <br/>Ataşehir, İstanbul, Türkiye"); //$NON-NLS-1$
		footerValue.append("            <br/>"); //$NON-NLS-1$
		footerValue.append("            <a href=\"http://www.likyateknoloji.com\" title=\"www.likyateknoloji.com\">www.likyateknoloji.com</a>&nbsp;&nbsp;"); //$NON-NLS-1$
		footerValue.append("            <a href=\"mailto:bilgi@likyateknoloji.com\">bilgi@likyateknoloji.com</a>"); //$NON-NLS-1$
		footerValue.append("        </h6>"); //$NON-NLS-1$

		return footerValue;
	}

	public static boolean isAllRepetitive(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (!(scheduledJob instanceof RepetitiveExternalProgram)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllDisabled(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (scheduledJob.getJobProperties().getStatus() != JobProperties.DISABLED) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllStandart(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (!(scheduledJob instanceof ExternalProgram)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllStandartBlocker(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (!(scheduledJob instanceof ExternalProgram) || !scheduledJob.getJobProperties().isBlocker()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllStandartNonBlocker(HashMap<String, Job> jobQueue) {
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();
			if (scheduledJob instanceof ExternalProgram && scheduledJob.getJobProperties().isBlocker()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllSuccessOrSkipOrFail(HashMap<String, Job> jobQueue) {

		Iterator<Job> jobsIterator = jobQueue.values().iterator();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			int myJobsStatus = scheduledJob.getJobProperties().getStatus();

			if (myJobsStatus != JobProperties.DISABLED) {

				if (myJobsStatus != JobProperties.SUCCESS && myJobsStatus != JobProperties.SKIP && myJobsStatus != JobProperties.FAIL) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean hasAnyBodyUpperThanMe(int myPriority) {

		Iterator<Job> jobsIterator = TlosServer.getJobQueue().values().iterator();

		while (jobsIterator.hasNext()) {
			Job scheduledJob = jobsIterator.next();

			int myJobsStatus = scheduledJob.getJobProperties().getStatus();
			int jobsPriority = scheduledJob.getJobProperties().getPriority();
			Date timeToExecute = scheduledJob.getJobProperties().getTime();

			if (scheduledJob instanceof RepetitiveExternalProgram && myJobsStatus == JobProperties.READY && timeToExecute.before(Calendar.getInstance().getTime()) && myPriority > jobsPriority) {
				return true;
			}
		}

		return false;
	}
	
}
