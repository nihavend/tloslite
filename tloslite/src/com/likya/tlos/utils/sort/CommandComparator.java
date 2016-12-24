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

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.likya.tlos.jobs.Job;
import com.likya.tlos.web.helpers.ViewHandler;

public class CommandComparator implements Comparator<Job> {

	@Override
	public int compare(Job firstJob, Job secondJob) {
		
		String firstJobCommand = firstJob.getJobProperties().getJobCommand();
		String secondJobCommand = secondJob.getJobProperties().getJobCommand();
		
		Collator collator = Collator.getInstance(new Locale("tr")); //$NON-NLS-1$
		
		if(ViewHandler.sortDirection == 1) {
			return collator.compare(firstJobCommand, secondJobCommand);
		}
		return collator.compare(secondJobCommand, firstJobCommand);
	}
}
