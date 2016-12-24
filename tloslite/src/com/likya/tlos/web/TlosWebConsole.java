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
package com.likya.tlos.web;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.likya.tlos.TlosServer;
import com.likya.tlos.model.TlosAuthorization;
import com.likya.tlos.utils.HtmlFileReaderV2;
import com.likya.tlos.utils.PasswordService;
import com.likya.tlos.web.helpers.FileViewHandler;
import com.likya.tlos.web.helpers.ImageHandler;
import com.likya.tlos.web.helpers.ParameterHandler;
import com.likya.tlos.web.helpers.PasswordHandler;
import com.likya.tlos.web.helpers.ViewHandler;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class TlosWebConsole {

	private final String ADMIN_CTX = "admin"; //$NON-NLS-1$
	private final String SYSTEM_CTX = "system"; //$NON-NLS-1$
	private final String LOGDETAIL_CTX = "logdetail"; //$NON-NLS-1$
	private final String JOBDETAIL_CTX = "jobdetail"; //$NON-NLS-1$
	private final String IMAGE_CTX = "img"; //$NON-NLS-1$
	private final String IMAGEV2_CTX = "images"; //$NON-NLS-1$
	private final String PARAMS_CTX = "params"; //$NON-NLS-1$
	private final String MSGDETAIL_CTX = "msgdetail"; //$NON-NLS-1$

	private int httpPort;
	private String hostName = "localhost"; //$NON-NLS-1$

	private String sessionUserName;
	private String sessionPassWord;

	private HashMap<String, TlosAuthorization> authorizationList;

	MyAuthenticator myAuthenticator;

	TlosServer tlosServer;

	TlosAuthorization tlosRealm;

	public TlosWebConsole() {

	}

	public TlosWebConsole(TlosServer tlosServer) {
		this.tlosServer = tlosServer;
		this.authorizationList = TlosServer.getAuthorizationList();
		httpPort = TlosServer.getTlosParameters().getHttpAccessPort();
		hostName = TlosServer.getTlosParameters().getHostName();
	}

	class MyAuthenticator extends BasicAuthenticator {

		public MyAuthenticator(String realm) {
			super(realm);
		}

		public boolean checkCredentials(String username, String password) {
			String encryptedPassword;
			try {
				encryptedPassword = PasswordService.encrypt(password);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			if (username != null && password != null && authorizationList.containsKey(username) && encryptedPassword.equals(authorizationList.get(username).getPassWord())) {
				sessionUserName = username;
				sessionPassWord = encryptedPassword;
				return true;
			}
			return false;
		}
	}

	public void initServer() throws Exception {

		
		HttpServer server;
		server = HttpServer.create(new InetSocketAddress(hostName, httpPort), 5);
		HttpContext httpContextV2 = null;
		HttpContext systemContextv2 = null;
		
		httpContextV2 = server.createContext("/", new ViewHandler(tlosServer, this)); //$NON-NLS-1$
		systemContextv2 = server.createContext("/" + SYSTEM_CTX, new PasswordHandler(tlosServer, this)); //$NON-NLS-1$
		HttpContext imgContext = server.createContext("/" + IMAGE_CTX, new ImageHandler()); //$NON-NLS-1$
		HttpContext imgContextv2 = server.createContext("/" + IMAGEV2_CTX, new ImageHandler(HtmlFileReaderV2.CONTENT_PATH)); //$NON-NLS-1$
		HttpContext logDetailContext = server.createContext("/" + LOGDETAIL_CTX, new FileViewHandler(tlosServer, this)); //$NON-NLS-1$
		HttpContext jobDetailContext = server.createContext("/" + JOBDETAIL_CTX, new FileViewHandler(tlosServer, this)); //$NON-NLS-1$
		HttpContext messageDetailContext = server.createContext("/" + MSGDETAIL_CTX, new FileViewHandler(tlosServer, this)); //$NON-NLS-1$
		
		HttpContext parameterContext = server.createContext("/" + PARAMS_CTX, new ParameterHandler(tlosServer, this));

		MyAuthenticator myAuthenticator = new MyAuthenticator("TlosWeb"); //$NON-NLS-1$
		httpContextV2.setAuthenticator(myAuthenticator);
		systemContextv2.setAuthenticator(myAuthenticator);
		imgContext.setAuthenticator(myAuthenticator);
		imgContextv2.setAuthenticator(myAuthenticator);
		messageDetailContext.setAuthenticator(myAuthenticator);
		
		parameterContext.setAuthenticator(myAuthenticator);
		
		if (!TlosServer.getTlosParameters().isJmxManagementEnabled()) {
			logDetailContext.setAuthenticator(myAuthenticator);
			jobDetailContext.setAuthenticator(myAuthenticator);
		}

		server.setExecutor(null); // creates a default executor
		server.start();

	}

	public int getHttpPort() {
		return httpPort;
	}

	public String getHostName() {
		return hostName;
	}

	public String getSessionUserName() {
		return sessionUserName;
	}

	public void setSessionUserName(String sessionUserName) {
		this.sessionUserName = sessionUserName;
	}

	public String getSessionPassWord() {
		return sessionPassWord;
	}

	public void setSessionPassWord(String sessionPassWord) {
		this.sessionPassWord = sessionPassWord;
	}

	public String getSYSTEM_CTX() {
		return SYSTEM_CTX;
	}

	public String getIMAGE_CTX() {
		return IMAGE_CTX;
	}

	public String getJOBDETAIL_CTX() {
		return JOBDETAIL_CTX;
	}

	public String getADMIN_CTX() {
		return ADMIN_CTX;
	}

	public String getMSGDETAIL_CTX() {
		return MSGDETAIL_CTX;
	}
}
