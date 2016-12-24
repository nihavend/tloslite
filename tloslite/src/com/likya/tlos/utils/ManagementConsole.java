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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.comm.TlosCommInterface;

public class ManagementConsole implements Runnable {

	private int PORT = 0; // 3030;
	private String IPADDRESS = null;
	
	private TlosCommInterface tlosCommInterface;
	// private ServerSocketChannel serverChannel; 
	private ServerSocket serverSocket;
	
	public static ManagementConsole initComm(TlosCommInterface tlosCommInterface, int port, String ipAddress) throws SocketException {
		return new ManagementConsole(tlosCommInterface, port, ipAddress);
	}
	
	private ManagementConsole(TlosCommInterface tlosCommInterface, int port, String ipAddress) throws SocketException {
		
		this.PORT = port;
		this.IPADDRESS = ipAddress;
		this.tlosCommInterface = tlosCommInterface;

		try {
//			serverChannel = ServerSocketChannel.open();
//			serverChannel.configureBlocking(true);
//			serverChannel.socket().bind(new InetSocketAddress(IPADDRESS, PORT));
			serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName(IPADDRESS));
		} catch (RuntimeException re) {
			re.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		TlosServer.getLogger().debug(LocaleMessages.getString("ManagementConsole.0")); //$NON-NLS-1$

	}

	public void run() {
		Thread.currentThread().setName("ManagementConsole"); //$NON-NLS-1$
				
		boolean loopPermit = true; 

//		SocketChannel client = null;
		
		try {

			while (loopPermit) {
				Socket client = serverSocket.accept();
				new Thread(new ServerSocketHandler(client, tlosCommInterface)).start();
			}

		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
