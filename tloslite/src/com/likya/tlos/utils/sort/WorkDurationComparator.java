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
package com.likya.tlos.utils.sort;

import java.util.Comparator;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.utils.DateUtils;
import com.likya.tlos.web.helpers.ViewHandler;

public class WorkDurationComparator implements Comparator<Job> {

	@Override
	public int compare(Job firstJob, Job secondJob) {
		
		String firstWorkDuration = null;
		
		if(firstJob.getJobProperties().getStatus() == JobProperties.WORKING || firstJob.getJobProperties().getStatus() == JobProperties.TIMEOUT) {
			if(firstJob.getJobProperties().getExecutionDate() != null) {
				firstWorkDuration = DateUtils.getDuration(firstJob.getJobProperties().getExecutionDate());
			}
		} else {
			firstWorkDuration = firstJob.getJobProperties().getWorkDuration();
		}
		
		if(firstWorkDuration == null) {
			if(ViewHandler.sortDirection == 1) {
				return -1;
			}
			return 1;
		} 
		
		String secondWorkDuration = null;
		
		if(secondJob.getJobProperties().getStatus() == JobProperties.WORKING || secondJob.getJobProperties().getStatus() == JobProperties.TIMEOUT) {
			if(secondJob.getJobProperties().getExecutionDate() != null) {
				secondWorkDuration = DateUtils.getDuration(secondJob.getJobProperties().getExecutionDate());
			}
		} else {
			secondWorkDuration = secondJob.getJobProperties().getWorkDuration();
		}
		
		if(secondWorkDuration == null) {
			if(ViewHandler.sortDirection == 1) {
				return 1;
			}
			return -1;
		}
		
		
		if(ViewHandler.sortDirection == 1) {
			return firstWorkDuration.compareTo(secondWorkDuration);
		}
		return secondWorkDuration.compareTo(firstWorkDuration);

	}

}
