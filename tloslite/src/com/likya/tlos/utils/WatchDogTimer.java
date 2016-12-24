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

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;
import com.likya.tlos.jobs.Job;
import com.likya.tlos.model.JobProperties;

public class WatchDogTimer extends Thread {


	private Thread ownerOfTimer;
	private int timeout;

	private Job job;

	public WatchDogTimer(Job job, String name, Thread ownerOfTimer, int timeout) {
		super(name);
		this.ownerOfTimer = ownerOfTimer;
		this.timeout = timeout;
		// Not 1 den dolayı gerek kalmadı
		this.job = job;
	}

	/**
	 * @author serkan Taş 18.03.2011 Daha önce : 1. isAutoRetry == true ise, ilk
	 *         time-out olduğunda kill edip yeniden çalıştırmayı deniyorduk 2.
	 *         isAutoRetry == false ise, ilk time-out sonrası mesajlar
	 *         gönderiliyor, bir time-out süresi daha bekleyip işlemi stop edip
	 *         statüyü fail yapıyorduk.
	 * 
	 *         (18:03.2011) : 1. isAutoRetry == true ise, ilk time-out olduğunda
	 *         kill edip yeniden çalıştırmayı deniyoruz
	 * 
	 *         2. isAutoRetry == false ise, ilk time-out sonrası mesajlar
	 *         gönderiliyor, kullanıcı stop edene kadar time-out da kalıyor.
	 * 
	 * 
	 * @author merve 11.02.2013
	 * 
	 *         İş bir kere time-out'a düştükten sonra bir daha time-out süresini
	 *         kontrol etmiyoruz.
	 * 
	 *         1. isAutoRetry == true ise, ilk time-out olduğunda kill edip
	 *         yeniden çalıştırmayı deniyoruz.
	 * 
	 *         2. isAutoRetry == false ise, ilk time-out olduğunda işi time-out
	 *         statüsüne çekip uyarı mesajlarını gönderiyoruz. İş timeout
	 *         statüsünde çalışmaya devam ediyor.
	 */

	public void run() {
		try {
			Thread.sleep(timeout);
			if (!job.getJobProperties().isAutoRetry()) {
				job.getJobProperties().setStatus(JobProperties.TIMEOUT);
				TlosServer.getLogger().info(LocaleMessages.getString("WatchDogTimer.0")); //$NON-NLS-1$
				try {
					job.sendEmail();
					job.sendSms();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				TlosServer.getLogger().info(LocaleMessages.getString("WatchDogTimer.1") + job.getJobProperties().getKey()); //$NON-NLS-1$
				ownerOfTimer.interrupt();
			}
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
		this.job = null;
		this.ownerOfTimer = null;
	}

}
