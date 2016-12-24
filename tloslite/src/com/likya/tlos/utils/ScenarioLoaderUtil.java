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

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.likya.tlos.LocaleMessages;
import com.likya.tloslite.model.DependencyInfo;
import com.likya.tlos.model.AutoRetryInfo;
import com.likya.tlos.model.JobProperties;

public class ScenarioLoaderUtil {

	public static ArrayList<DependencyInfo> getDependencyList(String dependencyStrig, boolean isManuel, boolean isStandart) {

		ArrayList<DependencyInfo> dependencyInfoList = new ArrayList<DependencyInfo>();

		StringTokenizer jobDependencyString = new StringTokenizer(dependencyStrig, "|"); //$NON-NLS-1$

		// if (isStandart || !isManuel) {
		if (!isManuel) {
			while (jobDependencyString.hasMoreTokens()) {
				String currentToken = jobDependencyString.nextToken();
				DependencyInfo dependencyInfo = parseDependencyRule(currentToken);
				dependencyInfoList.add(dependencyInfo);
			}
		} else {
			// dependencyInfoList.add(new
			// DependencyInfo(ScenarioLoader.UNDEFINED_VALUE, -1, '0'));
		}

		return dependencyInfoList;
	}
	
	private static DependencyInfo parseDependencyRule(String mixedString) {

		DependencyInfo dependencyInfo = null;

		// new DependencyInfo(currentToken, JobProperties.SUCCESS,
		// DependencyInfo.and);

		int openBraceIdx = mixedString.indexOf('(');
		int closeBraceIdx = mixedString.indexOf(')');

		if ((openBraceIdx < 0 && closeBraceIdx < 0)) {
			// no dependency rule defined use dafult values
			dependencyInfo = new DependencyInfo(mixedString, JobProperties.SUCCESS, DependencyInfo.and);
		} else if (openBraceIdx > 0 && closeBraceIdx > 0 && openBraceIdx < closeBraceIdx && mixedString.substring(openBraceIdx + 1, closeBraceIdx).length() == 1) {
			String realJobKey = mixedString.substring(0, openBraceIdx);
			String depStatStr = mixedString.substring(openBraceIdx + 1, closeBraceIdx);
			
			if("-".equals(depStatStr)) {
				dependencyInfo = new DependencyInfo(realJobKey, JobProperties.SUCSFAIL, DependencyInfo.and);
			} else {
				int status = Integer.parseInt(depStatStr);
				dependencyInfo = new DependencyInfo(realJobKey, (status == 1 ? JobProperties.SUCCESS : JobProperties.FAIL), DependencyInfo.and);
			}
			
		} else {
			System.err.println("(n) is required ! => " + mixedString);
		}

		return dependencyInfo;
	}

	public static AutoRetryInfo getAutoRetryInfo(String autoRetryStr, boolean isManuel, boolean isStandart) {

		boolean autoRetry = false;
		int autorRetryDelay = 1000;
		int autoRetryCount = 1;

		StringTokenizer autoRetryString = new StringTokenizer(autoRetryStr, "|"); //$NON-NLS-1$
		ArrayList<String> autoRetryList = new ArrayList<String>();

		while (autoRetryString.hasMoreTokens()) {
			String currentToken = autoRetryString.nextToken();
			autoRetryList.add(currentToken);
		}

		if (autoRetryList.size() >= 1 && Validation.getBooleanString("autoRestart", autoRetryList.get(0))) {
			autoRetry = Validation.getBooleanString("autoRestart", autoRetryList.get(0));
			autorRetryDelay = (autoRetryList.size() >= 2 && Integer.parseInt(autoRetryList.get(1)) > autorRetryDelay) ? autorRetryDelay = Integer.parseInt(autoRetryList.get(1)) : autorRetryDelay;
			autoRetryCount = (autoRetryList.size() == 3 && Integer.parseInt(autoRetryList.get(2)) > autoRetryCount) ? autoRetryCount = Integer.parseInt(autoRetryList.get(2)) : autoRetryCount;
		}

		return new AutoRetryInfo(autoRetry, autorRetryDelay, autoRetryCount);
	}

	public static boolean parseTimer(Logger schedulerLogger, String dateInfoField, JobProperties jobProperties) {

		String beginDate = null;
		String endDate = null;

		Date plannedEndTime = null;

		StringTokenizer doubleDateTokenizer = new StringTokenizer(dateInfoField, "-"); //$NON-NLS-1$

		if (!jobProperties.isStandart() && !jobProperties.isManuel()) {
			if (doubleDateTokenizer.countTokens() == 1) {
				beginDate = doubleDateTokenizer.nextToken();
				endDate = "23:59:59"; //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("ScenarioLoader.8") + beginDate + LocaleMessages.getString("ScenarioLoader.9") + endDate); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (doubleDateTokenizer.countTokens() > 1) {
				beginDate = doubleDateTokenizer.nextToken();
				endDate = doubleDateTokenizer.nextToken();
				System.out.println(LocaleMessages.getString("ScenarioLoader.13") + beginDate + LocaleMessages.getString("ScenarioLoader.17") + endDate); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Date tmpDate = DateUtils.generateJobDate(beginDate);

			if (tmpDate == null) {
				return false;
			}

			plannedEndTime = DateUtils.generateJobDate(endDate);

			if (plannedEndTime == null) {
				return false;
			}

			plannedEndTime = DateUtils.changeDateValue(tmpDate, plannedEndTime);

			schedulerLogger.info(LocaleMessages.getString("ScenarioLoader.1")); //$NON-NLS-1$
		} else if (jobProperties.isStandart()) {
			beginDate = doubleDateTokenizer.nextToken();
		}

		Date plannedStartTime = null;

		if (!jobProperties.isManuel()) {
			plannedStartTime = DateUtils.generateJobDate(beginDate);
		}

		if (plannedStartTime == null) {
			return false;
		}

		if (!jobProperties.isStandart() && !jobProperties.isManuel()) {
			if (plannedEndTime.before(plannedStartTime) || (DateUtils.diff(plannedStartTime, plannedEndTime) < (jobProperties.getPeriodTime() * 1000))) {
				schedulerLogger.fatal(LocaleMessages.getString("ScenarioLoader.0")); //$NON-NLS-1$
				return false;
			}
		}

		jobProperties.setTime(plannedStartTime);
		jobProperties.setJobPlannedStartTime(plannedStartTime);
		jobProperties.setJobPlannedEndTime(plannedEndTime);
		jobProperties.setScenarioTime(plannedStartTime);

		return true;
	}

}
