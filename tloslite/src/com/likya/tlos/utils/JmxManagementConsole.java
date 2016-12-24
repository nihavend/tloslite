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

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.comm.TlosCommInterface;

public class JmxManagementConsole {

	private static JMXConnectorServer jmxConnectorServer;
	private static MBeanServer mBeanServer;
	
	private static int PORT = 0; // 3030;
	private static String IPADDRESS = null;
	
	public static void initialize(TlosCommInterface tlosCommInterface, int port, String ipAddress) {
		
		JmxManagementConsole.PORT = port;
		JmxManagementConsole.IPADDRESS = ipAddress;
		
        try {
            // Instantiate the MBean server
            //
        	
//            System.out.println(LocaleMessages.getString("JmxManagementConsole.0")); //$NON-NLS-1$
            mBeanServer = MBeanServerFactory.createMBeanServer();

            // Create a JMXMP connector server
            //
            
            mBeanServer.createMBean("com.likya.tlos.model.FlexAdminConsole", new ObjectName("MBeans:type=flex"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
			
            System.out.println(LocaleMessages.getString("JmxManagementConsole.3")); //$NON-NLS-1$
            JMXServiceURL url = new JMXServiceURL("jmxmp", JmxManagementConsole.IPADDRESS, JmxManagementConsole.PORT); //$NON-NLS-1$
            System.out.println(LocaleMessages.getString("JmxManagementConsole.5") + url.getHost());// + LocaleMessages.getString("JmxManagementConsole.6") + InetAddress.getLocalHost().getHostAddress()); //$NON-NLS-1$ //$NON-NLS-2$
            jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);
            
            // Start the JMXMP connector server
            //
            
//            System.out.println(LocaleMessages.getString("JmxManagementConsole.7")); //$NON-NLS-1$
            jmxConnectorServer.start();
            System.out.println(LocaleMessages.getString("JmxManagementConsole.8")); //$NON-NLS-1$
//            System.out.println(LocaleMessages.getString("JmxManagementConsole.9")); //$NON-NLS-1$
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void disconnect() {
		try {
			
			System.out.println(LocaleMessages.getString("JmxManagementConsole.10"));	 //$NON-NLS-1$
			
			String [] connIdList = jmxConnectorServer.getConnectionIds();
			
			System.out.println(LocaleMessages.getString("JmxManagementConsole.11") + connIdList.length); //$NON-NLS-1$
			System.out.println(LocaleMessages.getString("JmxManagementConsole.12")); //$NON-NLS-1$
			
			int counter = 0;
			
			while(true) {
				if(jmxConnectorServer.getConnectionIds().length == 0 || counter++ == 20) {
					break;
				}
				try {
					Thread.sleep(1000);
					System.out.print("."); //$NON-NLS-1$
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(counter == 21) {
				System.out.println(LocaleMessages.getString("JmxManagementConsole.14")); //$NON-NLS-1$
			} else {
				System.out.println(LocaleMessages.getString("JmxManagementConsole.15")); //$NON-NLS-1$
			}
			
			jmxConnectorServer.stop();
			
			System.out.println(LocaleMessages.getString("JmxManagementConsole.16")); //$NON-NLS-1$
			System.out.println(LocaleMessages.getString("JmxManagementConsole.17"));	 //$NON-NLS-1$
			
			MBeanServerFactory.releaseMBeanServer(mBeanServer);
			
			System.out.println(LocaleMessages.getString("JmxManagementConsole.18")); //$NON-NLS-1$
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
