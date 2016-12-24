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
package com.likya.tlos.utils;

import java.util.ArrayList;

import com.likya.tlos.LocaleMessages;


public class HtmlPages {
	
	public static StringBuilder documentHeader() {
		return documentHeader(null);
	}
	
	public static StringBuilder documentHeader(String title) {
		
		StringBuilder documentHeader = new StringBuilder();
		
		if(title == null) {
			title = "Tlos Scheduler"; //$NON-NLS-1$
		}
		documentHeader.append("<html>"); //$NON-NLS-1$
		documentHeader.append("<head>"); //$NON-NLS-1$
		documentHeader.append("<title>" + title + "</title>"); //$NON-NLS-1$ //$NON-NLS-2$
		documentHeader.append("<META HTTP-EQUIV=\"CACHE-CONTROL\" CONTENT=\"NO-CACHE\">"); //$NON-NLS-1$
		documentHeader.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n");
		// documentHeader.append("<meta http-equiv=\"refresh\" content=\"20\" />");
		documentHeader.append("<style type=\"text/css\">"); //$NON-NLS-1$
		documentHeader.append("<!--"); //$NON-NLS-1$
		documentHeader.append(".style11 {font-size: 14mm;font-family: Georgia, \"Times New Roman\", Times, serif;}"); //$NON-NLS-1$
		documentHeader.append("-->"); //$NON-NLS-1$
		documentHeader.append("</style>"); //$NON-NLS-1$
		documentHeader.append("</head>"); //$NON-NLS-1$
		
		return documentHeader;
	}
	
	
	public static StringBuilder pageFooter() {
		
		StringBuilder footerValue = new StringBuilder();
		// footerValue.append("<div align=\"center\"style=\"color: #FF0000\">Her Hakkı saklıdır (c) 2008 Likya Bilgi Teknolojileri ve İlet. Hiz. Ltd.</div>");
		footerValue.append("<br/><br/>"); //$NON-NLS-1$
		
		footerValue.append("<HR  size=\"1\" COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$
		footerValue.append("<h6 align=\"center\">Her Hakki Saklidir (c) 2008 Likya Bilgi Teknolojileri ve Ilet. Hiz. Ltd. Sti.<br>"); //$NON-NLS-1$
		footerValue.append("<a href=\"http://www.likyateknoloji.com\" title=\"www.likyateknoloji.com\">www.likyateknoloji.com</a>"); //$NON-NLS-1$
		footerValue.append("&nbsp;&nbsp;<a href=\"mailto:bilgi@likyateknoloji.com\">bilgi@likyateknoloji.com</a></h6>"); //$NON-NLS-1$
		// footerValue.append("<h6 align=\"center\">www.likyateknoloji.com bilgi@likyateknoloji.com</h6>");
		return footerValue;
	}

	public static String getHtmlPageData(String logFile) {
		return getHtmlPageData(logFile, true);
	}
	
	public static String getHtmlPageData(String logFile, String jobName, int beginOffset, int endOffset) {
		return getHtmlPageDataControlled(logFile, true, jobName, beginOffset, endOffset);
	}
	
	public static String getHtmlPageData(String logFile, boolean cleanEscapeChars) {
		
		StringBuilder buf;

		buf = new StringBuilder();
		buf.append(documentHeader());
		buf.append("<body>"); //$NON-NLS-1$
		buf.append("<p><pre>"); //$NON-NLS-1$

		StringBuffer stringBuffer = FileUtils.readTxtFile(logFile, "ERROR", cleanEscapeChars); //$NON-NLS-1$
		if (stringBuffer != null) {
			String str = "<br/><font color=\"red\">" + LocaleMessages.getString("FileViewHandler.0") + "</font>" + '\n'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buf.append(str);
			buf.append(stringBuffer.toString());
			str = "<font color=\"red\">" + LocaleMessages.getString("FileViewHandler.1") + "</font>" + '\n'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buf.append(str);
		}

		buf.append(pageFooter());

		buf.append("</pre></p>"); //$NON-NLS-1$
		buf.append("</body></html>\n"); //$NON-NLS-1$

		buf.append("</form>"); //$NON-NLS-1$
		
		return buf.toString();
	}
	
	public static String getHtmlPageDataControlled(String logFile, boolean cleanEscapeChars, String jobName, int beginOffset, int endOffset) {
		
		StringBuilder buf;

		buf = new StringBuilder();
		buf.append(documentHeader());
		buf.append("<body>"); //$NON-NLS-1$
		buf.append("<p><pre>"); //$NON-NLS-1$

		buf.append("<form action=\"logdetail\">");
		
		
		ArrayList<Long> fileSize = new ArrayList<Long>();
		
		StringBuffer stringBuffer = getEndOfLog(fileSize, logFile, cleanEscapeChars, beginOffset, endOffset); //$NON-NLS-1$
		
		if (stringBuffer != null) {
			
			buf.append("<input type=\"hidden\" name=\"fname\" value='" + jobName + "'><br>");
			buf.append("Dosya boyu : " + fileSize.get(0).toString() + " bytes<br>");
			buf.append("Başlangıç offset: <input type=\"number\" name=\"beginoff\" value='" + beginOffset + "'><br>");
			buf.append("Bitiş offset: <input type=\"number\" name=\"endoff\"  value='" + endOffset + "'><br>");
			buf.append("<input type=\"submit\" value=\"Güncelle\">");
			buf.append("<textarea id='limitedLogView' rows=\"30\" cols=\"120\">");
			buf.append(stringBuffer.toString());
			buf.append("</textarea>");

//			String str = "<br/><font color=\"red\">" + LocaleMessages.getString("FileViewHandler.0") + "</font>" + '\n'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			buf.append(str);
//			str = "<font color=\"red\">" + LocaleMessages.getString("FileViewHandler.1") + "</font>" + '\n'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			buf.append(str);
		}

		buf.append(pageFooter());

		buf.append("<script>");
		buf.append("var textarea = document.getElementById('limitedLogView');");
		buf.append("textarea.scrollTop = textarea.scrollHeight");
		buf.append("</script>");
		
		buf.append("</form>");
		
		buf.append("</pre></p>"); //$NON-NLS-1$
		buf.append("</body></html>\n"); //$NON-NLS-1$

		return buf.toString();
	}
	
	public static String getBufferPage(StringBuffer bufferText) {
		
		StringBuilder buf;

		buf = new StringBuilder();
		buf.append(documentHeader());
		buf.append("<body>"); //$NON-NLS-1$
		buf.append("<p><pre>"); //$NON-NLS-1$

		buf.append(bufferText);
		
		buf.append("</pre></p>"); //$NON-NLS-1$
		buf.append("</body></html>\n"); //$NON-NLS-1$

		return buf.toString();
	}
	
	public static String getXMLPageData(String logFile) {
		
		StringBuilder buf;

		buf = new StringBuilder();
		StringBuffer stringBuffer = FileUtils.readXMLFile(logFile, ""); //$NON-NLS-1$
		if (stringBuffer != null) {
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
			buf.append("<?xml-stylesheet type=\"text/xsl\" href=\"logdetail?fname=tlos.xsl\"?>"); //$NON-NLS-1$
			buf.append(stringBuffer);
		}
		
		return buf.toString();
	}
	
	private static StringBuffer getEndOfLog(ArrayList<Long> fileSize, String logFile, boolean cleanEscapeChars, int beginOffset, int endOffset) {
		
		StringBuffer stringBuffer = FileUtils.readTextFile(fileSize, beginOffset, endOffset, logFile, "ERROR", cleanEscapeChars);
		
		return stringBuffer;
	}

}
