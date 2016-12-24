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

import java.util.Date;

/**
 * Implementations of <code>ScheduleIterator</code> specify a schedule as a series of <code>java.util.Date</code> objects.
 */

public interface ScheduleIterator {
    
/**
 * Returns the next time that the related {@link SchedulerTask} should be run.
 * @return the next time of execution
 */
public Date next();
}
