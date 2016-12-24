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
package com.likya.tlos.web.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.ExternalProgram;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.jobs.ManuelExternalProgram;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.HtmlFileReaderV2;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.utils.ObjectUtils;
import com.likya.tlos.utils.TlosException;
import com.likya.tlos.utils.loaders.ScenarioLoader;
import com.likya.tlos.utils.sort.CommandComparator;
import com.likya.tlos.utils.sort.ExecutionTimeComparator;
import com.likya.tlos.utils.sort.JobGroupComparator;
import com.likya.tlos.utils.sort.WorkDurationComparator;
import com.likya.tlos.web.TlosWebConsole;
import com.sun.net.httpserver.HttpExchange;

public class ViewHandler extends TlosHttpHandler {

	private static String MY_SELECTED_FILTER = null;
	
	private static String MY_STATUS_ID = "type"; //$NON-NLS-1$
	private static String MY_GROUP_ID = "group_id"; //$NON-NLS-1$
	private static String MY_SORT_ID = "sort_id"; //$NON-NLS-1$
	
	private int typeId = -1;
	private int grpId = -1;
	private int sortId = -1;
	public static int sortDirection = 1;
	
	private static final int SORTTYPE_EXECUTION_TIME = 1000;
	private static final int SORTTYPE_WORK_DURATION = 2000;
	private static final int SORTTYPE_JOB_GROUP = 3000;
	private static final int SORTTYPE_COMMAND = 4000;
	
	public ViewHandler(TlosServer tlosServer, TlosWebConsole tlosWebConsole) {
		super(tlosServer, tlosWebConsole);
	}

