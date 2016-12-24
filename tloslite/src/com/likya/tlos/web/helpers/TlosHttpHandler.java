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

import java.util.HashMap;
import java.util.StringTokenizer;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.comm.TlosCommInterface;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.utils.TlosException;
import com.likya.tlos.web.TlosWebConsole;
import com.sun.net.httpserver.HttpHandler;

public abstract class TlosHttpHandler implements HttpHandler {

	protected static final String RETRY_STR = "Retry"; //$NON-NLS-1$
	protected static final String SETSUCCESS_STR = "Set Success"; //$NON-NLS-1$
	protected static final String SKIP_STR = "Skip"; //$NON-NLS-1$
	protected static final String STOP_STR = "Stop"; //$NON-NLS-1$
	protected static final String PAUSE_STR = "Pause"; //$NON-NLS-1$
	protected static final String RESUME_STR = "Resume"; //$NON-NLS-1$
	protected static final String START_STR = "Start"; //$NON-NLS-1$

	protected static final String DISABLE_STR = "Disable"; //$NON-NLS-1$
	protected static final String ENABLE_STR = "Enable"; //$NON-NLS-1$
	
	protected static final String SUSPENDTLOS_STR = "Suspend Tlos"; //$NON-NLS-1$
	protected static final String RESUMETLOS_STR = "Resume Tlos"; //$NON-NLS-1$
	protected static final String NSTOP_STR = "Shut Down Tlos Normally"; //$NON-NLS-1$
	protected static final String FSTOP_STR = "Force Tlos To Shut Down"; //$NON-NLS-1$
	protected static final String REFRESH_STR = "Refresh"; //$NON-NLS-1$
	protected static final String SAVE_STR = "Save"; //$NON-NLS-1$
	
	protected TlosServer tlosServer;
	protected boolean terminate = false;
	
	protected static TlosWebConsole tlosWebConsole;
	protected TlosCommInterface tlosCommInterface;
	
	public TlosHttpHandler() {
	}
	
	public TlosHttpHandler(TlosServer tlosServer, TlosWebConsole tlosWebConsole) {
		this.tlosServer = tlosServer;
		TlosHttpHandler.tlosWebConsole = tlosWebConsole;
		this.tlosCommInterface = TlosServer.getTlosCommInterface();
	}
	
	public String queryHandler(String urlQuery) throws TlosException {
		return null;
	}

	protected StringBuilder logoSection() {
		return logoSection(true);
	}
	
	protected StringBuilder logoSection(boolean isImage) {
		
		StringBuilder logoSection = new StringBuilder();
		
		logoSection.append("<p><pre>"); //$NON-NLS-1$
		
		logoSection.append("<TABLE BORDER=\"0\" align=\"center\" width=\"80%\">"); //$NON-NLS-1$
		logoSection.append("<TR>"); //$NON-NLS-1$
		if(isImage) {
			logoSection.append("<TD width=\"%20\"><img src=\"/img/likya.jpg \"  height=\"56\" width=\"165\"></TD>"); //$NON-NLS-1$
		}
		logoSection.append("<TD align=\"center\"><span class=\"style11\">TLOS Scheduler</span></TD>"); //$NON-NLS-1$
		if(!terminate) {
			//			logoSection.append("<TD width=\"%20\" align=\"right\"><INPUT TYPE=\"button\" VALUE=\"STOP TLOS\" onClick=\"if(confirm('emin misiniz?'))  location.href='http://" + TlosServer.getTlosParameters().getHostName() + ":" + TlosServer.getTlosParameters().getHttpAccessPort() + "/" + tlosWebConsole.getADMIN_CTX() + "?button=terminate'\"</TD>");
			logoSection.append("<TD width=\"%20\" align=\"right\">"); //$NON-NLS-1$
			
			if(TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) {
				logoSection.append("<INPUT name=\"commandname\" id=\"" + RESUMETLOS_STR + "\" TYPE=\"submit\" VALUE=\"" + RESUMETLOS_STR + "\" onClick=\"if(!confirm('" + LocaleMessages.getString("TlosHttpHandler.0") + "')){return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				logoSection.append("<br/>"); //$NON-NLS-1$
			} else {
				logoSection.append("<INPUT name=\"commandname\" id=\"" + SUSPENDTLOS_STR + "\" TYPE=\"submit\" VALUE=\"" + SUSPENDTLOS_STR + "\" onClick=\"if(!confirm('" + LocaleMessages.getString("TlosHttpHandler.0") + "')){return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				logoSection.append("<br/>"); //$NON-NLS-1$
			}
		
			logoSection.append("<INPUT name=\"commandname\" id=\"" + NSTOP_STR + "\" TYPE=\"submit\" VALUE=\"" + NSTOP_STR + "\" onClick=\"if(!confirm('" + LocaleMessages.getString("TlosHttpHandler.0") + "')){return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			logoSection.append("<br/>"); //$NON-NLS-1$
			logoSection.append("<INPUT name=\"commandname\" TYPE=\"submit\" VALUE=\"" + FSTOP_STR + "\" onClick=\"if(!confirm('" + LocaleMessages.getString("TlosHttpHandler.0") + "')){return false;}\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			logoSection.append("</TD>"); //$NON-NLS-1$
		}
		logoSection.append("</TR>"); //$NON-NLS-1$
		logoSection.append("</TABLE>\n"); //$NON-NLS-1$
		
		return logoSection;
	}

	protected static HashMap<String, String> getFormMap(String formElements) throws TlosException {
		StringTokenizer stringTokenizer = new StringTokenizer(formElements, "&"); //$NON-NLS-1$
		HashMap<String, String> formMap = new HashMap<String, String> (); 
		
		while(stringTokenizer.hasMoreTokens()) {
			StringTokenizer smallTokenizer = new StringTokenizer(stringTokenizer.nextElement().toString(), "="); //$NON-NLS-1$
			if(smallTokenizer.countTokens() == 2) {
				formMap.put(smallTokenizer.nextElement().toString(), smallTokenizer.nextElement().toString());
			} else {
				formMap.put(smallTokenizer.nextElement().toString(), null);
			}
		}
//		System.out.println(formMap.get("username"));
//		System.out.println(formMap.get("new_password"));
//		System.out.println(formMap.get("old_password"));
		return formMap;
	}
}
