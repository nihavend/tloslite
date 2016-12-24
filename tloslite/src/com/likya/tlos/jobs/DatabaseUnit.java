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

import java.util.HashMap;

import com.likya.tlos.model.JobProperties;


public class DatabaseUnit extends Job {

	private static final long serialVersionUID = 1L;
	
	public DatabaseUnit(HashMap<String, Job> jobQueue, JobProperties jobProperties, boolean isMail, boolean isSms) {
		super(jobQueue, jobProperties, isMail, isSms);
	}
	public synchronized void terminate() {
	}
	
	public void run() {
	}
	
	public void stopMyDogBarking() {
	}

}
