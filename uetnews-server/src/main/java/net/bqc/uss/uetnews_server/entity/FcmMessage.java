package net.bqc.uss.uetnews_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FcmMessage {

	@JsonProperty("notification")
	private FcmNotification notification;
	
	@JsonProperty("to")
	private String receipt;

	public FcmMessage(FcmNotification notification, String receipt) {
		super();
		this.notification = notification;
		this.receipt = receipt;
	}

	public FcmNotification getNotification() {
		return notification;
	}

	public void setNotification(FcmNotification notification) {
		this.notification = notification;
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}
	
	
}
