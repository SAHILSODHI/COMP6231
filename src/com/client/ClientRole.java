package com.client;

public enum ClientRole {
	
	EVENT_MANAGER("M"),
	CUSTOMER("C");
	
	private String clientRole;
	
	private ClientRole(String clientRole){
		this.clientRole = clientRole;
	}
	
	public String getClientRole(){
		return this.clientRole;
	}
}
