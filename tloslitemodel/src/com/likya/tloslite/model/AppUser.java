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

public class AppUser implements Serializable {

	private static final long serialVersionUID = 6860725327569758592L;

	private int id;
	private String userName;
	private String password;
	private int jmxTimeout;
	private String role;
	private String serviceName;

	public AppUser() {
	}

	public AppUser(int id, String userName, String password, int jmxTimeout, String role) {
		super();
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.jmxTimeout = jmxTimeout;
		this.role = role;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getJmxTimeout() {
		return jmxTimeout;
	}

	public void setJmxTimeout(int jmxTimeout) {
		this.jmxTimeout = jmxTimeout;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
