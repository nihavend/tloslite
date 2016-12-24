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
import java.util.HashMap;
import java.util.StringTokenizer;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.utils.HtmlFileReaderV2;
import com.likya.tlos.utils.HtmlPages;
import com.likya.tlos.utils.PasswordService;
import com.likya.tlos.utils.TlosException;
import com.likya.tlos.utils.loaders.AuthorizationLoader;
import com.likya.tlos.web.TlosWebConsole;
import com.sun.net.httpserver.HttpExchange;

public class PasswordHandler extends TlosHttpHandler {

	
	public PasswordHandler(TlosServer tlosServer, TlosWebConsole tlosWebConsole) {
		super(tlosServer, tlosWebConsole);
	}
	
	private static String MY_PARAMETER = "istek"; //$NON-NLS-1$

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		InputStream inputStream;
		OutputStream outputStream;
		String query;
		String response = null;

		// system?islem=0

		inputStream = httpExchange.getRequestBody();
		query = httpExchange.getRequestURI().getQuery();

		try {

			if (query != null) {
				int istekId = Integer.parseInt(queryHandler(query));

				switch (istekId) {
				case 0:
					response = webPager();
					break;
				case 1:
					if (httpExchange.getRequestMethod().equals("POST")) { //$NON-NLS-1$
						String bufferString = ""; //$NON-NLS-1$
						int i;
						while ((i = inputStream.read()) != -1) {
							bufferString = bufferString + (char) i;
						}
						HashMap<String, String> formMap = getFormMap(bufferString);
						try {
							if(!tlosWebConsole.getSessionUserName().equals(formMap.get("username")) || !tlosWebConsole.getSessionPassWord().equals(PasswordService.encrypt(formMap.get("old_password")))) { //$NON-NLS-1$ //$NON-NLS-2$
								response = errorPage(LocaleMessages.getString("PasswordHandler.0")); //$NON-NLS-1$
							} else if(!formMap.get("new_password").equals(formMap.get("conf_password"))) { //$NON-NLS-1$ //$NON-NLS-2$
								response = errorPage(LocaleMessages.getString("PasswordHandler.30")); //$NON-NLS-1$
							} else {
								TlosServer.getAuthorizationList().get(tlosWebConsole.getSessionUserName()).setPassWord(PasswordService.encrypt(formMap.get("new_password"))); //$NON-NLS-1$
								AuthorizationLoader.persistAuthorizationList(TlosServer.getAuthorizationList());
								response = successPage();

							}
						} catch (Exception e) {
							e.printStackTrace();
							response = errorPage();
						}
					} else {
						response = errorPage();
					}
					break;
				default:
					response = errorPage();
					break;
				}
			}

		} catch (TlosException tlosException) {
			// tlosException.printStackTrace();
			response = errorPage();
		}
		byte outputByteArray [] = response.getBytes("utf-8");
		httpExchange.sendResponseHeaders(200, outputByteArray.length);
		outputStream = httpExchange.getResponseBody();
		outputStream.write(outputByteArray);
		outputStream.close();

	}

	public String webPager() {

		StringBuilder buff;
		HtmlFileReaderV2 htmlFileReader = new HtmlFileReaderV2();

		buff = new StringBuilder();
		buff.append(htmlFileReader.getHeader1());
		
		buff.append(htmlFileReader.getCss("css/SyntaxHighlighter.css")); //$NON-NLS-1$
		buff.append(htmlFileReader.getCss("css/ddsmoothmenu.css")); //$NON-NLS-1$
		buff.append(htmlFileReader.getCss("css/tloslite.css")); //$NON-NLS-1$
		buff.append(htmlFileReader.getCss("css/grid.css")); //$NON-NLS-1$

		buff.append(htmlFileReader.getJs("js/ModalPopups.js")); //$NON-NLS-1$
		buff.append(htmlFileReader.getAboutPopupJs());
		buff.append(htmlFileReader.getJs("js/jquery.min.js")); //$NON-NLS-1$
		String tmp = "/***********************************************\n";//$NON-NLS-1$
		tmp+= "* Smooth Navigational Menu- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)\n";//$NON-NLS-1$
		tmp+= "* This notice MUST stay intact for legal use\n";//$NON-NLS-1$
		tmp+= "* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code\n";//$NON-NLS-1$
		tmp+= "***********************************************/\n";//$NON-NLS-1$
		buff.append(htmlFileReader.getJs("js/ddsmoothmenu.js", tmp)); //$NON-NLS-1$

		buff.append(HtmlFileReaderV2.getSection("header2.html")); //$NON-NLS-1$
		buff.append(ViewHandler.getHeader3());
		
//		buff.append("<html>\n"); //$NON-NLS-1$
//		buff.append("<head>\n"); //$NON-NLS-1$
//		buff.append("<title>Tlos Scheduler</title>\n"); //$NON-NLS-1$
//		buff.append(" \n"); //$NON-NLS-1$
//		buff.append("</head><body> \n"); //$NON-NLS-1$
//		buff.append(" \n"); //$NON-NLS-1$
//		buff.append("\n"); //$NON-NLS-1$
//		buff.append(" \n"); //$NON-NLS-1$
//		buff.append("\n"); //$NON-NLS-1$
		buff.append("<center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE BORDER=\"0\" align=\"center\" width=\"1100\">\n"); //$NON-NLS-1$
		buff.append("<TR>\n"); //$NON-NLS-1$
		buff.append("<TD> \n"); //$NON-NLS-1$
		buff.append("<TD align=\"center\" >\n"); //$NON-NLS-1$
//		buff.append(" <img src=\"/img/tlosana.jpg\"> \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</TD>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br> \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.1") + "\">" + LocaleMessages.getString("PasswordHandler.1") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		buff.append("<form action=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=1\" method=\"post\">"); //$NON-NLS-1$ //$NON-NLS-2$

		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>\n"); //$NON-NLS-1$
		buff.append(" <TR>\n"); //$NON-NLS-1$
		buff.append("    <TD bgColor=#999999>\n"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>\n"); //$NON-NLS-1$
		buff.append("        \n"); //$NON-NLS-1$
		buff.append("        <TR>\n"); //$NON-NLS-1$
		buff.append("          <TD bgColor=#ffffff   >\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" 	<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='right'  WIDTH='50' HEIGHT='20'><img src=\"/img/anah2.jpg\" width=\"50\" height=\"34\" alt=\"\" border=\"0\"></A></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN=left'  WIDTH='400' HEIGHT='20'> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b>" + LocaleMessages.getString("PasswordHandler.3") + "</FONT> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b></FONT></TD>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'> </TD>\n"); //$NON-NLS-1$
		buff.append("	</tr>		\n"); //$NON-NLS-1$
		buff.append("	\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>\n"); //$NON-NLS-1$
		buff.append(" <TR>\n"); //$NON-NLS-1$
		buff.append("    <TD bgColor=#999999>\n"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>\n"); //$NON-NLS-1$
		buff.append("        \n"); //$NON-NLS-1$
		buff.append("        <TR>\n"); //$NON-NLS-1$
		buff.append("          <TD bgColor=#ffffff  >\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$

		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		\n"); //$NON-NLS-1$
		buff.append(" 	<TD ALIGN='center' valign=bottom BGCOLOR='white' WIDTH='200' HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=#663399 SIZE='2'><b>" + LocaleMessages.getString("PasswordHandler.4") + "</b>  </td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("	<TD ALIGN='left' valign=bottom BGCOLOR='white' WIDTH='400' HEIGHT='20'><input name=\"username\" type=\"text\" size=\"10\" maxlength=\"10\"></td>\n"); //$NON-NLS-1$
		buff.append(" 		\n"); //$NON-NLS-1$
		buff.append("	</tr>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		\n"); //$NON-NLS-1$
		buff.append(" 	<TD ALIGN='center' valign=bottom BGCOLOR='white' WIDTH='200' HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=#663399 SIZE='2'><b>" + LocaleMessages.getString("PasswordHandler.5") + "</b>  </td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("	<TD ALIGN='left' valign=bottom BGCOLOR='white' WIDTH='400' HEIGHT='20'><input name=\"old_password\" type=\"password\" size=\"10\" maxlength=\"10\"></td>\n"); //$NON-NLS-1$
		buff.append(" 		\n"); //$NON-NLS-1$
		buff.append("	</tr>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		\n"); //$NON-NLS-1$
		buff.append(" 	<TD ALIGN='center' valign=bottom BGCOLOR='white' WIDTH='200' HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=#663399 SIZE='2'><b>" + LocaleMessages.getString("PasswordHandler.6") + "</b>  </td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("	<TD ALIGN='left' valign=bottom BGCOLOR='white' WIDTH='400' HEIGHT='20'><input name=\"new_password\" type=\"password\" size=\"10\" maxlength=\"10\"></td>\n"); //$NON-NLS-1$
		buff.append(" 		\n"); //$NON-NLS-1$
		buff.append("	</tr>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		\n"); //$NON-NLS-1$
		buff.append(" 	<TD ALIGN='center' valign=bottom BGCOLOR='white' WIDTH='200' HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=#663399 SIZE='2'><b>" + LocaleMessages.getString("PasswordHandler.7") + "</b>   </td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("	<TD ALIGN='left' valign=bottom BGCOLOR='white' WIDTH='400' HEIGHT='20'><input name=\"conf_password\" type=\"password\" size=\"10\" maxlength=\"10\">&nbsp;&nbsp;&nbsp;<input name=\"Degistir\" type=\"submit\" value=\"        " + LocaleMessages.getString("PasswordHandlerV2.0") + "       \"></td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append(" 		\n"); //$NON-NLS-1$
		buff.append("	</tr>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("  \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("<br/><br/><HR  size=\"1\" COLOR=\"black\" WIDTH=\"800\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1> Her Hakki Saklidir &#169 2008 - " + "<script type=\"text/javascript\">var d = new Date();var curr_year = d.getFullYear();document.write(curr_year);</script>" + " Likya Bilgi Teknolojileri ve Ilet. Hiz. Ltd. Sti.</font>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1>\n"); //$NON-NLS-1$
		buff.append("<a href=\"http://www.likyateknoloji.com\" title=\"www.likyateknoloji.com\">\n"); //$NON-NLS-1$
		buff.append("www.likyateknoloji.com</a>&nbsp;&nbsp;\n"); //$NON-NLS-1$
		buff.append("<a href=\"mailto:bilgi@likyateknoloji.com\">bilgi@likyateknoloji.com</a>\n"); //$NON-NLS-1$
		buff.append("</font>\n"); //$NON-NLS-1$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("</center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE align=\"center\" WIDTH='1100' BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
//		buff.append("<td align=\"center\"><img src=\"/img/likya_k.jpg\" ></td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</body></html>\n"); //$NON-NLS-1$
		buff.append("</form>\n"); //$NON-NLS-1$

		return buff.toString();
	}
	
	
	public String webPagerOld() {

		StringBuilder buff;

		buff = new StringBuilder();
		buff.append(HtmlPages.documentHeader("TLOS Scheduler " + LocaleMessages.getString("PasswordHandler.8"))); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append("<body>"); //$NON-NLS-1$
		buff.append("<p><pre>"); //$NON-NLS-1$

		buff.append(logoSection());

		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.31") + "\">" + LocaleMessages.getString("PasswordHandler.9") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		buff.append("<HR COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$
		
		buff.append("<form action=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=1\" method=\"post\">"); //$NON-NLS-1$ //$NON-NLS-2$

		buff.append("<table border=\"0\" align=\"center\" width=\"400\">"); //$NON-NLS-1$

		buff.append("<tr>"); //$NON-NLS-1$
		// buff.append("<td><label>Kullan�c� Ad� : </label></td>");
		buff.append("<td><label>" + LocaleMessages.getString("PasswordHandler.10") + "</label></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("<td><input name=\"username\" type=\"text\" size=\"10\" maxlength=\"10\"></td>"); //$NON-NLS-1$
		buff.append("</tr>"); //$NON-NLS-1$

		buff.append("<tr>"); //$NON-NLS-1$
		// buff.append("<td><label>Eski �ifre : </label></td>");
		buff.append("<td><label>" + LocaleMessages.getString("PasswordHandler.11") + "</label></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("<td><input name=\"old_password\" type=\"password\" size=\"10\" maxlength=\"10\"></td>"); //$NON-NLS-1$
		buff.append("</tr>"); //$NON-NLS-1$

		buff.append("<tr>"); //$NON-NLS-1$
		buff.append("<td><label>" + LocaleMessages.getString("PasswordHandler.12") + "</label></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// buff.append("<td><label>Yeni �ifre : </label></td>");
		buff.append("<td><input name=\"new_password\" type=\"password\" size=\"10\" maxlength=\"10\"></td>"); //$NON-NLS-1$
		buff.append("</tr>"); //$NON-NLS-1$

		buff.append("<tr>"); //$NON-NLS-1$
		//buff.append("<td colspan=\"2\" align=\"center\"><input name=\"De�i�tir\" type=\"submit\" value=\"De�i�tir\"></td>");
		buff.append("<td colspan=\"2\" align=\"center\"><input name=\"Degistir\" type=\"submit\" value=\"Degistir\"></td>"); //$NON-NLS-1$
		buff.append("</tr>"); //$NON-NLS-1$

		buff.append("</table>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
		
		buff.append("</pre></p>"); //$NON-NLS-1$
		
		buff.append(HtmlPages.pageFooter());

		buff.append("</body></html>\n"); //$NON-NLS-1$

		return buff.toString();
	}

	public String errorPage() {
		String errorMessage = LocaleMessages.getString("PasswordHandler.13"); //$NON-NLS-1$
		return errorPage(errorMessage);
	}
	
	public String errorPage(String errorMessage) {

		StringBuilder buff;

		buff = new StringBuilder();
		
		buff.append("<html>\n"); //$NON-NLS-1$
		buff.append("<head>\n"); //$NON-NLS-1$
		buff.append("<title>TLOS Scheduler " + LocaleMessages.getString("PasswordHandler.14") + "</title>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</head><body> \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE BORDER=\"0\" align=\"center\" width=\"1100\">\n"); //$NON-NLS-1$
		buff.append("<TR>\n"); //$NON-NLS-1$
		buff.append("<TD> \n"); //$NON-NLS-1$
		buff.append("<TD align=\"center\" >\n"); //$NON-NLS-1$
		buff.append(" <img src=\"/img/tlosana.jpg\"> \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</TD>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br> \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.32") + "\">" + LocaleMessages.getString("PasswordHandler.15") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		buff.append("<form action=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=1\" method=\"post\">"); //$NON-NLS-1$ //$NON-NLS-2$

		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>\n"); //$NON-NLS-1$
		buff.append(" <TR>\n"); //$NON-NLS-1$
		buff.append("    <TD bgColor=#999999>\n"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>\n"); //$NON-NLS-1$
		buff.append("        \n"); //$NON-NLS-1$
		buff.append("        <TR>\n"); //$NON-NLS-1$
		buff.append("          <TD bgColor=#ffffff   >\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" 	<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='right'  WIDTH='50' HEIGHT='20'><img src=\"/img/error.jpg\" alt=\"\" border=\"0\"></A></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN=left'  WIDTH='400' HEIGHT='20'> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b>" + LocaleMessages.getString("PasswordHandler.3") + "</FONT> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b> Password</FONT></TD>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'> </TD>\n"); //$NON-NLS-1$
		buff.append("	</tr>		\n"); //$NON-NLS-1$
		buff.append("	\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		
		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>"); //$NON-NLS-1$
		buff.append(" <TR>"); //$NON-NLS-1$
		buff.append("<TD bgColor=#999999>"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>"); //$NON-NLS-1$
		buff.append("		        <TR>"); //$NON-NLS-1$
		buff.append("		          <TD bgColor=#ffffff  >"); //$NON-NLS-1$
		buff.append("		<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">"); //$NON-NLS-1$
		buff.append("<tr>"); //$NON-NLS-1$
		buff.append("		 	<TD ALIGN='center' valign=bottom BGCOLOR='white' HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=red  SIZE='3'><b>" + LocaleMessages.getString("PasswordHandler.17") + "</b>  </td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("			</tr>"); //$NON-NLS-1$
		buff.append("</table>"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>"); //$NON-NLS-1$
		
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("  \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("<br/><br/><HR  size=\"1\" COLOR=\"black\" WIDTH=\"800\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1> Her Hakki Saklidir (c) 2008-2009 Likya Bilgi Teknolojileri ve Ilet. Hiz. Ltd. Sti. </font>\n"); //$NON-NLS-1$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1>\n"); //$NON-NLS-1$
		buff.append("<a href=\"http://www.likyateknoloji.com\" title=\"www.likyateknoloji.com\">\n"); //$NON-NLS-1$
		buff.append("www.likyateknoloji.com</a>&nbsp;&nbsp;\n"); //$NON-NLS-1$
		buff.append("<a href=\"mailto:bilgi@likyateknoloji.com\">bilgi@likyateknoloji.com</a>\n"); //$NON-NLS-1$
		buff.append("</font>\n"); //$NON-NLS-1$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("</center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE align=\"center\" WIDTH='1100' BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td align=\"center\"><img src=\"/img/likya_k.jpg\" ></td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</body></html>\n"); //$NON-NLS-1$
		buff.append("</form>\n"); //$NON-NLS-1$

		return buff.toString();
	}
	
	public String errorPageOld(String errorMessage) {

		StringBuilder buff;

		buff = new StringBuilder();
		
		buff.append(HtmlPages.documentHeader("TLOS Scheduler " + LocaleMessages.getString("PasswordHandler.18"))); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<p><pre>"); //$NON-NLS-1$

		buff.append(logoSection());
		
		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.33") + "\">" + LocaleMessages.getString("PasswordHandler.19") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		buff.append("<HR COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$

		buff.append("<h1 align=\"center\" style=\"color: #FF0000\">" + errorMessage + "</h1>"); //$NON-NLS-1$ //$NON-NLS-2$

		buff.append("<div align=\"center\">"); //$NON-NLS-1$

		buff.append("<a href=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=0\" title=\"" + LocaleMessages.getString("PasswordHandler.34") + "\">" + LocaleMessages.getString("PasswordHandler.20") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		
		buff.append(HtmlPages.pageFooter());
		
		buff.append("</body></html>\n"); //$NON-NLS-1$

		return buff.toString();
	}

	public String successPage() {

		StringBuilder buff;

		buff = new StringBuilder();
		
		buff.append("<html>\n"); //$NON-NLS-1$
		buff.append("<head>\n"); //$NON-NLS-1$
		buff.append("<title>TLOS Scheduler " + LocaleMessages.getString("PasswordHandler.21") + "</title>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</head><body> \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE BORDER=\"0\" align=\"center\" width=\"1100\">\n"); //$NON-NLS-1$
		buff.append("<TR>\n"); //$NON-NLS-1$
		buff.append("<TD> \n"); //$NON-NLS-1$
		buff.append("<TD align=\"center\" >\n"); //$NON-NLS-1$
		buff.append(" <img src=\"/img/tlosana.jpg\"> \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</TD>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br> \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.35") + "\">" + LocaleMessages.getString("PasswordHandler.22") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		buff.append("<form action=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=1\" method=\"post\">"); //$NON-NLS-1$ //$NON-NLS-2$

		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>\n"); //$NON-NLS-1$
		buff.append(" <TR>\n"); //$NON-NLS-1$
		buff.append("    <TD bgColor=#999999>\n"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>\n"); //$NON-NLS-1$
		buff.append("        \n"); //$NON-NLS-1$
		buff.append("        <TR>\n"); //$NON-NLS-1$
		buff.append("          <TD bgColor=#ffffff   >\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" 	<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("	<tr>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='right'  WIDTH='50' HEIGHT='20'><img src=\"/img/ok2.jpg\" width=\"50\" height=\"34\" alt=\"\" border=\"0\"></A></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN=left'  WIDTH='400' HEIGHT='20'> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b>" + LocaleMessages.getString("PasswordHandler.3") + "</FONT> <FONT FACE='Century Gothic' COLOR='#6666CC' SIZE='3'><b> Password</FONT></TD>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'></TD>\n"); //$NON-NLS-1$
		buff.append("		<TD ALIGN='center'  WIDTH='100' HEIGHT='20'> </TD>\n"); //$NON-NLS-1$
		buff.append("	</tr>		\n"); //$NON-NLS-1$
		buff.append("	\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		
		buff.append("<table vspace=\"0\" hspace=\"0\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=600>"); //$NON-NLS-1$
		buff.append(" <TR>"); //$NON-NLS-1$
		buff.append("<TD bgColor=#999999>"); //$NON-NLS-1$
		buff.append("      <TABLE cellSpacing=1 cellPadding=0 width=600 border=0>"); //$NON-NLS-1$
		buff.append("		        <TR>"); //$NON-NLS-1$
		buff.append("		          <TD bgColor=#ffffff  >"); //$NON-NLS-1$
		buff.append("		<TABLE  WIDTH='600'  bgcolor=\"#e9ecf5\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">"); //$NON-NLS-1$
		buff.append("<tr>"); //$NON-NLS-1$
		buff.append("		 	<TD ALIGN='center' valign=bottom BGCOLOR='white'   HEIGHT='20'><FONT FACE='Geneva, Arial' COLOR=#008040 SIZE='3'><b>" + LocaleMessages.getString("PasswordHandler.24") + "</b>  </td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("			</tr>"); //$NON-NLS-1$
		buff.append("</table>"); //$NON-NLS-1$
		buff.append("</td></tr></table></td></tr></table>"); //$NON-NLS-1$
		
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("  \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<br>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("<br/><br/><HR  size=\"1\" COLOR=\"black\" WIDTH=\"800\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1> Her Hakki Saklidir (c) 2008-2009 Likya Bilgi Teknolojileri ve Ilet. Hiz. Ltd. Sti. </font>\n"); //$NON-NLS-1$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE  align=\"center\" BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td>\n"); //$NON-NLS-1$
		buff.append("<font face=\"century gothic\" color=\"gray\" size=1>\n"); //$NON-NLS-1$
		buff.append("<a href=\"http://www.likyateknoloji.com\" title=\"www.likyateknoloji.com\">\n"); //$NON-NLS-1$
		buff.append("www.likyateknoloji.com</a>&nbsp;&nbsp;\n"); //$NON-NLS-1$
		buff.append("<a href=\"mailto:bilgi@likyateknoloji.com\">bilgi@likyateknoloji.com</a>\n"); //$NON-NLS-1$
		buff.append("</font>\n"); //$NON-NLS-1$
		buff.append("</td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append(" \n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("</center>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<TABLE align=\"center\" WIDTH='1100' BORDER='0' cellspacing=\"2\" cellpadding=\"2\"  valign=\"TOP\">\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("<tr>\n"); //$NON-NLS-1$
		buff.append("<td align=\"center\"><img src=\"/img/likya_k.jpg\" ></td>\n"); //$NON-NLS-1$
		buff.append("</tr>\n"); //$NON-NLS-1$
		buff.append("</table>\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("\n"); //$NON-NLS-1$
		buff.append("</body></html>\n"); //$NON-NLS-1$
		buff.append("</form>\n"); //$NON-NLS-1$

		return buff.toString();
	}


	public String successPageOld() {

		StringBuilder buff;

		buff = new StringBuilder();
		
		buff.append(HtmlPages.documentHeader("TLOS Scheduler " + LocaleMessages.getString("PasswordHandler.25"))); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<p><pre>"); //$NON-NLS-1$

		buff.append(logoSection());
		
		buff.append("<body>"); //$NON-NLS-1$

		buff.append("<div align=\"center\"><a href=\"/" + tlosWebConsole.getADMIN_CTX() + "\" title=\"" + LocaleMessages.getString("PasswordHandler.36") + "\">" + LocaleMessages.getString("PasswordHandler.29") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		buff.append("<HR COLOR=\"black\" WIDTH=\"80%\">"); //$NON-NLS-1$

		buff.append("<h3 align=\"center\" style=\"color: #FF0000\">" + LocaleMessages.getString("PasswordHandler.26") + "</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buff.append("<div align=\"center\">"); //$NON-NLS-1$
		buff.append("<a href=\"/" + tlosWebConsole.getSYSTEM_CTX() + "?istek=0\" title=\"" + LocaleMessages.getString("PasswordHandler.37") + "\">" + LocaleMessages.getString("PasswordHandler.27") + "</a></div>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		buff.append(HtmlPages.pageFooter());
		
		buff.append("</body></html>\n"); //$NON-NLS-1$

		return buff.toString();
	}

	public String queryHandler(String urlQuery) throws TlosException {

		String name;

		StringTokenizer st = new StringTokenizer(urlQuery, "="); //$NON-NLS-1$

		if (st.hasMoreTokens()) {
			name = st.nextToken();

			if (!name.equals(MY_PARAMETER)) {
				throw new TlosException(LocaleMessages.getString("PasswordHandler.28")); //$NON-NLS-1$
			}

			String istek;

			if ((istek = st.nextToken()) != null) {
				return istek;
			}
		}
		throw new TlosException(LocaleMessages.getString("PasswordHandler.28")); //$NON-NLS-1$
	}
}
