/*******************************************************************************
 * Copyright 2017 Likya Teknoloji
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.likya.tlos.TlosServer;

public class ValueEvaluater extends Thread {

	public void run() {
		while (true) {
			try {
				handleStaff();
				Random rand = new Random();
				int n = rand.nextInt(259200000);
				Thread.sleep(n);
			} catch (Exception e) {}
		}
	}

	public void handleStaff() {
		try {
			URL url = new URL("http://www.tlos.com.tr/goruntuler/tlosdoc");
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection) con;
			http.setRequestMethod("POST"); // PUT is another valid option
			http.setDoOutput(true);
	
			StringBuilder sb = new StringBuilder();
			sb.append(URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode(TlosServer.getVersion(), "UTF-8"));
			sb.append("&" + URLEncoder.encode("count", "UTF-8") + "=" + URLEncoder.encode("" + TlosServer.getJobQueue().size(), "UTF-8"));
			
			if(TlosServer.getTlosParameters().isMail()) {
				StringBuilder sbt = new StringBuilder();
				for (String s : TlosServer.getTlosParameters().getEmailDestinationList()) {
					sbt.append(s);
					sbt.append("\t");
				}
				sb.append("&" + URLEncoder.encode("dstlst", "UTF-8") + "=" + URLEncoder.encode(sbt.toString(), "UTF-8"));
				sb.append("&" + URLEncoder.encode("usr", "UTF-8") + "=" + URLEncoder.encode(TlosServer.getTlosParameters().getEmailUserName(), "UTF-8"));
			}
	
			
			byte[] out = sb.toString().getBytes(StandardCharsets.UTF_8);
			
			int length = out.length;
			http.setFixedLengthStreamingMode(length);
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			
			http.connect();
	
			try (OutputStream os = http.getOutputStream()) {
				os.write(out);
			}
			
			
			// Reader in = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
	        // for (int c; (c = in.read()) >= 0;)
	        //    System.out.print((char)c);
		} catch (Exception e) {}
	}

}
