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

import java.util.Scanner;

import com.likya.tlos.LocaleMessages;
import com.likya.tlos.TlosServer;

public class PasswordEncrypter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.5")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.6") + TlosServer.getVersion() + LocaleMessages.getString("Tlos.7")); //$NON-NLS-1$ //$NON-NLS-2$
		TlosServer.println(LocaleMessages.getString("Tlos.8")); //$NON-NLS-1$
		TlosServer.println(LocaleMessages.getString("Tlos.4")); //$NON-NLS-1$

		boolean validated = false;
		String password = "";
		
		while(!validated) {
			System.out.print("En fazla 8 karakterlik bir sifre giriniz: ");
			Scanner scan = new Scanner(System.in);
			password = scan.next();
			
			if(password.length() > 8) {
				continue;
			}
			
			int i;
			for (i = 0; i < password.length(); i++) {
				int asciiValue = (int)password.charAt(i);
				
				//Türkçe karakterler kısıtlandı, tüm noktalama işaretlerine izin verildi
				if(asciiValue < 33 || asciiValue > 125) {
					System.out.println("Turkce karakter girmeyiniz");
					break;
				}
			}
			
			if(i == password.length()) {
				validated = true;
			}
			scan.close();
		}

		System.out.println("Encrypt edilmis sifre : " + LikyaEncryption.encryptPassword(password));
	}
}
