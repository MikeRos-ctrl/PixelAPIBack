package com.PixelApi.Entity;

import org.hibernate.validator.constraints.UniqueElements;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "CLIENT")
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CLIENT_ID")
	private Long clientId;

	@Column(name = "ACCT_KEY")
	@NotEmpty(message = "contraseña no puede estar vacia")
	private String acctKey;

	@Column(name = "EMAIL")
	@NotEmpty(message = "correo no puede estar vacio")
	@Email(message = "correo con formato no valido")
	private String email;

	@Column(name = "ROLE")
	private Integer role;

	public Client() {
		
	}
	
	public Client(Long clientId, String acctKey, String email, Integer role) {
		this.clientId = clientId;
		this.acctKey = acctKey;
		this.email = email;
		this.role = role;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getAcctKey() {
		return acctKey;
	}

	public void setAcctKey(String acctKey) {
		this.acctKey = acctKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}
}