	public void handle(HttpExchange httpExchange) throws IOException {

		InputStream inputStream;
		OutputStream os;
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		String response = null;

		inputStream = httpExchange.getRequestBody();
		String query = httpExchange.getRequestURI().getQuery();
		
		String ipAddress = null;
		
		if (httpExchange.getRemoteAddress() != null && httpExchange.getRemoteAddress().getAddress() != null) {
			ipAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
			TlosServer.getLogger().info(LocaleMessages.getString("ViewHandler.0") + ipAddress); //$NON-NLS-1$
		} else {
			TlosServer.getLogger().info(LocaleMessages.getString("ViewHandler.1")); //$NON-NLS-1$
		}
		
		
		if (query != null) {
			
			try {
				String queryStr = queryHandler(query);
				if(MY_SELECTED_FILTER == null) {
					new TlosException("MY_SELECTED_FILTER == null"); //$NON-NLS-1$
				} else if(MY_SELECTED_FILTER.equals(MY_STATUS_ID)) {
					typeId = Integer.parseInt(queryStr);
				} else if(MY_SELECTED_FILTER.equals(MY_GROUP_ID)) {
					grpId = Integer.parseInt(queryStr);
				} else if(MY_SELECTED_FILTER.equals(MY_SORT_ID)) {
					sortId = Integer.parseInt(queryStr);
					if(sortDirection == 1) {
						sortDirection = -1;
					} else {
						sortDirection = 1;
					}
				}
			} catch (TlosException e) {
				e.printStackTrace();
			}
		}

		if (httpExchange.getRequestMethod().equals("POST")) { //$NON-NLS-1$
			String bufferString = ""; //$NON-NLS-1$
			int i;
			while ((i = inputStream.read()) != -1) {
				bufferString = bufferString + (char) i;
			}
			
			try {
				
				HashMap<String, String> formMap = getFormMap(bufferString);
				String commandName = formMap.get("commandname").replace("+", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				String jobName = formMap.get("jobname"); //$NON-NLS-1$
				if(jobName != null) {
					jobName = URLDecoder.decode(jobName, "UTF-8");
				}

				String keyToken = formMap.get("keyToken"); //$NON-NLS-1$
				
				if(!keyToken.equals(tlosServer.getDateToken())) {
					commandName = REFRESH_STR;
				}
				
				if (commandName.equals(RETRY_STR)) {
					tlosCommInterface.retryExecution(jobName);
				} else if (commandName.equals(SETSUCCESS_STR)) {
					tlosCommInterface.setSuccess(jobName);
				} else if (commandName.equals(SKIP_STR)) {
					tlosCommInterface.skipJob(jobName);
				} else if (commandName.equals(STOP_STR)) {
					tlosCommInterface.stopJob(jobName);
				} else if (commandName.equals(PAUSE_STR)) {
					tlosCommInterface.pauseJob(jobName);
				} else if (commandName.equals(RESUME_STR)) {
					tlosCommInterface.resumeJob(jobName);
				} else if (commandName.equals(START_STR)) {
					tlosCommInterface.startJob(jobName);
				} else if (commandName.equals(SUSPENDTLOS_STR)) {
					tlosCommInterface.suspendTlos();
				} else if (commandName.equals(RESUMETLOS_STR)) {
					tlosCommInterface.resumeTlos();
				} else if (commandName.equals(NSTOP_STR)) {
					tlosCommInterface.gracefulShutDown();
					terminate = true;
					response = endPage();
				} else if (commandName.equals(FSTOP_STR)) {
					tlosCommInterface.forceFullShutDown();
					terminate = true;
					response = endPage();
				} else if (commandName.equals(REFRESH_STR)) {
					// Do nothing
				} else if (commandName.equals(SAVE_STR)) {
					byte[] data = FileUtils.saveJobListToExcel(byteArrayOS).toByteArray();

					httpExchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + LocaleMessages.getString("ViewHandlerV2.18") + ".xls"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					httpExchange.getResponseHeaders().set("Content-Type", "application/vnd.ms-excel"); //$NON-NLS-1$ //$NON-NLS-2$
					httpExchange.sendResponseHeaders(200, data.length);
					os = httpExchange.getResponseBody();
					os.write(data);
					os.close();
					return;
				} else if (commandName.equals(DISABLE_STR)) {
					tlosCommInterface.disableJob(jobName);
				} else if (commandName.equals(ENABLE_STR)) {
					tlosCommInterface.enableJob(jobName);
				} 
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if(response == null) {
			try {
				response = buildWebPage();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		byte outputByteArray [] = response.getBytes("utf-8");
		httpExchange.sendResponseHeaders(200, outputByteArray.length);
		os = httpExchange.getResponseBody();
		os.write(outputByteArray);
		os.close();
	}
	
	public String buildWebPage() throws Exception {
		HtmlFileReaderV2 htmlFileReader = new HtmlFileReaderV2();
		
		String str = ""; //$NON-NLS-1$

		try {
			str += htmlFileReader.getHeader1();
			
			str += htmlFileReader.getCss("css/SyntaxHighlighter.css"); //$NON-NLS-1$
			str += htmlFileReader.getCss("css/ddsmoothmenu.css"); //$NON-NLS-1$
			str += htmlFileReader.getCss("css/tloslite.css"); //$NON-NLS-1$
			str += htmlFileReader.getCss("css/grid.css"); //$NON-NLS-1$

			str += htmlFileReader.getJs("js/ModalPopups.js"); //$NON-NLS-1$
			str += htmlFileReader.getAboutPopupJs();
			str += htmlFileReader.getJs("js/jquery.min.js"); //$NON-NLS-1$
			String tmp = "/***********************************************\n";//$NON-NLS-1$
			tmp+= "* Smooth Navigational Menu- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)\n";//$NON-NLS-1$
			tmp+= "* This notice MUST stay intact for legal use\n";//$NON-NLS-1$
			tmp+= "* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code\n";//$NON-NLS-1$
			tmp+= "***********************************************/\n";//$NON-NLS-1$
			str += htmlFileReader.getJs("js/ddsmoothmenu.js", tmp); //$NON-NLS-1$

			str += HtmlFileReaderV2.getSection("header2.html"); //$NON-NLS-1$
			str += getHeader3();
			
			str += formBegin();
			str += getSmoothMenu();
			
			str += getGrid1();
			str += getGrid2();
			str += getGrid3();
			str += HtmlFileReaderV2.getSection("grid4.html"); //$NON-NLS-1$
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return str;
	}
	
	public String getFilterMenu() {
		String str = ""; //$NON-NLS-1$
		if(typeId != -1 || grpId != -1) { 
			if(typeId != -1) {
				str += "{" + LocaleMessages.getString("ViewHandler.44") + ObjectUtils.getStatusAsString(typeId) + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if(grpId != -1) {
				str += "{" + LocaleMessages.getString("ViewHandler.45") + TlosServer.getTlosParameters().getGroupList().get(grpId) + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return str;
	}
	
	public ArrayList<Job> sortJobList() {
		HashMap<String, Job> jobQueue = TlosServer.getJobQueue();
		ArrayList<Job> jobQueueArray = new ArrayList<Job>();

		Job job = null;
		
		Iterator<Job> jobsIterator = jobQueue.values().iterator();
		
		while (jobsIterator.hasNext()) {
			job = jobsIterator.next();
			
			if((typeId != -1 && job.getJobProperties().getStatus() != typeId)) {
				continue;
			}
			if(grpId != -1 && job.getJobProperties().getGroupId() != grpId) {
				continue;
			}
			//if(job.getJobProperties().getExecutionDate() != null) {
				jobQueueArray.add(job);
			//} 
		}
		
		switch(sortId) {
		
		case SORTTYPE_EXECUTION_TIME:
			Collections.sort(jobQueueArray, new ExecutionTimeComparator());
			break;

		case SORTTYPE_WORK_DURATION:
			Collections.sort(jobQueueArray, new WorkDurationComparator());
			break;

		case SORTTYPE_COMMAND:
			Collections.sort(jobQueueArray, new CommandComparator());
			break;

		case SORTTYPE_JOB_GROUP:
			Collections.sort(jobQueueArray, new JobGroupComparator());
			break;
		} 
		return jobQueueArray;
	}
	
	public String jobColor(int status) {
		String color = "black"; //$NON-NLS-1$

		switch (status) {
		case JobProperties.READY:// ready
			color = "#999999"; //$NON-NLS-1$
			break;
		case JobProperties.WAITING:// waiting
			color = "black"; //$NON-NLS-1$
			break;
		case JobProperties.WORKING:// working
			color = "blue"; //$NON-NLS-1$
			break;
		case JobProperties.SUCCESS:// success
			color = "green"; //$NON-NLS-1$
			break;
		case JobProperties.FAIL:// fail
			// color = "red";
			color = "#663399"; //$NON-NLS-1$
			break;
		case JobProperties.TIMEOUT:// timeout
			color = "pink"; //$NON-NLS-1$
			break;
		case JobProperties.SKIP:// skip
			color = "orange"; //$NON-NLS-1$
			break;
		case JobProperties.STOP:// stop
			color = "#CC0066"; //$NON-NLS-1$
			break;
		case JobProperties.PAUSE:// pause
			color = "#6699CC"; //$NON-NLS-1$
			break;
		}

		return color;
	}

	public String jobImage(int status) throws Exception {
		String imageName = "black"; //$NON-NLS-1$

		switch (status) {
		case JobProperties.READY:// ready
			imageName = "clock1V2.png"; //$NON-NLS-1$
			break;
		case JobProperties.WAITING:// waiting
			imageName = "kilitV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.WORKING:// working
			imageName = "kosu2.png"; //$NON-NLS-1$
			break;
		case JobProperties.SUCCESS:// success
			imageName = "ok2V2.png"; //$NON-NLS-1$
			break;
		case JobProperties.FAIL:// fail
			imageName = "errorV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.TIMEOUT:// timeout
			imageName = "timeoutV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.SKIP:// skip
			imageName = "skipV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.STOP:// stop
			imageName = "stopV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.PAUSE:// pause
			imageName = "pauseV2.png"; //$NON-NLS-1$
			break;
		case JobProperties.MSTART:// pause
			imageName = "kosu2.png"; //$NON-NLS-1$
			break;
		case JobProperties.DISABLED:// pause
			imageName = "disabledJob.png"; //$NON-NLS-1$
			break;		
		}

		return imageName;
	}


	public String endPage() throws Exception {

		StringBuilder buff;

		buff = new StringBuilder();
		buff.append("<script type=\"text/javascript\">");
		buff.append("window.open(\"closer.htm\", '_self');");
		buff.append("window.close();");;
		buff.append("</script>");
		
		buff.append(HtmlPages.documentHeader("TLOS Scheduler - " + LocaleMessages.getString("ViewHandler.2"))); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<p><pre>"); //$NON-NLS-1$

		buff.append(logoSection(false));
		
		//buff.append("<body>"); //$NON-NLS-1$

		buff.append("<HR COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$
		buff.append("<h3 align=\"center\" style=\"color: #FF0000\">TLOS Scheduler - " + LocaleMessages.getString("ViewHandler.3") + "</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		buff.append(HtmlPages.pageFooter());
		
		buff.append("</body></html>\n"); //$NON-NLS-1$

		return buff.toString();
	}
	
	public String queryHandler(String urlQuery) throws TlosException {

		String name;

		StringTokenizer st = new StringTokenizer(urlQuery, "="); //$NON-NLS-1$

		if (st.hasMoreTokens()) {
			name = st.nextToken();
			if (name.equals(MY_STATUS_ID) || name.equals(MY_GROUP_ID) || name.equals(MY_SORT_ID)) {
				MY_SELECTED_FILTER = name;
				String istek;
				if ((istek = st.nextToken()) != null) {
					return istek;
				}
			} else {
				throw new TlosException(LocaleMessages.getString("ViewHandler.4")); //$NON-NLS-1$
			}
		}
		
		throw new TlosException(LocaleMessages.getString("ViewHandler.4")); //$NON-NLS-1$
	}	
	
	public String getSmoothMenu() {

		String str = ""; //$NON-NLS-1$
	
		str += "<div id='tloslitemenu' class='ddsmoothmenu'>"; //$NON-NLS-1$
		str += "	<ul>"; //$NON-NLS-1$
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.12") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(!terminate) {
			str += "		<ul>"; //$NON-NLS-1$
			
			if(TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) {
				str += "		<li><a href=\"#\" onClick=\"javascript:if(!confirm('" + LocaleMessages.getString("ViewHandler.13") + "')){return false;} else {document.getElementById('commandnameId').value='" + RESUMETLOS_STR + "';document.forms[0].submit();};\">" + LocaleMessages.getString("ViewHandler.14") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			} else {
				str += "		<li><a href=\"#\" onClick=\"javascript:if(!confirm('" + LocaleMessages.getString("ViewHandler.15") + "')){return false;} else {document.getElementById('commandnameId').value='" + SUSPENDTLOS_STR + "';document.forms[0].submit();};\">" + LocaleMessages.getString("ViewHandler.16") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			}
			str += "			<li><a href=\"#\" onClick=\"javascript:if(!confirm('" + LocaleMessages.getString("ViewHandler.17") + "')){return false;} else {document.getElementById('commandnameId').value='" + NSTOP_STR + "';document.forms[0].submit();};\">" + LocaleMessages.getString("ViewHandler.18") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			str += "			<li><a href=\"#\" onClick=\"javascript:if(!confirm('" + LocaleMessages.getString("ViewHandler.19") + "')){return false;} else {document.getElementById('commandnameId').value='" + FSTOP_STR + "';document.forms[0].submit();};\">" + LocaleMessages.getString("ViewHandler.20") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			str += "		</ul>"; //$NON-NLS-1$
		}
		str += "		</li>"; //$NON-NLS-1$		
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.21") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		str += "				<li><a href=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=0\" title=\"" + LocaleMessages.getString("ViewHandler.22") + "\"\">" + LocaleMessages.getString("ViewHandler.23") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.24") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		str += "				<li><a href='#' onClick=\"javascript:window.open('logdetail?fname=tlosConfig', 'log_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=400,width=400');\";>" + LocaleMessages.getString("ViewHandler.25") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "				<li><a href='#' onClick=\"javascript:window.open('logdetail?fname=tlosScenario', 'log_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=400,width=400');\">" + LocaleMessages.getString("ViewHandler.26") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "				<li><a href='#' onClick=\"javascript:window.open('logdetail?fname=tlosLog', 'log_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=400,width=400');\">" + LocaleMessages.getString("ViewHandler.27") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "				<li><a href='#' onClick=\"javascript:window.open('logdetail?fname=tlosTrace', 'log_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=400,width=400');\">" + LocaleMessages.getString("ViewHandler.28") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$		
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.29") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=-1\" title=\"" + LocaleMessages.getString("ViewHandler.70") + "\">" + LocaleMessages.getString("ViewHandler.71") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.READY + "\" title=\"" + LocaleMessages.getString("ViewHandler.72") + "\">" + LocaleMessages.getString("ViewHandler.30") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.WAITING + "\" title=\"" + LocaleMessages.getString("ViewHandler.76") + "\">" + LocaleMessages.getString("ViewHandler.34") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.WORKING + "\" title=\"" + LocaleMessages.getString("ViewHandler.73") + "\">" + LocaleMessages.getString("ViewHandler.31") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.SUCCESS + "\" title=\"" + LocaleMessages.getString("ViewHandler.74") + "\">" + LocaleMessages.getString("ViewHandler.32") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.FAIL + "\" title=\"" + LocaleMessages.getString("ViewHandler.78") + "\">" + LocaleMessages.getString("ViewHandler.36") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.TIMEOUT + "\" title=\"" + LocaleMessages.getString("ViewHandler.75") + "\">" + LocaleMessages.getString("ViewHandler.33") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.SKIP + "\" title=\"" + LocaleMessages.getString("ViewHandler.751") + "\">" + LocaleMessages.getString("ViewHandler.331") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.STOP + "\" title=\"" + LocaleMessages.getString("ViewHandler.752") + "\">" + LocaleMessages.getString("ViewHandler.332") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.PAUSE + "\" title=\"" + LocaleMessages.getString("ViewHandler.77") + "\">" + LocaleMessages.getString("ViewHandler.35") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "				<li><a href=\"/?" + MY_STATUS_ID + "=" + JobProperties.DISABLED + "\" title=\"" + LocaleMessages.getString("ViewHandler.755") + "\">" + LocaleMessages.getString("ViewHandler.335") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.37") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		str += "				<li><a href=\"/?" + MY_GROUP_ID + "=-1\" title=\"" + LocaleMessages.getString("ViewHandler.38") + "\">" + LocaleMessages.getString("ViewHandler.39") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		Iterator<String> groupIdIterator = TlosServer.getTlosParameters().getGroupList().values().iterator();
		
		while(groupIdIterator.hasNext()) {
			String groupName = groupIdIterator.next();
			int id = ScenarioLoader.getJobGroupId(TlosServer.getTlosParameters().getGroupList(), groupName);
			
			//uzun olan grup adlar� k�salt�ld�
			String truncatedGroupName = Job.getTruncatedString(groupName, 25);
			
			str += "			<li><a href=\"/?" + MY_GROUP_ID + "=" + id + "\" title=\"" + groupName + "\">" + truncatedGroupName + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandlerV2.0") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		
		if(sortDirection == 1) {
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_EXECUTION_TIME + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.1") + "\">" + LocaleMessages.getString("ViewHandlerV2.2") + "</a></li>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_WORK_DURATION + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.3") + "\">" + LocaleMessages.getString("ViewHandlerV2.4") + "</a></li>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_COMMAND + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.5") + "\">" + LocaleMessages.getString("ViewHandlerV2.6") + "</a></li>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_JOB_GROUP + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.7") + "\">" + LocaleMessages.getString("ViewHandlerV2.8") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		
		} else if(sortDirection == -1){
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_EXECUTION_TIME + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.1") + "\">" + LocaleMessages.getString("ViewHandlerV2.9") + "</a></li>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_WORK_DURATION + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.3") + "\">" + LocaleMessages.getString("ViewHandlerV2.10") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_COMMAND + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.5") + "\">" + LocaleMessages.getString("ViewHandlerV2.11") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			str += "				<li><a href=\"/?" + MY_SORT_ID + "=" + SORTTYPE_JOB_GROUP + "\" title=\"" + LocaleMessages.getString("ViewHandlerV2.7") + "\">" + LocaleMessages.getString("ViewHandlerV2.12") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			
		}
		
		if(sortDirection == 1) {
			
		} else if(sortDirection == -1){
			
		}
		
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$
		str += "		<li><a href='#' onClick='javascript:{document.getElementById(\"commandnameId\").value=\"" + REFRESH_STR + "\";document.forms[0].submit();}\'>" + LocaleMessages.getString("ViewHandler.40") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		str += "		<li><a href='#' onClick='javascript:{document.getElementById(\"commandnameId\").value=\"" + SAVE_STR + "\";document.forms[0].submit();}\'>" + LocaleMessages.getString("ViewHandlerV2.24") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		str += "		<li><a href='#'>" + LocaleMessages.getString("ViewHandler.41") + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			<ul>"; //$NON-NLS-1$
		str += "				<li><a href='http://www.likyateknoloji.com/' target='_blank'>" + LocaleMessages.getString("ViewHandler.42") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "				<li><a href='javascript:ModalTlosLiteAboutBox();'>" + LocaleMessages.getString("ViewHandler.43") + "</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "			</ul>"; //$NON-NLS-1$
		str += "		</li>"; //$NON-NLS-1$
		str += "	</ul>"; //$NON-NLS-1$
		str += "	<br style='clear: left' />"; //$NON-NLS-1$
		str += "</div>"; //$NON-NLS-1$
	
		return str;
	}
	
	public String getGrid2() {

		String str = ""; //$NON-NLS-1$
		str += "<thead>"; //$NON-NLS-1$
		str += "<tr>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr1\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.46") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 100%;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		//yönetim kısmı taşındı
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.58") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div>&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		//yönetim kısmı sonu
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.47") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 100%;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.471") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 100%;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr1\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.48") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.49") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr1\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.50") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.51") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr1\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.52") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.531") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$		
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div style=\"cursor: e-resize; display: block; height: 24px;\" onmousedown=\"new Ice.ResizableGrid(event);\" class=\"iceDatTblResHdlr\">&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.53") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div>&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.54") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div>&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.55") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div>&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.56") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th valign=\"top\" class=\"datTblResBor\">"; //$NON-NLS-1$
		str += "		<div>&nbsp;</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "	<th class=\"datTblColHdr2\">"; //$NON-NLS-1$
		str += "		<div>"; //$NON-NLS-1$
		str += "			<span class=\"iceOutTxt\">" + LocaleMessages.getString("ViewHandler.57") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</th>"; //$NON-NLS-1$
		str += "</tr>"; //$NON-NLS-1$
		str += "</thead>"; //$NON-NLS-1$
	
		return str;
	}
	
