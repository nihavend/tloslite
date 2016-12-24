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
package com.likya.tlos.encryption;


public class LikyaEncryption {

	private static String conversionTable[][] = {
			{ "XX", "XX", "XX", "XX", "XX", "XX", "XX", "XX", "XX", "XX" },
			{ "_", "A", "B", "C", "D", "E", "F", "G", "H", "I" },
			{ "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S" },
			{ "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c" },
			{ "d", "e", "f", "g", "h", "i", "j", "k", "l", "m" },
			{ "n", "o", "p", "q", "r", "s", "t", "u", "v", "w" },
			{ "x", "y", "z", ".", ",", ":", ";", "'", "\"", "`" },
			{ "!", "@", "#", "$", "%", "^", "&", "*", "-", "+" },
			{ "(", ")", "[", "]", "{", "}", "?", "/", "<", ">" },
			{ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" } };

	public static String encryptPassword(String cleanPassword) {

		String convertedStr = convertPassword(cleanPassword);
		RSADecrypt.calculateKeys("20");
		String encryptedStr = RSAEncrypt.encrypt(convertedStr);
		
		return encryptedStr;
	}

	public static String decryptPassword(String mixedPassword) {

		RSADecrypt.calculateKeys("20");
		String decryptedStr = RSADecrypt.decrypt(mixedPassword);
		String calculatedPassword = getActualValue(decryptedStr);
		
		return calculatedPassword;
	}
	
	public static String convertPassword(String password) {

		String convertedStr = "";

		for (int i = 0; i < password.length(); i++) {
			convertedStr += getConvertedValue(password.substring(i, i + 1));
		}
		return convertedStr;
	}

	public static String getConvertedValue(String binaryData) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (binaryData.equals(conversionTable[i][j])) {
					Integer code = i * 10 + j;
					return code.toString();
				}
			}
		}
		return null;
	}

	public static String getActualValue(String convertedText) {
		String password = "";

		for (int i = 0; i < convertedText.length(); i = i + 2) {
			int row = Integer.parseInt(convertedText.substring(i, i + 1));
			int column = Integer.parseInt(convertedText.substring(i + 1, i + 2));

			password += conversionTable[row][column];
		}
		return password;
	}
}
