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

public class LdapInfo implements Serializable {

	private static final long serialVersionUID = 7927520087870987169L;

	private String host, port, rootdn, searchBase, username;
	private int jmxTimeout;

	public LdapInfo(String host, String port, String rootdn, String searchBase, String username, int jmxTimeout) {
		super();
		this.host = host;
		this.port = port;
		this.rootdn = rootdn;
		this.searchBase = searchBase;
		this.username = username;
		this.jmxTimeout = jmxTimeout;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getRootdn() {
		return rootdn;
	}

	public String getUsername() {
		return username;
	}

	public int getJmxTimeout() {
		return jmxTimeout;
	}

	public String getSearchBase() {
		return searchBase;
	}

}
