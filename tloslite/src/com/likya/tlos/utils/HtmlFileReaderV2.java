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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;

/*
 * TlosTest_V1.7
 *  : TestFileReader.java
 * @author Serkan Taş
 * Tarih : Feb 1, 2009 1:49:58 AM
 */

public class HtmlFileReaderV2 {

	public static final String CONTENT_PATH = "/webroot/htmlv2/"; //$NON-NLS-1$
	
	public String getAll() {
		return getSection("TlosLite2.html"); //$NON-NLS-1$
	}
	
	public String getHeader1() {
		
		StringBuilder myStringBuilder = new StringBuilder();
		
		myStringBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"); //$NON-NLS-1$
		myStringBuilder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"); //$NON-NLS-1$
		myStringBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n");
		myStringBuilder.append("<head><title>TlosLite Scheduler " + TlosServer.getVersion() + "</title>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return myStringBuilder.toString(); //$NON-NLS-1$
	}
	
	public String getCss(String fileName) {
		String str = "<!--BEGIN " + fileName + LocaleMessages.getString("HtmlFileReaderV2.0"); //$NON-NLS-1$ //$NON-NLS-2$
		str += "<style type=\"text/css\">\n"; //$NON-NLS-1$
		str += getSection(fileName); //$NON-NLS-1$
		str += "</style>\n"; //$NON-NLS-1$
		str += "<!--END " + fileName + LocaleMessages.getString("HtmlFileReaderV2.0"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return str;
	}

	public String getJs(String fileName) {
		return getJs(fileName, null);
	}
	
	public String getAboutPopupJs() {
		
		String str = ""; //$NON-NLS-1$
		str += "<!--BEGIN ModalTlosLiteAboutBox ***************************************************************************************** -->\n"; //$NON-NLS-1$
		str += "<script type=\"text/javascript\">\n"; //$NON-NLS-1$
		str += "var d = new Date();var curr_year = d.getFullYear();"; //$NON-NLS-1$
		str += "function ModalTlosLiteAboutBox() {"; //$NON-NLS-1$
		str += "    ModalPopups.Alert('tlosLiteAboutBox',"; //$NON-NLS-1$
		str += "        '<div style=\\'float:left;\\'><img src=\\'images/icon2.gif\\'></div><div style=\\'float:left; padding-left:5px;\\'>" + LocaleMessages.getString("HtmlFileReaderV2.12") + "</div>',"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "        '<div style=\\'\\'>' +";  //$NON-NLS-1$
		str += "		'<div style=\\'float:left; padding-left:10px;\\'><img src=\\'images/likya1.8.0.gif\\'></div>' +";  //$NON-NLS-1$
		str += "        '<div style=\\'float:left; padding-left:10px;\\'><br/>Likya Bilgi Teknolojileri ve İletişim Hizmetleri Ltd. Şti.' +";  //$NON-NLS-1$
		str += "		'<br/><br/>&#169; "; //$NON-NLS-1$
		str += "		2008-' + curr_year + "; //$NON-NLS-1$
		str += "		' Tüm Hakları Saklıdır.' +"; //$NON-NLS-1$
		str += "		'<br/><br/>" + LocaleMessages.getString("HtmlFileReader.0") + "-" + "'+"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		'<br/><br/>" + LocaleMessages.getString("HtmlFileReaderV2.24") + "-" + "' +"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "		'</div>' +";  //$NON-NLS-1$
		str += "		'<div style=\\'float:left; padding-left:10px;\\'><br/><iframe src=\\'logdetail?fname=license\\' width=\\'488\\' height=\\'140\\'><p>"; //$NON-NLS-1$
		str += "			" + LocaleMessages.getString("HtmlFileReaderV2.29"); //$NON-NLS-1$ //$NON-NLS-2$
		str += "			</p></iframe></div>' +"; 	 //$NON-NLS-1$
		str += "        '</div>',"; //$NON-NLS-1$
		str += "        {"; //$NON-NLS-1$
		str += "			shadowSize: 7,"; //$NON-NLS-1$
		str += "            okButtonText: '" + LocaleMessages.getString("HtmlFileReaderV2.35") + "',"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		str += "            width: 520,"; //$NON-NLS-1$
		str += "            height: 450,"; //$NON-NLS-1$
		str += "			backgroundColor: 'White',"; //$NON-NLS-1$
		str += "			titleBackColor: '#e2e5e7',"; //$NON-NLS-1$
		str += "			popupBackColor: 'White',"; //$NON-NLS-1$
		str += "			footerBackColor: '#e2e5e7',"; //$NON-NLS-1$
		str += "			footerText: 'right',"; //$NON-NLS-1$
		str += "			shadowColor: '#063b6d',"; //$NON-NLS-1$
		str += "			borderColor: '#999999',"; //$NON-NLS-1$
		str += "			titleFontColor: 'Black',"; //$NON-NLS-1$
		str += "			fontSize: '8pt'"; //$NON-NLS-1$
		str += "        }"; //$NON-NLS-1$
		str += "    );"; //$NON-NLS-1$
		str += "}"; //$NON-NLS-1$
		str += "</script>\n"; //$NON-NLS-1$
		str += "<!--END ModalTlosLiteAboutBox ***************************************************************************************** -->\n"; //$NON-NLS-1$
		
		return str;
	}
	
	public String getJs(String fileName, String comment) {

		String str = "<!--BEGIN " + fileName + LocaleMessages.getString("HtmlFileReaderV2.0"); //$NON-NLS-1$ //$NON-NLS-2$
		str += "<script type=\"text/javascript\">\n"; //$NON-NLS-1$
		if(comment != null) {
			str += comment;
		}
		str += getSection(fileName); //$NON-NLS-1$
		str += "</script>\n"; //$NON-NLS-1$
		str += "<!--END " + fileName + LocaleMessages.getString("HtmlFileReaderV2.0"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return str;
	}

	public String getJs1() {

//		printFile("3.html"); // jquery.min.js
		String str = "<!--BEGIN jquery.min.js ***************************************************************************************** -->\n"; //$NON-NLS-1$
		str += "<script type=\"text/javascript\">\n"; //$NON-NLS-1$
		str += getSection("jquery.min.js"); //$NON-NLS-1$
		str += "</script>\n"; //$NON-NLS-1$
		str += "<!--END jquery.min.js ***************************************************************************************** -->\n"; //$NON-NLS-1$
		
		return str;
	}

	public String getJs2() {

//		printFile("4.html"); // ddsmoothmenu.js
		String str = "<!--BEGIN ddsmoothmenu.js ***************************************************************************************** -->"; //$NON-NLS-1$
		str += "<script type=\"text/javascript\">"; //$NON-NLS-1$
		str += getSection("ddsmoothmenu.js"); //$NON-NLS-1$
		str += "</script>"; //$NON-NLS-1$
		str += "<!--END ddsmoothmenu.js ***************************************************************************************** -->"; //$NON-NLS-1$

		return str;
	}

	public static String getSection(String fileName) {
		InputStream fis = null;
		StringBuffer outputBuffer = new StringBuffer();

		try {
			fis = HtmlFileReaderV2.class.getResourceAsStream(CONTENT_PATH + fileName); //$NON-NLS-1$
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = null;

			while ((bufferString = bufferedReader.readLine()) != null) {
				outputBuffer.append(bufferString);
				outputBuffer.append("\n"); //$NON-NLS-1$
			}
			fis.close();
		} catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return outputBuffer.toString();
	}
}
