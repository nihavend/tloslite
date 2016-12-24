/*
 * Tlos_V1.61
 * com.likya.tlos.model : PersistObject.java
 * @author Serkan Taş
 * Tarih : 12.Ara.2010 19:27:50
 */

package com.likya.tlos.model;

import java.io.Serializable;
import java.util.HashMap;

import com.likya.tlos.jobs.Job;

public class PersistObject implements Serializable {

	private static final long serialVersionUID = -7212438515231106250L;

	private String tlosVersion;
	private HashMap<String, Job> jobQueue;
	private HashMap<Integer, String> groupList;

	public String getTlosVersion() {
		return tlosVersion;
	}

	public void setTlosVersion(String tlosVersion) {
		this.tlosVersion = tlosVersion;
	}

	public HashMap<String, Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(HashMap<String, Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public HashMap<Integer, String> getGroupList() {
		return groupList;
	}

	public void setGroupList(HashMap<Integer, String> groupList) {
		this.groupList = groupList;
	}
}
