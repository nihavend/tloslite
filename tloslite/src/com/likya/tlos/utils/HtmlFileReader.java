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

/*
 * TlosTest_V1.7
 *  : TestFileReader.java
 * @author Serkan Ta�
 * Tarih : Feb 1, 2009 1:49:58 AM
 */

public class HtmlFileReader {

	public String getCss() {
//		printFile("2.html"); // ddsmoothmenu.css
		String str = "<!--BEGIN ddsmoothmenu.css ***************************************************************************************** -->\n"; //$NON-NLS-1$
		str += "<style type=\"text/css\">\n"; //$NON-NLS-1$
		str += getSection("ddsmoothmenu.css"); //$NON-NLS-1$
		str += "</style>\n"; //$NON-NLS-1$
		str += "<!--END ddsmoothmenu.css ***************************************************************************************** -->\n"; //$NON-NLS-1$
		
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
	
	private String getSection(String fileName) {
		//FileInputStream fis = null;
		InputStream fis = null;
		StringBuffer outputBuffer = new StringBuffer();

		try {
			// fis = new FileInputStream(fileName);
			fis = this.getClass().getResourceAsStream("/webroot/html/" + fileName); //$NON-NLS-1$
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = null;

			while ((bufferString = bufferedReader.readLine()) != null) {
				//bufferString = bufferString.replaceAll("\\\\", "\\\\\\\\");
				// bufferString = bufferString.replaceAll("\"", "\\\\\"");
				outputBuffer.append(bufferString);
				outputBuffer.append("\n"); //$NON-NLS-1$
				// System.out.println("buff.append(\"" + bufferString + "\\n\");");
			}
			// System.out.println(outputBuffer);
			fis.close();
		} catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return outputBuffer.toString();
	}
}
