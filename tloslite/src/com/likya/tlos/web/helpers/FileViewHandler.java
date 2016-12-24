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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.utils.FileUtils;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.web.TlosWebConsole;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class FileViewHandler extends TlosHttpHandler {
	
	private final String licenseFile = "LICENSE-2.0.html";

	public FileViewHandler(TlosServer tlosServer, TlosWebConsole tlosWebConsole) {
		super(tlosServer, tlosWebConsole);
	}

	public void handle(HttpExchange httpExchange) throws IOException {

		boolean isXml = false;
		OutputStream os;
		String response = null;

		HttpContext httpContext = httpExchange.getHttpContext();
		String query = httpExchange.getRequestURI().getQuery();
		String viewFile = null;
		TlosServer.getLogger().warn(query);
		if (query != null) {
			StringTokenizer stringTokenizer = new StringTokenizer(query, "="); //$NON-NLS-1$
			String tokenStr = stringTokenizer.nextToken();
			if (tokenStr.equals("fname")) { //$NON-NLS-1$
				String elementText = stringTokenizer.nextElement().toString(); 
				if(elementText.equals("tlosConfig")) { //$NON-NLS-1$
//					viewFile = "TlosConfig.xml"; //$NON-NLS-1$
//					response = xmlPager(viewFile);
					response = TlosServer.getTlosParameters().getConfigFileContent();
					isXml = true;
					TlosParameters.setRequestedFileName(TlosServer.getConfigFileName());
				} else if(elementText.equals("tlosScenario")) { //$NON-NLS-1$
//					viewFile = TlosServer.getTlosParameters().getScenarioFile();
//					response = xmlPager(viewFile);
					response = TlosServer.getTlosParameters().getScenarioFileContent();
					isXml = true;
					TlosParameters.setRequestedFileName(TlosServer.getTlosParameters().getScenarioFile());//TlosParameters.getScenarioFile());
				} else if(elementText.equals("tlosTrace")) { //$NON-NLS-1$
					viewFile = "TlosTrace.log"; //$NON-NLS-1$
					response = webPager(viewFile);
				} else if(elementText.equals("tlosLog")) { //$NON-NLS-1$
					viewFile = TlosServer.getTlosParameters().getLogFile();
					response = webPager(viewFile);
				} else if(elementText.equals("tlos.xsl")) { //$NON-NLS-1$
					viewFile = "tlos.xsl"; //$NON-NLS-1$
					response = xslPager(viewFile);
					isXml = true;
				} else if(elementText.equals("license")) { //$NON-NLS-1$
					try {
						response = FileUtils.readFile(licenseFile /*"lisans.htm"*/).toString(); //$NON-NLS-1$	
					} catch (Exception e) {
						response = "\"" + licenseFile + "\"" + LocaleMessages.getString("FileViewHandler.6"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
				} else {
					if(httpContext.getPath().equals("/" + tlosWebConsole.getJOBDETAIL_CTX())) { //$NON-NLS-1$
						viewFile = TlosServer.getJobQueue().get(elementText).getJobProperties().getJobCommand();
						response = webPager(viewFile);
					} else {
						String [] paramArr = query.split("&");
						if(paramArr.length > 0 && paramArr[0].split("=").length > 0) {
							elementText = paramArr[0].split("=")[1];
						}
						viewFile = TlosServer.getJobQueue().get(elementText).getJobProperties().getLogFilePath();
						
						int beginOff = -1;
						int endOff = -1;
						
						if(paramArr.length > 1 && paramArr[1].split("=").length > 0) {
							String beginOffStr = paramArr[1].split("=")[1];
							beginOff = Integer.parseInt(beginOffStr);
						}
							
						if(paramArr.length > 2 && paramArr[2].split("=").length > 0) {
							String endOffStr = paramArr[2].split("=")[1];
							endOff = Integer.parseInt(endOffStr);
						}							
						response = webPager(viewFile, elementText, beginOff, endOff);
					}
					
				}
			} else if (tokenStr.equals("jobName")) { 
				String elementText = stringTokenizer.nextElement().toString();
				if(httpContext.getPath().equals("/" + tlosWebConsole.getMSGDETAIL_CTX())) { //$NON-NLS-1$
					StringBuffer bufferText = new StringBuffer();
					if(TlosServer.getJobQueue().get(elementText).getJobProperties().getMessageBuffer() != null) {
						bufferText.append(TlosServer.getJobQueue().get(elementText).getJobProperties().getMessageBuffer());
					} else {
						bufferText.append("");
					}
					
					response = HtmlPages.getBufferPage(bufferText);
				}
			}
		}
		byte[] responseBytes;
		if(isXml) {
			responseBytes = response.getBytes("UTF-8"); //$NON-NLS-1$
		} else {
			responseBytes = response.getBytes("UTF-8"); // response.getBytes();
		}
		int contentLength = responseBytes.length;
		httpExchange.sendResponseHeaders(200, contentLength);
		//httpExchange.setAttribute("content", "text/html; charset=UTF-8");
		os = httpExchange.getResponseBody();
		os.write(responseBytes);
		os.close();
	}

	public String webPager(String logFile, String jobName, int beginOffset, int endOffset) {
		return HtmlPages.getHtmlPageData(logFile, jobName, beginOffset, endOffset);
	}
	
	public String webPager(String logFile) {
		return HtmlPages.getHtmlPageData(logFile);
	}

	public String xmlPager(String logFile) {
		return HtmlPages.getXMLPageData(logFile);
	}
	
	public String xslPager(String xslFile) {
		StringBuilder buf;

		buf = new StringBuilder();
		
//		final URL xslFileUrl = Tlos.class.getClassLoader().getResource(xslFile);
//		if(xslFileUrl == null) {
//			return null;
//		}
		InputStream inputStream = this.getClass().getResourceAsStream("/" + xslFile); //$NON-NLS-1$
		
		if (inputStream == null) {
			return null;
		}
		
		StringBuffer stringBuffer = FileUtils.readXSLFile(inputStream, ""); //$NON-NLS-1$
//		System.out.println("XSL URL : " + xslFileUrl);
//		StringBuffer stringBuffer = FileUtils.readXMLFile(xslFile, "");
		
		if (stringBuffer != null) {
			buf.append(stringBuffer);
		}
		
		return buf.toString();
	}
	
	public String endPage() {

		StringBuilder buff;

		buff = new StringBuilder();

		buff.append(HtmlPages.documentHeader("TLOS Scheduler - " + LocaleMessages.getString("FileViewHandler.2"))); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<p><pre>"); //$NON-NLS-1$

		buff.append(logoSection());

		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<HR COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$
		buff.append("<h3 align=\"center\" style=\"color: #FF0000\">TLOS Scheduler - " + LocaleMessages.getString("FileViewHandler.3") + "</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		buff.append(HtmlPages.pageFooter());

		buff.append("</body></html>\n"); //$NON-NLS-1$

		return buff.toString();
	}
}
