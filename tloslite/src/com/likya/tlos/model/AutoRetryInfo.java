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
package com.likya.tlos.model;

public class AutoRetryInfo {
	
	private boolean autoRetry = false;
	private int autorRetryDelay = 1000;
	private int autoRetryCount = 1;
	
	public AutoRetryInfo(boolean autoRetry, int autorRetryDelay, int autoRetryCount) {
		super();
		this.autoRetry = autoRetry;
		this.autorRetryDelay = autorRetryDelay;
		this.autoRetryCount = autoRetryCount;
	}

	public boolean isAutoRetry() {
		return autoRetry;
	}

	public void setAutoRetry(boolean autoRetry) {
		this.autoRetry = autoRetry;
	}

	public int getAutorRetryDelay() {
		return autorRetryDelay;
	}

	public void setAutorRetryDelay(int autorRetryDelay) {
		this.autorRetryDelay = autorRetryDelay;
	}

	public int getAutoRetryCount() {
		return autoRetryCount;
	}

	public void setAutoRetryCount(int autoRetryCount) {
		this.autoRetryCount = autoRetryCount;
	}
	
}
