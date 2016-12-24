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

public class TlosConnection implements Serializable {

	private static final long serialVersionUID = -7233406693169321070L;

	private int id;
	private String tlosName;
	private String ipAddress;
	private int port;
	private String tlosUserName;
	private String tlosPassword;
	private boolean isConnected;
	private String enable;

	public TlosConnection() {
	}

	public TlosConnection(int id, String tlosName, String ipAddress, int port, String tlosUserName, String tlosPassword, boolean isConnected, String enable) {
		super();
		this.id = id;
		this.tlosName = tlosName;
		this.ipAddress = ipAddress;
		this.port = port;
		this.setTlosUserName(tlosUserName);
		this.setTlosPassword(tlosPassword);
		this.isConnected = isConnected;
		this.enable = enable;
	}

	public String getTlosName() {
		return tlosName;
	}

	public void setTlosName(String tlosName) {
		this.tlosName = tlosName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getTlosPassword() {
		return tlosPassword;
	}

	public void setTlosPassword(String tlosPassword) {
		this.tlosPassword = tlosPassword;
	}

	public String getTlosUserName() {
		return tlosUserName;
	}

	public void setTlosUserName(String tlosUserName) {
		this.tlosUserName = tlosUserName;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

}
