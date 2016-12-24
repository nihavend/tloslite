package com.likya.tloslite.model;

public abstract class ClientManager {
	public abstract ClientInfo getClientInfo(String version) throws Exception;
}
