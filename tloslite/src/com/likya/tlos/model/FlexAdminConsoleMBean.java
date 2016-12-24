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

import com.likya.tloslite.model.TlosConnection;
import com.likya.tloslite.model.TlosData;
import com.likya.tloslite.model.TlosUser;

public interface FlexAdminConsoleMBean {

	public void checkTlosConnection();

	public TlosData showLogFile(TlosConnection tlosConnection, String jobId, Integer command);

	//public TlosData filterByGroupId(TlosConnection tlosConnection, String command);

	public TlosData refresh(TlosConnection tlosConnection, String command);

	public TlosData changePassword(TlosUser user, String newPassword);

	public TlosData init(TlosConnection tlosConnection, String command);

	//public TlosData view(TlosConnection tlosConnection, String command);

	public void skipJob(TlosConnection tlosConnection, String jobName, String command);

	public void setSuccess(TlosConnection tlosConnection, String jobName, String command);

	public void retryJob(TlosConnection tlosConnection, String jobName, String command);

	public void resumeJob(TlosConnection tlosConnection, String jobName, String command);

	public void pauseJob(TlosConnection tlosConnection, String jobName, String command);

	public void stopJob(TlosConnection tlosConnection, String jobName, String command);

	public void startJob(TlosConnection tlosConnection, String jobName, String command);

	public void enableJob(TlosConnection tlosConnection, String jobName, String command);

	public void disableJob(TlosConnection tlosConnection, String jobName, String command);

	public TlosData resumeTlos(TlosUser user, String command);

	public TlosData suspendTlos(TlosUser user, String command);

	public TlosData forcefulShutDown(TlosUser user);

	public TlosData gracefulShutDown(TlosUser user);

	public TlosData setJobInputParam(TlosConnection tlosConnection, String jobName, String parameterList, String command);

	/**
	 * Getter: set the "State" attribute of the "Test" standard MBean.
	 * 
	 * @return the current value of the "State" attribute.
	 */
	public String getState();

	/**
	 * Setter: set the "State" attribute of the "Test" standard MBean.
	 * 
	 * @param <VAR>s</VAR> the new value of the "State" attribute.
	 */
	public void setState(String s);

	/**
	 * Getter: get the "NbChanges" attribute of the "Test" standard MBean.
	 * 
	 * @return the current value of the "NbChanges" attribute.
	 */
	public int getNbChanges();

	/**
	 * Operation: reset to their initial values the "State" and "NbChanges"
	 * attributes of the "Test" standard MBean.
	 */
	public void reset();
}
