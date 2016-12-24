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
package com.likya.tlos.utils.loaders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.likya.tlos.model.TlosAuthorization;

public class AuthorizationLoader {

	public static String fileToPersist = "Tlos.authorization"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	public static HashMap<String, TlosAuthorization> readAuthorizationList() throws Exception {

		FileInputStream fis = null;
		ObjectInputStream in = null;
		HashMap<String, TlosAuthorization> tlosAuthorizationList;

		fis = new FileInputStream(fileToPersist);
		in = new ObjectInputStream(fis);
		Object input = in.readObject();

		tlosAuthorizationList = (HashMap<String, TlosAuthorization>) input;
		in.close();

		return tlosAuthorizationList;
	}

	public static boolean persistAuthorizationList(HashMap<String, TlosAuthorization> tlosAuthorizationList) {

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		try {
			fos = new FileOutputStream(fileToPersist);
			out = new ObjectOutputStream(fos);
			out.writeObject(tlosAuthorizationList);
			out.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;

	}
}
