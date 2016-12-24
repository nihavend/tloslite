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

import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.likya.tlos.comm.TlosCommInterface;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.mail.TlosMailServer;
import com.likya.tlos.model.ScenarioRuntimeProperties;
import com.likya.tlos.model.TlosAuthorization;
import com.likya.tlos.model.TlosInfo;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.sms.TlosSMSServer;
import com.likya.tlos.utils.ManagementConsole;

public abstract class TlosServerBase implements Runnable {

	private static final String version = "V1.8.1"; //$NON-NLS-1$

	private static int executionState = TlosInfo.STATE_WORKING;
	
	protected static boolean isLicensed = false;
	
	private static boolean isRecovered = false;

	protected boolean executionPermission = true;

	protected static org.apache.log4j.Logger schedulerLogger = Logger.getLogger(TlosServer.class);

	protected static HashMap<String, Job> jobQueue;
	
	protected static HashMap<String, String> disabledJobQueue;

	protected static TlosMailServer tlosMailServer = null;

	protected static TlosSMSServer tlosSMSServer = null;

	protected static ManagementConsole managementConsoleHandler = null;

	private static TlosCommInterface tlosCommInterface;

	protected static TlosParameters tlosParameters = new TlosParameters();

	protected static HashMap<String, TlosAuthorization> authorizationList;

	protected static String configFileName = "TlosConfig.xml"; //$NON-NLS-1$

	protected static String queueStat = null;

	protected static boolean loadTest = false;

	protected static int loadTestTurnCount = 0;

	protected static boolean thresholdOverflow = false;
	
	private final String dateToken = new Long(Calendar.getInstance().getTimeInMillis()).toString();

	protected ScenarioRuntimeProperties scenarioRuntimeProperties = new ScenarioRuntimeProperties();
	
	public static Logger getLogger() {
		return schedulerLogger;
	}

	public static TlosParameters getTlosParameters() {
		return tlosParameters;
	}

	public static void print(String message) {
		System.out.print(message);
	}

	public static void println(String message) {
		System.out.println(message);
	}

	public static void println() {
		System.out.println();
	}

	public static String getQueueStat() {
		return queueStat;
	}

	public static void setQueueStat(String queueStat) {
		TlosServer.queueStat = queueStat;
	}

	public void setExecutionPermission(boolean executionPermission) {
		synchronized (this) {
			this.executionPermission = executionPermission;
		}

	}

	public static HashMap<String, Job> getJobQueue() {
		return jobQueue;
	}

	public static HashMap<String, TlosAuthorization> getAuthorizationList() {
		return authorizationList;
	}

	public static void setLoadTest(boolean loadTest) {
		TlosServerBase.loadTest = loadTest;
	}

	public static String getConfigFileName() {
		return configFileName;
	}

	public static void setTlosParameters(TlosParameters tlosParameters) {
		TlosServerBase.tlosParameters = tlosParameters;
	}

	public static TlosMailServer getTlosMailServer() {
		return tlosMailServer;
	}

	public static void setTlosMailServer(TlosMailServer tlosMailServer) {
		TlosServerBase.tlosMailServer = tlosMailServer;
	}

	public static boolean isLicensed() {
		return isLicensed;
	}

	public static void setLicensed(boolean isLicensed) {
		TlosServerBase.isLicensed = isLicensed;
	}

	public static String getVersion() {
		return version;
	}
	
	public static boolean isThresholdOverflow() {
		return thresholdOverflow;
	}

	public static boolean isLoadTest() {
		return loadTest;
	}

	public String getDateToken() {
		return dateToken;
	}

	public static int getExecutionState() {
		return executionState;
	}

	public static void setExecutionState(int executionState) {
		TlosServerBase.executionState = executionState;
	}

	public static boolean isRecovered() {
		return isRecovered;
	}
	
	public static void setRecovered(boolean isRecovered) {
		TlosServerBase.isRecovered = isRecovered;
	}

	public static TlosSMSServer getTlosSMSServer() {
		return tlosSMSServer;
	}

	public static void setTlosSMSServer(TlosSMSServer tlosSMSServer) {
		TlosServerBase.tlosSMSServer = tlosSMSServer;
	}

	public static void setTlosCommInterface(TlosCommInterface tlosCommInterface) {
		TlosServerBase.tlosCommInterface = tlosCommInterface;
	}

	public static TlosCommInterface getTlosCommInterface() {
		return tlosCommInterface;
	}

	public ScenarioRuntimeProperties getScenarioRuntimeProperties() {
		return scenarioRuntimeProperties;
	}

	public void setScenarioRuntimeProperties(
			ScenarioRuntimeProperties scenarioRuntimeProperties) {
		this.scenarioRuntimeProperties = scenarioRuntimeProperties;
	}

	public static HashMap<String, String> getDisabledJobQueue() {
		return disabledJobQueue;
	}
}
