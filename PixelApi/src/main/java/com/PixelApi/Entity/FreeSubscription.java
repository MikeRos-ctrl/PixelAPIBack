package com.PixelApi.Entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FREE_SUBSCRIPTION")
public class FreeSubscription {

	@Id
	@Column(name = "TOKEN")
	private String token;

	@Column(name = "CLIENT_ID")
	private Long clientId;
		
	@Column(name = "START_DATE")
	private Timestamp startDay;

	public FreeSubscription() {
		
	}
	
	public FreeSubscription(String token, Long clientId, Timestamp startDay) {
		this.token = token;
		this.clientId = clientId;
		this.startDay = startDay;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Timestamp getStartDay() {
		return startDay;
	}

	public void setStartDay(Timestamp startDay) {
		this.startDay = startDay;
	}
	
}