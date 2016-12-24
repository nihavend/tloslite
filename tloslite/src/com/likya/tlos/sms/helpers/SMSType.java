/*
 * Tlos_V1.7
 * com.likya.tlos.sms.helpers : SMSType.java
 * @author Serkan Taş
 * Tarih : 08.Eki.2010 16:02:58
 */

package com.likya.tlos.sms.helpers;

public class SMSType {
	
	private final int MAX_SMS_LENGTH = 160;
	private String messageTxt;
	
	public SMSType(String messageTxt) {
		super();
		if(messageTxt.length() > MAX_SMS_LENGTH) {
			this.messageTxt = messageTxt.substring(0, MAX_SMS_LENGTH - 1);
		} else {
			this.messageTxt = messageTxt;
		}
	}
	
	public String getMessageTxt() {
		return messageTxt;
	}
	public void setMessageTxt(String messageTxt) {
		if(messageTxt.length() > MAX_SMS_LENGTH) {
			this.messageTxt = messageTxt.substring(0, MAX_SMS_LENGTH - 1);
		} else {
			this.messageTxt = messageTxt;
		}
	}
	
}
