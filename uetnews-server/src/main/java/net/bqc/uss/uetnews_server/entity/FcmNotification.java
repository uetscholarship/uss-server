package net.bqc.uss.uetnews_server.entity;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FcmNotification {

	@JsonProperty("title")
	private String title;
	
	@JsonProperty("body")
	private String body;
	
	@JsonProperty("data")
	private Map<String, Object> data;

	public FcmNotification(String title, String body) {
		super();
		this.title = title;
		this.body = body;
		this.data = new HashMap<>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void putData(String key, Object value) {
		this.data.put(key, value);
	}
	
	
}
