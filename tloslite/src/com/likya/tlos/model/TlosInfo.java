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
package com.likya.tlos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import com.likya.tlos.LocaleMessages;

public class TlosInfo implements Serializable {
	
	private static final long serialVersionUID = 6113817205009241653L;

	private int errCode = 0;
	private String errDesc = ""; //$NON-NLS-1$
	
	public final static int TLOS_STATUS = 10;
	public final static int JOB_STATUSLIST = 20;
	public final static int NORMAL_TERMINATE = 30;
	public final static int FORCED_TERMINATE = 40;
	public final static int RESUME_JOB = 50;
	public final static int DUMP_JOB_LIST = 60; 
	
	//Tlos's status
	public static final int STATE_STOP = 10;
	public static final int STATE_SUSPENDED = 20;
	public static final int STATE_JOBOVERFLOW = 30;
	public static final int STATE_WORKING = 40;
	
	String clientId = LocaleMessages.getString("TlosInfo.0"); //$NON-NLS-1$
	private CommandType commandType;
	
	private int tlosStatus;
	private String jobId;
	private String tlosId;
	private Date executionDate;
	private Date nextExecutionDate;
	private ArrayList<Integer> jobStatusHistory = new ArrayList<Integer>();
	
	public TlosInfo() {
	}
	
	public static int getCommandTypeValue(String commandType) {
		
		int retValue = -1;

		if (commandType.equals("TLOS_STATUS")) { //$NON-NLS-1$
			retValue = TLOS_STATUS;
		} else if (commandType.equals("JOB_STATUSLIST")) { //$NON-NLS-1$
			retValue = JOB_STATUSLIST;
		} else if (commandType.equals("NORMAL_TERMINATE")) { //$NON-NLS-1$
			retValue = NORMAL_TERMINATE;
		} else if (commandType.equals("FORCED_TERMINATE")) { //$NON-NLS-1$
			retValue = FORCED_TERMINATE;
		} else if (commandType.equals("RESUME_JOB")) { //$NON-NLS-1$
			retValue = RESUME_JOB;
		} else if (commandType.equals("DUMP_JOB_LIST")) { //$NON-NLS-1$
			retValue = DUMP_JOB_LIST;
		}
		
		return retValue;

	}
	
	public static String getCommandTypeDescription(int commandType) {
		
		String retValue = null;
		
		switch (commandType){
		
		case TLOS_STATUS:
			retValue = "TLOS_STATUS"; //$NON-NLS-1$
			break;
		
		case JOB_STATUSLIST:
			retValue = "JOB_STATUSLIST"; //$NON-NLS-1$
			break;
			
		case NORMAL_TERMINATE:
			retValue = "NORMAL_TERMINATE"; //$NON-NLS-1$
			break;
		
		case FORCED_TERMINATE:
			retValue = "FORCED_TERMINATE"; //$NON-NLS-1$
			break;
			
		case RESUME_JOB:
			retValue = "RESUME_JOB"; //$NON-NLS-1$
			break;
		
		case DUMP_JOB_LIST:
			retValue = "DUMP_JOB_LIST"; //$NON-NLS-1$
			break;
		}
		
		return retValue;

	}
	
	public static String getStatusString(int status) {

		String retStr = "-"; //$NON-NLS-1$

		switch (status) {
		case STATE_STOP:
			retStr = "STATE_STOP"; //$NON-NLS-1$
			break;
		case STATE_SUSPENDED:
			retStr = "STATE_SUSPENDED"; //$NON-NLS-1$
			break;
		case STATE_JOBOVERFLOW:
			retStr = "STATE_JOBOVERFLOW"; //$NON-NLS-1$
			break;
		}

		return retStr;
	}

	public static int getStatusValue(String status) {

		int retValue = -1;

		if (status.equals("STATE_STOP")) { //$NON-NLS-1$
			retValue = STATE_STOP;
		} else if (status.equals("STATE_SUSPENDED")) { //$NON-NLS-1$
			retValue = STATE_SUSPENDED;
		} else if (status.equals("STATE_JOBOVERFLOW")) { //$NON-NLS-1$
			retValue = STATE_JOBOVERFLOW;
		}
		return retValue;

	}
	
	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}
	
	public CommandType getCommandType() {
		return commandType;
	}

	public static byte[] toBytes(Object object) {
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		try {
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
					baos);
			oos.writeObject(object);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static Object toObject(byte[] bytes) {
		Object object = null;
		try {
			object = new java.io.ObjectInputStream(
					new java.io.ByteArrayInputStream(bytes)).readObject();
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public Date getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	public Date getNextExecutionDate() {
		return nextExecutionDate;
	}

	public void setNextExecutionDate(Date nextExecutionDate) {
		this.nextExecutionDate = nextExecutionDate;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public ArrayList<Integer> getJobStatusHistory() {
		return jobStatusHistory;
	}

	public void setJobStatusHistory(ArrayList<Integer> jobStatusHistory) {
		this.jobStatusHistory = jobStatusHistory;
	}

	public int getTlosStatus() {
		return tlosStatus;
	}

	public void setTlosStatus(int tlosStatus) {
		this.tlosStatus = tlosStatus;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getErrDesc() {
		return errDesc;
	}

	public void setErrDesc(String errDesc) {
		this.errDesc = errDesc;
	}

	public void setTlosId(String tlosId) {
		this.tlosId = tlosId;
	}

	public String getTlosId() {
		return tlosId;
	}
	
}
