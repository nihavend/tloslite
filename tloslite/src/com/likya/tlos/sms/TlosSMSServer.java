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
package com.likya.tlos.sms;

import java.util.ArrayList;
import java.util.Iterator;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.model.TlosParameters;
import com.likya.tlos.sms.helpers.SMSType;
import com.likya.tlos.sms.helpers.TlosSMSHandler;

/**
 * @author vista
 * 
 */
public class TlosSMSServer implements Runnable {

	private final int timeout = 10000;
	private boolean executePermission = true;
	private ArrayList<SMSType> smsQueue = new ArrayList<SMSType>();
	
	private ArrayList<String> msisdnList;

	private TlosSMSHandler tlosSMSHandler;

	public TlosSMSServer(TlosParameters tlosParameters, TlosSMSHandler tlosSMSHandler) {
		this.tlosSMSHandler = tlosSMSHandler;
		this.msisdnList = tlosSMSHandler.getMsisdnList();
	}

	public void terminate(boolean forcedTerminate) {
		synchronized (this) {
			if (forcedTerminate) {
				smsQueue.clear();
			}
			this.executePermission = false;
		}
	}

	public void run() {
		
		while (executePermission || (smsQueue.size() > 0 && msisdnList.size() > 0)) {
			
			while (smsQueue.size() > 0) {
				SMSType smsType = (SMSType) smsQueue.get(0);
				
				Iterator<String> msisdnIterator = msisdnList.iterator();
				
				while (msisdnIterator.hasNext()) {
					
					TlosServer.getLogger().debug(LocaleMessages.getString("TlosSMSServer.0")); //$NON-NLS-1$
					try {
						tlosSMSHandler.sendSMS(msisdnIterator.next(), smsType.getMessageTxt());
					} catch (Exception e) {
						e.printStackTrace();
						TlosServer.getLogger().info(LocaleMessages.getString("TlosSMSServer.1") + e.getMessage() + LocaleMessages.getString("TlosSMSServer.2")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				smsQueue.remove(0);
			}
			try {
				// TlosServer.getLogger().debug("Mail server sleeping !");
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		TlosServer.getLogger().info(LocaleMessages.getString("TlosSMSServer.3")); //$NON-NLS-1$
		TlosServer.getLogger().info(LocaleMessages.getString("TlosSMSServer.4") + smsQueue.size()); //$NON-NLS-1$
	}

	private synchronized void addSMS(SMSType smsType) {
		smsQueue.add(smsType);
	}

	public void sendSMS(SMSType smsType) {
		addSMS(smsType);
	}

	public int getQueueSize() {
		return smsQueue.size();
	}
}
