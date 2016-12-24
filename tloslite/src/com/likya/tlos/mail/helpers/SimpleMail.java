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
package com.likya.tlos.mail.helpers;

public class SimpleMail extends TlosMail {

	private String mailSubject;
	private String mailText;

	public SimpleMail(String mailSubject, String mailText) {
		this.mailSubject = mailSubject;
		this.mailText = mailText;
		setMAIL_TYPE(TlosMail.SIMPLE);
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public String getMailText() {
		return mailText;
	}

}
