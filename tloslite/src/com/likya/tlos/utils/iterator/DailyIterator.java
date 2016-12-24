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
package com.likya.tlos.utils.iterator;

import java.util.Calendar;
import java.util.Date;



/**
 * A <code>DailyIterator</code> returns a sequence of dates on subsequent days
 * representing the same time each day.
 */
public class DailyIterator implements ScheduleIterator {
	@SuppressWarnings("unused")
	private final int hourOfDay, minute, second;
	private final Calendar calendar = Calendar.getInstance();

	public DailyIterator(int hourOfDay, int minute, int second) {
		this(hourOfDay, minute, second, new Date());
	}

	public DailyIterator(int hourOfDay, int minute, int second, Date date) {
		this.hourOfDay = hourOfDay;
		this.minute = minute;
		this.second = second;
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		if (!calendar.getTime().before(date)) {
			calendar.add(Calendar.DATE, -1);
		}
	}

	public Date next() {
		calendar.add(Calendar.DATE, 1);
		return calendar.getTime();
	}

}
