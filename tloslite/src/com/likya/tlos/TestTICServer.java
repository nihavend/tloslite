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

/**
 * @author vista
 * 
 */
public class TestTICServer implements Runnable {

	private Thread reference;

	public Thread getReference() {
		return reference;
	}

	public void setReference(Thread reference) {
		this.reference = reference;
	}

	public TestTICServer() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		Thread.currentThread().setName("LikyaTICServer");

		System.out.println("run : Starting...");

		SocketChannel socketChannel = null;

		int retryCount = 0;

		boolean executionPermission = true;

		int port = 5001;
		String ipAddress = "127.0.0.1";

		int clientCounter = 0;
		
		while (executionPermission) {

			TlosInfo tlosInfoTx = new TlosInfo();
			tlosInfoTx.setClientId("Test Client");

			CommandType commandType = new CommandType();
			commandType.setId(TlosInfo.TLOS_STATUS);
			commandType.setDescription(TlosInfo.getStatusString(TlosInfo.TLOS_STATUS));
			tlosInfoTx.setCommandType(commandType);

			try {
				if (socketChannel == null || !socketChannel.isOpen()) {
					socketChannel = SocketChannel.open();
					System.out.println("server socket opened isOpen: " + socketChannel.isOpen());
				}

				try {
					System.out.println("Trying to connect to server with ip: " + ipAddress + " and port: " + port);
					socketChannel.connect(new InetSocketAddress(ipAddress, port));
					System.out.println("Tlos has been connected to server");
				} catch (ConnectException ce) {
					socketChannel.close();
					if (retryCount++ < 100) {
						System.out.println("Server connection is being retried !");
						Thread.sleep(1000);
						continue;
					}

					System.out.println("Connection attempts reached to retryCount number !");
					System.exit(2);
				}

				System.out.println("Request is being sent to server with parameters:");
				System.out.println("Client Id: " + tlosInfoTx.getClientId());
				System.out.println("Command Type: " + tlosInfoTx.getCommandType().getDescription());

				tlosInfoTx.setClientId(tlosInfoTx.getClientId() + " " + clientCounter ++);
				
				System.err.println("Client Id : " + tlosInfoTx.getClientId());
				writeData(socketChannel, tlosInfoTx);

				System.out.println("Request has been sent to server successfully !");
				System.out.println("\nResponse is being waited...");

				TlosInfo tlosInfoRx = readData(socketChannel);

				int errCode = tlosInfoRx.getErrCode();

				if (errCode != 0) {
					System.out.println(tlosInfoRx.getErrDesc());
					// System.exit(2);
				}

				System.out.println("Response has come with paramaters:");

				if (tlosInfoTx.getCommandType().getId() == TlosInfo.TLOS_STATUS) {
					System.out.println("Tlos's status is suitable for this application, so this job has been terminated successfully.");
					System.out.println("Tlos Status: " + tlosInfoRx.getTlosStatus());
					// System.exit(0);
				} else {
					System.out.println("Undefined commandType value. commandType can be " + TlosInfo.JOB_STATUSLIST + " or " + TlosInfo.TLOS_STATUS);
					System.exit(2);
				}

				socketChannel.close();

				System.out.println("server socket closed isOpen: " + socketChannel.isOpen());
				System.out.println("This job is sleeping for the next period !");

				Thread.sleep(3000);

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		}

		System.out.println("This job has not been terminated successfully !");
		
		System.exit(2);
	
	}

	public static TlosInfo readData(SocketChannel socketChannel) throws IOException, ClassNotFoundException {

		InputStream is = Channels.newInputStream(socketChannel);
		ObjectInputStream ois = new ObjectInputStream(is);
		TlosInfo tlosInfo = (TlosInfo) ois.readObject();
		ois.close();
		is.close();

		return tlosInfo;
	}

	public static void writeData(SocketChannel socketChannel, TlosInfo tlosInfo) throws IOException, ClassNotFoundException {

		OutputStream os = Channels.newOutputStream(socketChannel);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(tlosInfo);

		return;
	}
}
