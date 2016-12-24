/*
 * TlosFaz2
 * com.likya.tlos.core.spc.helpers : ValidPlatforms.java
 * @author Serkan Ta�
 * Tarih : 09.Kas.2008 23:40:05
 */

package com.likya.tlos.utils;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;

public class ValidPlatforms {
	
	public static final String OS_WINDOWS = "Windows"; //$NON-NLS-1$
	private static final String OS_WINDOWS7 = OS_WINDOWS + " " + "7"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_WINDOWS_VISTA = OS_WINDOWS + " " + "Vista"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_WINDOWS_2003 = OS_WINDOWS + " " + "2003"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_WINDOWS_NT = OS_WINDOWS + " " + "NT"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_WINDOWS_95 = OS_WINDOWS + " " + "95"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_WINDOWS_XP = OS_WINDOWS + " " + "XP"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String OS_HP_UX = "HP-UX"; //$NON-NLS-1$
	private static final String OS_AIX = "AIX"; //$NON-NLS-1$
	private static final String OS_LINUX = "Linux"; //$NON-NLS-1$
	private static final String OS_SUNOS = "SunOS"; //$NON-NLS-1$
	private static final String MAC_OS_X = "Mac OS X"; //$NON-NLS-1$
	
	private static boolean osValid = true;
	
	public static boolean isOSValid() {
//		if (getCommand("") != null) {
//			return true;
//		}
//		return false;
		return osValid;
	}

	public static String getOSName() {
		return System.getProperty("os.name"); //$NON-NLS-1$
	}
	
	// TODO Bu kısım değişmeli!
	/**
	 * Aslında aşağıdaki gibi bir liste olmamalı. Sadece deploy edilecek sisteme
	 * ait lisans olmalı
	 */
	public static String[] getCommand(String jobCommand) {
		
		if(TlosServer.getTlosParameters().getJobCommand() != null && TlosServer.getTlosParameters().getJobCommand().length != 0) {
			TlosServer.println(LocaleMessages.getString("TlosServer.71") + " " + TlosServer.getTlosParameters().getJobCommand()); //$NON-NLS-1$
			return TlosServer.getTlosParameters().getJobCommand();
		}

		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		String[] cmd;

		if (osName.equals(OS_WINDOWS7)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = jobCommand;
			tmpCmd[1] = ""; //$NON-NLS-1$
			tmpCmd[2] = ""; //$NON-NLS-1$
			cmd = tmpCmd;
		} else if (osName.equals(OS_WINDOWS_VISTA)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = jobCommand;
			tmpCmd[1] = ""; //$NON-NLS-1$
			tmpCmd[2] = ""; //$NON-NLS-1$
			cmd = tmpCmd;
		} else if (osName.equals(OS_WINDOWS_2003)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = jobCommand;
			tmpCmd[1] = ""; //$NON-NLS-1$
			tmpCmd[2] = ""; //$NON-NLS-1$
			cmd = tmpCmd;
		} else if (osName.equals(OS_WINDOWS_NT)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = "cmd.exe"; //$NON-NLS-1$
			tmpCmd[1] = "/C"; //$NON-NLS-1$
			tmpCmd[2] = jobCommand;
			cmd = tmpCmd;
		} else if (osName.equals(OS_WINDOWS_95)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = "command.com"; //$NON-NLS-1$
			tmpCmd[1] = "/C"; //$NON-NLS-1$
			tmpCmd[2] = jobCommand;
			cmd = tmpCmd;
		} else if (osName.equals(OS_WINDOWS_XP)) {
			String[] tmpCmd = new String[3];
			tmpCmd[0] = "cmd.exe"; //$NON-NLS-1$
			tmpCmd[1] = "/C"; //$NON-NLS-1$
			tmpCmd[2] = jobCommand;
			cmd = tmpCmd;
		} else if (osName.equals(OS_HP_UX)) {
			String[] tmpCmd = new String[2];
			tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
			tmpCmd[1] = jobCommand;
			cmd = tmpCmd;
		} else if (osName.equals(OS_AIX)) {
			String[] tmpCmd = new String[2];
			tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
			tmpCmd[1] = jobCommand;
			cmd = tmpCmd;
		} else if (osName.equals(OS_LINUX)) {
			String[] tmpCmd = new String[2];
			tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
			tmpCmd[1] = jobCommand;
			cmd = tmpCmd;
		}  else if (osName.equals(OS_SUNOS)) {
			String[] tmpCmd = new String[2];
			tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
			tmpCmd[1] = jobCommand;
			cmd = tmpCmd;
		}  else if (osName.equals(MAC_OS_X)) {
			String[] tmpCmd = new String[2];
			tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
			tmpCmd[1] = jobCommand;
			cmd = tmpCmd;
		}  else {
			if (osName.indexOf("Windows") != -1) { //$NON-NLS-1$
				String[] tmpCmd = new String[3];
				tmpCmd[0] = "cmd.exe"; //$NON-NLS-1$
				tmpCmd[1] = "/C"; //$NON-NLS-1$
				tmpCmd[2] = jobCommand;
				cmd = tmpCmd;
			} else {
				String[] tmpCmd = new String[2];
				tmpCmd[0] = "/bin/sh"; //$NON-NLS-1$
				tmpCmd[1] = jobCommand;
				cmd = tmpCmd;
			}
			osValid = false;
		}
		
		if(!osValid) { 
			TlosServer.println(osName + LocaleMessages.getString("ValidPlatforms.0")); //$NON-NLS-1$
		} else {
			// TlosServer.println(LocaleMessages.getString("LicenseManager.19") + " " + osName); //$NON-NLS-1$
		}

		return cmd;
	}

}

