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

import java.math.BigInteger;

public class RSAEncrypt {

	static boolean DEBUG = false;

	public static String encrypt(String plainText) {

		String keys[] = RSADecrypt.getPublicKeys();

		long asciiVal = Long.parseLong(plainText);

		BigInteger bigB_pubKey = new BigInteger(keys[0]);
		BigInteger bigB_n = new BigInteger(keys[1]);

		BigInteger bigB_val = new BigInteger("" + asciiVal);
		BigInteger bigB_cipherVal = bigB_val.modPow(bigB_pubKey, bigB_n);
		if (DEBUG) {
			System.out.println("ciphertext: " + bigB_cipherVal);
		}
		return bigB_cipherVal.toString();
	}
}
