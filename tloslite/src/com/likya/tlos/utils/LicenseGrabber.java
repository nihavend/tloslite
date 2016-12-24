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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class LicenseGrabber extends Thread {
	
	InputStream is;
	String type;
	String outputBuffer = ""; //$NON-NLS-1$
	
	boolean live = true;
	
	public LicenseGrabber(InputStream is) {
		this.is = is;
	}
	
	public synchronized void stopStreamGobbler() {
		live = false;
	}

	public void run() {
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String bufferString = ""; //$NON-NLS-1$

			/*
			 * Not 1 : Eğer, bufferedReader.ready() olmadan
			 * bufferedReader.readLine() yapacak olur isek, thread okuma
			 * satırında takıldığında, terminate etmek işe yaramıyor.
			 */
			while (!bufferedReader.ready()) {
				if(!live) {
					bufferedReader.close();
					inputStreamReader.close();
					exitClass();
					return;
				}
				Thread.sleep(200);
			}

			while ((bufferString = bufferedReader.readLine()) != null) {
				outputBuffer += bufferString + '\n';
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		exitClass();
	}
	
	private void exitClass() {
		try {
			is.close();
		} catch (IOException e) {
		}
		is = null;
	}

	public String getMessage() {
		return outputBuffer;
	}
}
