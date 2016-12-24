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
package com.likya.tlos.web.helpers;

import java.io.IOException;
import java.io.OutputStream;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.web.TlosWebConsole;
import com.sun.net.httpserver.HttpExchange;

public class ParameterHandler extends TlosHttpHandler {

	public ParameterHandler(TlosServer tlosServer, TlosWebConsole tlosWebConsole) {
		super(tlosServer, tlosWebConsole);
	}

	public void handle(HttpExchange httpExchange) throws IOException {

		final String jobNameStr = "jobname";
		final String actionStr = "action";
		final String valueStr = "value";

		final String actionReset = "reset";
		final String actionSet = "set";

		OutputStream os;
		String response = "Öngörülmeyen bir hata oluştu !";

		// HttpContext httpContext = httpExchange.getHttpContext();
		String query = httpExchange.getRequestURI().getQuery();

		TlosServer.getLogger().warn(query);

		if (query != null) {

			// =job1&=reset&=
			// Jira - TL-194 
			String[] paramArray = query.split("&");

			if (paramArray[0] != null) {
				String[] keyValue = paramArray[0].split("=");
				if (keyValue[0] != null && keyValue[0].equals(jobNameStr)) {
					// set job name
					// System.out.println("Job id : " + keyValue[1]);
					Job job = TlosServer.getJobQueue().get(keyValue[1]);

					if (job == null || job.getJobProperties().getStatus() != JobProperties.READY) {
						TlosServer.print(LocaleMessages.getString("ViewHandler.874"));
						response = LocaleMessages.getString("ViewHandler.874");
					} else {

						if (paramArray[1] != null) {
							keyValue = paramArray[1].split("=");
							if (keyValue[0] != null && keyValue[0].equals(actionStr)) {
								// set action
								// System.out.println("Action id : " + keyValue[1]);

								if (keyValue[1] != null && keyValue[1].equals(actionReset)) {
									// reset jobcommand parameter
									job.getJobProperties().setJobParamList("");
									response = "paramater for " + job.getJobProperties().getKey() + " is cleared !";
								} else if (keyValue[1] != null && keyValue[1].equals(actionSet)) {
									// set jobcommand parameter
									if (paramArray[2] != null) {
										keyValue = paramArray[2].split("=");
										if (keyValue[0].equals(valueStr)) {
											// set value if action not reset
											if(keyValue.length >= 2) {
												String tmpStr = paramArray[2];
												tmpStr = tmpStr.replace(valueStr, "");
												tmpStr = tmpStr.replaceFirst("=", "");
												job.getJobProperties().setJobParamList(tmpStr);
												// System.out.println("Pramater value : " + job.getJobProperties().getJobParamList());
												response = tmpStr + " " + LocaleMessages.getString("ViewHandler.873") + " " + job.getJobProperties().getKey();
											} else {
												job.getJobProperties().setJobParamList("");
												// System.out.println("Pramater value : " + job.getJobProperties().getJobParamList());
												response = "paramater for " + job.getJobProperties().getKey() + " is cleared !";
											}
										}
									}
								}

							}
						}
					}

				}
			}
		}

		byte[] responseBytes = response.getBytes();

		int contentLength = responseBytes.length;
		httpExchange.sendResponseHeaders(200, contentLength);
		os = httpExchange.getResponseBody();
		os.write(responseBytes);
		os.close();
	}

}