	public String getGrid1() {

		String str = ""; //$NON-NLS-1$
		str += "		<div id=\"TlosLiteContainer\" class=\"none\">"; //$NON-NLS-1$
		str += "			<div>"; //$NON-NLS-1$
		str += "				<div id=\"TlosLiteGridContainer\">"; //$NON-NLS-1$
		str += "					<div id=\"TlosLiteGridContainerInner\">"; //$NON-NLS-1$
		str += "						<div class=\"top\">"; //$NON-NLS-1$
		str += "							<div><div>"; //$NON-NLS-1$
		if (TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) {
			str += "<div align=\"center\"><font color=\"red\">"; //$NON-NLS-1$
			// str += "							<H2>" + LocaleMessages.getString("ViewHandler.7") + "</H2><br><H4>" + LocaleMessages.getString("ViewHandler.8") + "</H4>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			str += "							   <blink><strong>" + LocaleMessages.getString("ViewHandler.7") + "</strong></blink> " + LocaleMessages.getString("ViewHandler.8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			str += "</div></font>"; //$NON-NLS-1$
		}

		if(TlosServer.isLoadTest()) {
			str += "							<font color=\"red\"><h2 align=\"left\"><div align=\"center\">" + LocaleMessages.getString("ViewHandler.9") + "</div></h5></font>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (TlosServer.isThresholdOverflow()) {
			str += "<div align=\"center\"><font color=\"red\">"; //$NON-NLS-1$
			str += LocaleMessages.getString("ViewHandler.10")+ TlosServer.getNumOfActiveJobs()+ LocaleMessages.getString("ViewHandler.11") + TlosServer.getTlosParameters().getSchedulerLowerThreshold(); //$NON-NLS-1$ //$NON-NLS-2$
			str += "</div></font>"; //$NON-NLS-1$
		}	
		
		str += " 							</div></div>"; //$NON-NLS-1$
		str += "						</div>"; //$NON-NLS-1$
		str += "						<div class=\"middle\">"; //$NON-NLS-1$
		str += "							<div class=\"r\">"; //$NON-NLS-1$
		str += "								<div class=\"c\">"; //$NON-NLS-1$
		str += "									<div class=\"c2\">"; //$NON-NLS-1$
		str += "										<div class=\"lt-titlebar\">"; //$NON-NLS-1$
		str += "											<div class=\"float-container\">"; //$NON-NLS-1$
		str += "												<div class=\"titlebar-title-panel\">"; //$NON-NLS-1$
		str += "													<h2>" + LocaleMessages.getString("ViewHandlerV2.13") + "</h2>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "												</div>"; //$NON-NLS-1$
		str += "												<div class=\"titlebar-button-panel\">&nbsp;</div>"; //$NON-NLS-1$
		str += "											</div>"; //$NON-NLS-1$
		str += "										</div>"; //$NON-NLS-1$
		str += "										<div class=\"window-content\">"; //$NON-NLS-1$
		str += "											<table id=\"templatePanelStack\" class=\"icePnlStk\">"; //$NON-NLS-1$
		str += "												<tbody>"; //$NON-NLS-1$
		str += "													<tr class=\"icePnlStkRow\">"; //$NON-NLS-1$
		str += "														<td class=\"icePnlStkCol\">"; //$NON-NLS-1$
		str += "															<div id=\"jobViewPanel\" class=\"icePnlGrp\">"; //$NON-NLS-1$
		str += "																<div style=\"text-align: center;\" id=\"jvp:j_idt139:j_idt140\" class=\"icePnlGrp formSeparator\">"; //$NON-NLS-1$
		str += "																	<h2>"; //$NON-NLS-1$
		str += "																		<span>" + LocaleMessages.getString("TlosServer.31") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "																		<span>&nbsp;</span>"; //$NON-NLS-1$
		str += "																		<span>" + TlosServer.getTlosParameters().getScenarioName() + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
		str += "																	</h2>"; //$NON-NLS-1$
		str += "																</div>"; //$NON-NLS-1$
		str += "																<br>"; //$NON-NLS-1$
		str += "																<br>"; //$NON-NLS-1$
		str += "																<div class=\"icePnlGrp tlosFormBorderHighlight\">"; //$NON-NLS-1$
		str += "																	<table width=\"100%\" cellspacing=\"0\" >"; //$NON-NLS-1$

		return str;
	}

