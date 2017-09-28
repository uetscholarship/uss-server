package net.bqc.uetscholarship.messenger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	private int id;
	
	@JsonProperty("fb_id")
	private String fbId;
	
	@JsonProperty("first_name")
	private String firstName;
	
	@JsonProperty("last_name")
	private String lastName;
	
	private boolean isSubscribed;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFbId() {
		return fbId;
	}
	
	public void setFbId(String fbId) {
		this.fbId = fbId;
	}
	
	@JsonProperty("is_subscribed")
	public boolean isSubscribed() {
		return isSubscribed;
	}
	
	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", fbId=" + fbId + ", isSubscribed=" + isSubscribed + "]";
	}
	
}
