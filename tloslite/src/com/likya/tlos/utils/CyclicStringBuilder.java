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

public class CyclicStringBuilder {

	private static int MAX_LENGTH = 1600;
	
	private StringBuilder stringBuilder = new StringBuilder();
	
	
	public CyclicStringBuilder(int maxLength) {
		super();
		if(maxLength > Integer.MAX_VALUE) {
			MAX_LENGTH = Integer.MAX_VALUE;
		} if(maxLength < 80) {
		} else {
			MAX_LENGTH = maxLength;
		}
	}

	public void append(String s) {
		if (stringBuilder.length() + s.length() > MAX_LENGTH) {
			stringBuilder.delete(0, stringBuilder.length() + s.length() - MAX_LENGTH);
		}
		stringBuilder.append(s, Math.max(0, s.length() - MAX_LENGTH), s.length());
	}
	
	public StringBuilder toStringBuilder() {
		return stringBuilder;
	}
	
	public String toString() {
		return stringBuilder.toString();
	}

	public static int getMAX_LENGTH() {
		return MAX_LENGTH;
	}
}
