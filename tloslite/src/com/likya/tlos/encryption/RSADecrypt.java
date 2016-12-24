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

import java.util.*;
import java.math.BigInteger;

public class RSADecrypt {

	static boolean DEBUG = false;

	private static BigInteger bigB_pubKey;
	private static BigInteger bigB_n;
	private static BigInteger bigB_prvKey;

	public static void calculateKeys(String publicKey) {

		/* Creating two random number generators, each with a different seed */
		Random rand1 = new Random(111);
		Random rand2 = new Random(777);
		long pubKey = Long.parseLong(publicKey); /*
												 * public key is at least a
												 * certain value input by the
												 * user
												 */
		/*
		 * returns a BigInteger that is not prime with probability less than
		 * 2^(-100)
		 */
		BigInteger bigB_p = BigInteger.probablePrime(32, rand1);
		BigInteger bigB_q = BigInteger.probablePrime(32, rand2);
		bigB_n = bigB_p.multiply(bigB_q); // 9856135347580887187
		BigInteger bigB_p_1 = bigB_p.subtract(new BigInteger("1")); // p-1
		BigInteger bigB_q_1 = bigB_q.subtract(new BigInteger("1")); // q-1
		BigInteger bigB_p_1_q_1 = bigB_p_1.multiply(bigB_q_1); // (p-1)*(q-1)
		// generating the correct public key
		while (true) {
			BigInteger BigB_GCD = bigB_p_1_q_1.gcd(new BigInteger("" + pubKey));
			if (BigB_GCD.equals(BigInteger.ONE)) {
				break;
			}
			pubKey++;
		}
		bigB_pubKey = new BigInteger("" + pubKey);
		bigB_prvKey = bigB_pubKey.modInverse(bigB_p_1_q_1);
		if (DEBUG) {
			// System.out.println("bigB_p: "+bigB_p);
			// System.out.println("bigB_q: "+bigB_q);
			System.out.println("public key : " + bigB_pubKey + " , " + bigB_n);
			// System.out.println("private key: "+bigB_prvKey);
		}
	}

	public static String[] getPublicKeys() {
		String keys[] = { bigB_pubKey.toString(), bigB_n.toString() };

		return keys;
	}

	public static String decrypt(String encryptedText) {
		BigInteger bigB_cipherVal = new BigInteger(encryptedText);

		/*
		 * encrypting an ASCII integer value using the public key and decrypting
		 * the cipher value using the private key and extracting the ASCII value
		 * back
		 */
		BigInteger bigB_plainVal = bigB_cipherVal.modPow(bigB_prvKey, bigB_n);
		long plainVal = bigB_plainVal.longValue();
		if (DEBUG) {
			System.out.println("plaintext: " + plainVal);
		}

		return bigB_plainVal.toString();
	}
}
