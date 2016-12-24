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
package com.likya.tlos;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

import com.likya.tlos.model.CommandType;
import com.likya.tlos.model.TlosInfo;

public class TlosRemoteManager {
	// static final int PORT = 3030;

	private static final String USAGE_MSG = LocaleMessages.getString("TlosRemoteManager.0"); //$NON-NLS-1$
	private static final int retryCountMax = 10;
	
	public static void main(String[] args) throws Exception {

		// String msg = (args.length > 0) ? args[0] : "TERMINATE";
		// InetAddress dest = InetAddress.getByName((args.length > 1) ? args[1]
		// : "127.0.0.1");

		int i = 0;
		String arg;

		String port = null;
		String host = null;

		String jobname = null;
		String terminate = null;
		String fterminate = null;
		
		boolean vflag = false;
		boolean jflag = false;

		while (i < args.length && args[i].startsWith("-")) { //$NON-NLS-1$

			arg = args[i++];
			// use this type of check for "wordy" arguments
			if (arg.equals("-verbose")) { //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("TlosRemoteManager.3")); //$NON-NLS-1$
				vflag = true;

			} else if (arg.equals("-port")) { //$NON-NLS-1$
				if (i < args.length)
					port = args[i++];
				else
					System.err.println(LocaleMessages.getString("TlosRemoteManager.5")); //$NON-NLS-1$
				if (vflag)
					System.out.println(LocaleMessages.getString("TlosRemoteManager.6") + port); //$NON-NLS-1$
			} else if (arg.equals("-host")) { //$NON-NLS-1$
				if (i < args.length)
					host = args[i++];
				else
					System.err.println(LocaleMessages.getString("TlosRemoteManager.8")); //$NON-NLS-1$
				if (vflag)
					System.out.println(LocaleMessages.getString("TlosRemoteManager.9") + host); //$NON-NLS-1$

			} else if (arg.equals("-jobname")) { //$NON-NLS-1$

				if (i < args.length) {
					jobname = args[i++];
				} else {
					System.err.println(LocaleMessages.getString("TlosRemoteManager.11")); //$NON-NLS-1$

					System.exit(0);
				}
			} else if (arg.equals("-dumpjoblist")) { //$NON-NLS-1$
				System.out.println(LocaleMessages.getString("TlosRemoteManager.13")); //$NON-NLS-1$
				jflag = true;
				
			} else if(arg.equals("-terminate")) { //$NON-NLS-1$
				terminate = arg;
			} else if(arg.equals("-fterminate")) { //$NON-NLS-1$
				fterminate = arg;
			} else {
				System.err.println(USAGE_MSG);
				System.exit(0);
			}

		}

		if ((host == null) || (port) == null) {
			System.err.println(USAGE_MSG);
			System.exit(0);
		} else {
			
			SocketChannel socketChannel = null;
			
			while (true) {
				int retryCount = 0;
				try {
					if (socketChannel == null || !socketChannel.isOpen()) {
						socketChannel = SocketChannel.open();
					}
					socketChannel.connect(new InetSocketAddress(host, Integer.parseInt(port)));
				} catch (ConnectException ce) {
					socketChannel.close();
					if (retryCount++ < retryCountMax) {
						// TICServer.println("Server connection is being retried!");
						System.out.println(LocaleMessages.getString("TlosRemoteManager.16")); //$NON-NLS-1$
						Thread.sleep(1000);
						continue;
					}
				}
				break;
			}
			
			TlosInfo tlosInfoTx = new TlosInfo();
			tlosInfoTx.setClientId("TlosRemoteManager"); //$NON-NLS-1$
			
			CommandType commandType = new CommandType();
			
			if (jobname != null) {
				commandType.setId(TlosInfo.RESUME_JOB);
				commandType.setDescription(TlosInfo.getCommandTypeDescription(TlosInfo.RESUME_JOB));
				tlosInfoTx.setJobId(jobname);
				
			} else if(terminate != null) {
				commandType.setId(TlosInfo.NORMAL_TERMINATE);
				commandType.setDescription(TlosInfo.getCommandTypeDescription(TlosInfo.NORMAL_TERMINATE));
				System.out.println(LocaleMessages.getString("TlosRemoteManager.18")); //$NON-NLS-1$
			} else if(fterminate != null) {
				commandType.setId(TlosInfo.FORCED_TERMINATE);
				commandType.setDescription(TlosInfo.getCommandTypeDescription(TlosInfo.FORCED_TERMINATE));
				System.out.println(LocaleMessages.getString("TlosRemoteManager.19")); //$NON-NLS-1$
			} else if(jflag) {
				commandType.setId(TlosInfo.DUMP_JOB_LIST);
				commandType.setDescription(TlosInfo.getCommandTypeDescription(TlosInfo.DUMP_JOB_LIST));
				System.out.println(LocaleMessages.getString("TlosRemoteManager.20")); //$NON-NLS-1$
			} else {
				System.err.println(USAGE_MSG);
				System.exit(0);
			}
			
			tlosInfoTx.setCommandType(commandType);
			writeData(socketChannel, tlosInfoTx);
			
			TlosInfo tlosInfoRx = readData(socketChannel);
			
			if(tlosInfoRx.getErrCode() != 0) {
				System.out.println(LocaleMessages.getString("TlosRemoteManager.21") + tlosInfoRx.getErrCode() + LocaleMessages.getString("TlosRemoteManager.22") + tlosInfoRx.getErrDesc()); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}

	}
	
	public static void writeData(SocketChannel socketChannel, TlosInfo tlosInfo) throws IOException, ClassNotFoundException {

		OutputStream is = Channels.newOutputStream(socketChannel);
		ObjectOutputStream ois = new ObjectOutputStream(is);
		ois.writeObject(tlosInfo);

		return;
	}
	
	public static TlosInfo readData(SocketChannel socketChannel) throws IOException, ClassNotFoundException {

		InputStream is = Channels.newInputStream(socketChannel);
		ObjectInputStream ois = new ObjectInputStream(is);
		TlosInfo tlosInfo = (TlosInfo) ois.readObject();

		return tlosInfo;
	}
}
