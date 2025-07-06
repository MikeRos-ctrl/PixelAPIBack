package com.PixelApi.Entity;

import java.sql.Timestamp;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CLIENT_EMAIL")
public class ClientEmail {

	@Id
	@Column(name = "TOKEN_ID")
	private String tokenId;

	@Column(name = "CLIENT_ID")
	private Long clientId;

	@Column(name = "CREATION")
	private Timestamp creation;

	@Column(name = "EXPIRATION_DATE")
	private Timestamp expirationDate;

	@Column(name = "REASON")
	private String reason;

	@Column(name = "USED")
	private Boolean used;

	public ClientEmail() {

	}

	public ClientEmail(String tokenId, Long client, Timestamp expirationDate, String reason, Boolean used) {
		this.tokenId = tokenId;
		this.clientId = client;
		this.expirationDate = expirationDate;
		this.reason = reason;
		this.used = used;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Long getClient() {
		return clientId;
	}

	public void setClient(Long client) {
		this.clientId = client;
	}

	public Timestamp getCreation() {
		return creation;
	}

	public void setCreation(Timestamp creation) {
		this.creation = creation;
	}

	public Timestamp getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Timestamp expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Boolean getUsed() {
		return used;
	}

	public void setUsed(Boolean used) {
		this.used = used;
	}
}