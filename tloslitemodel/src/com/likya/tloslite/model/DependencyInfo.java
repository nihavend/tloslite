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
package com.likya.tloslite.model;

import java.io.Serializable;

public class DependencyInfo implements Serializable {

	private static final long serialVersionUID = 7852582930627343234L;

	public static final char and = '|';
	public static final char or = '^';

	private String jobKey;
	private int status;
	private char logicalOperator;

	public DependencyInfo(String jobKey, int status, char logicalOperator) {
		super();
		this.jobKey = jobKey;
		this.status = status;
		this.logicalOperator = logicalOperator;
	}

	public String getJobKey() {
		return jobKey;
	}

	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public char getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(char logicalOperator) {
		this.logicalOperator = logicalOperator;
	}
}
