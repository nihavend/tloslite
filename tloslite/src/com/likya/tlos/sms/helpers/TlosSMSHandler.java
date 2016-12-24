/*
 * Tlos_V1.7
 * com.likya.tlos.sms : SMSHandler.java
 * @author Serkan Taş
 * Tarih : 08.Eki.2010 15:39:19
 */

package com.likya.tlos.sms.helpers;

import java.util.ArrayList;

public abstract class TlosSMSHandler {
	public abstract boolean sendSMS(String msisdn, String messageTxt);
	public abstract ArrayList<String> getMsisdnList();
}