	public static String getHeader3() {
		
		HtmlFileReaderV2 htmlFileReader = new HtmlFileReaderV2();
		
		String str = ""; //$NON-NLS-1$
		
		str += "<div class=\"lt-window-content\">"; //$NON-NLS-1$
		str += "	<div class=\"toolbar\">"; //$NON-NLS-1$
		str += "		<div class=\"toolbar-menu\">"; //$NON-NLS-1$
		str += "			<div class=\"tbframe\">"; //$NON-NLS-1$
		str += "				<div class=\"tbframeTop\"><div><div>&nbsp;</div></div></div>"; //$NON-NLS-1$
		str += "				<div id=\"topMenu\" class=\"tbframeContent\">"; //$NON-NLS-1$
		str += "					<ul>"; //$NON-NLS-1$
		str += "						<li class=\"first\">"; //$NON-NLS-1$
		str += "							<!--a title=\"" + LocaleMessages.getString("ViewHandlerV2.14") + "\" href=\"#\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "					 		<img src=\"images/tlosent/home_ena.gif\" alt=\"" + LocaleMessages.getString("ViewHandlerV2.14") + " \">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "							Home"; //$NON-NLS-1$
		str += "							</a-->"; //$NON-NLS-1$
		int numberOfJobs = TlosServer.getJobQueue().size();
		str += "				{ " + LocaleMessages.getString("ViewHandler.5") + numberOfJobs + " } <span id=\"numofjobslisted\">{ " + LocaleMessages.getString("ViewHandler.6") + "0 }</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		str += "				  		</li>"; //$NON-NLS-1$
		str += "				  		<li>"; //$NON-NLS-1$
		str += "							<!--a title=\"��k�� \" onfocus=\"setFocus(this.id);\" onclick=\"var form=formOf(this);form['j_idt59:j_idcl'].value='j_idt59:logoutButton';iceSubmit(form,this,event);form['j_idt59:j_idcl'].value='';return false;\" onblur=\"setFocus('');\" id=\"j_idt59:logoutButton\" href=\"javascript:;\" class=\"iceCmdLnk\"><span id=\"j_idt59:j_idt61\" class=\"iceOutTxt\">Logout</span></a-->"; //$NON-NLS-1$
		str += "				  		</li>"; //$NON-NLS-1$
//		str += "				  		<!--li>";
//		str += "							<a href=\"#\">";
//		str += "					  		Preferences";
//		str += "							</a>";
//		str += "				  		</li>";
//		str += "				  		<li>";					
//		str += "							<a title=\"Start Recording\" onclick=\"return wls.console.startAdHocRecord('frsc','0x2be8eb1522dd61bae321605a2eda1968d3493a71712aa601');\" id=\"recordLink\" href=\"#\">";						   
//		str += "						  		<img src=\"images/tlosent/recording.gif\" id=\"recordingIcon\" alt=\"Start Recording \">";
//		str += "								Record";								
//		str += "							</a>";							
//		str += "				  		</li>";
//		str += "				  		<li>";
//		str += "							<a title=\"Help - New window\" onclick=\"return wls.console.launchHelp()\" href=\"#\">";
//		str += "								Help";
//		str += "							</a>";
//		str += "				  		</li-->";
		str += "					</ul>"; //$NON-NLS-1$
		str += "				</div>"; //$NON-NLS-1$
		str += "				<div class=\"tbframeBottom\"><div><div>&nbsp;</div></div></div>"; //$NON-NLS-1$
		str += "			</div>"; //$NON-NLS-1$
		str += "		</div>"; //$NON-NLS-1$
		str += "		<div class=\"toolbar-info\">"; //$NON-NLS-1$
		str += "			<div class=\"tbframe\">"; //$NON-NLS-1$
		str += "				<div class=\"tbframeTop\"><div><div>&nbsp;</div></div></div>"; //$NON-NLS-1$
		str += "				<div class=\"tbframeContent\">";		 //$NON-NLS-1$
		str += "					<div id=\"welcome\">"; //$NON-NLS-1$
		str += "						<p>" + LocaleMessages.getString("ViewHandlerV2.15");  //$NON-NLS-1$ //$NON-NLS-2$
		str += "						<span id=\"j_idt59:j_idt66\" class=\"iceOutTxt\">" + tlosWebConsole.getSessionUserName() + "</span></p>"; //$NON-NLS-1$ //$NON-NLS-2$
		str += "			  		</div>"; //$NON-NLS-1$
		str += "			  		<div id=\"domain\">"; //$NON-NLS-1$
		str += "						<p>"; //$NON-NLS-1$
		str += "						<strong>"; //$NON-NLS-1$
		str += htmlFileReader.getJs("js/liveclock.js"); //$NON-NLS-1$
		str += "						</strong>"; //$NON-NLS-1$
		str += "						</p>";									 //$NON-NLS-1$
		str += "		  			</div>"; //$NON-NLS-1$
		str += "				</div>"; //$NON-NLS-1$
		str += "				<div class=\"tbframeBottom\"><div><div>&nbsp;</div></div></div>"; //$NON-NLS-1$
		str += "			</div>"; //$NON-NLS-1$
		str += "		</div>"; //$NON-NLS-1$
		str += "	</div>";	 //$NON-NLS-1$
		str += "</div>"; //$NON-NLS-1$
		
		
		return str;
	}

