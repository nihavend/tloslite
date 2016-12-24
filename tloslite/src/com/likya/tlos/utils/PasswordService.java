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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;

public final class PasswordService {

	public static synchronized String encrypt(String plaintext) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA"); // step 2 //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
		try {
			md.update(plaintext.getBytes("UTF-8")); // step 3 //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}
		byte raw[] = md.digest(); // step 4

		/**
		 * Sun uygulamasından vazgeçilip apache uygulaması kullanıldı
		 * 16.03.2011 
		 * 
		 */
		// String hash = (new BASE64Encoder()).encode(raw); // step 5
		
		String hash = Base64.encodeBase64String(raw);
			
		return hash; // step 6
	}

}
