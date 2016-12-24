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

import com.likya.tlos.TlosServer;

public class Validation {

	public static boolean getBooleanString(String parameter, String value) {
		
		if(value.toLowerCase().equals("evet") || value.toLowerCase().equals("true") || value.toLowerCase().equals("yes") || value.equals("1")) {
			return true;
		} else if(value.toLowerCase().equals("hayır") || value.toLowerCase().equals("hayir") || value.toLowerCase().equals("false") || value.toLowerCase().equals("no") || value.equals("0")) {
			return false;
		} else {
			TlosServer.getLogger().info("\"" + value + "\" degeri \"" + parameter + "\" parametresi icin gecerli olmadigindan false/0 olarak ayarlandi !");
			return false;
		}
	}
}