	public String getGrid3() {

		String str = ""; //$NON-NLS-1$
		
		str += "<tbody>"; //$NON-NLS-1$
			
		StringBuilder buff = new StringBuilder();;
		String color;
		String jobImage = null;
		Job tlosJob = null;
		HashMap<String, Job> jobQueue = TlosServer.getJobQueue();
		
		Iterator<Job> jobsIterator = null;
		
		if(sortId == -1) {
			jobsIterator = jobQueue.values().iterator();
		} else {
			ArrayList<Job> jobList = sortJobList();
			jobsIterator = jobList.iterator();
		}
		
		int numOfJobsListed = 0;
		
		while (jobsIterator.hasNext()) {
			tlosJob = jobsIterator.next();
			
			if((typeId != -1 && tlosJob.getJobProperties().getStatus() != typeId)) {
				continue;
			}
			if(grpId != -1 && tlosJob.getJobProperties().getGroupId() != grpId) {
				continue;
			}

			color = jobColor(tlosJob.getJobProperties().getStatus());
			try {
				jobImage = jobImage(tlosJob.getJobProperties().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			buff.append("<tr class=\"iceDatTblRow1\">\n"); //$NON-NLS-1$
			if(JobProperties.FAIL == tlosJob.getJobProperties().getStatus()) {
				buff.append("	<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + /*title + */"' href='#' onClick=\"javascript:window.open('msgdetail?jobName=" + tlosJob.getJobProperties().getKey().toString() + "', 'msg_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=300,width=500');\"><img src='/images/" + jobImage + "'" + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				buff.append("	<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + "<img src='/images/" + jobImage + "'" + "</span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
			buff.append("	<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			//yönetim butonları taşındı
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">");  //$NON-NLS-1$ //$NON-NLS-2$

			String jobName = tlosJob.getJobProperties().getKey().toString();
			
			if(tlosJob.getJobProperties().getStatus() != JobProperties.DISABLED && tlosJob.getJobProperties().isPausable() && tlosJob.getJobProperties().isStartable() && !tlosJob.getJobProperties().isManuel()) {
				// Disable işlemleri
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.91") + "' src=\"" + "images/disableJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.89") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.DISABLE_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.87") + "' src=\"" + "images/startJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.66") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.START_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.85") + "' src=\"" + "images/pauseJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.67") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.PAUSE_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if(tlosJob.getJobProperties().getStatus() == JobProperties.DISABLED) {
				// Enable işlemleri
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.92") + "' src=\"" + "images/enableJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.90") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.ENABLE_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if ((tlosJob.getJobProperties().getStatus() == JobProperties.FAIL) || (tlosJob.getJobProperties().getStatus() == JobProperties.STOP)) {
				if(tlosJob.getJobProperties().isSafeRestart()) {
					buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.80") + "' src=\"" + "images/retryJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.59") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.RETRY_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false; }\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				} else {
					buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.80") + "' src=\"" + "images/retryJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.60") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.RETRY_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false; }\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				}
				if(tlosJob instanceof ExternalProgram) {
					buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.82") + "' src=\"" + "images/setSuccessJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.61") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.SETSUCCESS_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
					buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.83") + "' src=\"" + "images/skipJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.62") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.SKIP_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				}
			} else if(tlosJob.getJobProperties().isStopable()) {
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.84") + "' src=\"" + "images/stopJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.63") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.STOP_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if(tlosJob.getJobProperties().isPausable() && !tlosJob.getJobProperties().isStartable()) {
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.85") + "' src=\"" + "images/pauseJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.64") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.PAUSE_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if(tlosJob.getJobProperties().getStatus() == JobProperties.PAUSE) {
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.86") + "' src=\"" + "images/resumeJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.65") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.RESUME_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.82") + "' src=\"" + "images/setSuccessJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.61") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.SETSUCCESS_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.83") + "' src=\"" + "images/skipJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.62") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.SKIP_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if(tlosJob.getJobProperties().isPausable() && tlosJob.getJobProperties().isStartable() && !tlosJob.getJobProperties().isManuel()) {
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.87") + "' src=\"" + "images/startJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.66") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.START_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.85") + "' src=\"" + "images/pauseJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.67") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.PAUSE_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else if(tlosJob.getJobProperties().isStartable()) {
				// Sadece manuel işler için kullanılıyor
				buff.append("<input name=\"commandname\" type=\"image\" title='" + LocaleMessages.getString("ViewHandler.87") + "' src=\"" + "images/startJob.png" + "\" onClick=\"if(confirm('" + jobName + LocaleMessages.getString("ViewHandler.66") + "')) { document.getElementById('commandnameId').value = '" + TlosHttpHandler.START_STR + "'; document.forms[0].jobname.value = '" + jobName + "'; submit();} else { return false;}\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
			} else {
				buff.append("-"); //$NON-NLS-1$
			}
			buff.append("</span></td>"); //$NON-NLS-1$
			//yönetim butonları sonu
			
			buff.append("	<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			// parametereler
			String paramText = tlosJob.getJobProperties().getJobParamList();
			String paramButton = "";
			if(paramText != null && !paramText.equals("") /** Bağımlılık nedeni ile kaldırıldı && tlosJob.getJobProperties().isStartable() */) {
				// jobname=job3&action=set&value=
				paramButton = "<button onclick=\"myFunction('" +  LocaleMessages.getString("ViewHandler.872") + " ', '" + jobName + "'," + "'jobname=" + jobName + "&action=set&value='" + ");return false;\">" + LocaleMessages.getString("ViewHandler.871") + "</button>";
				buff.append("<input id = \"jobParameter" + "_" + jobName + "\" name=\"jobParameter" + "_" + jobName + "\" type=\"hidden\" value=\"" + (paramText == null ? "" : paramText) + "\">");
			}

			//uzun job isimleri kısaltıldı
			String jobKey = Job.getTruncatedString(tlosJob.getJobProperties().getKey().toString(), 30);
			buff.append("	<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + "<a title='" + tlosJob.getJobProperties().getKey().toString() + "'>" + jobKey + "</a>" + "</span><br/>" + paramButton + "</td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("	<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			// İş Tipi
			String jobType = LocaleMessages.getString("ViewHandler.880");
			if(tlosJob.getJobProperties().isStandart()) {
				//
			} else if(tlosJob.getJobProperties().isManuel()) {
				jobType = LocaleMessages.getString("ViewHandler.881");
			} else {
				jobType = LocaleMessages.getString("ViewHandler.882");
			}
			
			buff.append("	<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + "<a title='" + jobType + "'>" + jobType + "</a>" + "</span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("	<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			//uzun grup isimleri kısaltıldı
			String groupName = Job.getTruncatedString(TlosServer.getTlosParameters().getGroupList().get(tlosJob.getJobProperties().getGroupId()), 20);
			
			buff.append("	<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + "<a title='" + TlosServer.getTlosParameters().getGroupList().get(tlosJob.getJobProperties().getGroupId()) + "'>" + groupName + "</a>" + "</span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("	<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			String jobcommandStr = tlosJob.getJobProperties().getJobCommand();
			String tmp = jobcommandStr;
			if(tmp.lastIndexOf(System.getProperty("file.separator")) > 0) { //$NON-NLS-1$
				tmp = tmp.substring(tmp.lastIndexOf(System.getProperty("file.separator")) + 1); //$NON-NLS-1$
			}
			if(tmp.indexOf('.') > 0) {
				tmp = tmp.substring(0, tmp.indexOf('.'));
			}
			
			//uzun jobcommand isimleri kısaltıldı
			tmp = Job.getTruncatedString(tmp, 30);

			if (tlosJob.getJobProperties().getJobType().toLowerCase().equals(ScenarioLoader.SYSTEM_PROCESS.toLowerCase())) {
				if(FileUtils.checkFile(jobcommandStr)) {
					String title = jobcommandStr;
					buff.append("<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + title + "' href='#' onClick=\"javascript:window.open('jobdetail?fname=" + tlosJob.getJobProperties().getKey().toString() + "', 'job_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=0,width=0');\"><b>" + tmp + "</b></a></span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				} else {
					String title = jobcommandStr;
					buff.append("<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + title + "' href='#'><b>" + tmp + "</b></a></span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
			} else if (tlosJob.getJobProperties().getJobType().toLowerCase().equals(ScenarioLoader.JAVA_PROCESS.toLowerCase())) {
				String title = jobcommandStr;
				tmp = "TlosDependentJob"; //$NON-NLS-1$
				buff.append("	<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + title + "' href='#'><b>" + tmp + "</b></a></span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
			
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			String logFile = tlosJob.getJobProperties().getLogFilePath();
			if(FileUtils.checkFile(logFile)) {
				String title = tlosJob.getJobProperties().getLogFilePath();
				buff.append("	<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + title + "' href='#' onClick=\"javascript:window.open('logdetail?fname=" + tlosJob.getJobProperties().getKey().toString() + "', 'log_detail', 'status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1,scrollbars =1,height=600,width=900');\"><img src='/images/logfile.png' alt='' border='0'></a></span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				String title = tlosJob.getJobProperties().getLogFilePath();
				buff.append("	<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\"><a title='" + title + "' href='#'><img src='/images/noLogfile.png' alt='' border='0'></a></span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			String jobTime = "-" ;
			if (tlosJob.getJobProperties().getTime() != null && !(tlosJob instanceof ManuelExternalProgram)) {
				jobTime = DateUtils.getDate(tlosJob.getJobProperties().getTime());
			} 
			
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			buff.append("		<td class=\"datTblCol2\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + jobTime + "</span></td>");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			String executionDate = tlosJob.getJobProperties().getExecutionDateStr();
			if (executionDate == null) {
				executionDate = "-"; //$NON-NLS-1$
			}
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + executionDate + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			// Bir önceki çalışmanın süresi
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + tlosJob.getJobProperties().getRecentWorkDuration() + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			String workDuration = null;
			
			/**
			 * Eğer iş halen çalışıyorsa veya time-out olduysa, başlangıç zamanından bu yana geçen süreyi ekrana yazıyoruz.
			 * Serkan Taş
			 * 02.02.2009
			 */
			if(tlosJob.getJobProperties().getStatus() == JobProperties.WORKING || tlosJob.getJobProperties().getStatus() == JobProperties.TIMEOUT) {
				if(tlosJob.getJobProperties().getExecutionDate() != null) {
					workDuration = DateUtils.getDuration(tlosJob.getJobProperties().getExecutionDate());
				}
			} else {
				workDuration = tlosJob.getJobProperties().getWorkDuration();
			}
			
			if (workDuration == null) {
				workDuration = "-"; //$NON-NLS-1$
			}

			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + workDuration + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			String previousStatus = tlosJob.getPreviousStatusListString();
			if (previousStatus == null) {
				previousStatus = "-"; //$NON-NLS-1$
			}
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + previousStatus + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$

			int statusValue = tlosJob.getJobProperties().getStatus();
			int resultCode = tlosJob.getJobProperties().getProcessExitValue();

			String status = tlosJob.getJobProperties().getStatusString(statusValue, resultCode);

			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + status + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			//uzun ba��ml�l�k listesi k�salt�larak g�steriliyor
			String dependencyListString = tlosJob.getDependencyListStringForTooltip(jobQueue, tlosJob.getJobProperties().getKey().toString(), true);
			String truncatedDependencyListString = tlosJob.getTruncatedDependencyListString(jobQueue, tlosJob.getJobProperties().getKey().toString());
			
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + "<a title='" + dependencyListString + "'>" + truncatedDependencyListString + "</a>" + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			buff.append("		<td class=\"datTblCol1\"><span style=\"text-align: center;color:" + color + ";\" class=\"iceOutTxt\">" + (tlosJob.getJobProperties().isSafeRestart() ? LocaleMessages.getString("ViewHandler.81") : LocaleMessages.getString("ViewHandler.88")) + "</span></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			buff.append("		<td class=\"datTblBlkTd\"><img src=\"images/spacer.gif\"></td>"); //$NON-NLS-1$
			
			numOfJobsListed ++;
		}
		
		str += buff.toString();
		
		str += "		</tr>\n"; //$NON-NLS-1$
		str += "	</tbody>\n"; //$NON-NLS-1$
		str += "</table>\n"; //$NON-NLS-1$
		String tmp = getFilterMenu();
		str += "<script language=\"JavaScript\">document.getElementById(\"numofjobslisted\").innerHTML='{ " + LocaleMessages.getString("ViewHandler.69") + numOfJobsListed + " }' + '" + tmp + "'</script>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		return str;
	}
	
	public String formBegin() {

		String str = ""; //$NON-NLS-1$
		
		str += "<form name=\"tlosview\" action=\"http://" + TlosServer.getTlosParameters().getHostName() + ":" + TlosServer.getTlosParameters().getHttpAccessPort() + "/" + tlosWebConsole.getADMIN_CTX() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		+ "\" method=\"post\">"; //$NON-NLS-1$

		str += "<input name=\"keyToken\" type=\"hidden\" value=\"" + tlosServer.getDateToken() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$
		str += "<input name=\"jobname\" type=\"hidden\" value=\"\">"; //$NON-NLS-1$
		str += "<input id=\"commandnameId\" name=\"commandname\" type=\"hidden\" value=\"nocommand\">"; //$NON-NLS-1$
		
		return str;
		
	}
}
