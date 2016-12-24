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

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.model.JobProperties;

public class ObjectUtils {
	
	
	public static String getStatusAsString(int status) {

		String retStr = "-"; //$NON-NLS-1$

		switch (status) {
		case JobProperties.READY:
			retStr = LocaleMessages.getString("JobProperties.0"); //$NON-NLS-1$
			break;
		case JobProperties.WORKING:
			retStr = LocaleMessages.getString("JobProperties.1"); //$NON-NLS-1$
			break;
		case JobProperties.WAITING:
			retStr = LocaleMessages.getString("JobProperties.2"); //$NON-NLS-1$
			break;
		case JobProperties.SUCCESS:
			retStr = LocaleMessages.getString("JobProperties.3"); //$NON-NLS-1$
			break;
		case JobProperties.FAIL:
			retStr = LocaleMessages.getString("JobProperties.4"); //$NON-NLS-1$
			break;
		case JobProperties.TIMEOUT:
			retStr = LocaleMessages.getString("JobProperties.6"); //$NON-NLS-1$
			break;
		case JobProperties.SKIP:
			retStr = LocaleMessages.getString("JobProperties.7"); //$NON-NLS-1$
			break;
		case JobProperties.STOP:
			retStr = LocaleMessages.getString("JobProperties.8"); //$NON-NLS-1$
			break;
		case JobProperties.PAUSE:
			retStr = LocaleMessages.getString("JobProperties.9"); //$NON-NLS-1$
			break;
		case JobProperties.RESUME:
			retStr = LocaleMessages.getString("JobProperties.5"); //$NON-NLS-1$
			break;
		case JobProperties.MSTART:
			retStr = LocaleMessages.getString("JobProperties.10"); //$NON-NLS-1$
			break;
		case JobProperties.DISABLED:
			retStr = LocaleMessages.getString("JobProperties.12"); //$NON-NLS-1$
			break;			
		default:
			retStr = "Undefined";
		}

		return retStr;
	}
	
	public static JobProperties cloneJobProperties(JobProperties jobPropertiesSrc) {
		
		JobProperties jobPropertiesDest = new JobProperties();
		
		jobPropertiesDest.setAutoRetry(jobPropertiesSrc.isAutoRetry());
		jobPropertiesDest.setAutoRetryCount(jobPropertiesSrc.getAutoRetryCount());
		jobPropertiesDest.setAutoRetryDelay(jobPropertiesSrc.getAutoRetryDelay());
		jobPropertiesDest.setBlocker(jobPropertiesSrc.isBlocker());
		jobPropertiesDest.setCompletionDate(jobPropertiesSrc.getCompletionDate());
		jobPropertiesDest.setCompletionDateTime(jobPropertiesSrc.getCompletionDateTime());
		jobPropertiesDest.setDiscardList(jobPropertiesSrc.getDiscardList());
		jobPropertiesDest.setExecutionDate(jobPropertiesSrc.getExecutionDate());
		jobPropertiesDest.setGroupId(jobPropertiesSrc.getGroupId());
		jobPropertiesDest.setGroupName(jobPropertiesSrc.getGroupName());
		jobPropertiesDest.setDangerZoneGroupId(jobPropertiesSrc.getDangerZoneGroupId());
		jobPropertiesDest.setDangerZoneGroup(jobPropertiesSrc.getDangerZoneGroup());
		jobPropertiesDest.setJobBaseType(jobPropertiesSrc.getJobBaseType());
		jobPropertiesDest.setJobCommand(jobPropertiesSrc.getJobCommand());
		jobPropertiesDest.setJobParamList(jobPropertiesSrc.getJobParamList());
		jobPropertiesDest.setJobParamListPerm(jobPropertiesSrc.getJobParamListPerm());
		jobPropertiesDest.setJobDependencyInfoList(jobPropertiesSrc.getJobDependencyInfoList());
		jobPropertiesDest.setJobPlannedEndTime(jobPropertiesSrc.getJobPlannedEndTime());
		jobPropertiesDest.setJobPlannedStartTime(jobPropertiesSrc.getJobPlannedStartTime());
		jobPropertiesDest.setJobType(jobPropertiesSrc.getJobType());
		jobPropertiesDest.setKey(jobPropertiesSrc.getKey());
		jobPropertiesDest.setLogAnalyzeString(jobPropertiesSrc.getLogAnalyzeString());
		jobPropertiesDest.setLogFilePath(jobPropertiesSrc.getLogFilePath());
		jobPropertiesDest.setManuel(jobPropertiesSrc.isManuel());
		jobPropertiesDest.setPeriodTime(jobPropertiesSrc.getPeriodTime());
		jobPropertiesDest.setPreviousTime(jobPropertiesSrc.getPreviousTime());
		jobPropertiesDest.setPriority(jobPropertiesSrc.getPriority());
		jobPropertiesDest.setProcessExitValue(jobPropertiesSrc.getProcessExitValue());
		jobPropertiesDest.setSafeRestart(jobPropertiesSrc.isSafeRestart());
		jobPropertiesDest.setScenarioTime(jobPropertiesSrc.getScenarioTime());
		jobPropertiesDest.setStandart(jobPropertiesSrc.isStandart());
		jobPropertiesDest.setStatus(jobPropertiesSrc.getStatus());
		jobPropertiesDest.setTime(jobPropertiesSrc.getTime());
		jobPropertiesDest.setTimeout(jobPropertiesSrc.getTimeout());
		jobPropertiesDest.setWorkDuration(jobPropertiesSrc.getWorkDuration());
		jobPropertiesDest.setWorkDurationNumeric(jobPropertiesSrc.getWorkDurationNumeric());
		
		return jobPropertiesDest;
		
	}
}
