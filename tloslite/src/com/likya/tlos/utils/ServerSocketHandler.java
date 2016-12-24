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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.comm.TlosCommInterface;
import com.likya.tlos.model.JobProperties;
import com.likya.tlos.model.TlosInfo;

public class ServerSocketHandler implements Runnable {

	private Socket client;
	private TlosCommInterface tlosCommInterface;
	
	private ObjectInputStream objectInputStream = null;
	private ObjectOutputStream objectOutputStream = null;
	
	public ServerSocketHandler(Socket client, TlosCommInterface tlosCommInterface) {
		super();
		this.client = client;
		this.tlosCommInterface = tlosCommInterface;
	}
	
	@Override
	public void run() {
		
		try {	
		
			TlosInfo tlosInfo = readData(client, objectInputStream);		
			
			int commandTypeValue = tlosInfo.getCommandType().getId();
			TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.0") + tlosInfo.getCommandType().getDescription()); //$NON-NLS-1$
			TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.1") + tlosInfo.getClientId()); //$NON-NLS-1$
			
			switch (commandTypeValue) {
			
			case TlosInfo.NORMAL_TERMINATE:
				TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.2")); //$NON-NLS-1$
				
				tlosInfo.setErrCode(0);
				writeData(client, tlosInfo);
				cleanUp();
				
				tlosCommInterface.gracefulShutDown();
//				loopPermit = false;
				
				break;
			
			case TlosInfo.FORCED_TERMINATE:
				TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.3")); //$NON-NLS-1$
				
				tlosInfo.setErrCode(0);
				writeData(client, tlosInfo);
				cleanUp();
				
				tlosCommInterface.forceFullShutDown();
//				loopPermit = false;
				
				break;

			case TlosInfo.RESUME_JOB:
				String jobName = tlosInfo.getJobId();
				if(TlosServer.getJobQueue().containsKey(jobName) && ((TlosServer.getJobQueue().get(jobName).getJobProperties().getStatus() == JobProperties.FAIL) || (TlosServer.getJobQueue().get(jobName).getJobProperties().getStatus() == JobProperties.STOP))) {
					TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.4") + jobName + " READY !"); //$NON-NLS-1$ //$NON-NLS-2$
					tlosCommInterface.retryExecution(jobName);
				} else {
					TlosServer.getLogger().info(LocaleMessages.getString("ServerSocketHandler.6") + jobName); //$NON-NLS-1$
					tlosInfo.setErrCode(1);
				}
				tlosInfo.setErrCode(0);
				
				writeData(client, tlosInfo);
				cleanUp();
				
				break;
			
			case TlosInfo.TLOS_STATUS:						
				if (TlosServer.isThresholdOverflow()) {
					tlosInfo.setTlosStatus(TlosInfo.STATE_JOBOVERFLOW);
					tlosInfo.setErrCode(0);
				} else if (TlosServer.getExecutionState() == TlosInfo.STATE_SUSPENDED) {
					tlosInfo.setTlosStatus(TlosInfo.STATE_SUSPENDED);
					tlosInfo.setErrCode(0);
				} else {
					tlosInfo.setErrCode(1);
					tlosInfo.setErrDesc(LocaleMessages.getString("ServerSocketHandler.7")); //$NON-NLS-1$
				}
				writeData(client, tlosInfo);
				cleanUp();
				
				break;
			
			case TlosInfo.JOB_STATUSLIST:

				jobName = tlosInfo.getJobId();
				if(TlosServer.getJobQueue().containsKey(jobName)) {
					ArrayList<Integer> statusList = new ArrayList<Integer>();
					statusList.add(TlosServer.getJobQueue().get(jobName).getJobProperties().getStatus());
					
					for (int i = TlosServer.getJobQueue().get(jobName).getJobProperties().getPreviousStatusList().size() - 1; i >= 0; i--){
						statusList.add(TlosServer.getJobQueue().get(jobName).getJobProperties().getPreviousStatusList().get(i));
					}
					
					tlosInfo.setJobStatusHistory(statusList);
					
					tlosInfo.setExecutionDate(TlosServer.getJobQueue().get(jobName).getJobProperties().getExecutionDate());
					tlosInfo.setNextExecutionDate(TlosServer.getJobQueue().get(jobName).getJobProperties().getTime());
					tlosInfo.setErrCode(0);
				} else {
					tlosInfo.setErrCode(1);
					tlosInfo.setErrDesc(jobName + LocaleMessages.getString("ServerSocketHandler.8")); //$NON-NLS-1$
				}
				writeData(client, tlosInfo);
				cleanUp();
				
				break;
				
			case TlosInfo.DUMP_JOB_LIST:
				
				JobQueueOperations.dumpJobQueue(TlosServer.getJobQueue(), true);
				tlosInfo.setErrCode(0);
				
				writeData(client, tlosInfo);
				cleanUp();

				break;

			default:
				writeData(client, tlosInfo);
				cleanUp();
				
				break;
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void writeData(Socket client, TlosInfo tlosInfoTx) throws IOException {
		
		OutputStream os = client.getOutputStream();
		
		objectOutputStream = new ObjectOutputStream(os);
		
		objectOutputStream.writeObject(tlosInfoTx);
		
		objectOutputStream.flush();
		objectOutputStream.close();
	}
	

	public TlosInfo readData(Socket client, ObjectInputStream ois) throws IOException, ClassNotFoundException {
		InputStream is = client.getInputStream();
		ois = new ObjectInputStream(is);
		TlosInfo tlosInfoRx = (TlosInfo) ois.readObject();
		return tlosInfoRx;
	}
	
	private void cleanUp() {
		
		try {
			if(objectOutputStream != null) objectOutputStream.close();
			if(objectInputStream != null) objectInputStream.close();
			if(client != null) client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
}
